package middleware;

import java.io.IOException;

import dao.RoomDAO;
import dao.UserRoomDAO;
import dto.RoomDTO;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebFilter(urlPatterns = { "/room", "/room/index.html" })
public class RoomMiddleware implements Filter {
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse res = (HttpServletResponse) response;
		Object passwordSession = req.getSession().getAttribute("roompw");
		Object userSession = req.getSession().getAttribute("user");
		String roomId = req.getParameter("id");
		if (roomId == null || userSession == null) { // 방 정보를 받아올 때까지 통과, 세션 없어도 통과: Middleware에서 처리
			chain.doFilter(request, response);
			return;
		}
//		System.out.println("room middleware 실행");

		RoomDAO roomDao = new RoomDAO();
		RoomDTO roomDto = roomDao.readRoomById(roomId);
		UserRoomDAO userRoomDao = new UserRoomDAO();
		int userCount = userRoomDao.readUserRoomCountByRoomId(roomId);
		int userId = Integer.parseInt(userSession.toString());

		if (roomDto == null) { // 방 정보가 없음
//			System.out.println("방 정보가 없음");
			res.sendRedirect("/keyboard-battle/free-channel");
			return;
		}

		boolean allowSpectator = roomDto.isAllowSpectator();
//		System.out.println("client: " + userCount);

		if (roomDto.getPassword() == null || roomDto.getPassword().equals("")) { // 비밀번호가 없는 방
//			System.out.println("비밀번호가 없는 방");
			if (allowSpectator) {
				if (userCount > 10) { // 10명 인원 제한
					userRoomDao.deleteUserRoomByUserId(userId);
					res.sendRedirect("/keyboard-battle/free-channel");
					return;
				}
				userRoomDao.createUserRoom(userId, roomId);
				chain.doFilter(request, response);
				return;
			} else {
				if (userCount > 2) { // 2명 인원 제한
					userRoomDao.deleteUserRoomByUserId(userId);
					res.sendRedirect("/keyboard-battle/free-channel");
					return;
				}
				userRoomDao.createUserRoom(userId, roomId);
				chain.doFilter(request, response);
				return;
			}
		} else {
			if (passwordSession == null) { // 세션에 비밀번호가 없음
//				System.out.println("세션에 비밀번호가 없음");
				userRoomDao.deleteUserRoomByUserId(userId);
				res.sendRedirect("/keyboard-battle/free-channel");
				return;
			}
			if (!roomDto.getPassword().equals(passwordSession.toString())) { // 비밀번호가 일치하지 않음
//				System.out.println("비밀번호가 일치하지 않음");
				userRoomDao.deleteUserRoomByUserId(userId);
				res.sendRedirect("/keyboard-battle/free-channel");
				return;
			}
		}

		if (allowSpectator) {
			if (userCount > 10) { // 10명 인원 제한
				userRoomDao.deleteUserRoomByUserId(userId);
				res.sendRedirect("/keyboard-battle/free-channel");
				return;
			}
			userRoomDao.createUserRoom(userId, roomId);
			chain.doFilter(request, response);
			return;
		} else {
			if (userCount > 2) { // 2명 인원 제한
				userRoomDao.deleteUserRoomByUserId(userId);
				res.sendRedirect("/keyboard-battle/free-channel");
				return;
			}
			userRoomDao.createUserRoom(userId, roomId);
			chain.doFilter(request, response);
			return;
		}
	}
}
