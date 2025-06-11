package com.example.splitbooks.activity.chats;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.splitbooks.DTO.response.ChatResponse;
import com.example.splitbooks.R;
import com.example.splitbooks.network.ApiClient;
import com.example.splitbooks.network.ApiService;
import com.google.android.material.appbar.MaterialToolbar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatInfoActivity extends AppCompatActivity {

    private ImageView groupPhoto;
    private TextView groupName,groupType;
    private RecyclerView participantsRecycler;
    private ChatParticipantsAdapter adapter;
    private MaterialToolbar back;

    private Long chatId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_info);

        groupPhoto = findViewById(R.id.chat_info_group_photo);
        groupName = findViewById(R.id.chat_info_group_name);
        groupType = findViewById(R.id.chat_info_group_type);
        participantsRecycler = findViewById(R.id.chat_info_participants_recycler);
        back = findViewById(R.id.back_arrow_group);
        back.setOnClickListener(v -> onBackPressed());
        participantsRecycler.setLayoutManager(new LinearLayoutManager(this));

        chatId = getIntent().getLongExtra("chatId", -1);

        if (chatId == -1) {
            Toast.makeText(this, "Invalid chat ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }


        fetchChatInfo();
    }

    private void fetchChatInfo() {
        ApiService api = ApiClient.getApiService(getApplicationContext());
        Call<ChatResponse> call = api.getChatInfo(chatId);

        call.enqueue(new Callback<ChatResponse>() {
            @Override
            public void onResponse(Call<ChatResponse> call, Response<ChatResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    displayChatInfo(response.body());
                } else {
                    Toast.makeText(ChatInfoActivity.this, "Failed to load chat info", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(Call<ChatResponse> call, Throwable t) {
                Toast.makeText(ChatInfoActivity.this, "Network error", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void displayChatInfo(ChatResponse chat) {
        groupName.setText(chat.getGroupName());
        groupType.setText(chat.getGroupChatType().toString());
        Glide.with(this)
                .load(chat.getChatPhotoUrl())
                .placeholder(R.drawable.group_photo)
                .circleCrop()
                .into(groupPhoto);

        adapter = new ChatParticipantsAdapter(chat.getParticipants());
        participantsRecycler.setAdapter(adapter);
    }
}
