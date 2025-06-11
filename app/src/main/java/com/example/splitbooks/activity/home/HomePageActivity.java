package com.example.splitbooks.activity.home;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageButton;


import androidx.appcompat.app.AppCompatActivity;

import com.example.splitbooks.activity.chats.AllChatsActivity;

import com.example.splitbooks.activity.profile.PublicProfileActivity;
import com.example.splitbooks.R;
import com.example.splitbooks.activity.search.SearchBookActivity;
import com.example.splitbooks.activity.search.SearchProfilesActivity;


public class HomePageActivity extends AppCompatActivity {

    private ImageButton profile,chat,library,search;
    private TypeWriterTextView quoteTextView;
    private int quoteIndex = 0;
    private final Handler quoteHandler = new Handler();

    private final String[] quotes = {
            "“A reader lives a thousand lives before he dies.” – George R.R. Martin",
            "“Books are a uniquely portable magic.” – Stephen King",
            "“Until I feared I would lose it, I never loved to read.” – Harper Lee",
            "“A room without books is like a body without a soul.” – Cicero"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        profile = findViewById(R.id.icon_profile);
        chat = findViewById(R.id.icon_chats);

        library = findViewById(R.id.icon_library);
        search = findViewById(R.id.icon_search);

        quoteTextView = findViewById(R.id.book_quote);
        quoteTextView.setCharacterDelay(40);
        quoteHandler.post(quoteRunnable);

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
        library.setOnClickListener(v->{
            Intent intent = new Intent(HomePageActivity.this, SearchBookActivity.class);
            startActivity(intent);
            finish();
        });
        chat.setOnClickListener(v->{
            Intent intent = new Intent(HomePageActivity.this, AllChatsActivity.class);
            startActivity(intent);
            finish();
        });


    }

    private final Runnable quoteRunnable = new Runnable() {
        @Override
        public void run() {
            quoteTextView.animateText(quotes[quoteIndex]);
            quoteIndex = (quoteIndex + 1) % quotes.length;
            quoteHandler.postDelayed(this, 9000);
        }
    };


}
