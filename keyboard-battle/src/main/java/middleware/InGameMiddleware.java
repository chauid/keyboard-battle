package middleware;

import java.io.IOException;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;

@WebFilter(urlPatterns = { "/room/in-game", "/room/in-game.html" })
public class InGameMiddleware implements Filter {
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		// 요청한 url에서 roomid값을 추출하고 db에 해당 방이 있는지 확인
		// user-room db에서 user가 존재하는가? 존재하지 않으면 방을 나가게 한다. user는 pk이기 때문에
		// 해당 유저가 인게임 상태가 아니라면 방을 나가게 한다.
		// 위에 조건을 통과하면 chain.doFilter(request, response)를 실행한다.
		
		chain.doFilter(request, response);
	}
}
