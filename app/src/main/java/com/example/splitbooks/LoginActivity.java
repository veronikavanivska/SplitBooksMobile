package com.example.splitbooks;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.splitbooks.DTO.request.LoginRequest;
import com.example.splitbooks.DTO.response.LoginResponse;
import com.example.splitbooks.DTO.response.ProfileResponse;
import com.example.splitbooks.network.ApiClient;
import com.example.splitbooks.network.ApiService;
import com.example.splitbooks.network.JwtManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {


    private ImageView eyeIcon;
    private EditText emailField, passwordField;
    private Button loginButton;
    private TextView registerText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailField = findViewById(R.id.email_field);
        passwordField = findViewById(R.id.password_field);
        loginButton = findViewById(R.id.login_button);
        registerText = findViewById(R.id.register_text);
        eyeIcon = findViewById(R.id.eye_icon);

        passwordField.setTransformationMethod(PasswordTransformationMethod.getInstance());


        eyeIcon.setOnClickListener(v -> {

            int cursorPosition = passwordField.getSelectionStart();

            if (passwordField.getTransformationMethod() == PasswordTransformationMethod.getInstance()) {

                passwordField.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                eyeIcon.setImageResource(R.drawable.eye);
            } else {

                passwordField.setTransformationMethod(PasswordTransformationMethod.getInstance());
                eyeIcon.setImageResource(R.drawable.eye_closed);
            }
            passwordField.setSelection(cursorPosition);
        });

        registerText.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        loginButton.setOnClickListener(v -> {
            String email = emailField.getText().toString().trim();
            String password = passwordField.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Please fill in both fields", Toast.LENGTH_SHORT).show();
            } else {
                loginUser(email, password);
            }
        });
    }

    public void loginUser(String email, String password) {
        try {
            ApiService apiService = ApiClient.getApiService(getApplicationContext());
            LoginRequest loginRequest = new LoginRequest(email, password);

            Call<LoginResponse> call = apiService.login(loginRequest);
            call.enqueue(new Callback<LoginResponse>() {
                @Override
                public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                    if (response.isSuccessful()) {

                        LoginResponse loginResponse = response.body();
                        if (loginResponse != null) {

                            JwtManager.saveToken(getApplicationContext(), loginResponse.getToken());

                            Log.d("LoginActivity", "Saved Token: " + loginResponse.getToken());
                            fetchProfileAndNavigate();

                        }
                    } else {
                        Toast.makeText(LoginActivity.this, "Login failed. Please check your credentials.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<LoginResponse> call, Throwable t) {
                    Toast.makeText(LoginActivity.this, "Network error. Please try again.", Toast.LENGTH_SHORT).show();
                    t.printStackTrace();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(LoginActivity.this, "An unexpected error occurred.", Toast.LENGTH_SHORT).show();
        }
    }
    private void fetchProfileAndNavigate() {
        ApiService apiService = ApiClient.getApiService(getApplicationContext());
        Call<ProfileResponse> call = apiService.getProfile();

        call.enqueue(new Callback<ProfileResponse>() {
            @Override
            public void onResponse(Call<ProfileResponse> call, Response<ProfileResponse> response) {
                if (response.isSuccessful()) {
                    ProfileResponse profile = response.body();
                    if (profile != null) {
                        long profileId = profile.getId();

                        JwtManager.saveProfileId(getApplicationContext(),profileId);

                        Intent intent = new Intent(LoginActivity.this, HomePageActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(LoginActivity.this, "Profile is empty", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(LoginActivity.this, "Failed to load profile", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ProfileResponse> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "Network error while loading profile", Toast.LENGTH_SHORT).show();
                t.printStackTrace();
            }
        });
    }


}
