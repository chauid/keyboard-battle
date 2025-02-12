package socket;

import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import dao.ChatLogDAO;
import dao.RoomDAO;
import dao.UserDAO;
import dao.UserRoomDAO;
import dto.ChatLogDTO;
import dto.RoomDTO;
import dto.UserDTO;
import dto.UserRoomDTO;

@ServerEndpoint("/room-chat/{roomId}")
public class RoomChat {
	private static final Map<String, Set<Session>> rooms = Collections.synchronizedMap(new ConcurrentHashMap<>());
	private static final Map<String, int[]> spaces = Collections.synchronizedMap(new ConcurrentHashMap<>());
	private Session session;
	private String roomId;

	@OnOpen
	public void onOpen(@PathParam("roomId") String roomId, Session session) {
		this.session = session;
		this.roomId = roomId;
		int[] roomSpace = spaces.get(roomId);
		rooms.computeIfAbsent(roomId, k -> ConcurrentHashMap.newKeySet()).add(session); // 세션 추가
		if (roomSpace == null) { // 없으면 생성
			spaces.computeIfAbsent(roomId, k -> new int[10]); // 방에 10개의 공간 생성
		}
		roomSpace = spaces.get(roomId); // 생성 후 가져오기
		Set<Session> roomSessions = rooms.get(this.roomId);

		RoomDAO roomDao = new RoomDAO();
		RoomDTO room = roomDao.readRoomById(roomId);
		int maxUsersInRoom = room.isAllowSpectator() ? 10 : 2;

		try {
			sendToAllClientsInRoom("new");
			sendToAllClientsInRoom("client:" + roomSessions.size() + ":" + maxUsersInRoom);
			sendToAllClientsInRoom("title:" + room.getName());
		} catch (IOException | IllegalStateException e) {
			System.out.println("전송 끊김: 정상");
		}

		for (int i = 0; i < roomSpace.length; i++) { // 모든 자리의 유저 정보 전송
			try {
				sendToAllClientsInRoom("move:" + i + ":" + roomSpace[i]);
			} catch (IOException | IllegalStateException e) {
				System.out.println("전송 끊김: 정상");
			}
		}
	}

	@OnMessage
	public void onMessage(String message) throws IOException {
		System.out.println("Received message: " + message);
		String messageType = message.split(":")[0];
		switch (messageType) {
		case "new":
			newUserRegist(message);
			break;
		case "chat":
			sendChatMessage(message);
			break;
		case "move":
			moveUser(message);
			break;
		case "remove":
			removeUser(message);
			break;
		case "host":
			changeHost(message);
			break;
		case "ready":
			readyUser(message);
			break;
		case "start":

			break;
		default:
			System.out.println("Unknown message type: " + messageType);
			break;
		}

		int[] roomSpace = spaces.get(this.roomId);
		if (roomSpace != null) {
			System.out.print("Room space: ");
			for (int i = 0; i < roomSpace.length; i++) {
				System.out.print(roomSpace[i] + " ");
			}
			System.out.println();
		}
	}

	@OnClose
	public void onClose() {
		Set<Session> roomSessions = rooms.get(this.roomId);
		int[] roomSpace = spaces.get(this.roomId);

		if (roomSessions == null) {
			return;
		}

		RoomDAO roomDao = new RoomDAO();
		RoomDTO room = roomDao.readRoomById(this.roomId);
		UserRoomDAO userRoomDao = new UserRoomDAO();
		UserRoomDTO userRoom = userRoomDao.readUserRoomBySocketSessionId(this.session.getId(), this.roomId);

		int maxUsersInRoom = room.isAllowSpectator() ? 10 : 2;
		if (userRoom != null) { // 소켓에 유저를 등록한 이후
			int spaceIndex = 0;
			for (int i = 0; i < roomSpace.length; i++) { // 배열을 순회하면서 나간 유저를 자리에서 삭제
				if (roomSpace[i] == userRoom.getUserId()) {
					roomSpace[i] = 0;
					spaceIndex = i;
					break;
				}
			}

			try {
				sendToAllClientsInRoom("remove:" + spaceIndex);
				sendToAllClientsInRoom("client:" + roomSessions.size() + ":" + maxUsersInRoom);
			} catch (IOException | IllegalStateException e) {
				System.out.println("전송 끊김: 정상");
			}
		}

		roomSessions.remove(this.session);
		if (roomSessions.isEmpty()) { // 유저가 모두 나간 경우
			roomDao.deleteRoom(this.roomId); // cascade delete user_room
			rooms.remove(this.roomId);
			spaces.remove(this.roomId);
		} else { // 유저가 1명 이상 남은 경우
			if (userRoom == null) {
				return;
			}
			if (userRoom.getUserId() == room.getUserId()) { // 나간 유저가 방장인 경우
				int newRoomHost = 0;
				int newRoomHostIndex = 0;
				for (int i = 0; i < roomSpace.length; i++) {
					if (roomSpace[i] != 0) {
						newRoomHost = roomSpace[i];
						newRoomHostIndex = i;
						break;
					}
				}
				room.setUserId(newRoomHost);
				roomDao.updateRoom(room); // 새 방장 위임
				try {
					sendToAllClientsInRoom("host:" + newRoomHostIndex);
				} catch (IOException | IllegalStateException e) {
					System.out.println("전송 끊김: 정상");
				}
			}
			userRoomDao.deleteUserRoomBySocketSessionId(this.session.getId(), this.roomId);
		}
	}

