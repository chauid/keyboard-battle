package socket;

import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint("/room-chat/{roomId}")
public class RoomChat {
    private static final Map<String, Set<Session>> rooms = new ConcurrentHashMap<>();
    private Session session;
    private String roomId;

    @OnOpen
    public void onOpen(@PathParam("roomId") String roomId, Session session) {
        this.session = session;
        this.roomId = roomId;
        rooms.computeIfAbsent(roomId, k -> ConcurrentHashMap.newKeySet()).add(session);
        System.out.println("Client connected to room: " + roomId);
    }

    @OnMessage
    public void onMessage(String message) {
        broadcastToRoom(roomId, message);
    }

    @OnClose
    public void onClose() {
        Set<Session> roomSessions = rooms.get(roomId);
        if (roomSessions != null) {
            roomSessions.remove(session);
            if (roomSessions.isEmpty()) {
                rooms.remove(roomId);
            }
        }
        System.out.println("Client disconnected from room: " + roomId);
    }

    @OnError
    public void onError(Throwable throwable) {
        throwable.printStackTrace();
    }

    private void broadcastToRoom(String roomId, String message) {
        Set<Session> roomSessions = rooms.get(roomId);
        if (roomSessions != null) {
            for (Session s : roomSessions) {
                if (s.isOpen()) {
                    try {
                        s.getBasicRemote().sendText(message);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
