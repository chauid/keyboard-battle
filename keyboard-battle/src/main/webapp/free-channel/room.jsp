<%@page import="org.json.simple.JSONObject"%>
<%@page import="dto.RoomDTO"%>
<%@page import="dao.RoomDAO"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
String roomId = request.getParameter("id");
String inputPassword = request.getParameter("password");

RoomDAO roomDao = new RoomDAO();
RoomDTO room = roomDao.readRoomById(roomId);
JSONObject reponseJson = new JSONObject();

if (room.getPassword().equals(inputPassword)) {
	session.setAttribute("roompw", room.getPassword());
	reponseJson.put("result", "success");
} else {
	reponseJson.put("result", "failed");
}

response.setContentType("application/json");
response.setCharacterEncoding("UTF-8");
response.getWriter().print(reponseJson);
%>