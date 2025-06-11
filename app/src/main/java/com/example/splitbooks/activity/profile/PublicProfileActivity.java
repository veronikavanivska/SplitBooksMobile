package com.example.splitbooks.activity.profile;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityOptionsCompat;

import com.bumptech.glide.Glide;
import com.example.splitbooks.DTO.response.ProfileResponse;
import com.example.splitbooks.R;
import com.example.splitbooks.activity.auth.MainActivity;
import com.example.splitbooks.activity.edit.EditGenresActivity;
import com.example.splitbooks.activity.edit.EditLanguageActivity;
import com.example.splitbooks.activity.edit.EditProfileActivity;
import com.example.splitbooks.activity.home.HomePageActivity;
import com.example.splitbooks.activity.setup.GenreActivity;
import com.example.splitbooks.network.ApiClient;
import com.example.splitbooks.network.ApiService;
import com.example.splitbooks.network.JwtManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PublicProfileActivity extends AppCompatActivity {

    private TextView usernameText, followersText, followingText, fullNameText, phoneNumberText;
    private ChipGroup genresContainer, languagesContainer, formatsContainer;
    private ImageView avatarImage;
    private boolean hasAnonymousProfile;
    private boolean isAnonymousProfile;
    private Long currentProfileId;
    private TextView editGenres, editLanguages;
    private Button logOut;
    private BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_public_profile);
        usernameText = findViewById(R.id.username_text);
        followersText = findViewById(R.id.followers_text);
        followingText = findViewById(R.id.following_text);
        fullNameText = findViewById(R.id.full_name);
        phoneNumberText = findViewById(R.id.phone_number);
        avatarImage = findViewById(R.id.avatar_image);
        logOut = findViewById(R.id.btn_logout);
        genresContainer = findViewById(R.id.genres_container);
        languagesContainer = findViewById(R.id.languages_container);
        formatsContainer = findViewById(R.id.formats_container);

        editGenres = findViewById(R.id.edit_genres);
        editLanguages = findViewById(R.id.edit_languages);

        bottomNavigation = findViewById(R.id.bottom_navigation);

        setClickListeners();
        fetchProfile();
    }
    @Override
    protected void onResume() {
        super.onResume();
        fetchProfile();
    }

    private void setClickListeners() {
        editGenres.setOnClickListener(v -> {
            startActivity(new Intent(this, EditGenresActivity.class));
            finish();
        });

        editLanguages.setOnClickListener(v -> {
            startActivity(new Intent(this, EditLanguageActivity.class));
            finish();
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




        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.action_edit_profile) {
                Intent intent = new Intent(this, EditProfileActivity.class);
                intent.putExtra("profileType", isAnonymousProfile ? "ANONYMOUS" : "PUBLIC");
                startActivity(intent);
                finish();
                return true;
            } else if (id == R.id.action_switch_to_anonymous) {
                if (!hasAnonymousProfile) {
                    toggleToAnonymousThenSetup();
                } else {
                    toggleProfile();
                }
                return true;
            } else if (id == R.id.action_back) {
                startActivity(new Intent(this, HomePageActivity.class));
                finish();
                return true;
            }
            return false;
        });
        followersText.setOnClickListener(v -> {
            Intent intent = new Intent(this, FollowListActivity.class);
            intent.putExtra("profileId", currentProfileId);
            intent.putExtra("isFollowers", true);
            startActivity(intent);
        });

        followingText.setOnClickListener(v -> {
            Intent intent = new Intent(this, FollowListActivity.class);
            intent.putExtra("profileId", currentProfileId);
            intent.putExtra("isFollowers", false);
            startActivity(intent);
        });

        logOut.setOnClickListener(v->{
            JwtManager.clearToken(this);

            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void fetchProfile() {
        ApiService apiService = ApiClient.getApiService(getApplicationContext());
        apiService.getProfile().enqueue(new Callback<ProfileResponse>() {
            @Override
            public void onResponse(Call<ProfileResponse> call, Response<ProfileResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ProfileResponse profile = response.body();

                    if (!profile.isSetupCompleted()) {
                        Intent intent = new Intent(PublicProfileActivity.this, GenreActivity.class);
                        intent.putExtra("profileType", profile.isAnonymous() ? "ANONYMOUS" : "PUBLIC");
                        startActivity(intent);
                        finish();
                        return;
                    }

                    usernameText.setText(profile.getUsername());
                    followersText.setText("Followers: " + profile.getFollowers());
                    followingText.setText("Following: " + profile.getFollowing());

                    if (profile.isAnonymous()) {
                        fullNameText.setVisibility(View.GONE);
                        phoneNumberText.setVisibility(View.GONE);
                    } else {
                        fullNameText.setVisibility(View.VISIBLE);
                        phoneNumberText.setVisibility(View.VISIBLE);
                        fullNameText.setText(profile.getFirstName() + " " + profile.getLastName());
                        phoneNumberText.setText(profile.getPhone());
                    }
                    currentProfileId = profile.getId();

                    hasAnonymousProfile = profile.isHasAnonymous();
                    isAnonymousProfile = profile.isAnonymous();

                    MenuItem switchItem = bottomNavigation.getMenu().findItem(R.id.action_switch_to_anonymous);

                    if (profile.isAnonymous()) {
                        switchItem.setTitle("Switch to Public");
                        switchItem.setIcon(R.drawable.user_icon_svgrepo_com);
                    } else {
                        switchItem.setTitle("Switch to Anonymous");
                        switchItem.setIcon(R.drawable.anonymous_user_icon);
                    }

                    populateChips(genresContainer, profile.getGenreNames(),profile.isAnonymous());
                    populateChips(languagesContainer, profile.getLanguageNames(),profile.isAnonymous());
                    populateChips(formatsContainer, profile.getFormatNames(),profile.isAnonymous());

                    if (profile.getAvatarUrl() != null && !profile.getAvatarUrl().isEmpty()) {
                        avatarImage.setTag(profile.getAvatarUrl());
                        Glide.with(PublicProfileActivity.this)
                                .load(profile.getAvatarUrl())
                                .placeholder(R.drawable.default_avatar)
                                .circleCrop()
                                .into(avatarImage);
                    }

                    JwtManager.saveProfileId(getApplicationContext(),profile.getId());
                }
            }

            @Override
            public void onFailure(Call<ProfileResponse> call, Throwable t) {
                Toast.makeText(PublicProfileActivity.this, "Failed to load profile", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void toggleToAnonymousThenSetup() {
        ApiService apiService = ApiClient.getApiService(getApplicationContext());
        apiService.toggleProfile().enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    createAnonymousProfile();
                } else {
                    Toast.makeText(PublicProfileActivity.this, "Failed to switch to anonymous", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                t.printStackTrace();
                Toast.makeText(PublicProfileActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void createAnonymousProfile() {
        ApiService apiService = ApiClient.getApiService(getApplicationContext());
        apiService.createAnonymousProfile().enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Intent intent = new Intent(PublicProfileActivity.this, GenreActivity.class);
                    intent.putExtra("profileType", "ANONYMOUS");
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(PublicProfileActivity.this, "Anonymous profile already exists or failed to create", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(PublicProfileActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void toggleProfile() {
        ApiService apiService = ApiClient.getApiService(getApplicationContext());
        apiService.toggleProfile().enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    fetchProfile();
                } else {
                    Toast.makeText(PublicProfileActivity.this, "Failed to toggle profile", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(PublicProfileActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void populateChips(ChipGroup container, List<String> items,boolean dimmed) {
        container.removeAllViews();
        if (items != null) {
            for (String item : items) {
                Chip chip = new Chip(this);
                chip.setText(item);
                chip.setTextColor(Color.BLACK);
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
