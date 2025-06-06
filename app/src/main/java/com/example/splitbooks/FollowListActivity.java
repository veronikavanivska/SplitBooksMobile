package com.example.splitbooks;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.splitbooks.DTO.response.ShortProfileResponse;
import com.example.splitbooks.network.ApiClient;
import com.example.splitbooks.network.ApiService;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FollowListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FollowListAdapter adapter;
    private Long profileId;
    private boolean isFollowers;

    private MaterialToolbar back;

    private BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_follow);

        recyclerView = findViewById(R.id.follow_list_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        back = findViewById(R.id.back_arrow_follow);

        bottomNavigation = findViewById(R.id.bottom_navigation);


        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if(id == R.id.action_profile){
                Intent intent = new Intent(this, PublicProfileActivity.class);
                startActivity(intent);
                finish();
            }
            return false;
        });


        profileId = getIntent().getLongExtra("profileId", -1L);
        isFollowers = getIntent().getBooleanExtra("isFollowers", true);

        back.setTitle(isFollowers ? "Followers" : "Following");

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        fetchFollowList();
    }
    @Override
    protected void onResume() {
        super.onResume();
        fetchFollowList();
    }

    private void fetchFollowList() {
        ApiService api = ApiClient.getApiService(getApplicationContext());
        Call<List<ShortProfileResponse>> call = isFollowers
                ? api.getFollowers(profileId)
                : api.getFollowing(profileId);

        call.enqueue(new Callback<List<ShortProfileResponse>>() {
            @Override
            public void onResponse(Call<List<ShortProfileResponse>> call, Response<List<ShortProfileResponse>> response) {
                if (response.isSuccessful()) {
                    List<ShortProfileResponse> data = response.body();
                    Long myProfileId = getSharedPreferences("MyPrefs", MODE_PRIVATE)
                            .getLong("myProfileId", -1);
                    adapter = new FollowListAdapter(data);
                    recyclerView.setAdapter(adapter);
                } else {
                    Toast.makeText(FollowListActivity.this, "Error loading data", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<ShortProfileResponse>> call, Throwable t) {
                Toast.makeText(FollowListActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
