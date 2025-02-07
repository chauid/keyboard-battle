<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@page import="org.json.simple.JSONObject"%>
<%@page import="dto.UserDTO"%>
<jsp:useBean id="dao" class="dao.UserDAO" />
<jsp:useBean id="dto" class="dto.UserDTO" />
<jsp:setProperty property="*" name="dto" />
<%
UserDTO user = dao.readUserByEmail(dto.getEmail());
if (user != null && dto.getPassword().equals(user.getPassword())) {
	session.setAttribute("user", user.getId());
	Cookie cookie = new Cookie("nickname", user.getNickname());
	cookie.setPath("/");
	cookie.setMaxAge(60 * 60 * 24 * 7);
	response.addCookie(cookie);
	
	JSONObject reponseJson = new JSONObject();
	reponseJson.put("result", "success");

	response.setStatus(200);
	response.getWriter().write(reponseJson.toString());

} else {
	JSONObject reponseJson = new JSONObject();
	reponseJson.put("result", "failed");
	response.setStatus(200);
	response.getWriter().write(reponseJson.toString());
}
%>