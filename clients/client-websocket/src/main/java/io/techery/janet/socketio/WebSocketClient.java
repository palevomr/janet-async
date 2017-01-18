package io.techery.janet.socketio;

import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;
import com.neovisionaries.ws.client.WebSocketFrame;
import com.neovisionaries.ws.client.WebSocketState;

import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLContext;

import io.techery.janet.AsyncClient;

public abstract class WebSocketClient extends AsyncClient {

    private WebSocket webSocket;
    private SSLContext sslContext;

    public WebSocketClient(SSLContext sslContext) {
        this.sslContext = sslContext;
    }

    @Override protected boolean isConnected() {
        return webSocket != null && webSocket.getState() == WebSocketState.CREATED;
    }

    @Override protected void connect(String url, boolean reconnectIfConnected) throws Throwable {
        if (isConnected()) {
            if (reconnectIfConnected) {
                webSocket.addListener(new WebSocketAdapter() {
                    @Override
                    public void onDisconnected(WebSocket websocket, WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame, boolean closedByServer) throws Exception {
                        callback.onDisconnect(serverCloseFrame.getCloseReason());
                    }
                });
                webSocket.disconnect();
            } else {
                callback.onConnect();
            }
            return;
        }
        WebSocketFactory factory = new WebSocketFactory();
        factory.setSSLContext(sslContext);
        webSocket = factory.createSocket(url);
        webSocket.addListener(new WebSocketAdapter() {
            @Override public void onConnected(WebSocket websocket, Map<String, List<String>> headers) throws Exception {
                callback.onConnect();
            }

            @Override public void onConnectError(WebSocket websocket, WebSocketException exception) throws Exception {
                callback.onConnectionError(exception);
            }

            @Override public void onError(WebSocket websocket, WebSocketException cause) throws Exception {
                callback.onError(cause);
            }

            @Override
            public void onDisconnected(WebSocket websocket, WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame, boolean closedByServer) throws Exception {
                callback.onDisconnect(serverCloseFrame.getCloseReason());
            }

            @Override public void onBinaryMessage(WebSocket websocket, byte[] binary) throws Exception {
                callback.onMessage(getEventFromMessage(websocket, binary), binary);
            }
        });
        webSocket.connectAsynchronously();
    }

    protected abstract String getEventFromMessage(WebSocket websocket, byte[] binary);

    @Override protected void disconnect() throws Throwable {
        webSocket.disconnect();
    }

    @Override protected void send(String event, String payload) throws Throwable {
        if (!isConnected()) return;
        webSocket.sendText(payload);
    }

    @Override protected void send(String event, byte[] payload) throws Throwable {
        if (!isConnected()) return;
        webSocket.sendBinary(payload);
    }

    @Override protected void subscribe(String event) {

    }
}
