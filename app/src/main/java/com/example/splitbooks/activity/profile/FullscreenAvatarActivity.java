package com.example.splitbooks.activity.profile;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.splitbooks.R;
import com.github.chrisbanes.photoview.PhotoView;

public class FullscreenAvatarActivity extends AppCompatActivity {
    private PhotoView photoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen_avatar);

        photoView = findViewById(R.id.photo_view);

        String avatarUrl = getIntent().getStringExtra("avatarUrl");
        photoView.setTransitionName("profile_avatar");

        Glide.with(this)
                .load(avatarUrl)
                .placeholder(R.drawable.default_avatar)
                .into(photoView);

        // Optional: tap to exit
        photoView.setOnClickListener(v -> supportFinishAfterTransition());
    }
}
