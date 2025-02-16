<%@page import="dao.UserRoomDAO"%>
<%@page import="socket.RoomChat"%>
<%@page import="dto.UserDTO"%>
<%@page import="dao.UserDAO"%>
<%@page import="org.json.simple.JSONObject"%>
<%@page import="org.json.simple.JSONArray"%>
<%@page import="dto.RoomDTO"%>
<%@page import="java.util.List"%>
<%@page import="dao.RoomDAO"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
Object userSession = session.getAttribute("user");

String keyword = request.getParameter("keyword");
String pageString = request.getParameter("page");

RoomDAO roomDao = new RoomDAO();
UserDAO userDao = new UserDAO();
UserRoomDAO userRoomDao = new UserRoomDAO();

JSONArray roomList = new JSONArray();

if (keyword == null) {
	keyword = "";
}
if (pageString == null || pageString.equals("")) {
	pageString = "1";
}
int totalCount = roomDao.readRoomCountBySearch(keyword);
List<RoomDTO> rooms = roomDao.readRoomBySearch(keyword, Integer.parseInt(pageString));

JSONObject jsonResponse = new JSONObject();
jsonResponse.put("roomCount", totalCount);

for (RoomDTO room : rooms) {
	int clientInRoom = userRoomDao.readUserRoomCountByRoomId(room.getId());
	UserDTO user = userDao.readUserById(room.getUserId());
	boolean isPassword = true;
	if (room.getPassword().equals("")) {
		isPassword = false;
	}
	JSONObject json = new JSONObject();
	json.put("roomId", room.getId());
	json.put("roomNickname", user.getNickname());
	json.put("roomName", room.getName());
	json.put("roomPassword", isPassword);
	json.put("roomAllowSpectator", room.isAllowSpectator());
	json.put("roomIsInGame", room.isIngame());
	json.put("roomClientCount", clientInRoom);
	roomList.add(json);
}
jsonResponse.put("roomList", roomList);

response.setContentType("application/json");
response.setCharacterEncoding("UTF-8");
response.setStatus(200);
response.getWriter().print(jsonResponse);
%>