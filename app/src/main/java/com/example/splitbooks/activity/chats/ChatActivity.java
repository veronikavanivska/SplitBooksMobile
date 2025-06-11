package com.example.splitbooks.activity.chats;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.transition.Explode;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.splitbooks.DTO.request.SendMessageRequest;
import com.example.splitbooks.DTO.response.SendMessageResponse;
import com.example.splitbooks.DTO.response.PageResponse;
import com.example.splitbooks.R;
import com.example.splitbooks.activity.chats.MessageAdapter;
import com.example.splitbooks.network.ApiClient;

import com.example.splitbooks.network.ApiService;
import com.example.splitbooks.network.JwtManager;
import com.example.splitbooks.network.WebSocketManager;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView recyclerViewMessages;
    private EditText editTextMessage;
    private ImageButton buttonSend,buttonBack;


    private MessageAdapter messageAdapter;
    private List<SendMessageResponse> messageList = new ArrayList<>();

    private WebSocketManager webSocketManager;

    private Long chatId;
    private Long currentUserId;
    TextView chatName;
    ImageView profileImage;
    private int currentPage = 0;
    private final int pageSize = 20;
    private boolean isLoading = false;
    private String chatNameText,chatPhoto;
    LinearLayoutManager layoutManager;
    private TextView buttonChatInfo, buttonDelete;
    private boolean isGroupChat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_chat);

        recyclerViewMessages = findViewById(R.id.recyclerViewMessages);
        editTextMessage = findViewById(R.id.editTextMessage);
        buttonSend = findViewById(R.id.buttonSend);
        buttonBack = findViewById(R.id.buttonBack);
        buttonDelete = findViewById(R.id.buttonChatDelete);
        chatName = findViewById(R.id.textViewChatName);
        profileImage = findViewById(R.id.imageViewProfile);

        layoutManager = new LinearLayoutManager(this);
        chatId = getIntent().getLongExtra("chatId", -1);

        chatNameText  = getIntent().getStringExtra("chatName");
        chatPhoto = getIntent().getStringExtra("chatPhoto");
        isGroupChat = getIntent().getBooleanExtra("isGroup", false);

        chatName.setText(chatNameText);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Explode explode = new Explode();
            explode.setDuration(400);
            getWindow().setEnterTransition(explode);
        }


        if (chatPhoto != null && !chatPhoto.isEmpty()) {
            Glide.with(this)
                    .load(chatPhoto)
                    .circleCrop()
                    .into(profileImage);
        }

        buttonChatInfo = findViewById(R.id.buttonChatInfo);

        if (isGroupChat) {
            buttonChatInfo.setVisibility(View.VISIBLE);
        } else {
            buttonChatInfo.setVisibility(View.GONE);
        }


        buttonDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Delete Chat")
                    .setMessage("Are you sure you want to delete this chat?")
                    .setPositiveButton("Delete", (dialog, which) -> deleteChat())
                    .setNegativeButton("Cancel", null)
                    .show()
                    .getWindow().setBackgroundDrawableResource(android.R.color.white);
        });

        currentUserId = JwtManager.getMyProfileId(getApplicationContext());

        if (chatId == -1 || currentUserId == -1) {
            Toast.makeText(this, "Invalid chat or user ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        messageAdapter = new MessageAdapter(messageList, currentUserId);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        recyclerViewMessages.setLayoutManager(layoutManager);
        recyclerViewMessages.setAdapter(messageAdapter);
        recyclerViewMessages.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (!recyclerView.canScrollVertically(1) && !isLoading) {
                    loadChatHistory();
                }
            }
        });

        loadChatHistory();

        webSocketManager = new WebSocketManager();
        webSocketManager.connect(getApplicationContext(), chatId, messageResponse -> runOnUiThread(() -> {
            messageList.add(0, messageResponse);
            messageAdapter.notifyItemInserted(0);
            recyclerViewMessages.scrollToPosition(0);
        }));

        buttonChatInfo.setOnClickListener(v -> {
            Intent intent = new Intent(this, ChatInfoActivity.class);
            intent.putExtra("chatId",chatId);
            startActivity(intent);
        });

        buttonSend.setOnClickListener(v -> sendMessage());

        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });


    }
    private void deleteChat(){
        ApiService api = ApiClient.getApiService(getApplicationContext());
        api.deleteChat(chatId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ChatActivity.this, "Chat deleted", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(ChatActivity.this, "Failed to delete chat", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(ChatActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadChatHistory() {
        if (isLoading) return;
        isLoading = true;
        ApiService apiService = ApiClient.getApiService(getApplicationContext());
        apiService.getChatMessages(chatId, currentPage, pageSize, "timestamp,desc")
                .enqueue(new Callback<PageResponse<SendMessageResponse>>() {
                    @Override
                    public void onResponse(Call<PageResponse<SendMessageResponse>> call, Response<PageResponse<SendMessageResponse>> response) {
                        isLoading = false;
                        if (response.isSuccessful() && response.body() != null) {
                            List<SendMessageResponse> messages = response.body().getContent();

                            int previousItemCount = messageList.size();

                            messageList.addAll(messages);
                            messageAdapter.notifyItemRangeInserted(previousItemCount, messages.size());

                            currentPage++;
                        } else {
                            Toast.makeText(ChatActivity.this, "Error loading messages", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<PageResponse<SendMessageResponse>> call, Throwable t) {
                        isLoading = false;
                        Toast.makeText(ChatActivity.this, "Failed to load messages", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void sendMessage() {
        String content = editTextMessage.getText().toString().trim();
        if (TextUtils.isEmpty(content)) return;

        SendMessageRequest request = new SendMessageRequest();
        request.setChatId(chatId);
        request.setContent(content);

        webSocketManager.send(request);

        editTextMessage.setText("");
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        webSocketManager.disconnect();
    }
}
