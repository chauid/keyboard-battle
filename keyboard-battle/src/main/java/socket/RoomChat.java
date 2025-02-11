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
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import dao.RoomDAO;
import dao.UserRoomDAO;
import dto.RoomDTO;
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
		int[] roomSpace = spaces.get(this.roomId);
		rooms.computeIfAbsent(roomId, k -> ConcurrentHashMap.newKeySet()).add(session); // 세션 추가
		if (roomSpace == null || roomSpace.length == 0) {
			spaces.computeIfAbsent(roomId, k -> new int[10]); // 방에 10개의 공간 생성
		}

		Set<Session> roomSessions = rooms.get(this.roomId);
		try {
			sendToAllClientsInRoom("client:" + roomSessions.size());
		} catch (IOException e) {
			e.printStackTrace();
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
	}

	@OnClose
	public void onClose() {
		Set<Session> roomSessions = rooms.get(this.roomId);
		int[] roomSpace = spaces.get(this.roomId);
		if (roomSessions != null) {
			roomSessions.remove(this.session);
			RoomDAO roomDao = new RoomDAO();
			UserRoomDAO userRoomDao = new UserRoomDAO();
			UserRoomDTO userRoom = userRoomDao.readUserRoomBySocketSessionId(this.session.getId(), this.roomId);
			// ===================================================== 이게 null이라네
			
			
			System.out.println("session id"+this.session.getId());
			System.out.println("room id"+this.roomId);

			for (int i = 0; i < roomSpace.length; i++) { // 배열을 순회하면서 나간 유저를 자리에서 삭제
				if (roomSpace[i] == userRoom.getUserId()) {
					roomSpace[i] = 0;
					break;
				}
			}
			
			try {
				sendToAllClientsInRoom("remove:" + roomSpace.length);
			} catch (IOException e) {
				e.printStackTrace();
			}

			if (roomSessions.isEmpty()) {
				roomDao.deleteRoom(this.roomId); // cascade delete user_room
				rooms.remove(this.roomId);
				spaces.remove(this.roomId);
			} else {
				RoomDTO room = roomDao.readRoomById(roomId);
				if (userRoom.getUserId() == room.getUserId()) { // 나간 유저가 방장인 경우
					int newRoomHost = 0;
					for (int i = 0; i < roomSpace.length; i++) {
						if (roomSpace[i] != 0) {
							newRoomHost = roomSpace[i];
							break;
						}
					}
					room.setUserId(newRoomHost);
					roomDao.updateRoom(room); // 새 방장 위임
				}
				userRoomDao.deleteUserRoomBySocketSessionId(this.session.getId(), this.roomId);
			}
		}
	}

	@OnError
	public void onError(Throwable throwable) {
		throwable.printStackTrace();
	}

	private void sendToAllClientsInRoom(String message) throws IOException {
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

		if (userRoom != null) {
			userRoom.setSocketSessionId(this.session.getId());
			userRoomDao.updateUserRoom(userRoom);
		} else { // middleware에 의해 삭제된 유저 정보 복구: 새로고침 시에만 발생
			userRoom = new UserRoomDTO();
			userRoom.setUserId(Integer.parseInt(userId));
			userRoom.setRoomId(this.roomId);
			userRoom.setSocketSessionId(this.session.getId());
			userRoomDao.createUserRoom(userRoom);
		}
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
			sendToAllClientsInRoom("move:" + userId + ":" + spaceIndex);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void sendChatMessage(String message) throws IOException {
		Set<Session> roomSessions = rooms.get(this.roomId);
		synchronized (rooms) {
			if (roomSessions == null) {
				return;
			}
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
		int[] roomSpace = spaces.get(this.roomId);
		int oldSpaceIndex = 0;
		for (int i = 0; i < roomSpace.length; i++) { // 배열을 순회하면서 기존 유저 위치 찾기
			if (roomSpace[i] == userId) {
				oldSpaceIndex = i;
				break;
			}
		}

		if (roomSpace[spaceIndex] != 0) { // 이미 차있는 자리인 경우
			return;
		}

		roomSpace[oldSpaceIndex] = 0;
		roomSpace[spaceIndex] = userId;
		try {
			sendToAllClientsInRoom("remove:" + oldSpaceIndex);
			sendToAllClientsInRoom("move:" + spaceIndex + ":" + userId);
		} catch (IOException e) {
			e.printStackTrace();
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
			} catch (IOException e) {
				e.printStackTrace();
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
			} catch (IOException e) {
				e.printStackTrace();
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
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static int getNumberOfClientsInRoom(String roomId) {
		Set<Session> roomSessions = rooms.get(roomId);
		return roomSessions != null ? roomSessions.size() : 0;
	}
}
