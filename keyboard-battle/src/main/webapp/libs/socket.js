export function connectWebSocket(url, element) {
    const socket = new WebSocket(url);

    socket.onopen = function(event) {
        console.log('WebSocket is open now.');
        element.innerHTML += '<p>WebSocket connection established.</p>';
    };

    socket.onmessage = function(event) {
        console.log('WebSocket message received:', event.data);
        element.innerHTML += `<p>Message: ${event.data}</p>`;
    };

    socket.onclose = function(event) {
        console.log('WebSocket is closed now.');
        element.innerHTML += '<p>WebSocket connection closed.</p>';
    };

    socket.onerror = function(error) {
        console.error('WebSocket error observed:', error);
        element.innerHTML += `<p>Error: ${error.message}</p>`;
    };
}