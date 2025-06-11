package com.example.splitbooks.activity.auth;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.splitbooks.R;
import com.example.splitbooks.activity.home.HomePageActivity;
import com.example.splitbooks.network.JwtManager;

public class MainActivity extends AppCompatActivity {

    private Button getStartedButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        getStartedButton = findViewById(R.id.get_started_button);

        getStartedButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
        String token = JwtManager.getToken(this);
        if (token != null && !token.isEmpty()) {

            startActivity(new Intent(this, HomePageActivity.class));
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            finish();

        } else {

            startActivity(new Intent(this, LoginActivity.class));
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            finish();
        }
    }
}