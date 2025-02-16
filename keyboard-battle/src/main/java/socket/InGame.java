package socket;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;

import dao.ChatLogDAO;
import dao.UserDAO;
import dto.ChatLogDTO;
import dto.UserDTO;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;

@ServerEndpoint("/in-game/{roomId}")
public class InGame {
	private static final Map<String, Set<Session>> rooms = Collections.synchronizedMap(new ConcurrentHashMap<>());
	private static final Map<String, Boolean> isPlaying = Collections.synchronizedMap(new ConcurrentHashMap<>());
	private static final Map<String, int[]> spaces = Collections.synchronizedMap(new ConcurrentHashMap<>());
	private static final Map<String, ScheduledExecutorService> schedules = Collections.synchronizedMap(new ConcurrentHashMap<>());
	@OnOpen
	public void onOpen(Session session) {
		System.out.println(session.getId() + " 접속");
		
//		ScheduledExecutorService scheduler = schedules.get(roomId);
//		scheduler = Executors.newScheduledThreadPool(1);
//        scheduler.scheduleAtFixedRate(() -> {
//            try {
//                if (session.isOpen()) {
//                    session.getBasicRemote().sendText("서버 메시지: " + System.currentTimeMillis());
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }, 0, 1, TimeUnit.SECONDS);
	}

	@OnMessage
	public void onMessage(String message, Session session) throws IOException {
		synchronized (clients) {
			String chatClient = message.split(":")[0];
			String chatMsg = message.split(":")[1];
			UserDAO userDAO = new UserDAO();
			UserDTO user = new UserDTO();
			user = userDAO.readUserByNickname(chatClient);
			
			ChatLogDAO chatLogDAO = new ChatLogDAO();
			ChatLogDTO chatLog = new ChatLogDTO();
			chatLog.setUserId(user.getId());
			chatLog.setMessage(chatMsg);
			chatLog.setPlace(ChatLogDTO.Place.FREE);
			chatLogDAO.createChatLog(chatLog);
			
			for (Session client : clients) {
//				System.out.println(session.getId() + " : " + message);
				if (!client.equals(session)) { // 메시지를 보낸 클라이언트는 제외
					client.getBasicRemote().sendText(message);
				}
			}
		}
	}

	@OnClose
	public void onClose(Session session) {
		clients.remove(session);
	}

	@OnError
	public void onError(Throwable e) {
		e.printStackTrace();
	}
	
	public static int getNumberOfClients() {
		return clients.size();
	}
}
