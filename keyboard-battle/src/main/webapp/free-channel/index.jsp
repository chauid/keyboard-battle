<%@page import="socket.FreeChat" %>
	<%@page import="dao.UserDAO" %>
		<%@page import="dto.UserDTO" %>
			<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
				<!DOCTYPE html>
				<html lang="ko">

				<% Object userSession=session.getAttribute("user"); Integer userId; if (userSession==null) {
					response.sendRedirect("/keyboard-battle/login"); userId=0; } else {
					userId=Integer.parseInt(userSession.toString()); } UserDAO userDao=new UserDAO(); UserDTO
					user=userDao.readUserById(userId); %>

					<head>
						<meta charset="UTF-8">
						<meta name="viewport" content="width=device-width, initial-scale=1.0">
						<title>제목</title>
						<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css"
							rel="stylesheet">
						<link rel="stylesheet"
							href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
						<link rel="stylesheet"
							href="https://cdn.jsdelivr.net/gh/orioncactus/pretendard@v1.3.9/dist/web/variable/pretendardvariable-dynamic-subset.min.css" />
						<link rel="stylesheet" href="/styles/free-channel.css">
						<link rel="stylesheet" href="/keyboard-battle/styles/free-channel.css">
						<script
							src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
						<script src="https://code.jquery.com/jquery-3.7.1.js"></script>
						<script>
							$(document).ready(function () {
								$('#user-info').on('click', function () {
									window.location.href = "/keyboard-battle/profile?user=" + "<%=user.getNickname()%>";
								});

								const ws = new WebSocket('/keyboard-battle/free-chat');
								ws.onopen = function () {
									// console.log('connected');
								};
								ws.onmessage = function (event) {
									const user = event.data.split(':')[0];
									const message = event.data.split(':')[1];
									const chatBody = document.getElementById('chat-box-body');
									chatBody.innerHTML += `<div class="message-box"><span class="chat-user">\${user}</span><span class="chat-message">\${message}</span></div>`;
									const messageBox = document.querySelector('.message-box');
									chatBody.scrollTop = chatBody.scrollHeight;
								};
								ws.onclose = function () {
									console.log('disconnected');
								};
								ws.onerror = function () {
									console.log('error');
								};

								function sendMessage() {
									const chatInput = $('#chat-input').val();
									const nickname = document.cookie.split('=')[1];
									const chatMessage = `\${nickname}:\${chatInput}`;
									console.log(chatMessage);
									const chatBody = document.getElementById('chat-box-body');
									chatBody.innerHTML += `<div class="message-box"><span class="chat-user">\${nickname}</span><span class="chat-message">\${chatInput}</span></div>`;
									const messageBox = document.querySelector('.message-box');
									chatBody.scrollTop = chatBody.scrollHeight;
									$('#chat-input').val('');
									ws.send(chatMessage);
								}

								$('#chat-send').on('click', sendMessage);

								$('#chat-input').on('keypress', function (e) {
									if (e.key === 'Enter') {
										sendMessage();
									}
								});

								setInterval(() => {
									$.ajax({
										type: 'GET',
										url: './client-count.jsp',
										success: function (response) {
											console.log(response);
											$('#client-count').text(response);
										}
									});
								}, 1000);
							});
						</script>
					</head>

					<body>
						<jsp:include page="/components/header.jsp" />
						<aside>
							<div class="channel-info">
								<p style="font-size: smaller;">자유 채널</p>
								<p style="font-size: small;">
									현재 접속자[<%=FreeChat.getNumberOfClients()%>명]
								</p>
							</div>
							<div id="user-info" class="user-info">
								<p>내 정보</p>
								<div>
									<figure class="user-img">
										<img src="https://i.namu.wiki/i/zkey2H6ka0WwWS7C210XdqA5pQ_4oaPrYIfyLZFbvwhVaVgi56czAsx1yrG0Jt2WhKKh6hrOZ4JEVE2stG7cxg.webp"
											alt="">
									</figure>
									<p class="user-name">
										<%=user.getNickname()%>
									</p>
									<p class="user-level">
										레벨
										<%=user.getLevel()%>
									</p>
									<span class="user-exp">exp</span>
									<div style="display: inline;">
										<span>
											<%=user.getCurrentExp()%>
										</span> <span> / </span> <span>
											<%=user.getLevel() * 10%>
										</span>
									</div>
								</div>
							</div>
							<div class="user-list">
								<p>유저 목록</p>
								<div class="user-list-item"></div>
							</div>
						</aside>
						<main>
							<div class="main-menu">
								<ul class="nav nav-tabs main-menu">
									<li class="nav-item">
										<button class="nav-link" style="background-color: #7ad875;">빠른 시작</button>
									</li>
									<li class="nav-item">
										<button class="nav-link" style="background-color: #81cdf8;">방 검색</button>
									</li>
									<li class="nav-item">
										<button class="nav-link" style="background-color: #97c3dd;">방 만들기</button>
									</li>
								</ul>
							</div>
							<div class="room-list">
								<div>
									<p>방 목록</p>
									<div>
										<table>
											<tbody>
												<tr>
													<td>누구님의 방</td>
													<td>대기중</td>
													<td style="width: 100px;">[1명] 관전중</td>
													<td style="width: 50px;"><i class="bi bi-key-fill"></i></td>
												</tr>
											</tbody>
										</table>
									</div>
								</div>
								<div class="room-nav-buttons">
									<button class="prev-button">이전</button>
									<button class="next-button">다음</button>
								</div>
							</div>
							<div class="chat-box">
								<div class="chat-box-header">
									<p class="chat-box-title" style="font-size: small;">채팅</p>
								</div>
								<div id="chat-box-body" class="chat-box-body">
									<!-- <div class="message-box">
                    <span class="chat-user">유저 이름</span>
                    <span class="chat-message">메시지</span>
                </div> -->
								</div>
								<div class="chat-box-message-input">
									<input id="chat-input" type="text" placeholder="메시지 입력">
									<button id="chat-send">전송</button>
								</div>
						</main>



						<!-- Modal -->
						<div class="modal fade" id="exampleModal" tabindex="-1" aria-labelledby="exampleModalLabel"
							aria-hidden="true">
							<div class="modal-dialog">
								<div class="modal-content">
									<div class="modal-header">
										<h5 class="modal-title" id="exampleModalLabel">Modal title</h5>
										<button type="button" class="btn-close" data-bs-dismiss="modal"
											aria-label="Close"></button>
									</div>
									<div class="modal-body">Modal body text goes here.</div>
									<div class="modal-footer">
										<button type="button" class="btn btn-secondary"
											data-bs-dismiss="modal">Close</button>
										<button type="button" class="btn btn-primary">Save changes</button>
									</div>
								</div>
							</div>
						</div>

						<!-- Button trigger modal -->
						<button type="button" class="btn btn-primary" data-bs-toggle="modal"
							data-bs-target="#exampleModal">Launch demo modal</button>
					</body>

				</html>