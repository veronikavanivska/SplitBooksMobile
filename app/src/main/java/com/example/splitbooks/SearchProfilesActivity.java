package com.example.splitbooks;

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
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.splitbooks.DTO.response.ShortProfileResponse;
import com.example.splitbooks.network.ApiClient;
import com.example.splitbooks.network.ApiService;

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

    // New Suggested views
    private TextView textSuggestedTitle;
    private RecyclerView recyclerSuggested;
    private FollowListAdapter suggestedAdapter;
    private List<ShortProfileResponse> suggestedList = new ArrayList<>();
    private ImageView btnClearSearch;
    private ApiService apiService;

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
        apiService = ApiClient.getApiService(getApplicationContext());

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
            editSearch.setText(""); // clears the text and triggers reset
            editSearch.clearFocus(); // optionally close keyboard
        });
    }

    private void loadSuggestedFriends() {
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
                // Optionally show error
                textSuggestedTitle.setVisibility(View.GONE);
                recyclerSuggested.setVisibility(View.GONE);
            }
        });
    }

    private void searchProfiles(String username) {
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
