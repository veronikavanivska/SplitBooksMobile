package com.example.splitbooks.activity.profile;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.transition.TransitionInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityOptionsCompat;

import com.bumptech.glide.Glide;
import com.example.splitbooks.DTO.request.CreatePrivateChatRequest;
import com.example.splitbooks.DTO.response.ChatResponse;
import com.example.splitbooks.DTO.response.ProfileResponse;
import com.example.splitbooks.R;
import com.example.splitbooks.activity.chats.AllChatsActivity;
import com.example.splitbooks.activity.chats.ChatActivity;
import com.example.splitbooks.activity.search.ProfileBokksActivity;
import com.example.splitbooks.activity.search.SearchBookActivity;
import com.example.splitbooks.activity.search.SearchProfilesActivity;
import com.example.splitbooks.activity.home.HomePageActivity;
import com.example.splitbooks.network.ApiClient;
import com.example.splitbooks.network.ApiService;
import com.example.splitbooks.network.JwtManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OtherProfileActivity extends AppCompatActivity {
    private TextView usernameText, followersText, followingText, fullNameText, phoneNumberText;
    private ChipGroup genresContainer, languagesContainer, formatsContainer;
    private ImageView avatarImage;
    private Long profileId , myProfileId;
    private boolean isFollowing;
    private Button follow,chat,library;
    private MaterialButton back;
    private BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_someones_profile);
        usernameText = findViewById(R.id.username_text);
        followersText = findViewById(R.id.followers_text);
        followingText = findViewById(R.id.following_text);
        fullNameText = findViewById(R.id.full_name);
        phoneNumberText = findViewById(R.id.phone_number);
        avatarImage = findViewById(R.id.avatar_image);

        genresContainer = findViewById(R.id.genres_container);
        languagesContainer = findViewById(R.id.languages_container);
        formatsContainer = findViewById(R.id.formats_container);
        back = findViewById(R.id.btn_back);
        follow = findViewById(R.id.btn_follow);
        chat = findViewById(R.id.btn_chat);
        library = findViewById(R.id.btn_library);

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

        myProfileId = JwtManager.getMyProfileId(getApplicationContext());
        profileId = getIntent().getLongExtra("profileId", -1);
        String transitionName = getIntent().getStringExtra("transitionName");
        if (transitionName != null) {
            avatarImage.setTransitionName(transitionName);
        }


        boolean isOwnProfile = profileId.equals(myProfileId);
        if (isOwnProfile) {
            follow.setVisibility(View.GONE);
            chat.setVisibility(View.GONE);
            library.setVisibility(View.GONE);
        }

        chat.setOnClickListener(v->{
            if (profileId != null) {
                createOrGoToPrivateChat(profileId);
            }
        });
        library.setOnClickListener(v->{
            Intent intent = new Intent(this, ProfileBokksActivity.class);
            intent.putExtra("profileId", profileId);
            startActivity(intent);
        });
        followersText.setOnClickListener(v -> {
            Intent intent = new Intent(this, FollowListActivity.class);
            intent.putExtra("profileId", profileId);
            intent.putExtra("isFollowers", true);
            startActivity(intent);
        });

        followingText.setOnClickListener(v -> {
            Intent intent = new Intent(this, FollowListActivity.class);
            intent.putExtra("profileId", profileId);
            intent.putExtra("isFollowers", false);
            startActivity(intent);
        });
        avatarImage.setOnClickListener(v -> {
            Intent intent = new Intent(this, FullscreenAvatarActivity.class);
            intent.putExtra("avatarUrl", avatarImage.getTag().toString());
            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                    this,
                    avatarImage,
                    avatarImage.getTransitionName()
            );
            startActivity(intent, options.toBundle());
        });
        follow.setOnClickListener(v -> toggleFollow());

        back.setOnClickListener(v -> supportFinishAfterTransition());
        fetchProfile();
        checkIfFollowing();

    }

    private void createOrGoToPrivateChat(Long otherParticipantId) {
        CreatePrivateChatRequest request = new CreatePrivateChatRequest(otherParticipantId);
        ApiService apiService = ApiClient.getApiService(getApplicationContext());

        Call<ChatResponse> call = apiService.createPrivateChat(request);
        call.enqueue(new Callback<ChatResponse>() {
            @Override
            public void onResponse(Call<ChatResponse> call, Response<ChatResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ChatResponse chat = response.body();
                    String chatName = usernameText.getText().toString();
                    String chatPhotoUrl = "";

                    Object tag = avatarImage.getTag();
                    if (tag != null) {
                        chatPhotoUrl = tag.toString();
                    }
                    Intent intent = new Intent(OtherProfileActivity.this, ChatActivity.class);
                    intent.putExtra("chatId", chat.getChatId());
                    intent.putExtra("chatName",chatName);
                    intent.putExtra("chatPhoto", chatPhotoUrl);
                    startActivity(intent);
                } else {
                    Toast.makeText(OtherProfileActivity.this, "Failed to open chat", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ChatResponse> call, Throwable t) {
                Toast.makeText(OtherProfileActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void fetchProfile(){
        ApiService apiService = ApiClient.getApiService(getApplicationContext());
        apiService.getProfileById(profileId).enqueue(new Callback<ProfileResponse>() {
            @Override
            public void onResponse(Call<ProfileResponse> call, Response<ProfileResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ProfileResponse profile = response.body();
                    usernameText.setText(profile.getUsername());
                    followersText.setText("Followers: " + profile.getFollowers());
                    followingText.setText("Following: " + profile.getFollowing());
                    String firstName = profile.getFirstName() != null ? profile.getFirstName() : " ";
                    String lastName = profile.getLastName() != null ? profile.getLastName() : " ";
                    String phone = profile.getPhone() != null ? profile.getPhone() : " ";

                    fullNameText.setText(firstName + " " + lastName);
                    phoneNumberText.setText(phone);

                    populateChips(genresContainer, profile.getGenreNames(),profile.isAnonymous());
                    populateChips(languagesContainer, profile.getLanguageNames(),profile.isAnonymous());
                    populateChips(formatsContainer, profile.getFormatNames(),profile.isAnonymous());

                    if (profile.getAvatarUrl() != null && !profile.getAvatarUrl().isEmpty()) {
                        avatarImage.setTag(profile.getAvatarUrl());
                        postponeEnterTransition();
                        Glide.with(OtherProfileActivity.this)
                                .load(profile.getAvatarUrl())
                                .placeholder(R.drawable.default_avatar)
                                .circleCrop()
                                .into(avatarImage);

                        avatarImage.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                            @Override
                            public boolean onPreDraw() {
                                avatarImage.getViewTreeObserver().removeOnPreDrawListener(this);
                                supportStartPostponedEnterTransition();
                                return true;
                            }
                        });
                    }
                }
            }

            @Override
            public void onFailure(Call<ProfileResponse> call, Throwable t) {
                Toast.makeText(OtherProfileActivity.this, "Failed to load profile", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkIfFollowing() {
        ApiService apiService = ApiClient.getApiService(getApplicationContext());
        apiService.isFollowing(profileId).enqueue(new Callback<Boolean>() {
            @Override
            public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                if (response.isSuccessful() && response.body() != null) {
                    isFollowing = response.body();
                    updateFollowButton();
                }
            }

            @Override
            public void onFailure(Call<Boolean> call, Throwable t) {
                Toast.makeText(OtherProfileActivity.this, "Failed to check follow status", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void toggleFollow() {
        ApiService apiService = ApiClient.getApiService(getApplicationContext());
        if (isFollowing) {
            apiService.unfollow(profileId).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        isFollowing = false;
                        updateFollowButton();
                        Toast.makeText(OtherProfileActivity.this, "Unfollowed", Toast.LENGTH_SHORT).show();
                        fetchProfile();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Toast.makeText(OtherProfileActivity.this, "Unfollow failed", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            apiService.follow(profileId).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        isFollowing = true;
                        updateFollowButton();
                        Toast.makeText(OtherProfileActivity.this, "Followed", Toast.LENGTH_SHORT).show();
                        fetchProfile();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Toast.makeText(OtherProfileActivity.this, "Follow failed", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void updateFollowButton() {
        if (isFollowing) {
            follow.setText("Unfollow");
            follow.setBackgroundTintList(ColorStateList.valueOf(Color.GRAY));
        } else {
            follow.setText("Follow");
            follow.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#006400")));
        }
    }


    private void populateChips(ChipGroup container, List<String> items,boolean dimmed) {
        container.removeAllViews();
        if (items != null) {
            for (String item : items) {
                Chip chip = new Chip(this);
                chip.setText(item);
                if (dimmed) {
                    chip.setTextColor(Color.parseColor("#777777"));
                    chip.setChipBackgroundColor(ColorStateList.valueOf(Color.parseColor("#EEEEEE")));
                    chip.setChipStrokeColor(ColorStateList.valueOf(Color.parseColor("#006400")));
                } else {
                    chip.setTextColor(Color.BLACK);
                    chip.setChipBackgroundColor(ColorStateList.valueOf(Color.WHITE));
                    chip.setChipStrokeColor(ColorStateList.valueOf(Color.parseColor("#006400")));
                }
                chip.setChipStrokeWidth(2f);
                chip.setClickable(false);
                chip.setCheckable(false);
                container.addView(chip);
            }
        }
    }
}