	@OnError
	public void onError(Throwable throwable) {
		throwable.printStackTrace();
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

	private void newUserRegist(String message) {
		String userId = message.split(":")[1];
		UserRoomDAO userRoomDao = new UserRoomDAO();
		UserRoomDTO userRoom = userRoomDao.readUserRoomByUserId(Integer.parseInt(userId));
		if (userRoom == null) {
			return;
		}

		userRoom.setSocketSessionId(this.session.getId());
		userRoomDao.updateUserRoom(userRoom);

		int[] roomSpace = spaces.get(this.roomId);
		int spaceIndex = 0;
		for (int i = 0; i < roomSpace.length; i++) { // 배열을 순회하면서 빈 자리에 유저 추가
			if (roomSpace[i] == 0) {
				roomSpace[i] = Integer.parseInt(userId);
				spaceIndex = i;
				break;
			}
		}

		try {
			sendToAllClientsInRoom("move:" + spaceIndex + ":" + userId);
		} catch (IOException | IllegalStateException e) {
			System.out.println("전송 끊김: 정상");
		}
	}

	private void sendChatMessage(String message) throws IOException {
		Set<Session> roomSessions = rooms.get(this.roomId);
		synchronized (rooms) {
			if (roomSessions == null) {
				return;
			}

			String chatClient = message.split(":")[1];
			String chatMsg = message.split(":")[2];
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

	private void moveUser(String message) {
		int userId = Integer.parseInt(message.split(":")[1]);
		int spaceIndex = Integer.parseInt(message.split(":")[2]);
		UserRoomDAO userRoomDao = new UserRoomDAO();
		UserRoomDTO userRoom = userRoomDao.readUserRoomByUserId(userId);
		if (userRoom == null) {
			return;
		}

		int[] roomSpace = spaces.get(this.roomId);
		int oldSpaceIndex = 0;
		for (int i = 0; i < roomSpace.length; i++) { // 배열을 순회하면서 기존 유저 위치 찾기
			if (roomSpace[i] == userId) {
				oldSpaceIndex = i;
				break;
			}
		}

		if (userRoom.isReady()) { // 준비 완료 상태에서는 이동 불가
			try {
				sendToAllClientsInRoom("move:" + oldSpaceIndex + ":" + userId); // 제자리 반환
			} catch (IOException | IllegalStateException e) {
				System.out.println("전송 끊김: 정상");
			}
			return;
		}

		if (roomSpace[spaceIndex] != 0) { // 이미 차있는 자리인 경우
			try {
				sendToAllClientsInRoom("move:" + oldSpaceIndex + ":" + userId); // 제자리 반환
			} catch (IOException | IllegalStateException e) {
				System.out.println("전송 끊김: 정상");
			}
			return;
		}

		roomSpace[oldSpaceIndex] = 0;
		roomSpace[spaceIndex] = userId;
		try {
			sendToAllClientsInRoom("remove:" + oldSpaceIndex);
			sendToAllClientsInRoom("move:" + spaceIndex + ":" + userId);
		} catch (IOException | IllegalStateException e) {
			System.out.println("전송 끊김: 정상");
		}
	}

	private void removeUser(String message) {
		int userId = Integer.parseInt(message.split(":")[1]);
		int spaceIndex = Integer.parseInt(message.split(":")[2]);
		RoomDAO roomDao = new RoomDAO();
		RoomDTO room = roomDao.readRoomById(this.roomId);
		if (userId == room.getUserId()) { // 방장 인증
			try {
				sendToAllClientsInRoom("remove:" + spaceIndex);
			} catch (IOException | IllegalStateException e) {
				System.out.println("전송 끊김: 정상");
			}
		}
	}

	private void changeHost(String message) {
		int userId = Integer.parseInt(message.split(":")[1]);
		int spaceIndex = Integer.parseInt(message.split(":")[2]);
		RoomDAO roomDao = new RoomDAO();
		RoomDTO room = roomDao.readRoomById(this.roomId);
		if (userId == room.getUserId()) { // 방장 인증
			int[] roomSpace = spaces.get(this.roomId);
			room.setUserId(roomSpace[spaceIndex]);
			roomDao.updateRoom(room);
			try {
				sendToAllClientsInRoom("host:" + spaceIndex);
			} catch (IOException | IllegalStateException e) {
				System.out.println("전송 끊김: 정상");
			}
		}
	}

	private void readyUser(String message) {
		int userId = Integer.parseInt(message.split(":")[1]);
		UserRoomDAO userRoomDao = new UserRoomDAO();
		UserRoomDTO userRoom = userRoomDao.readUserRoomByUserId(userId);
		userRoom.setReady(true);
		userRoomDao.updateUserRoom(userRoom);
		int[] roomSpace = spaces.get(this.roomId);
		int spaceIndex = 0;
		for (int i = 0; i < roomSpace.length; i++) {
			if (roomSpace[i] == userId) {
				spaceIndex = i;
				break;
			}
		}
		try {
			sendToAllClientsInRoom("ready:" + spaceIndex);
		} catch (IOException | IllegalStateException e) {
			System.out.println("전송 끊김: 정상");
		}
	}

	public static int getNumberOfClientsInRoom(String roomId) {
		Set<Session> roomSessions = rooms.get(roomId);
		return roomSessions != null ? roomSessions.size() : 0;
	}
}
