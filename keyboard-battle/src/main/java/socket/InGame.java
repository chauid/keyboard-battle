package socket;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;

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
	private static final Map<String, ScheduledExecutorService> schedules = Collections.synchronizedMap(new ConcurrentHashMap<>());
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
	}

	@OnClose
	public void onClose(Session session) {

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
		schedules.computeIfAbsent(roomId, k -> null); // 스케줄러 추가
		if (roomSpace == null) { // 없으면 생성
			spaces.computeIfAbsent(this.roomId, k -> new int[10]); // 방에 10개의 공간 생성
		}
		roomSpace = spaces.get(this.roomId); // 생성 후 가져오기
		Set<Session> roomSessions = rooms.get(this.roomId);

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
			placeMessage += titleName + "::";

			try {
				sendToAllClientsInRoom(placeMessage);
			} catch (IllegalStateException | IOException e) {
				System.out.println("전송 끊김: 정상");
			}
		}
	}

	private void sendChatMessage(String message, Session session) throws IOException {
		Set<Session> roomSessions = rooms.get(this.roomId);
		synchronized (rooms) {
			if (roomSessions == null) {
				return;
			}

			String chatClient = message.split("::")[1];
			String chatMsg = message.split("::")[2];
			UserDAO userDAO = new UserDAO();
			UserDTO user = new UserDTO();
			user = userDAO.readUserByNickname(chatClient);

			ChatLogDAO chatLogDAO = new ChatLogDAO();
			ChatLogDTO chatLog = new ChatLogDTO();
			chatLog.setUserId(user.getId());
			chatLog.setMessage(chatMsg);
			chatLog.setPlace(ChatLogDTO.Place.ROOM);
			chatLogDAO.createChatLog(chatLog);

			for (Session s : roomSessions) {
				if (s.isOpen()) {
					if (!s.equals(session)) { // 메시지를 보낸 클라이언트는 제외
						s.getBasicRemote().sendText(message);
					}
				}
			}
		}
	}

	private void inputTaja(String message, Session session) {

	}

	private void completeTaja(String message, Session session) {

	}
}
