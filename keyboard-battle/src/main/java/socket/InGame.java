package socket;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import dao.ChatLogDAO;
import dao.RoomDAO;
import dao.TitleDAO;
import dao.TypingTestDAO;
import dao.UserDAO;
import dao.UserRoomDAO;
import dto.ChatLogDTO;
import dto.RoomDTO;
import dto.TitleDTO;
import dto.TypingTestDTO;
import dto.UserDTO;
import dto.UserRoomDTO;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;

@ServerEndpoint("/in-game/{roomId}")
public class InGame {
	public class PlayerInfo {
		int level;
		int score;
		int averageTajaSpeed;
		int averageAccuracy;
		int maxCombo;
		int bomb = 2;
		boolean isGameOver;
	}

	private static final Map<String, Set<Session>> rooms = Collections.synchronizedMap(new ConcurrentHashMap<>());
	private static final Map<String, int[]> spaces = Collections.synchronizedMap(new ConcurrentHashMap<>());
	private static final Map<String, boolean[]> isGameStarted = Collections.synchronizedMap(new ConcurrentHashMap<>());
	private static final Map<String, PlayerInfo[]> playerInfo = Collections.synchronizedMap(new ConcurrentHashMap<>()); // size: 2
	private String roomId;

	@OnOpen
	public void onOpen(@PathParam("roomId") String roomId, Session session) {
		this.roomId = roomId;

		RoomDAO roomDao = new RoomDAO();
		RoomDTO room = roomDao.readRoomById(roomId);
		if (room == null) {
			synchronized (rooms) {
				if (session.isOpen()) {
					try {
						session.getBasicRemote().sendText("end");
					} catch (IOException e) {
						System.out.println("전송 끊김0: 정상");
					}
				}
			}
		}
		if (!room.isIngame()) {
			synchronized (rooms) {
				if (session.isOpen()) {
					try {
						session.getBasicRemote().sendText("end");
					} catch (IOException e) {
						System.out.println("전송 끊김0: 정상");
					}
				}
			}
		}
	}

	@OnMessage
	public void onMessage(String message, Session session) throws IOException {
//		System.out.println("Received message: " + message);
		String messageType = message.split("::")[0];
		switch (messageType) {
		case "check":
			checkUser(message, session);
			break;
		case "chat":
			sendChatMessage(message, session);
			break;
		case "level":
			levelMessage(message);
			break;
		case "taja":
			sendToOtherClientsInRoom(message, session);
			break;
		case "complete":
			completeMessage(message, session);
			break;
		case "bomb":
			bombMessage(message, session);
			break;
		case "attack":
			sendToAllClientsInRoom(message);
			break;
		case "gameover":
			gameoverMessage(message, session);
			break;
		default:
			System.out.println("Unknown message type: " + messageType);
			break;
		}
//		int[] roomSpace = spaces.get(this.roomId);
//		if (roomSpace != null) {
//			System.out.print("Room space: ");
//			for (int i = 0; i < roomSpace.length; i++) {
//				System.out.print(roomSpace[i] + " ");
//			}
//			System.out.println();
//		}
	}

	@OnClose
	public void onClose(Session session) {
		Set<Session> roomSessions = rooms.get(this.roomId);
		synchronized (rooms) {
			roomSessions.remove(session);
		}
	}

	@OnError
	public void onError(Throwable e) {
		e.printStackTrace();
	}

	private void sendToAllClientsInRoom(String message) throws IOException, IllegalStateException {
		Set<Session> roomSessions = rooms.get(this.roomId);
		synchronized (rooms) {
			for (Session s : roomSessions) {
				if (s.isOpen()) {
					s.getBasicRemote().sendText(message);
				}
			}
		}
	}

	private void sendToOtherClientsInRoom(String message, Session session) throws IOException, IllegalStateException {
		Set<Session> roomSessions = rooms.get(this.roomId);
		synchronized (rooms) {
			for (Session s : roomSessions) {
				if (s.isOpen() && !s.equals(session)) {
					s.getBasicRemote().sendText(message);
				}
			}
		}
	}

