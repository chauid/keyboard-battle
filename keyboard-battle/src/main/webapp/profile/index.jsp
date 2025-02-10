<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<%@page import="dao.UserDAO"%>
<%@page import="dto.UserDTO"%>
<html lang="ko">

<%
String userNickname = request.getParameter("user");
out.println(userNickname);
UserDAO userDao = new UserDAO();
UserDTO user = userDao.readUserByNickname(request.getParameter("user"));
%>

<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>프로필</title>
<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
<link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
<link rel="stylesheet" href="https://cdn.jsdelivr.net/gh/orioncactus/pretendard@v1.3.9/dist/web/variable/pretendardvariable-dynamic-subset.min.css" />
<link rel="stylesheet" href="/keyboard-battle/styles/profile.css" />
<script src="https://code.jquery.com/jquery-3.7.1.js"></script>

</head>

<body>
	<jsp:include page="/components/header.jsp" />
	<div class="container">
		<div class="row">
			<div class="col-12">
				<h1><%=user.getNickname()%>
					프로필
				</h1>
			</div>
		</div>
		<div class="row">
			<div class="col-12">
				<figure class="user-img">
					<img src="/keyboard-battle/images/no-image.png" alt="">
				</figure>
			</div>
		</div>
		<div class="row">
			<div class="col-12">
				<p>
					이메일: <span id="email"> <%=user.getEmail()%></span>
				</p>
			</div>
		</div>
		<div class="row">
			<div class="col-12">
				<p>
					닉네임: <span id="nickname"> <%=user.getNickname()%></span>
				</p>
			</div>
		</div>
		<div class="row">
			<div class="col-12">
				<p>
					레벨: <span id="level"> <%=user.getLevel()%></span>
				</p>
			</div>
		</div>
		<div class="row">
			<div class="col-12">
				<p>
					경험치: <span id="exp"><%=user.getCurrentExp()%></span>
				</p>
			</div>
		</div>
		<div class="row">
			<div class="col-12">
				<p>
					최고점수: <span id="highscore"> <%=user.getHighScore()%></span>
				</p>
			</div>
		</div>
		<div class="row">
			<div class="col-12">
				<p>
					플레이 횟수: <span id="playcount"> <%=user.getPlayCount()%></span>
				</p>
			</div>
		</div>
		<div class="row">
			<div class="col-12">
				<p>
					소개글: <span id="description"> <%=user.getDescription()%></span>
				</p>
			</div>
		</div>
	</div>
</body>
</html>