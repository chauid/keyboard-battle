<%@page import="dao.UserRoomDAO"%>
<%@page import="org.json.simple.JSONObject"%>
<%@page import="dto.RoomDTO"%>
<%@page import="dao.RoomDAO"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
String roomId = request.getParameter("id");
String inputPassword = request.getParameter("password");

RoomDAO roomDao = new RoomDAO();
RoomDTO room = roomDao.readRoomById(roomId);
UserRoomDAO userRoomDao = new UserRoomDAO();
JSONObject responseJson = new JSONObject();

if(room == null) {
	responseJson.put("result", "failed");
    responseJson.put("exist", "false");
    response.setContentType("application/json");
    response.setCharacterEncoding("UTF-8");
    response.getWriter().print(responseJson);
    return;
} else {
	responseJson.put("exist", "true");
}

int maxUsersInRoom = room.isAllowSpectator() ? 10 : 2;
int currentUsersInRoom = userRoomDao.readUserRoomCountByRoomId(roomId) + 1; // 인원이 들어왔다고 가정: + 1

if (room.getPassword() == null || room.getPassword().equals("")) {
	if (currentUsersInRoom > maxUsersInRoom) {
		responseJson.put("result", "failed");
		responseJson.put("room", "full");
	} else {
		responseJson.put("result", "success");
	}
} else {
	if (room.getPassword().equals(inputPassword)) {
		if (currentUsersInRoom > maxUsersInRoom) {
			responseJson.put("password", "true");
			responseJson.put("result", "failed");
			responseJson.put("room", "full");
		} else {
			session.setAttribute("roompw", room.getPassword());
			responseJson.put("password", "true");
			responseJson.put("result", "success");
		}
	} else {		
		responseJson.put("password", "false");
		responseJson.put("result", "failed");
	}
}

response.setContentType("application/json");
response.setCharacterEncoding("UTF-8");
response.getWriter().print(responseJson);
%>