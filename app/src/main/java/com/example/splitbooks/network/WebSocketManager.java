    package com.example.splitbooks.network;

    import android.content.Context;
    import android.util.Log;

    import com.example.splitbooks.DTO.request.SendMessageRequest;
    import com.example.splitbooks.DTO.response.SendMessageResponse;
    import com.google.gson.Gson;

    import java.util.HashMap;

    import java.util.Map;
    import java.util.concurrent.TimeUnit;

    import okhttp3.OkHttpClient;
    import ua.naiksoftware.stomp.Stomp;
    import ua.naiksoftware.stomp.StompClient;

    public class WebSocketManager {

        private static final String TAG = "WebSocketManager";


        private static final String WS_URL = "ws://172.20.10.3:8888/ws/websocket";
        private static final String SEND_DESTINATION = "/app/chat.sendMessage";
        private static final String SUBSCRIBE_PREFIX = "/topic/chat.";

        private StompClient stompClient;
        private Gson gson = new Gson();

        public interface MessageListener {
            void onMessageReceived(SendMessageResponse message);
        }

        public void connect(Context context, Long chatId, MessageListener listener) {
            if (stompClient != null && stompClient.isConnected()) {
                Log.d(TAG, "Already connected");
                return;
            }

            String token = JwtManager.getToken(context);
            if (token == null) {
                Log.e(TAG, "JWT Token not found");
                return;
            }

            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .readTimeout(0, TimeUnit.MILLISECONDS)
                    .build();

            Map<String, String> connectHeaders = new HashMap<>();
            connectHeaders.put("Authorization", "Bearer " + token);

            stompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, WS_URL, connectHeaders, okHttpClient);

            stompClient.lifecycle().subscribe(lifecycleEvent -> {
                switch (lifecycleEvent.getType()) {
                    case OPENED:
                        Log.d(TAG, "WebSocket connection opened");
                        subscribeToChat(chatId, listener);
                        break;
                    case ERROR:
                        Log.e(TAG, "WebSocket connection error", lifecycleEvent.getException());
                        break;
                    case CLOSED:
                        Log.d(TAG, "WebSocket connection closed");
                        break;
                }
            });

            stompClient.connect();
        }

        private void subscribeToChat(Long chatId, MessageListener listener) {
            String topic = SUBSCRIBE_PREFIX + chatId;
            stompClient.topic(topic).subscribe(topicMessage -> {
                SendMessageResponse response = gson.fromJson(topicMessage.getPayload(), SendMessageResponse.class);
                listener.onMessageReceived(response);
            });
        }

        public void send(SendMessageRequest request) {
            if (stompClient == null || !stompClient.isConnected()) {
                Log.w(TAG, "Cannot send message. STOMP client is not connected.");
                return;
            }

            String payload = gson.toJson(request);
            stompClient.send(SEND_DESTINATION, payload)
                    .subscribe(() -> Log.d(TAG, "Message sent"),
                            throwable -> Log.e(TAG, "Send error", throwable));
        }

        public void disconnect() {
            if (stompClient != null && stompClient.isConnected()) {
                stompClient.disconnect();
            }
        }
    }
