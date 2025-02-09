<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@page import="dto.UserDTO"%>
<%@page import="java.util.List"%>
<%@page import="dao.UserDAO"%>
<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>제목</title>
<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
<link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
<link rel="stylesheet" href="https://cdn.jsdelivr.net/gh/orioncactus/pretendard@v1.3.9/dist/web/variable/pretendardvariable-dynamic-subset.min.css" />
<script src="https://code.jquery.com/jquery-3.7.1.js"></script>
</head>
<body>
<%
Cookie[] cookies = request.getCookies();
if (cookies != null) {
	for (int i = 0; i < cookies.length; i++) {
		String name = cookies[i].getName();
		if (name.equals("JSESSIONID")) {
			Cookie loginCookie = cookies[i];
			loginCookie.setPath("/");
			loginCookie.setMaxAge(0);
			response.addCookie(loginCookie);
			Cookie cookie = new Cookie("JSESSIONID", "123");
			cookie.setPath("/keyboard-battle");
			cookie.setHttpOnly(true);
			response.addCookie(cookie);
			break;
		}
	}
}
UserDAO userDao = new UserDAO();
	List<UserDTO> userList = userDao.readAllUser();
	// 모든 userList를 출력
	for (UserDTO user : userList) {
		out.println(user.getId() + " " + user.getPassword() + " " + user.getNickname() + "<br>");
	}
%>
</body>
</html>