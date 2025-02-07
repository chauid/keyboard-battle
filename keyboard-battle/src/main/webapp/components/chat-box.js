class ChatBox extends HTMLElement {
    constructor() {
        super();
        this.innerHTML = `
            <style>
                .chat-box {
                    width: 100%;
                    height: 25%;
                    background-color: #e0e0e0;
                    padding: 10px;
                }

                .chat-box-header {
                    width: 100%;
                    height: 20%;
                    background-color: #e9e9e9;
                    margin: 0 0 10px 0;
                    padding: 10px;
                }

                .chat-box-body {
                    width: 100%;
                    height: 50%;
                    background-color: #f0f0f0;
                    margin: 0 0 10px 0;
                    padding: 10px;
                    overflow-y: auto;
                }

                .message-box {
                    width: 100%;
                    height: 20%;
                    margin: 0 0 10px 0;
                    padding: 10px;
                    font-size: smaller;
                }

                .message-box .chat-user {
                    margin: 0 10px;
                    color: #6c44d8;
                }

                .message-box .chat-message {
                    margin: 0 10px;
                }

                .chat-box-message-input {
                    display: flex;
                    width: 100%;
                    height: 50px;
                    margin: 0;
                    padding: 10px;
                }

                .chat-box-message-input input {
                    width: 80%;
                    height: 30px;
                    margin: 0 10px;
                }

                .chat-box-message-input button {
                    display: block;
                    width: 80px;
                    height: 30px;
                }
            </style>
            <div class="chat-box">
                <div class="chat-box-header">
                    <p class="chat-box-title">채팅</p>
                </div>
                <div class="chat-box-body">
                    <div class="message-box">
                        <span class="chat-user">사용자명</span>
                        <span class="chat-message">메시지</span>
                    </div>
                </div>
                <div class="chat-box-message-input">
                    <input type="text" placeholder="메시지 입력">
                    <button>전송</button>
                </div>
            </div>
        `;
    }
}

customElements.define('chat-box', ChatBox);