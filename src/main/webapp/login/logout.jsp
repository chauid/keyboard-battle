<%@page import="org.json.simple.JSONObject"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@page import="dto.UserDTO"%>
<jsp:useBean id="sessionDao" class="dao.UserSessionDAO" />
<jsp:setProperty property="*" name="dto" />
<%
Cookie[] cookies = request.getCookies();
if (cookies != null) {
	for (int i = 0; i < cookies.length; i++) {
		String name = cookies[i].getName();
		if (name.equals("nickname")) {
	Cookie loginCookie = cookies[i];
	loginCookie.setPath("/");
	loginCookie.setMaxAge(0);
	response.addCookie(loginCookie);
	break;
		}
	}
}
Object userId = session.getAttribute("user");
if (userId == null) {
	response.sendRedirect("/");
	return;
}
sessionDao.deleteUserSessionByUserId(Integer.parseInt(userId.toString()));
session.invalidate();
response.sendRedirect("/");
%>