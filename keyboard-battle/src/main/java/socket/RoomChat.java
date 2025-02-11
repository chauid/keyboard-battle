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

import dao.RoomDAO;
import dao.UserDAO;
import dao.UserRoomDAO;
import dto.UserRoomDTO;

@ServerEndpoint("/room-chat/{roomId}")
public class RoomChat {
	private static final Map<String, Set<Session>> rooms = Collections.synchronizedMap(new ConcurrentHashMap<>());
	private static final Map<String, int[]> spaces = Collections.synchronizedMap(new ConcurrentHashMap<>(10));
	private Session session;
	private String roomId;

	@OnOpen
	public void onOpen(@PathParam("roomId") String roomId, Session session) {
		this.session = session;
		this.roomId = roomId;
		rooms.computeIfAbsent(roomId, k -> ConcurrentHashMap.newKeySet()).add(session); // 세션 추가
		spaces.computeIfAbsent(roomId, k -> new int[10]); // 방에 10개의 공간 생성

//		UserRoomDAO userRoomDAO = new UserRoomDAO();
//		UserRoomDTO userRoom = userRoomDAO.readUserRoomBySocketSessionId(session.getId(), roomId);
//		
//		int clientCount = getNumberOfClientsInRoom(roomId);
//		String message = "client:" + clientCount;
//
//		try {
//			sendToAllClientsInRoom(message);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
	}

	@OnMessage
	public void onMessage(String message) throws IOException {
		System.out.println("Received message: " + message);
		String messageType = message.split(":")[0];
		switch (messageType) {
		case "chat":
			sendChatMessage(message);
			break;
		case "new":
			newUserRegist(message);
			break;
		default:
			System.out.println("Unknown message type: " + messageType);
			break;
		}
	}

	@OnClose
	public void onClose() {
		Set<Session> roomSessions = rooms.get(this.roomId);
		if (roomSessions != null) {
			roomSessions.remove(this.session);
			RoomDAO roomDAO = new RoomDAO();
			UserRoomDAO userRoomDAO = new UserRoomDAO();
			if (roomSessions.isEmpty()) {
				rooms.remove(this.roomId);
				roomDAO.deleteRoom(this.roomId); // cascade delete user_room
			} else {
//				RoomDTO room = roomDAO.readRoomById(roomId);
//				room.setUserId(users.get(roomId).iterator().next());
//				roomDAO.updateRoom(room);
				userRoomDAO.deleteUserRoomBySocketSessionId(this.session.getId(), this.roomId);
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
		int[] space = spaces.get(this.roomId);
		for (int i = 0; i < space.length; i++) { // 배열을 순회하면서 빈 자리에 유저 추가
			if (space[i] == 0) {
				space[i] = Integer.parseInt(userId);
				break;
			}
		}
		
		try {
			sendToAllClientsInRoom("new:" + userId);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static int getNumberOfClientsInRoom(String roomId) {
		Set<Session> roomSessions = rooms.get(roomId);
		return roomSessions != null ? roomSessions.size() : 0;
	}
}
