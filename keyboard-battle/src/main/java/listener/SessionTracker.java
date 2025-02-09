package listener;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import jakarta.servlet.annotation.WebListener;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;

@WebListener
public class SessionTracker implements HttpSessionListener {
    private static final Set<HttpSession> activeSessions = Collections.synchronizedSet(new HashSet<HttpSession>());

    @Override
    public void sessionCreated(HttpSessionEvent event) {
        HttpSession session = event.getSession();
        activeSessions.add(session);
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent event) {
        HttpSession session = event.getSession();
        activeSessions.remove(session);
    }

    public static Set<HttpSession> getActiveSessions() {
        return activeSessions;
    }
}