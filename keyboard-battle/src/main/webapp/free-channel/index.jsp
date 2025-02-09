<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>자유 채널</title>
<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
<link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
<link rel="stylesheet" href="https://cdn.jsdelivr.net/gh/orioncactus/pretendard@v1.3.9/dist/web/variable/pretendardvariable-dynamic-subset.min.css" />
<link rel="stylesheet" href="/keyboard-battle/styles/free-channel.css">
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script src="https://code.jquery.com/jquery-3.7.1.js"></script>
<script>
	let userLocation;
	function fetchData() {
		$.ajax({
			type: 'GET',
			url: './client-in-channel.jsp',
			success: function (response) {
				$('#client-count').text("현재 로그인한 유저[" + response.trim() + "명]");
			}
		});
		$.ajax({
			type: 'GET',
			url: './get-user.jsp',
			success: function (response) {
				$('#user-name').text(response.nickname);
				$('#user-level').text("레벨 " + response.level);
				$('#user-exp').text(response.exp);
				$('#user-max-exp').text(response.level * 10);
				$('#user-image').attr('src', response.thumbnail);
				userLocation = "/keyboard-battle/profile?user=" + response.nickname;
			}
		});
		$.ajax({
			type: 'GET',
			url: './chat-count.jsp',
			success: function (response) {
				$('#chat-status').text("채팅 [" + response.trim() + "명]");
			}
		});
 		$.ajax({
			type: 'GET',
			url: './get-user-list.jsp',
			success: function (response) {
				const userList = $('#user-list-items');
				userList.empty();
				response.forEach((user) => {
					userList.append(`
				<div id="user-\${user.nickname}" class="user-list-item" onclick="window.location.href = '/keyboard-battle/profile/?user=\${user.nickname}'">
					<span class="user-list-level">\${user.level}</span>
					<span class="user-list-name">\${user.nickname}</span>
				</div>
					`);
				});
			}
		});
	}

	$(document).ready(function () {
		$('#create-room-form').on('submit', function (event) {
			event.preventDefault();
			$.ajax({
				type: 'POST',
				url: './create-room.jsp',
				data: $(this).serialize(),
				success: function (response) {
					console.log(response);
				}
			});
		});

		$('#user-info').on('click', function () {
			window.location.href = userLocation;
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
			const nickname = $('#user-name').text();
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

		fetchData();
	});
	
	$(window).on('load', function () {
		console.log('object');
		setInterval(() => {
			fetchData();
		}, 3000);
	});
</script>
</head>
<body>
	<jsp:include page="/components/header.jsp" />
	<aside>
		<div class="channel-info">
			<p style="font-size: smaller;">자유 채널</p>
			<p id="client-count" style="font-size: small;">현재 로그인한 유저[0명]</p>
		</div>
		<div id="user-info" class="user-info">
			<p>내 정보</p>
			<div>
				<figure class="user-img">
					<img id="user-image" src="https://i.namu.wiki/i/zkey2H6ka0WwWS7C210XdqA5pQ_4oaPrYIfyLZFbvwhVaVgi56czAsx1yrG0Jt2WhKKh6hrOZ4JEVE2stG7cxg.webp"
						alt="">
				</figure>
				<p id="user-name" class="user-name"></p>
				<p id="user-level" class="user-level">레벨</p>
				<span class="user-exp">exp</span>
				<div style="display: inline;">
					<span id="user-exp"></span> <span> / </span> <span id="user-max-exp"> </span>
				</div>
			</div>
		</div>
		<div class="user-list">
			<p>유저 목록</p>
			<div id="user-list-items" class="user-list-items">
				<!-- <div class="user-list-item">
					<span class="user-list-level">1</span>
					<span class="user-list-name">유저 이름</span>
				</div> -->
			</div>
		</div>
	</aside>
	<main>
		<div class="main-menu">
			<div>
				<input class="search-input" type="text" placeholder="방 검색">
				<button type="button"><i class="bi bi-search"></i></button>
			</div>
			<button type="button" class="btn btn-success" data-bs-toggle="modal" data-bs-target="#create-room">방 만들기</button>
		</div>
		<div class="room-list">
			<div>
				<p style="font-weight: bold;">방 목록</p>
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
				<p id="chat-status" class="chat-box-title" style="font-size: small;">채팅 [0명]</p>
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
	<div class="modal fade" id="create-room" data-bs-backdrop="static" tabindex="-1" aria-labelledby="create-room-label" aria-hidden="true">
		<div class="modal-dialog modal-dialog-centered">
			<div class="modal-content">
				<div class="modal-header">
					<h1 class="modal-title fs-5" id="create-room-label">Modal title</h1>
					<button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
				</div>
				<div class="modal-body">
					<form id="create-room-form" action="./create-room.jsp" method="post">
						<div>
							<label for="room-name">방 제목</label>
							<input id="room-name" name="room-name" type="text" placeholder="방 제목" required>
						</div>
						<div>
							<label for="room-password">비밀번호 설정</label>
							<input id="room-password" type="password" name="room-password" placeholder="비밀번호">
						</div>
							<label for="room-allow-spectator">관전자 허용</label>
							<input id="room-allow-spectator" type="checkbox" name="room-allow-spectator">
					</form>
				</div>
				<div class="modal-footer">
					<button type="button" class="btn btn-secondary" data-bs-dismiss="modal">닫기</button>
					<button type="button" class="btn btn-primary">방 만들기</button>
				</div>
			</div>
		</div>
	</div>
	<button type="button" class="btn btn-primary" data-bs-toggle="modal" data-bs-target="#create-room">Launch demo modal</button>
</body>

</html>