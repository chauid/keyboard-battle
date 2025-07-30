<%@page import="dto.TitleDTO"%>
<%@page import="dao.TitleDAO"%>
<%@page import="org.json.simple.JSONObject"%>
<%@page import="dto.UserDTO"%>
<%@page import="dao.UserDAO"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
String userNickname = request.getParameter("user");
UserDAO userDao = new UserDAO();
UserDTO user = userDao.readUserByNickname(request.getParameter("user"));

if (user == null) {
	response.setStatus(404);
	return;
}

TitleDAO titleDao = new TitleDAO();
TitleDTO title = titleDao.readTitleById(user.getTitle());
String titleName = "null";
if (title != null) {
	titleName = title.getName();
}
JSONObject reponseJson = new JSONObject();
reponseJson.put("email", user.getEmail());
reponseJson.put("nickname", user.getNickname());
reponseJson.put("level", user.getLevel());
reponseJson.put("exp", user.getCurrentExp());
reponseJson.put("playCount", user.getPlayCount());
reponseJson.put("win", user.getWinCount());
reponseJson.put("defeat", user.getDefeatCount());
reponseJson.put("highScore", user.getHighScore());
reponseJson.put("thumbnail", user.getThumbnailImage());
reponseJson.put("description", user.getDescription());
reponseJson.put("title", titleName);

response.setContentType("application/json");
response.setCharacterEncoding("UTF-8");
response.getWriter().print(reponseJson);
%>
