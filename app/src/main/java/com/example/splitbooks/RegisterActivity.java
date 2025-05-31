package com.example.splitbooks;

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

import com.example.splitbooks.DTO.RegistrationRequest;
import com.example.splitbooks.DTO.RegistrationResponse;
import com.example.splitbooks.network.ApiClient;
import com.example.splitbooks.network.ApiService;
import com.example.splitbooks.network.JwtManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {
    private ImageView eyeIcon;
    private EditText usernameField ,emailField, passwordField;
    private Button registerButton;
    private TextView loginText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        usernameField = findViewById(R.id.username_field);
        emailField = findViewById(R.id.email_field);
        passwordField = findViewById(R.id.password_field);
        eyeIcon = findViewById(R.id.eye_icon);
        loginText = findViewById(R.id.login_text);
        registerButton = findViewById(R.id.register_button);

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



        loginText.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
        });

        registerButton.setOnClickListener(v->{
            String username = usernameField.getText().toString().trim();
            String email = emailField.getText().toString().trim();
            String password = passwordField.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty() || username.isEmpty()) {
                Toast.makeText(RegisterActivity.this, "Please fill in both fields", Toast.LENGTH_SHORT).show();
            } else {
                registerUser(username, email, password);
            }
        });

    }

    public void registerUser(String username,String email, String password){

        try{
            ApiService apiService = ApiClient.getApiService(getApplicationContext());
            RegistrationRequest registrationRequest = new RegistrationRequest(email, password, username);


            Call<RegistrationResponse> call = apiService.register(registrationRequest);

            call.enqueue(new Callback<RegistrationResponse>() {
                @Override
                public void onResponse(Call<RegistrationResponse> call, Response<RegistrationResponse> response) {
                    if (response.isSuccessful()) {
                        RegistrationResponse registrationResponse = response.body();
                        if (registrationResponse != null) {

                            JwtManager.saveToken(getApplicationContext(), registrationResponse.getToken());

                            Log.d("RegistrationActivity", "Saved Token: " + registrationResponse.getToken());
                            Toast.makeText(RegisterActivity.this, "Registration Successful", Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent(RegisterActivity.this, GenreActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    } else {
                        Toast.makeText(RegisterActivity.this, "Registration Failed. Try again!", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<RegistrationResponse> call, Throwable t) {

                    Toast.makeText(RegisterActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(RegisterActivity.this, "An unexpected error occurred.", Toast.LENGTH_SHORT).show();
        }
    }
}
