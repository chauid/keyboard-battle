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
<script src="/keyboard-battle/jquery.twbsPagination.min.js"></script>
<script>
			let userLocation;
			let searchInput = "";
			let searchPage = 1;
			let totalPage = 1;
			let roomId = "";

			function roomOnClickHandle(id, pw) {
				roomId = id;
				if (pw === true) {
					$('#password-modal').modal('show');
				} else {
					window.location.href = '/keyboard-battle/room?id=' + id;
				}
			}

			function fetchTable() {
				$.ajax({
					type: 'GET',
					url: './get-room-list.jsp',
					data: {
						keyword: searchInput,
						page: searchPage
					},
					success: function (response) {
						const responseList = response.roomList;
						const roomList = $('#room-list-items');
						roomList.empty();
						responseList.forEach((res) => {
							const isIngame = res.isIngame ? "게임중" : "대기중";
							const allowSpector = res.roomAllowSpectator === true ? "10" : "2";
							const allowSpectorMsg = res.roomAllowSpectator === true ? "관전가능" : "관전불가";
							const isPassword = res.roomPassword ? "<i class=\"bi bi-key-fill\"></i>" : "";
							roomList.append(`
								<tr onclick="roomOnClickHandle('\${res.roomId}', \${res.roomPassword})">
								<td>\${res.roomName}</td>
								<td>\${res.roomNickname}</td>
								<td style="width: 100px;">\${isIngame}</td>
								<td style="width: 150px;">[\${res.roomClientCount}/\${allowSpector}] \${allowSpectorMsg}</td>
								<td style="width: 50px;">\${isPassword}</td>
								</tr>
								`);
						});
					}
				});
			}

			function fetchTableData() {
				$.ajax({
					type: 'GET',
					url: './get-room-list.jsp',
					data: {
						keyword: searchInput,
						page: searchPage
					},
					success: function (response) {
						totalPage = response.roomCount;
						const responseList = response.roomList;
						let totalPageNum = Math.ceil(totalPage / 10);
						if (totalPageNum === 0) {
							totalPageNum = 1;
						}

						$('#pagination').twbsPagination('destroy');
						$('#pagination').twbsPagination({
							visiblePages: 5,
							prev: "이전",
							next: "다음",
							first: '«',
							last: '»',
							startPage: searchPage,
							totalPages: totalPageNum,
							onPageClick: function (event, page) {
								searchPage = page;
								$('#page-content').text('Page ' + page);
								fetchTable();
							},
						});

						const roomList = $('#room-list-items');
						roomList.empty();
						responseList.forEach((res) => {
							const isIngame = res.isIngame ? "게임중" : "대기중";
							const allowSpector = res.roomAllowSpectator === true ? "10" : "2";
							const allowSpectorMsg = res.roomAllowSpectator === true ? "관전가능" : "관전불가";
							const isPassword = res.roomPassword ? "<i class=\"bi bi-key-fill\"></i>" : "";
							roomList.append(`
								<tr onclick="roomOnClickHandle('\${res.roomId}', \${res.roomPassword})">
								<td>\${res.roomName}</td>
								<td>\${res.roomNickname}</td>
								<td style="width: 100px;">\${isIngame}</td>
								<td style="width: 150px;">[\${res.roomClientCount}/\${allowSpector}] \${allowSpectorMsg}</td>
								<td style="width: 50px;">\${isPassword}</td>
								</tr>
								`);
						});
					}
				});
			}

			function fetchData() {
				$.ajax({
					type: 'GET',
					url: './get-user.jsp',
					success: function (response) {
						response.thumbnail = response.thumbnail ? response.thumbnail : "/keyboard-battle/images/no-image.png";
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

			function fetchClientData() {
				$.ajax({
					type: 'GET',
					url: './client-in-channel.jsp',
					success: function (response) {
						$('#client-count').text("현재 로그인한 유저[" + response.trim() + "명]");
					}
				});
			}

			$(document).ready(function () {
				$('#create-room-form').on('submit', function (event) {
					event.preventDefault();
					$.ajax({
						type: 'POST',
						dataType: 'json',
						url: './create-room.jsp',
						data: $(this).serialize(),
						beforeSend: function () {
							$('#loading').css('display', 'block');
							$('#create-room-submit').prop('disabled', true);
						},
						success: function (response) {
							if (response.result === 'success') {
								window.location.href = '/keyboard-battle/room?id=' + response.roomId;
							} else {
								alert('방 생성에 실패했습니다.');
							}
							$('#loading').css('display', 'none');
							$('#create-room-submit').prop('disabled', false);
						}
					});
				});

				$('#password-form').on('submit', function (event) {
					event.preventDefault();
					$.ajax({
						type: 'POST',
						dataType: 'json',
						url: './room.jsp',
						data: {
							id: roomId,
							password: $('#password-input').val(),
						},
						beforeSend: function () {
							$('#password-loading').css('display', 'block');
							$('#password-form-submit').prop('disabled', true);
						},
						success: function (response) {
							if (response.result === 'success') {
								window.location.href = '/keyboard-battle/room?id=' + roomId;
							} else {
								$('#password-form-error-msg').text('비밀번호가 일치하지 않습니다.');
							}
							$('#password-loading').css('display', 'none');
							$('#password-form-submit').prop('disabled', false);
						}
					});
				});

				$('#search-button').on('click', function () {
					searchInput = $('#search-input').val();
					searchPage = 1;
					fetchTableData();
				});

				$('#user-info').on('click', function () {
					window.location.href = userLocation;
				});

				const ws = new WebSocket('/keyboard-battle/free-chat');
				ws.onopen = function () {
					// console.log('connected');
				};
				ws.onmessage = function (event) {
					const msgType = event.data.split(':')[0];
					if(msgType === 'client') {
						const loginUserCount = event.data.split(':')[1];
						$('#chat-status').text("채팅 [" + loginUserCount.trim() + "명]");
						//console.log(msgType, loginUserCount);
					} else if (msgType === 'chat') {
						const user = event.data.split(':')[1];
						const message = event.data.split(':')[2];
						const chatBody = document.getElementById('chat-box-body');
						chatBody.innerHTML += `<div class="message-box"><span class="chat-user">\${user}</span><span class="chat-message">\${message}</span></div>`;
						const messageBox = document.querySelector('.message-box');
						chatBody.scrollTop = chatBody.scrollHeight;
					} else {
						console.error('unknown message type');
					}
				};
				ws.onclose = function () {
					// console.log('disconnected');
				};
				ws.onerror = function () {
					// console.log('error');
				};

				function sendMessage() {
					const chatInput = $('#chat-input').val();
					const nickname = $('#user-name').text();
					const chatMessage = `\${nickname}:\${chatInput}`;
					// console.log(chatMessage);
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

				$('#search-input').on('keypress', function (e) {
					if (e.key === 'Enter') {
						searchInput = $('#search-input').val();
						searchPage = 1;
						fetchTableData();
					}
				});

				fetchData();
				fetchClientData();
				fetchTableData();
			});

			$(window).on('load', function () {
				setInterval(() => {
					fetchData();
				}, 7000);
				setInterval(() => {
					fetchClientData();
				}, 3000);
				setInterval(() => {
					fetchTableData();
				}, 5000);
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
					<img id="user-image" src="/keyboard-battle/images/no-image.png" alt="">
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
		<div class="main-first">
			<div class="main-menu">
				<div class="main-menu-items">
					<div class="main-menu-search">
						<div class="input-group">
							<input id="search-input" type="text" class="form-control" placeholder="방 제목 검색..." style="width: 300px;" aria-describedby="search-button">
							<button id="search-button" class="btn btn-outline-secondary" type="button">
								<i class="bi bi-search"></i>
							</button>
						</div>
					</div>
					<button type="button" class="btn btn-success" data-bs-toggle="modal" data-bs-target="#create-room">방 만들기</button>
				</div>
			</div>
			<div class="room-list">
				<div>
					<p style="font-weight: bold;">방 목록</p>
					<div>
						<table>
							<thead>
								<tr>
									<th>방 제목</th>
									<th>방장</th>
									<th style="width: 100px;">상태</th>
									<th style="width: 150px;">인원</th>
									<th style="width: 50px;">비밀번호</th>
								</tr>
							</thead>
							<tbody id="room-list-items">
								<!-- <tr>
										<td>누구님의 방</td>
										<td>호스트</td>
										<td style="width: 100px;">대기중</td>
										<td style="width: 150px;">[1명] 관전중</td>
										<td style="width: 50px;"><i class="bi bi-key-fill"></i></td>
									</tr> -->
							</tbody>
						</table>
					</div>
				</div>
				<nav class="nav-pagination">
					<ul id="pagination" class="pagination">
					</ul>
				</nav>
			</div>
		</div>
		<div class="chat-box">
			<p id="chat-status" class="chat-box-header">채팅 [0명]</p>
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
		</div>
	</main>
	<!-- Create Room Modal -->
	<div class="modal fade" id="create-room" data-bs-backdrop="static" tabindex="-1" aria-labelledby="create-room-label" aria-hidden="true">
		<div id="create-room-modal" class="modal-dialog modal-dialog-centered">
			<div class="modal-content">
				<div class="modal-header">
					<h1 class="modal-title fs-5" id="create-room-label">Modal title</h1>
					<button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
				</div>
				<div class="modal-body">
					<form id="create-room-form" method="post">
						<div>
							<label for="room-name">방 제목</label> <input id="room-name" name="room-name" type="text" placeholder="방 제목" required>
						</div>
						<div>
							<label for="room-password">비밀번호 설정</label> <input id="room-password" type="password" name="room-password" placeholder="비밀번호">
						</div>
						<div>
							<label for="room-allow-spectator">관전자 허용</label> <input id="room-allow-spectator" type="checkbox" name="room-allow-spectator">
						</div>
						<div class="modal-footer">
							<button type="button" class="btn btn-secondary" data-bs-dismiss="modal">닫기</button>
							<button id="create-room-submit" type="submit" class="btn btn-primary create-room-submit">
								<span>방 만들기</span> <span id="loading" class="spinner-border spinner-border-sm" role="status" aria-hidden="true" style="display: none;"></span>
							</button>
						</div>
					</form>
				</div>
			</div>
		</div>
	</div>
	<!-- Password Modal -->
	<div class="modal fade" id="password-modal" tabindex="-1" aria-labelledby="password-modal-label" aria-hidden="true">
		<div class="modal-dialog">
			<div class="modal-content">
				<div class="modal-header">
					<h1 class="modal-title fs-5" id="password-modal-label">Modal title</h1>
					<button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
				</div>
				<div class="modal-body">
					<form id="password-form" method="post">
						<div>
							<label for="password-input">비밀번호</label> <input id="password-input" type="password" name="password-input" placeholder="비밀번호" required>
							<p id="password-form-error-msg" style="color: red;"></p>
						</div>
						<div class="modal-footer">
							<button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Close</button>
							<button id="password-form-submit" type="submit" class="btn btn-primary password-submit">
								<span>입력</span> <span id="password-loading" class="spinner-border spinner-border-sm" role="status" aria-hidden="true" style="display: none;"></span>
							</button>
						</div>
					</form>
				</div>
			</div>
		</div>
	</div>
</body>

</html>