package middleware;

import java.io.IOException;
import java.util.Set;

import dao.UserSessionDAO;
import dto.UserSessionDTO;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import listener.SessionTracker;

@WebFilter(urlPatterns = { "/free-channel/*", "/profile/*", "/room/*" })
public class Middleware implements Filter {
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse res = (HttpServletResponse) response;

		Object currentSession = req.getSession().getAttribute("user");
		Set<HttpSession> sessionSet = SessionTracker.getActiveSessions();

		if (currentSession != null) { // 인증 성공
			chain.doFilter(request, response);
			return;
		}
//		System.out.println("middleware 실행");

		
		// 세션이 일치하지 않음
		int userId = 0;
		Cookie[] cookies = req.getCookies();
		if (cookies == null) { // 쿠키가 없음
			res.sendRedirect("/login");
			return;
		}
		for (Cookie cookie : cookies) {
			if (cookie.getName().equals("userid")) {
				userId = Integer.parseInt(cookie.getValue());
			}
		}
		if (userId == 0) { // userid 쿠키가 없음
//			System.out.println("currentCookie is null");
			res.sendRedirect("/login");
			return;
		}

		UserSessionDAO userSessionDAO = new UserSessionDAO();
		UserSessionDTO sessionDTO = userSessionDAO.readUserSessionByUserId(userId);
		if (sessionDTO == null) { // 세션 정보가 없음
//			System.out.println("sessionDTO is null");
			res.sendRedirect("/login");
			return;
		}

		boolean isSessionExist = false;
		for (HttpSession curSession : sessionSet) {
			String curSessionId = curSession.getId();
//			System.out.println("sessionDTO.getSessionId(): " + sessionDTO.getSessionId());
//			System.out.println("curSessionId: " + curSessionId);
			if (sessionDTO.getSessionId().equals(curSessionId)) {
				for (int i = 0; i < cookies.length; i++) {
					String name = cookies[i].getName();
					if (name.equals("JSESSIONID")) {
						Cookie loginCookie = cookies[i];
						loginCookie.setPath("/");
						loginCookie.setMaxAge(0); // 현재 세션 쿠키 삭제
						res.addCookie(loginCookie);
						Cookie cookie = new Cookie("JSESSIONID", curSessionId); // 기존 세션 사용
						cookie.setPath("/");
						cookie.setHttpOnly(true);
						res.addCookie(cookie);
						break;
					}
				}
				isSessionExist = true;
				break;
			}
		}

//		sessionSet.forEach(session -> {
//			System.out.println("session id: " + session.getId());
//		});

		if (isSessionExist) {
//			System.out.println("session is exist");
			chain.doFilter(request, response);
			return;
		} else {
//			System.out.println("session is not exist");
			res.sendRedirect("/login");
			return;
		}
	}

}
