<%@page import="java.nio.charset.StandardCharsets"%>
<%@page import="java.security.MessageDigest"%>
<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@page import="org.json.simple.JSONObject"%>
<%@page import="dto.UserDTO"%>
<jsp:useBean id="userDao" class="dao.UserDAO" />
<jsp:useBean id="userDto" class="dto.UserDTO" />
<jsp:useBean id="sessionDao" class="dao.UserSessionDAO" />
<jsp:setProperty property="*" name="userDto" />
<%
UserDTO user = userDao.readUserByEmail(userDto.getEmail());
String SALT = "!#%&(keyboard-battle#!@$%(&%#!";
String plainPassword = userDto.getPassword();
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

if (user != null && HashedPassword.equals(user.getPassword())) {
	session.setAttribute("user", user.getId());
	Cookie cookie = new Cookie("nickname", user.getNickname());
	cookie.setPath("/");
	cookie.setMaxAge(60 * 60 * 24);
	response.addCookie(cookie);

	Integer userId = user.getId();
	cookie = new Cookie("userid", userId.toString());
	cookie.setPath("/");
	cookie.setMaxAge(60 * 60 * 24);
	response.addCookie(cookie);

	JSONObject reponseJson = new JSONObject();
	reponseJson.put("result", "success");

	sessionDao.createUserSession(userId, session.getId());
	response.setStatus(200);
	response.getWriter().write(reponseJson.toString());

} else {
	JSONObject reponseJson = new JSONObject();
	reponseJson.put("result", "failed");
	response.setStatus(200);
	response.getWriter().write(reponseJson.toString());
}
%>