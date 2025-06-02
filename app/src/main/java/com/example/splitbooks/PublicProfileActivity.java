package com.example.splitbooks;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.splitbooks.DTO.response.ProfileResponse;
import com.example.splitbooks.network.ApiClient;
import com.example.splitbooks.network.ApiService;
import com.example.splitbooks.network.JwtManager;
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

    private TextView editGenres, editLanguages;
    private Button btnEditProfile, btnBack, switchToAnonymous,logOut;

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

        switchToAnonymous = findViewById(R.id.btn_switch_to_anonymous);
        btnEditProfile = findViewById(R.id.btn_edit_profile);
        btnBack = findViewById(R.id.btn_back);

        setClickListeners();
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

        btnEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(this, EditProfileActivity.class);
            intent.putExtra("profileType", isAnonymousProfile ? "ANONYMOUS" : "PUBLIC");
            startActivity(intent);
            finish();
        });

        switchToAnonymous.setOnClickListener(v -> {
            if (!hasAnonymousProfile) {
                toggleToAnonymousThenSetup();
            } else {
                toggleProfile();
            }
        });

        btnBack.setOnClickListener(v -> {
            startActivity(new Intent(this, HomePageActivity.class));
            finish();
        });

        followersText.setOnClickListener(v ->
                Toast.makeText(this, "View all followers", Toast.LENGTH_SHORT).show()
        );

        followingText.setOnClickListener(v ->
                Toast.makeText(this, "View all followings", Toast.LENGTH_SHORT).show()
        );

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

                    hasAnonymousProfile = profile.isHasAnonymous();
                    isAnonymousProfile = profile.isAnonymous();
                    switchToAnonymous.setText(profile.isAnonymous() ? "Switch to Public" : "Switch to Anonymous");

                    populateChips(genresContainer, profile.getGenreNames());
                    populateChips(languagesContainer, profile.getLanguageNames());
                    populateChips(formatsContainer, profile.getFormatNames());

                    if (profile.getAvatarUrl() != null && !profile.getAvatarUrl().isEmpty()) {
                        Glide.with(PublicProfileActivity.this)
                                .load(profile.getAvatarUrl())
                                .placeholder(R.drawable.default_avatar)
                                .circleCrop()
                                .into(avatarImage);
                    }
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
                    recreate();
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

    private void populateChips(ChipGroup container, List<String> items) {
        container.removeAllViews();
        if (items != null) {
            for (String item : items) {
                Chip chip = new Chip(this);
                chip.setText(item);
                chip.setTextColor(Color.BLACK);
                chip.setChipBackgroundColor(ColorStateList.valueOf(Color.WHITE));
                chip.setChipStrokeColor(ColorStateList.valueOf(Color.parseColor("#006400")));
                chip.setChipStrokeWidth(2f);
                chip.setCloseIconTint(ColorStateList.valueOf(Color.parseColor("#4CAF50")));
                chip.setClickable(false);
                chip.setCheckable(false);
                container.addView(chip);
            }
        }
    }
}
