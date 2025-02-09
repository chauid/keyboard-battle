<%@page import="org.json.simple.JSONObject"%>
<%@page import="java.util.Set"%>
<%@page import="listener.SessionTracker"%>
<%@page import="dao.UserDAO"%>
<%@page import="dto.UserDTO"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
Object userSession = session.getAttribute("user");
Integer userId = 0;
if (userSession != null) {
	userId = Integer.parseInt(userSession.toString());
}
UserDAO userDao = new UserDAO();
UserDTO user = userDao.readUserById(userId);

response.setContentType("application/json");
response.setCharacterEncoding("UTF-8");
JSONObject json = new JSONObject();
json.put("nickname", user.getNickname());
json.put("level", user.getLevel());
json.put("exp", user.getCurrentExp());
json.put("thumbnail", user.getThumbnailImage());
response.getWriter().print(json);
%>
