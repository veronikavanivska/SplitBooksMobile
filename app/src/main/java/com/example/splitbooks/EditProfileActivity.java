package com.example.splitbooks;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.splitbooks.DTO.request.EditProfileRequest;
import com.example.splitbooks.DTO.response.ProfileResponse;
import com.example.splitbooks.helper.CameraHelper;
import com.example.splitbooks.network.ApiClient;
import com.example.splitbooks.network.ApiService;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditProfileActivity extends AppCompatActivity {
    private ImageView avatarPreview;
    private Button uploadAvatarButton;
    private Button submitButton,backButton;
    private EditText phoneField, nameField, lastField, usernameField, usernamePublicField;
    private ArrayList<Integer> selectedGenres, selectedLanguages, selectedFormats;

    private MaterialToolbar back;
    private Uri selectedAvatarUri;
    private CameraHelper cameraHelper;
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<Intent> galleryLauncher;
    private boolean isAnonymous;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);


        usernameField = findViewById(R.id.anon_username_field);
        usernamePublicField = findViewById(R.id.edit_username_field);
        usernamePublicField = findViewById(R.id.edit_username_field);
        avatarPreview = findViewById(R.id.edit_avatar_preview);
        uploadAvatarButton = findViewById(R.id.edit_upload_avatar_button);
        submitButton = findViewById(R.id.save_edit_button);
        phoneField = findViewById(R.id.edit_phone_field);
        nameField = findViewById(R.id.edit_first_name_field);
        lastField = findViewById(R.id.edit_last_name_field);
        back = findViewById(R.id.back_arrow_edit_profile);

        String profileType = getIntent().getStringExtra("profileType");
        isAnonymous = "ANONYMOUS".equals(profileType);

        if (isAnonymous) {
            usernameField.setVisibility(View.VISIBLE);
            phoneField.setVisibility(View.GONE);
            nameField.setVisibility(View.GONE);
            lastField.setVisibility(View.GONE);
            usernamePublicField.setVisibility(View.GONE);
        } else {
            usernameField.setVisibility(View.GONE);
            usernamePublicField.setVisibility(View.VISIBLE);
            phoneField.setVisibility(View.VISIBLE);
            nameField.setVisibility(View.VISIBLE);
            lastField.setVisibility(View.VISIBLE);
        }



        cameraHelper = new CameraHelper(this);

        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && cameraHelper.getCameraImageUri() != null) {
                        selectedAvatarUri = cameraHelper.getCameraImageUri();
                        avatarPreview.setImageURI(selectedAvatarUri);
                    } else {
                        Toast.makeText(this, "Camera cancelled", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri selectedImage = result.getData().getData();
                        if (selectedImage != null) {
                            selectedAvatarUri = selectedImage;
                            avatarPreview.setImageURI(selectedImage);
                        }
                    } else {
                        Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        uploadAvatarButton.setOnClickListener(v -> showImagePickerDialog());

        submitButton.setOnClickListener(v -> {
            String username = usernameField.getText().toString().trim();
            String name = nameField.getText().toString().trim();
            String lastName = lastField.getText().toString().trim();
            String phone = phoneField.getText().toString().trim();
            String pubUsername = usernamePublicField.getText().toString().trim();
            if (isAnonymous && username.isEmpty()) {
                Toast.makeText(this, "Username is required for anonymous profiles", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!isAnonymous && (pubUsername.isEmpty() || name.isEmpty() || lastName.isEmpty())) {
                Toast.makeText(this, "Please fill username, name and lastname fields", Toast.LENGTH_SHORT).show();
            } else {

                EditProfileRequest request;
                if (isAnonymous) {
                    request = new EditProfileRequest( username);
                    System.out.println("Received request: " + request);
                } else {
                    String phoneValue = phone.isEmpty() ? " " : phone;
                    request = new EditProfileRequest(name, lastName, phoneValue,pubUsername);
                    System.out.println("Received request: " + request);
                }

                editUser(request);
            }
        });
        back.setNavigationOnClickListener(v -> {
            Intent intent = new Intent(this, PublicProfileActivity.class);
            startActivity(intent);
            finish();
        });

        loadProfileData();
    }

    private void showImagePickerDialog() {
        String[] options = {"Take Photo", "Choose from Gallery"};
        new MaterialAlertDialogBuilder(this)
                .setTitle("Select Avatar")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        if (cameraHelper.checkCameraPermission()) {
                            cameraHelper.openCamera(cameraLauncher);
                        } else {
                            cameraHelper.requestCameraPermission();
                        }
                    } else {
                        if (cameraHelper.checkStoragePermission()) {
                            cameraHelper.openGallery(galleryLauncher);
                        } else {
                            cameraHelper.requestStoragePermission();
                        }
                    }
                })
                .setBackground(ContextCompat.getDrawable(this, R.drawable.dialog_background))
                .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CameraHelper.REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                cameraHelper.openCamera(cameraLauncher);
            } else {
                cameraHelper.handlePermissionDenied("Camera");
            }
        } else if (requestCode == CameraHelper.REQUEST_STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                cameraHelper.openGallery(galleryLauncher);
            } else {
                cameraHelper.handlePermissionDenied("Storage");
            }
        }
    }

    private MultipartBody.Part createMultipartFromUri(String partName, Uri uri) {
        try {
            ContentResolver contentResolver = getContentResolver();
            InputStream inputStream = contentResolver.openInputStream(uri);
            File tempFile = File.createTempFile("avatar", ".jpg", getCacheDir());
            try (OutputStream outputStream = new FileOutputStream(tempFile)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }
            RequestBody requestFile = RequestBody.create(tempFile, MediaType.parse(contentResolver.getType(uri)));
            return MultipartBody.Part.createFormData(partName, tempFile.getName(), requestFile);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    private void loadProfileData() {
        ApiService apiService = ApiClient.getApiService(getApplicationContext());
        apiService.getProfile().enqueue(new Callback<ProfileResponse>() {
            @Override
            public void onResponse(Call<ProfileResponse> call, Response<ProfileResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ProfileResponse profile = response.body();
                    if (!isAnonymous) {
                        nameField.setText(profile.getFirstName());
                        lastField.setText(profile.getLastName());
                        phoneField.setText(profile.getPhone());
                        usernamePublicField.setText(profile.getUsername());
                    } else {
                        usernameField.setText(profile.getUsername());
                    }

                    Glide.with(EditProfileActivity.this)
                            .load(profile.getAvatarUrl())
                            .placeholder(R.drawable.default_avatar)
                            .into(avatarPreview);
                }
            }

            @Override
            public void onFailure(Call<ProfileResponse> call, Throwable t) {
                Toast.makeText(EditProfileActivity.this, "Failed to load profile", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void editUser(EditProfileRequest request) {
        try{
            ApiService apiService = ApiClient.getApiService(getApplicationContext());
            MultipartBody.Part avatarPart = selectedAvatarUri != null ? createMultipartFromUri("avatar", selectedAvatarUri) : null;

            Gson gson = new Gson();
            String json = gson.toJson(request);
            RequestBody jsonBody = RequestBody.create(json, MediaType.parse("application/json"));

            Call<ResponseBody> call = apiService.editProfile(jsonBody, avatarPart);
            call.enqueue(new Callback<ResponseBody>() {

                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if(response.isSuccessful()){

                        Toast.makeText(EditProfileActivity.this, "Profile edited succesfully", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(EditProfileActivity.this, PublicProfileActivity.class));
                        finish();
                    }else{
                        Toast.makeText(EditProfileActivity.this, "Failed: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {

                }
            });
        }catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "An unexpected error occurred.", Toast.LENGTH_SHORT).show();
        }
    }
}
