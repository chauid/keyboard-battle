<%@page import="org.json.simple.JSONObject"%>
<%@page import="dto.RoomDTO"%>
<%@page import="dao.RoomDAO"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
RoomDAO roomDao = new RoomDAO();
RoomDTO room = new RoomDTO();
room.setName(request.getParameter("room-name"));
room.setPassword(request.getParameter("room-password"));
Object user = session.getAttribute("user");

if(room.getPassword() != null) {
	session.setAttribute("roompw", room.getPassword());
}

if (user != null) {
	room.setUserId(Integer.parseInt(user.toString()));
}

String allowSpectator = request.getParameter("room-allow-spectator");
boolean isAllowSpectator = false;
if (allowSpectator == null) {
	isAllowSpectator = false;
} else {
	isAllowSpectator = true;
}
room.setAllowSpectator(isAllowSpectator);

roomDao.createRoom(room);

JSONObject reponseJson = new JSONObject();
reponseJson.put("result", "success");
reponseJson.put("roomId", room.getId());
response.setContentType("application/json");
response.setStatus(200);
response.getWriter().print(reponseJson.toString());
%>