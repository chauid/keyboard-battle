<%@page import="java.nio.charset.StandardCharsets"%>
<%@page import="java.security.MessageDigest"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@page import="org.json.simple.JSONObject"%>
<%@page import="dto.UserDTO"%>
<jsp:useBean id="dao" class="dao.UserDAO" />
<jsp:useBean id="dto" class="dto.UserDTO" />
<jsp:setProperty property="*" name="dto" />
<%
UserDTO user = new UserDTO();
JSONObject reponseJson = new JSONObject();
if (dao.readUserByEmail(dto.getEmail()) != null) {
	reponseJson.put("result", "failed");
	response.setStatus(200);
	response.getWriter().write(reponseJson.toString());
} else {
	String SALT = "!#%&(keyboard-battle#!@$%(&%#!";
	String plainPassword = dto.getPassword();
	plainPassword += SALT;
	MessageDigest digest = MessageDigest.getInstance("SHA-512");
	byte[] encodedHash = digest.digest(plainPassword.getBytes(StandardCharsets.UTF_8));
	StringBuilder hexString = new StringBuilder();
	for (byte b : encodedHash) {
	    String hex = Integer.toHexString(0xff & b);
	    if (hex.length() == 1) {
	        hexString.append('0');
	    }
	    hexString.append(hex);
	}
	String HashedPassword = hexString.toString();
	
	user.setEmail(dto.getEmail());
	user.setPassword(HashedPassword);
	user.setNickname(dto.getNickname());
	dao.createUser(user);
	reponseJson.put("result", "success");
	response.setStatus(200);
	response.getWriter().write(reponseJson.toString());
}
%>
