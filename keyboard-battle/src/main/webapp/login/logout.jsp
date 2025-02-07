<%@page import="org.json.simple.JSONObject"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@page import="dto.UserDTO"%>
<jsp:useBean id="dao" class="dao.UserDAO" />
<jsp:useBean id="dto" class="dto.UserDTO" />
<jsp:setProperty property="*" name="dto" />
<%
session.removeAttribute("user");
Cookie[] cookies = request.getCookies();
if (cookies != null) {
	for (int i = 0; i < cookies.length; i++) {
		String name = cookies[i].getName();
		if (name.equals("user")) {
			Cookie loginCookie = cookies[i];
			loginCookie.setPath("/");
			loginCookie.setMaxAge(0);
			response.addCookie(loginCookie);
			break;
		}
	}
}
//메인 페이지로 이동
response.sendRedirect("/keyboard-battle");
%>