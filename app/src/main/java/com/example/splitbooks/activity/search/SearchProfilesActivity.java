package com.example.splitbooks.activity.search;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.splitbooks.DTO.response.ShortProfileResponse;
import com.example.splitbooks.R;
import com.example.splitbooks.activity.chats.AllChatsActivity;
import com.example.splitbooks.activity.home.HomePageActivity;
import com.example.splitbooks.activity.profile.FollowListAdapter;
import com.example.splitbooks.activity.profile.PublicProfileActivity;
import com.example.splitbooks.network.ApiClient;
import com.example.splitbooks.network.ApiService;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
public class SearchProfilesActivity extends AppCompatActivity {

    private EditText editSearch;
    private RecyclerView recyclerView;
    private FollowListAdapter adapter;
    private List<ShortProfileResponse> profileList = new ArrayList<>();
    private TextView textSuggestedTitle;
    private RecyclerView recyclerSuggested;
    private FollowListAdapter suggestedAdapter;
    private List<ShortProfileResponse> suggestedList = new ArrayList<>();
    private ImageView btnClearSearch;
    private BottomNavigationView bottomNavigation;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_profiles);

        editSearch = findViewById(R.id.editSearch);
        recyclerView = findViewById(R.id.recyclerView);
        textSuggestedTitle = findViewById(R.id.textSuggestedTitle);
        recyclerSuggested = findViewById(R.id.recyclerSuggested);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new FollowListAdapter(profileList);
        recyclerView.setAdapter(adapter);

        recyclerSuggested.setLayoutManager(new LinearLayoutManager(this));
        suggestedAdapter = new FollowListAdapter(suggestedList);
        recyclerSuggested.setAdapter(suggestedAdapter);
        btnClearSearch = findViewById(R.id.btnClearSearch);
        bottomNavigation = findViewById(R.id.search_bottom_navigation);

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
                onResume();
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

        loadSuggestedFriends();

        editSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {  }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {  }

            public void afterTextChanged(Editable s) {
                String query = s.toString().trim();
                if (!query.isEmpty()) {
                    searchProfiles(query);
                    textSuggestedTitle.setVisibility(View.GONE);
                    recyclerSuggested.setVisibility(View.GONE);
                    btnClearSearch.setVisibility(View.VISIBLE);
                } else {
                    profileList.clear();
                    adapter.notifyDataSetChanged();
                    textSuggestedTitle.setVisibility(View.VISIBLE);
                    recyclerSuggested.setVisibility(View.VISIBLE);
                    btnClearSearch.setVisibility(View.GONE);
                }
            }
        });

        btnClearSearch.setOnClickListener(v -> {
            editSearch.setText("");
            editSearch.clearFocus();
        });
    }

    private void loadSuggestedFriends() {
        ApiService apiService = ApiClient.getApiService(getApplicationContext());
        Call<List<ShortProfileResponse>> call = apiService.getBestFriendSuggestions();
        call.enqueue(new Callback<List<ShortProfileResponse>>() {
            @Override
            public void onResponse(Call<List<ShortProfileResponse>> call, Response<List<ShortProfileResponse>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    suggestedList.clear();
                    suggestedList.addAll(response.body());
                    suggestedAdapter.notifyDataSetChanged();
                    textSuggestedTitle.setVisibility(View.VISIBLE);
                    recyclerSuggested.setVisibility(View.VISIBLE);
                } else {
                    textSuggestedTitle.setVisibility(View.GONE);
                    recyclerSuggested.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<List<ShortProfileResponse>> call, Throwable t) {
                textSuggestedTitle.setVisibility(View.GONE);
                recyclerSuggested.setVisibility(View.GONE);
            }
        });
    }

    private void searchProfiles(String username) {
        ApiService apiService = ApiClient.getApiService(getApplicationContext());
        Call<List<ShortProfileResponse>> call = apiService.searchProfiles(username);
        call.enqueue(new Callback<List<ShortProfileResponse>>() {
            @Override
            public void onResponse(Call<List<ShortProfileResponse>> call, Response<List<ShortProfileResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    profileList.clear();
                    profileList.addAll(response.body());
                    adapter.notifyDataSetChanged();
                } else {
                    profileList.clear();
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<List<ShortProfileResponse>> call, Throwable t) {
                Toast.makeText(SearchProfilesActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
