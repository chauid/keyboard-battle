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
import dao.TitleDAO;
import dao.UserDAO;
import dao.UserRoomDAO;
import dto.ChatLogDTO;
import dto.RoomDTO;
import dto.TitleDTO;
import dto.UserDTO;
import dto.UserRoomDTO;

@ServerEndpoint("/room-chat/{roomId}")
public class RoomChat {
	private static final Map<String, Set<Session>> rooms = Collections.synchronizedMap(new ConcurrentHashMap<>());
	private static final Map<String, int[]> spaces = Collections.synchronizedMap(new ConcurrentHashMap<>());
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
			return;
		}
		if (room.isIngame()) {
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
		case "new":
			newUserRegist(message, session);
			break;
		case "chat":
			sendChatMessage(message, session);
			break;
		case "move":
			moveUser(message, session);
			break;
		case "remove":
			removeUser(message, session);
			break;
		case "host":
			changeHost(message, session);
			break;
		case "ready":
			readyUser(message, session);
			break;
		case "start":
			startGame(message, session);
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
		int[] roomSpace = spaces.get(this.roomId);
		if (roomSessions == null) {
			return;
		}

		RoomDAO roomDao = new RoomDAO();
		UserRoomDAO userRoomDao = new UserRoomDAO();
		RoomDTO room = roomDao.readRoomById(this.roomId);
		UserRoomDTO userRoom = userRoomDao.readUserRoomBySocketSessionId(session.getId(), this.roomId);

		boolean isIngame = false;
		List<UserRoomDTO> userRooms = userRoomDao.readUserRoomsByRoomId(this.roomId);
		for (UserRoomDTO ur : userRooms) {
			if (ur.isIngame()) {
				isIngame = true;
				break;
			}
		}

		if (isIngame) { // 유저가 게임중이라면 방은 그대로 유지. 유저 정보만 삭제
			roomSessions.remove(session);
			for (int i = 0; i < roomSpace.length; i++) {
				if (roomSpace[i] == userRoom.getUserId()) {
					roomSpace[i] = 0;
				}
			}
			return; // 대신에 인게임에서 돌아올 때는 db(user-room)에서 유저를 삭제하고 와야 함.
		}

		roomSessions.remove(session);

		if (roomSessions.isEmpty()) { // 유저가 모두 나간 경우
			roomDao.deleteRoom(this.roomId); // cascade delete user_room
			rooms.remove(this.roomId);
			spaces.remove(this.roomId);
			return;
		}

		int userCount = roomSessions.size();
		int maxUsersInRoom = room.isAllowSpectator() ? 10 : 2;
		synchronized (rooms) {
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
					for (Session s : roomSessions) {
						if (s.isOpen()) {
							s.getBasicRemote().sendText("client::" + userCount + "::" + maxUsersInRoom);
							s.getBasicRemote().sendText("remove::" + spaceIndex);
						}
					}
				} catch (IOException | IllegalStateException e) {
					System.out.println("전송 끊김1: 정상");
				}
			}

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
						for (Session s : roomSessions) {
							if (s.isOpen()) {
								s.getBasicRemote().sendText("host::" + newRoomHostIndex + "::" + roomSpace[newRoomHostIndex]);
							}
						}
					} catch (IOException | IllegalStateException e) {
						System.out.println("전송 끊김2: 정상");
					}
				}
				userRoomDao.deleteUserRoomBySocketSessionId(session.getId(), this.roomId);
			}
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

	private void newUserRegist(String message, Session session) {
		RoomDAO roomDao = new RoomDAO();
		RoomDTO room = roomDao.readRoomById(roomId);
		if (room == null) {
			return;
		}

		int[] roomSpace = spaces.get(this.roomId);
		rooms.computeIfAbsent(this.roomId, k -> ConcurrentHashMap.newKeySet()).add(session); // 세션 추가
		if (roomSpace == null) { // 없으면 생성
			spaces.computeIfAbsent(this.roomId, k -> new int[10]); // 방에 10개의 공간 생성
		}
		roomSpace = spaces.get(this.roomId); // 생성 후 가져오기
		Set<Session> roomSessions = rooms.get(this.roomId);

		UserDAO userDao = new UserDAO();
		TitleDAO titleDao = new TitleDAO();

		int maxUsersInRoom = room.isAllowSpectator() ? 10 : 2;

		try {
			sendToAllClientsInRoom("client::" + roomSessions.size() + "::" + maxUsersInRoom);
			sendToAllClientsInRoom("title::" + room.getName());
		} catch (IOException | IllegalStateException e) {
			System.out.println("전송 끊김3: 정상");
		}

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
			String isHost = room.getUserId() == user.getId() ? "true" : "false";

			UserRoomDAO userRoomDao = new UserRoomDAO();
			UserRoomDTO userRoom = userRoomDao.readUserRoomByUserId(user.getId());

			String moveMessage = "move::";
			moveMessage += i + "::"; // spaceIndex
			moveMessage += user.getNickname() + "::";
			moveMessage += user.getLevel() + "::";
			moveMessage += user.getCurrentExp() + "::";
			moveMessage += user.getThumbnailImage() + "::";
			moveMessage += titleName + "::";
			moveMessage += isHost + "::";
			moveMessage += userRoom != null && userRoom.isReady() ? "on" : "off";

			try {
				sendToAllClientsInRoom(moveMessage);
			} catch (IOException | IllegalStateException e) {
				System.out.println("전송 끊김4: 정상");
			}
		}

		int userId = Integer.parseInt(message.split("::")[1]);
		UserRoomDAO userRoomDao = new UserRoomDAO();
		UserRoomDTO userRoom = new UserRoomDTO();
		userRoom.setUserId(userId);
		userRoom.setRoomId(this.roomId);
		userRoomDao.createUserRoom(userRoom);

		for (Session s : roomSessions) {
			if (s.getId().equals(userRoom.getSocketSessionId())) { // 이미 등록된 세션이 있는 경우
				try {
					s.close(); // 기존 세션 종료
					roomSpace[userRoom.getSpaceIndex()] = 0; // 자리 반환
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		userRoom.setSocketSessionId(session.getId());

		int spaceIndex = 0;
		for (int i = 0; i < roomSpace.length; i++) { // 배열을 순회하면서 빈 자리에 유저 추가
			if (roomSpace[i] == 0) {
				roomSpace[i] = userId;
				spaceIndex = i;
				break;
			}
		}
		userRoom.setSpaceIndex(spaceIndex);
		userRoomDao.updateUserRoom(userRoom);

		room = roomDao.readRoomById(this.roomId);
		UserDTO user = userDao.readUserById(userId);
		TitleDTO title = titleDao.readTitleById(user.getTitle());
		String titleName = "null";
		if (title != null) {
			titleName = title.getName();
		}
		String isHost = room.getUserId() == user.getId() ? "true" : "false";

		String moveMessage = "move::";
		moveMessage += spaceIndex + "::";
		moveMessage += user.getNickname() + "::";
		moveMessage += user.getLevel() + "::";
		moveMessage += user.getCurrentExp() + "::";
		moveMessage += user.getThumbnailImage() + "::";
		moveMessage += titleName + "::";
		moveMessage += isHost;

		int hostIndex = 0;
		for (int i = 0; i < roomSpace.length; i++) {
			if (roomSpace[i] == room.getUserId()) {
				hostIndex = i;
				break;
			}
		}

		try {
			sendToAllClientsInRoom(moveMessage);
			sendToAllClientsInRoom("host::" + hostIndex + "::" + roomSpace[hostIndex]);
		} catch (IOException | IllegalStateException e) {
			System.out.println("전송 끊김5: 정상");
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

	private void moveUser(String message, Session session) {
		int userId = Integer.parseInt(message.split("::")[1]);
		int spaceIndex = Integer.parseInt(message.split("::")[2]);
		UserRoomDAO userRoomDao = new UserRoomDAO();
		UserRoomDTO userRoom = userRoomDao.readUserRoomByUserId(userId);
		if (userRoom == null) {
			return;
		}
		if (userRoom.isReady()) { // 준비완료 상태에는 이동 불가
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

		RoomDAO roomDao = new RoomDAO();
		RoomDTO room = roomDao.readRoomById(this.roomId);
		UserDAO userDao = new UserDAO();
		UserDTO user = userDao.readUserById(userId);
		TitleDAO titleDao = new TitleDAO();
		TitleDTO title = titleDao.readTitleById(user.getTitle());
		String titleName = "null";
		if (title != null) {
			titleName = title.getName();
		}
		String isHost = room.getUserId() == user.getId() ? "true" : "false";

		String moveMessage = "move::";
		if (userRoom.isReady() || roomSpace[spaceIndex] != 0) { // 준비 완료 상태에 또는 이미 차있는 자리인 경우 제자리 반환
			moveMessage += oldSpaceIndex + "::";
			userRoom.setSpaceIndex(oldSpaceIndex);
		} else {
			moveMessage += spaceIndex + "::";
			roomSpace[oldSpaceIndex] = 0;
			roomSpace[spaceIndex] = userId;
			userRoom.setSpaceIndex(spaceIndex);
		}
		moveMessage += user.getNickname() + "::";
		moveMessage += user.getLevel() + "::";
		moveMessage += user.getCurrentExp() + "::";
		moveMessage += user.getThumbnailImage() + "::";
		moveMessage += titleName + "::";
		moveMessage += isHost;

		userRoomDao.updateUserRoom(userRoom);

		try {
			sendToAllClientsInRoom("remove::" + oldSpaceIndex);
			sendToAllClientsInRoom(moveMessage);
		} catch (IOException | IllegalStateException e) {
			System.out.println("전송 끊김6: 정상");
		}
	}

	private void removeUser(String message, Session session) {
		int userId = Integer.parseInt(message.split("::")[1]);
		int spaceIndex = Integer.parseInt(message.split("::")[2]);
		RoomDAO roomDao = new RoomDAO();
		RoomDTO room = roomDao.readRoomById(this.roomId);
		if (userId == room.getUserId()) { // 방장 인증
			try {
				sendToAllClientsInRoom("remove::" + spaceIndex);
			} catch (IOException | IllegalStateException e) {
				System.out.println("전송 끊김7: 정상");
			}
		}
	}

	private void changeHost(String message, Session session) {
		int userId = Integer.parseInt(message.split("::")[1]);
		int spaceIndex = Integer.parseInt(message.split("::")[2]);
		RoomDAO roomDao = new RoomDAO();
		RoomDTO room = roomDao.readRoomById(this.roomId);
		if (userId == room.getUserId()) { // 방장 인증
			int[] roomSpace = spaces.get(this.roomId);
			room.setUserId(roomSpace[spaceIndex]);
			roomDao.updateRoom(room);
			try {
				sendToAllClientsInRoom("host::" + spaceIndex + "::" + roomSpace[spaceIndex]);
			} catch (IOException | IllegalStateException e) {
				System.out.println("전송 끊김8: 정상");
			}
		}
	}

	private void readyUser(String message, Session session) {
		int userId = Integer.parseInt(message.split("::")[1]);
		UserRoomDAO userRoomDao = new UserRoomDAO();
		UserRoomDTO userRoom = userRoomDao.readUserRoomByUserId(userId);
		String readyState = userRoom.isReady() ? "off" : "on";
		if (userRoom.isReady()) {
			userRoom.setReady(false);
		} else {
			userRoom.setReady(true);
		}
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
			sendToAllClientsInRoom("ready::" + spaceIndex + "::" + readyState);
		} catch (IOException | IllegalStateException e) {
			System.out.println("전송 끊김9: 정상");
		}
	}

	private void startGame(String message, Session session) {
		int userId = Integer.parseInt(message.split("::")[1]);
		RoomDAO roomDao = new RoomDAO();
		UserRoomDAO userRoomDao = new UserRoomDAO();
		RoomDTO room = roomDao.readRoomById(this.roomId);
		if (room.getUserId() != userId) { // 방장 인증
			try {
				session.getBasicRemote().sendText("start::false::auth");
			} catch (IOException | IllegalStateException e) {
				System.out.println("전송 끊김10: 정상");
			}
			return;
		}
		int[] roomSpace = spaces.get(this.roomId);
		int playerCount = 0;
		if (roomSpace[0] != 0) {
			playerCount++;
		}
		if (roomSpace[1] != 0) {
			playerCount++;
		}

		if (playerCount < 2) { // 플레이어가 2명이어야 시작 가능
			try {
				session.getBasicRemote().sendText("start::false::two");
			} catch (IOException | IllegalStateException e) {
				System.out.println("전송 끊김11: 정상");
			}
			return;
		}

		List<UserRoomDTO> userRoom = userRoomDao.readUserRoomsByRoomId(this.roomId);
		boolean allReady = true;
		for (UserRoomDTO ur : userRoom) {
			if (ur.getUserId() == userId) { // 방장은 준비에서 제외
				continue;
			}
			if (!ur.isReady()) {
				allReady = false;
				break;
			}
		}

		if (allReady) {
			for (UserRoomDTO ur : userRoom) {
				ur.setIngame(true);
				userRoomDao.updateUserRoom(ur);
			}
			room.setIngame(true);
			roomDao.updateRoom(room);

			try {
				sendToAllClientsInRoom("start::true::null");
			} catch (IOException | IllegalStateException e) {
				System.out.println("전송 끊김12: 정상");
			}
		} else {
			try {
				session.getBasicRemote().sendText("start::false::ready");
			} catch (IOException | IllegalStateException e) {
				System.out.println("전송 끊김13: 정상");
			}
		}
	}

	public static int getNumberOfClientsInRoom(String roomId) {
		Set<Session> roomSessions = rooms.get(roomId);
		return roomSessions != null ? roomSessions.size() : 0;
	}
}
