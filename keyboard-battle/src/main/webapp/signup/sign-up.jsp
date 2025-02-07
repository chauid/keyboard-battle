<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@page import="org.json.simple.JSONObject"%>
<%@page import="dto.UserDTO"%>
<jsp:useBean id="dao" class="dao.UserDAO" />
<jsp:useBean id="dto" class="dto.UserDTO" />
<jsp:setProperty property="*" name="dto" />
<%
UserDTO user = new UserDTO();
user.setEmail(dto.getEmail());
JSONObject reponseJson = new JSONObject();
if (dao.readUserByEmail(dto.getEmail()) != null) {
	reponseJson.put("result", "failed");
	response.setStatus(200);
	response.getWriter().write(reponseJson.toString());
} else {
	user.setPassword(dto.getPassword());
	user.setNickname(dto.getNickname());
	dao.createUser(user);
	reponseJson.put("result", "success");
	response.setStatus(200);
	response.getWriter().write(reponseJson.toString());
}
%>
