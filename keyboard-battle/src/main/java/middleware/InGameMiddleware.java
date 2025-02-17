package middleware;

import java.io.IOException;

import dao.RoomDAO;
import dao.UserRoomDAO;
import dto.RoomDTO;
import dto.UserRoomDTO;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebFilter(urlPatterns = { "/room/in-game", "/room/in-game.html" })
public class InGameMiddleware implements Filter {
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse res = (HttpServletResponse) response;
		Object userSession = req.getSession().getAttribute("user");
		String roomId = req.getParameter("id");
		
		if (roomId == null || userSession == null) { // 방 정보를 받아올 때까지 통과, 세션 없어도 통과: Middleware에서 처리
			chain.doFilter(request, response);
			return;
		}
		
		RoomDAO roomDao = new RoomDAO();
		RoomDTO roomDto = roomDao.readRoomById(roomId);
		int userId = Integer.parseInt(userSession.toString());
		
		if (roomDto == null) { // 방 정보가 없음
			res.sendRedirect("/free-channel");
			return;
		}
		
		UserRoomDAO userRoomDao = new UserRoomDAO();
		UserRoomDTO userRoomDto = userRoomDao.readUserRoomByUserId(userId);
		
		if (userRoomDto == null) { // 해당 방에 유저가 없음
			res.sendRedirect("/free-channel");
			return;
		}
		
		if (!userRoomDto.isIngame()) { // 해당 유저가 인게임 상태가 아님
			res.sendRedirect("/free-channel");
			return;
		}
		
		chain.doFilter(request, response);
	}
}
