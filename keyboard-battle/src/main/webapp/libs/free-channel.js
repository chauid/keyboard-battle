const url = '/keyboard-battle/free-chat';
const socket = new WebSocket(url);

if (document.readyState === 'complete') {
    const bb = document.getElementById('chat-send');
    bb.onclick = function () {
        console.log('Send button clicked');
    };
    bb.addEventListener('click', function () {
        console.log('Send button clicked');
    });
}

function sendMessage() {
    const chatInput = document.getElementById('chat-input');
    const chatSendButton = document.getElementById('chat-send');
    const chatBody = document.getElementById('chat-box-body');

    socket.onopen = function (event) {
        console.log('WebSocket is open now.');
        chatBody.innerHTML += '<p>WebSocket connection established.</p>';
    };

    socket.onmessage = function (event) {
        console.log('WebSocket message received:', event.data);
        chatBody.innerHTML += `<div class="message-box">
        <span class="chat-user">사용자명</span>
        <span class="chat-message">메시지</span>
        </div>`;
    };

    socket.onclose = function (event) {
        console.log('WebSocket is closed now.');
        chatBody.innerHTML += '<p>WebSocket connection closed.</p>';
    };

    socket.onerror = function (error) {
        console.error('WebSocket error observed:', error);
        chatBody.innerHTML += `<p>Error: ${error.message}</p>`;
    };

    chatSendButton.addEventListener('click', () => {
        const message = chatInput.value;
        if (message) {
            connectWebSocket(url, element).send(message);
            chatInput.value = '';
        }
    });

    chatInput.addEventListener('keydown', (event) => {
        if (event.key === 'Enter') {
            chatSendButton.click();
        }
    });

}