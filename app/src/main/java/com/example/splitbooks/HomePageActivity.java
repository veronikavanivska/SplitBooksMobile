package com.example.splitbooks;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

public class HomePageActivity extends AppCompatActivity {

    private ImageButton profile,chat,game,library,search;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        profile = findViewById(R.id.icon_profile);
        chat = findViewById(R.id.icon_chats);
        game = findViewById(R.id.icon_game);
        library = findViewById(R.id.icon_library);
        search = findViewById(R.id.icon_search);

        profile.setOnClickListener(v -> {
            Intent intent = new Intent(HomePageActivity.this, PublicProfileActivity.class);
            startActivity(intent);
            finish();
        });
        search.setOnClickListener(v->{
            Intent intent = new Intent(HomePageActivity.this, SearchProfilesActivity.class);
            startActivity(intent);
            finish();
        });
    }

}
