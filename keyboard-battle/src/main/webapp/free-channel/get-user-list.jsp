<%@page import="java.util.List"%>
<%@page import="org.json.simple.JSONArray"%>
<%@page import="org.json.simple.JSONObject"%>
<%@page import="java.util.Set"%>
<%@page import="listener.SessionTracker"%>
<%@page import="dao.UserDAO"%>
<%@page import="dto.UserDTO"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
Set<HttpSession> sessionSet = SessionTracker.getActiveSessions();
UserDAO userDao = new UserDAO();
List<UserDTO> users = userDao.readAllUser();
JSONArray userList = new JSONArray();

for (HttpSession s : sessionSet) {
	Object userSession = s.getAttribute("user");
	if (userSession != null) {
		UserDTO user = userDao.readUserById(Integer.parseInt(userSession.toString()));
		JSONObject json = new JSONObject();
		json.put("nickname", user.getNickname());
		json.put("level", user.getLevel());
		json.put("exp", user.getCurrentExp());
		json.put("thumbnail", user.getThumbnailImage());
		userList.add(json);
	}
}

response.setContentType("application/json");
response.setCharacterEncoding("UTF-8");
response.getWriter().print(userList);
%>
