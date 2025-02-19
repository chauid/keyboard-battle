package socket;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import dao.ChatLogDAO;
import dao.RoomDAO;
import dao.TitleDAO;
import dao.UserDAO;
import dao.UserRoomDAO;
import dto.ChatLogDTO;
import dto.RoomDTO;
import dto.TitleDTO;
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
	private static final Map<String, Set<Session>> rooms = Collections.synchronizedMap(new ConcurrentHashMap<>());
	private static final Map<String, int[]> spaces = Collections.synchronizedMap(new ConcurrentHashMap<>());
	private static final Map<String, Boolean> isGameStarted = Collections.synchronizedMap(new ConcurrentHashMap<>());
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
		case "taja":
			inputTaja(message, session);
			break;
		case "complete":
			completeTaja(message, session);
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

	private void checkUser(String message, Session session) {
		int[] roomSpace = spaces.get(this.roomId);
		rooms.computeIfAbsent(this.roomId, k -> ConcurrentHashMap.newKeySet()).add(session); // 세션 추가
		if (roomSpace == null) { // 없으면 생성
			spaces.computeIfAbsent(this.roomId, k -> new int[10]); // 방에 10개의 공간 생성
		}
		roomSpace = spaces.get(this.roomId); // 생성 후 가져오기

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

		for (int i = 0; i < roomSpace.length; i++) { // 모든 자리의 유저 정보 전송
			if (roomSpace[i] == 0) {
				continue;
			}

			UserDTO user = userDao.readUserById(roomSpace[i]);
			TitleDTO title = titleDao.readTitleById(user.getTitle());
			String titleName = "null";
			if (title != null) {
				titleName = title.getName();
			}

			String placeMessage = "place::";
			placeMessage += userSpaceIndex + "::";
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

		int userRoomCount = userRoomDao.readUserRoomCountByRoomId(this.roomId);
		int userCount = 0;
		for (int i = 0; i < roomSpace.length; i++) {
			if (roomSpace[i] != 0) {
				userCount++;
			}
		}

		if (userCount != userRoomCount) {
			return;
		}

		try {
			sendToAllClientsInRoom("start");
		} catch (IllegalStateException | IOException e) {
			System.out.println("전송 끊김: 정상");
		}

		if (isGameStarted.get(this.roomId) != null) { // 이미 게임이 시작되었을 경우 시간 정보를 전송하지 않음
			return; // 먼저 들어온 유저가 받고 나머지는 못 받는 문제
		} // =============================================================이거 고쳐야 함

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

		timeScheduler.schedule(() -> {
			timeScheduler.shutdown();
		}, 5, TimeUnit.SECONDS);

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
			} catch (IllegalStateException | IOException e) {
				e.printStackTrace();
			}
		}, 305, TimeUnit.SECONDS);

		textScheduler.scheduleAtFixedRate(() -> { // Level 1
			try {
				if (session.isOpen()) {
					session.getBasicRemote().sendText("text::" + "레벨1 문장 테스트를 해보자");
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}, 6, 12, TimeUnit.SECONDS); // 카운트다운 종료 후 1초 여유: 5 + 1
		
		textScheduler.schedule(() -> {
			textScheduler.shutdown();
        }, 77, TimeUnit.SECONDS); // 6 + 71 = 77
		
		textScheduler.scheduleAtFixedRate(() -> { // Level 2
			try {
				if (session.isOpen()) {
					session.getBasicRemote().sendText("text::" + "레벨2 문장 테스트를 해보자");
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}, 77, 10, TimeUnit.SECONDS);
		
		textScheduler.schedule(() -> {
			textScheduler.shutdown();
		}, 146, TimeUnit.SECONDS); // 77 + 69 = 146
		
		textScheduler.scheduleAtFixedRate(() -> { // Level 3
			try {
				if (session.isOpen()) {
					session.getBasicRemote().sendText("text::" + "레벨3 문장 테스트를 해보자");
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}, 146, 8, TimeUnit.SECONDS);
		
		textScheduler.schedule(() -> {
			textScheduler.shutdown();
		}, 209, TimeUnit.SECONDS); // 146 + 63 = 209

		isGameStarted.computeIfAbsent(this.roomId, k -> true);
	}

	private void sendChatMessage(String message, Session session) throws IOException {
		Set<Session> roomSessions = rooms.get(this.roomId);
		synchronized (rooms) {
			if (roomSessions == null) {
				return;
			}
			String chatClient = message.split("::")[1];
			String clientType = message.split("::")[2];
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

			for (Session s : roomSessions) {
				if (s.isOpen()) {
					if (!s.equals(session)) { // 메시지를 보낸 클라이언트는 제외
						s.getBasicRemote().sendText("chat::" + chatClient + "::" + clientType + "::" + chatMsg);
					}
				}
			}
		}
	}

	private void inputTaja(String message, Session session) {
		String position = message.split("::")[1];
		String currentText = message.split("::")[2];
		String currentTajaSpeed = message.split("::")[3];
		String currentAccuracy = message.split("::")[4];
		
		Set<Session> roomSessions = rooms.get(this.roomId);
		
	}

	private void completeTaja(String message, Session session) {

	}
}
