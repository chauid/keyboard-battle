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

@ServerEndpoint("/room-chat/{roomId}")
public class RoomChat {
	private static final Map<String, Set<Session>> rooms = Collections.synchronizedMap(new ConcurrentHashMap<>());
	private Session session;
	private String roomId;
	
	@OnOpen
	public void onOpen(@PathParam("roomId") String roomId, Session session) {
		this.session = session;
		this.roomId = roomId;
		rooms.computeIfAbsent(roomId, k -> ConcurrentHashMap.newKeySet()).add(session); // 세션 추가
//		System.out.println("Client connected to room: " + roomId);

		int clientCount = getNumberOfClientsInRoom(roomId);
		String message = "client:" + clientCount;

		Set<Session> roomSessions = rooms.get(roomId);
		try {
			sendToAllClientsInRoom(message);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@OnMessage
	public void onMessage(String message) throws IOException {
		Set<Session> roomSessions = rooms.get(roomId);
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

	@OnClose
	public void onClose() {
		Set<Session> roomSessions = rooms.get(roomId);
		if (roomSessions != null) {
			roomSessions.remove(session);
			RoomDAO roomDAO = new RoomDAO();
			UserRoomDAO userRoomDAO = new UserRoomDAO();
			if (roomSessions.isEmpty()) {
				rooms.remove(roomId);
				roomDAO.deleteRoom(roomId); // cascade delete user_room
			} else {
//				RoomDTO room = roomDAO.readRoomById(roomId);
//				room.setUserId(users.get(roomId).iterator().next());
//				roomDAO.updateRoom(room);
				// ================================================= 여기부터 이어하기
			}
		}
//		System.out.println("Client disconnected from room: " + roomId);
	}

	@OnError
	public void onError(Throwable throwable) {
		throwable.printStackTrace();
	}

	private void sendToAllClientsInRoom(String message) throws IOException {
		Set<Session> roomSessions = rooms.get(roomId);
		for (Session s : roomSessions) {
			if (s.isOpen()) {
				s.getBasicRemote().sendText(message);
			}
		}
	}

	public static int getNumberOfClientsInRoom(String roomId) {
		Set<Session> roomSessions = rooms.get(roomId);
		return roomSessions != null ? roomSessions.size() : 0;
	}
}
