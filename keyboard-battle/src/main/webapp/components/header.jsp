<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@page import="dto.UserDTO"%>
<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>제목</title>
<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
<link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
<link rel="stylesheet" href="https://cdn.jsdelivr.net/gh/orioncactus/pretendard@v1.3.9/dist/web/variable/pretendardvariable-dynamic-subset.min.css">
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script src="https://code.jquery.com/jquery-3.7.1.js"></script>
<style>
body {
	margin: 0;
}

header {
	position: fixed;
	top: 0;
	width: 100%;
	height: 60px;
	background-color: #fff;
	box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
	z-index: 1000;
	display: flex;
	align-items: center;
	justify-content: space-around;
	padding: 10px 20px;
}

nav ul {
	display: flex;
	list-style: none;
	margin: 0;
	padding: 0;
}

nav ul li {
	margin-left: 20px;
}

nav ul li a {
	text-decoration: none;
	color: #000;
	font-weight: bold;
}

header h1 {
	margin: 0;
}

header h1 a {
	text-decoration: none;
	color: #000;
}
</style>
</head>
<body>
	<header>
		<h1>
			<a href="/keyboard-battle">Keyboard Battle</a>
		</h1>
		<nav>
			<ul>
				<li><a href="/keyboard-battle/free-channel">게임시작</a></li>
				<%
				Object sessionUser = session.getAttribute("user");
				if (sessionUser != null) {
				%>
				<li><a href="/keyboard-battle/login/logout.jsp">로그아웃</a></li>
				<%
				} else {
				%>
				<li><a href="/keyboard-battle/login">로그인</a></li>
				<li><a href="/keyboard-battle/signup">회원가입</a></li>
				<%
				}
				%>
			</ul>
		</nav>
	</header>
</body>
</html>