	private void checkUser(String message, Session session) {
		PlayerInfo[] playerInfoSpace = playerInfo.get(this.roomId); // size: 2
		int[] roomSpace = spaces.get(this.roomId);
		boolean[] userStartedSpace = isGameStarted.get(this.roomId);
		rooms.computeIfAbsent(this.roomId, k -> ConcurrentHashMap.newKeySet()).add(session); // 세션 추가
		if (roomSpace == null) { // 없으면 생성
			spaces.computeIfAbsent(this.roomId, k -> new int[10]); // 방에 10개의 공간 생성
		}
		if (userStartedSpace == null) {
			isGameStarted.computeIfAbsent(this.roomId, k -> new boolean[10]);
		}
		if (playerInfoSpace == null) {
			playerInfo.computeIfAbsent(this.roomId, k -> new PlayerInfo[2]);
		}
		roomSpace = spaces.get(this.roomId); // 생성 후 가져오기1
		userStartedSpace = isGameStarted.get(this.roomId); // 생성 후 가져오기2
		playerInfoSpace = playerInfo.get(this.roomId); // 생성 후 가져오기3
		playerInfoSpace[0] = new PlayerInfo(); // 플레이어1 정보 생성
		playerInfoSpace[1] = new PlayerInfo(); // 플레이어2 정보 생성

		int userId = Integer.parseInt(message.split("::")[1]);

		UserRoomDAO userRoomDao = new UserRoomDAO();
		UserRoomDTO userRoom = userRoomDao.readUserRoomByUserId(userId);
		if (userRoom == null) {
			return;
		}

		int userSpaceIndex = userRoom.getSpaceIndex();
		roomSpace[userSpaceIndex] = userId;

		synchronized (rooms) {
			if (userSpaceIndex == 0 || userSpaceIndex == 1) {
				if (session.isOpen()) {
					try {
						session.getBasicRemote().sendText("position::" + userSpaceIndex);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}

		UserDAO userDao = new UserDAO();
		TitleDAO titleDao = new TitleDAO();

		List<UserRoomDTO> userRoomList = userRoomDao.readUserRoomsByRoomId(this.roomId);
		for (UserRoomDTO ur : userRoomList) { // 모든 자리의 유저 정보 전송
			UserDTO user = userDao.readUserById(ur.getUserId());
			TitleDTO title = titleDao.readTitleById(user.getTitle());
			String titleName = "null";
			if (title != null) {
				titleName = title.getName();
			}

			String placeMessage = "place::";
			placeMessage += ur.getSpaceIndex() + "::";
			placeMessage += user.getNickname() + "::";
			placeMessage += user.getLevel() + "::";
			placeMessage += user.getThumbnailImage() + "::";
			placeMessage += titleName;

			try {
				sendToAllClientsInRoom(placeMessage);
			} catch (IllegalStateException | IOException e) {
				System.out.println("전송 끊김: 정상");
			}
		}

		try {
			sendToAllClientsInRoom("start");
		} catch (IllegalStateException | IOException e) {
			System.out.println("전송 끊김: 정상");
		}

		if (userSpaceIndex != 0) { // 플레이어0(방장)만 스케줄러 실행
			return;
		}

		ScheduledExecutorService timeScheduler = Executors.newScheduledThreadPool(1);
		ScheduledExecutorService textScheduler = Executors.newScheduledThreadPool(1);

		for (int i = 5; i >= 0; i--) { // 카운트다운 5초
			int count = i;
			timeScheduler.schedule(() -> {
				try {
					sendToAllClientsInRoom("countdown::" + count);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}, 5 - i, TimeUnit.SECONDS);
		}

		for (int i = 305; i >= 0; i--) { // 게임 시간 5분
			int time = i;
			timeScheduler.schedule(() -> {
				try {
					sendToAllClientsInRoom("time::" + time);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}, 305 - i, TimeUnit.SECONDS);
		}

		timeScheduler.schedule(() -> { // 5분 후 게임 종료
			timeScheduler.shutdown();
			try {
				sendToAllClientsInRoom("result");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}, 305, TimeUnit.SECONDS);

		TypingTestDAO typingTestDao = new TypingTestDAO();
		List<TypingTestDTO> typingTestList = typingTestDao.readAllTypingTest();
		int typingTestSize = typingTestList.size();
		for (int i = 0; i < 6; i++) { // Level 1: 10초마다
			int count = i;
			int randomIndex = (int) (Math.random() * typingTestSize);
			textScheduler.schedule(() -> {
				try {
					if (count == 0) {
						sendToAllClientsInRoom("level::1");
					}
					sendToAllClientsInRoom("text::" + typingTestList.get(randomIndex).getSentence());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}, 6 + i * 10, TimeUnit.SECONDS); // 카운트다운 종료 후 1초 여유: 5 + 1
		}

		for (int i = 0; i < 7; i++) { // Level 2: 8초마다
			int count = i;
			int randomIndex = (int) (Math.random() * typingTestSize);
			textScheduler.schedule(() -> {
				try {
					if (count == 0) {
						sendToAllClientsInRoom("level::2");
					}
					sendToAllClientsInRoom("text::" + typingTestList.get(randomIndex).getSentence());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}, 75 + i * 8, TimeUnit.SECONDS); // 6 + 69 = 75, Level 1 종료 후 9초 대기
		}

		for (int i = 0; i < 9; i++) { // Level 3: 7초마다
			int count = i;
			int randomIndex = (int) (Math.random() * typingTestSize);
			textScheduler.schedule(() -> {
				try {
					if (count == 0) {
						sendToAllClientsInRoom("level::3");
					}
					sendToAllClientsInRoom("text::" + typingTestList.get(randomIndex).getSentence());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}, 138 + i * 7, TimeUnit.SECONDS); // 75 + 63 = 138, Level 2 종료 후 7초 대기
		}

		for (int i = 0; i < 10; i++) { // Level 4: 5초마다
			int count = i;
			int randomIndex = (int) (Math.random() * typingTestSize);
			textScheduler.schedule(() -> {
				try {
					if (count == 0) {
						sendToAllClientsInRoom("level::4");
					}
					sendToAllClientsInRoom("text::" + typingTestList.get(randomIndex).getSentence());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}, 207 + i * 5, TimeUnit.SECONDS); // 138 + 69 = 207, Level 3 종료 후 6초 대기
		}

		for (int i = 0; i < 11; i++) { // Level 5: 4초마다
			int count = i;
			int randomIndex = (int) (Math.random() * typingTestSize);
			textScheduler.schedule(() -> {
				try {
					if (count == 0) {
						sendToAllClientsInRoom("level::5");
					}
					sendToAllClientsInRoom("text::" + typingTestList.get(randomIndex).getSentence());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}, 261 + i * 4, TimeUnit.SECONDS); // 138 + 69 = 207, Level 4 종료 후 4초 대기
		}

		textScheduler.schedule(() -> {
			textScheduler.shutdown();
			try {
				gameResult();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}, 306, TimeUnit.SECONDS);
	}

	private void sendChatMessage(String message, Session session) throws IOException {
		Set<Session> roomSessions = rooms.get(this.roomId);
		if (roomSessions == null) {
			return;
		}
		String chatClient = message.split("::")[1];
//		String clientType = message.split("::")[2];
		String chatMsg = message.split("::")[3];
		UserDAO userDAO = new UserDAO();
		UserDTO user = new UserDTO();
		user = userDAO.readUserByNickname(chatClient);

		ChatLogDAO chatLogDAO = new ChatLogDAO();
		ChatLogDTO chatLog = new ChatLogDTO();
		chatLog.setUserId(user.getId());
		chatLog.setMessage(chatMsg);
		chatLog.setPlace(ChatLogDTO.Place.INGAME);
		chatLogDAO.createChatLog(chatLog);

		sendToOtherClientsInRoom(message, session);
	}
	
	private void levelMessage(String message) throws IOException {
        PlayerInfo[] playerInfoSpace = playerInfo.get(this.roomId); // size: 2
        int playerSpaceIndex = Integer.parseInt(message.split("::")[1]);
        int level = Integer.parseInt(message.split("::")[2]);

        playerInfoSpace[playerSpaceIndex].level = level;
	}

	private void completeMessage(String message, Session session) throws IOException {
		PlayerInfo[] playerInfoSpace = playerInfo.get(this.roomId); // size: 2
		int playerSpaceIndex = Integer.parseInt(message.split("::")[1]);
		int averageTajaSpeed = Integer.parseInt(message.split("::")[2]);
		int averageAccuracy = Integer.parseInt(message.split("::")[3]);
		int addScore = Integer.parseInt(message.split("::")[4]);
		int combo = Integer.parseInt(message.split("::")[5]);

		playerInfoSpace[playerSpaceIndex].averageTajaSpeed = averageTajaSpeed;
		playerInfoSpace[playerSpaceIndex].averageAccuracy = averageAccuracy;
		playerInfoSpace[playerSpaceIndex].score += addScore;
		playerInfoSpace[playerSpaceIndex].maxCombo = Math.max(playerInfoSpace[playerSpaceIndex].maxCombo, combo);

		sendToOtherClientsInRoom(message, session);
	}

	private void bombMessage(String message, Session session) throws IOException {
		PlayerInfo[] playerInfoSpace = playerInfo.get(this.roomId); // size: 2
		int playerSpaceIndex = Integer.parseInt(message.split("::")[1]);

		playerInfoSpace[playerSpaceIndex].bomb--;
		if (playerInfoSpace[playerSpaceIndex].bomb < 0) {
			playerInfoSpace[playerSpaceIndex].bomb = 0;
		}

		sendToOtherClientsInRoom(message, session);
	}

	private void gameoverMessage(String message, Session session) throws IOException {
		PlayerInfo[] playerInfoSpace = playerInfo.get(this.roomId); // size: 2
		int playerSpaceIndex = Integer.parseInt(message.split("::")[1]);

		playerInfoSpace[playerSpaceIndex].isGameOver = true;

		sendToOtherClientsInRoom(message, session);
		
		if (playerInfoSpace[0].isGameOver && playerInfoSpace[1].isGameOver) {
			gameResult();
		}
	}

	private void gameResult() throws IOException {
		PlayerInfo[] playerInfoSpace = playerInfo.get(this.roomId); // size: 2
		int playe0Score = playerInfoSpace[0].score;
		int playe1Score = playerInfoSpace[1].score;
		String message = "result::0::";
		message += playerInfoSpace[0].level + "::";
		message += playerInfoSpace[0].score + "::";
		message += playerInfoSpace[0].averageTajaSpeed + "::";
		message += playerInfoSpace[0].averageAccuracy + "::";
		message += playerInfoSpace[0].maxCombo + "::";
		message += playerInfoSpace[0].bomb + "::";
		message += "1::";
		message += playerInfoSpace[1].level + "::";
		message += playerInfoSpace[1].score + "::";
		message += playerInfoSpace[1].averageTajaSpeed + "::";
		message += playerInfoSpace[1].averageAccuracy + "::";
		message += playerInfoSpace[1].maxCombo + "::";
		message += playerInfoSpace[1].bomb;

		sendToAllClientsInRoom(message);
	}
}
