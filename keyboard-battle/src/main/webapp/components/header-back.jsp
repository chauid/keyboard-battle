<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@page import="dto.UserDTO"%>
<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<style>
body {
	margin: 0;
}

header.root-header {
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

header.root-header nav ul {
	display: flex;
	list-style: none;
	margin: 0;
	padding: 0;
}

header.root-header nav ul li {
	margin-left: 20px;
}

header.root-header nav ul li a {
	text-decoration: none;
	color: #000;
	font-weight: bold;
}

header.root-header h1 {
	margin: 0;
}

header.root-header h1 a {
	text-decoration: none;
	color: #000;
}
</style>
</head>
<body>
	<header class="root-header">
		<h1>
			<a href="/">Keyboard Battle</a>
		</h1>
		<nav>
			<ul>
				<li><a href="/free-channel">게임시작</a></li>
				<%
				Object sessionUser = session.getAttribute("user");
				if (sessionUser != null) {
				%>
				<li><a href="/login/logout.jsp">로그아웃</a></li>
				<%
				} else {
				%>
				<li><a href="/login">로그인</a></li>
				<li><a href="/signup">회원가입</a></li>
				<%
				}
				%>
			</ul>
		</nav>
	</header>
</body>
</html>