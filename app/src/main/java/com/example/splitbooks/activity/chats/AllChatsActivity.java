package com.example.splitbooks.activity.chats;


import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.transition.Explode;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.splitbooks.DTO.response.PageResponse;
import com.example.splitbooks.DTO.response.ShortChatResponse;
import com.example.splitbooks.R;
import com.example.splitbooks.activity.home.HomePageActivity;
import com.example.splitbooks.activity.profile.PublicProfileActivity;
import com.example.splitbooks.activity.search.SearchBookActivity;
import com.example.splitbooks.activity.search.SearchProfilesActivity;
import com.example.splitbooks.network.ApiClient;
import com.example.splitbooks.network.ApiService;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AllChatsActivity extends AppCompatActivity implements ChatAdapter.OnChatClickListener {

    private RecyclerView recyclerViewChats;
    private ChatAdapter chatAdapter;
    private FloatingActionButton fabCreateGroupChat;

    private int currentPage = 0;
    private final int pageSize = 20;
    private boolean isLoading = false;
    private boolean isLastPage = false;

    private LinearLayoutManager layoutManager;
    private BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chats);

        recyclerViewChats = findViewById(R.id.recyclerViewChats);
        fabCreateGroupChat = findViewById(R.id.fabCreateGroupChat);

        layoutManager = new LinearLayoutManager(this);
        recyclerViewChats.setLayoutManager(layoutManager);

        chatAdapter = new ChatAdapter(this);
        recyclerViewChats.setAdapter(chatAdapter);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setExitTransition(new Explode());
            getWindow().setReenterTransition(new Explode());
        }

        fabCreateGroupChat.setOnClickListener(v -> {
            Intent intent = new Intent(this,CreateGroupChatActivity.class);
            startActivity(intent);
        });
        bottomNavigation = findViewById(R.id.other_bottom_navigation);

        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if(id == R.id.action_profile){
                Intent intent = new Intent(this, PublicProfileActivity.class);
                startActivity(intent);
                finish();
                return true;
            }else if(id == R.id.action_home){
                Intent intent = new Intent(this, HomePageActivity.class);
                startActivity(intent);
                finish();
                return true;
            }else if(id == R.id.action_search){
                Intent intent = new Intent(this, SearchProfilesActivity.class);
                startActivity(intent);
                finish();
                return true;
            }else if(id == R.id.action_library){
                Intent intent = new Intent(this, SearchBookActivity.class);
                startActivity(intent);
                finish();
                return true;
            }else if(id == R.id.action_chats){
                Intent intent = new Intent(this, AllChatsActivity.class);
                startActivity(intent);
                finish();
                return true;
            }
            return false;
        });
        setupRecyclerViewScrollListener();
        fetchChats(currentPage);
    }
    @Override
    protected void onResume() {
        super.onResume();
        fetchChats(currentPage);
    }
    private void setupRecyclerViewScrollListener() {
        recyclerViewChats.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                int totalItemCount = layoutManager.getItemCount();
                int visibleItemCount = layoutManager.getChildCount();
                int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                if (!isLoading && !isLastPage) {
                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount - 3) {
                        fetchChats(currentPage + 1);
                    }
                }
            }
        });
    }

    private void fetchChats(int page) {
        isLoading = true;
        ApiService apiService = ApiClient.getApiService(getApplicationContext());

        apiService.getAllChats(page, pageSize).enqueue(new Callback<PageResponse<ShortChatResponse>>() {
            @Override
            public void onResponse(Call<PageResponse<ShortChatResponse>> call, Response<PageResponse<ShortChatResponse>> response) {
                isLoading = false;

                if (response.isSuccessful() && response.body() != null) {
                    List<ShortChatResponse> chats = response.body().getContent();
                    if (page == 0) {
                        chatAdapter.setChatList(chats);
                    } else {
                        chatAdapter.appendChatList(chats);
                    }

                    currentPage = page;
                    isLastPage = response.body().isLast();
                } else {
                    Toast.makeText(AllChatsActivity.this, "Failed to load chats", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<PageResponse<ShortChatResponse>> call, Throwable t) {
                isLoading = false;
                Toast.makeText(AllChatsActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onChatClick(ShortChatResponse chat) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("chatId",chat.getChatId());
        intent.putExtra("chatName",chat.getChatName());
        intent.putExtra("chatPhoto", chat.getChatProfileUrl());
        intent.putExtra("isGroup", chat.isGroupChat());
        Log.d("AllChatActivity", "isGroupChat = " + chat.isGroupChat());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
        } else {
            startActivity(intent);
        }
    }
}