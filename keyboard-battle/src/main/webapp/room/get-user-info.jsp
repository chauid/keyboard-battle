<%@page import="dto.RoomDTO"%>
<%@page import="dao.RoomDAO"%>
<%@page import="dto.TitleDTO"%>
<%@page import="dao.TitleDAO"%>
<%@page import="org.json.simple.JSONObject"%>
<%@page import="dto.UserDTO"%>
<%@page import="dao.UserDAO"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
String userId = request.getParameter("user");
String roomId = request.getParameter("room");

//System.out.println("user: " + userId);
//System.out.println("room: " + roomId);

RoomDAO roomDao = new RoomDAO();
RoomDTO room = roomDao.readRoomById(roomId);
UserDAO userDao = new UserDAO();
UserDTO user = userDao.readUserById(Integer.parseInt(userId));
TitleDAO titleDao = new TitleDAO();
TitleDTO title = titleDao.readTitleById(user.getTitle());
JSONObject reponseJson = new JSONObject();

boolean isHost = room.getUserId() == user.getId();

if(title != null) {
	reponseJson.put("title", title.getName());
}

reponseJson.put("nickname", user.getNickname());
reponseJson.put("level", user.getLevel());
reponseJson.put("exp", user.getCurrentExp());
reponseJson.put("thumbnail", user.getThumbnailImage());
reponseJson.put("host", isHost);

response.setContentType("application/json");
response.setCharacterEncoding("UTF-8");
response.getWriter().print(reponseJson);
%>
