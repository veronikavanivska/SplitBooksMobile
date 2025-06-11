package com.example.splitbooks.activity.chats;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.*;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.splitbooks.DTO.request.CreateGroupChatRequest;
import com.example.splitbooks.DTO.response.ChatResponse;
import com.example.splitbooks.DTO.response.GroupChatType;
import com.example.splitbooks.DTO.response.ShortProfileResponse;
import com.example.splitbooks.R;
import com.example.splitbooks.helper.CameraHelper;
import com.example.splitbooks.network.ApiClient;
import com.example.splitbooks.network.ApiService;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.*;
import java.util.*;

import okhttp3.*;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreateGroupChatActivity extends AppCompatActivity {

    private MaterialToolbar back;
    private ImageView groupAvatar, btnClearSearch;
    private EditText groupNameInput;
    private Spinner groupTypeSpinner;
    private AutoCompleteTextView searchInput;
    private RecyclerView participantsRecyclerView;
    private Button createGroupButton;


    private SelectableProfileAdapter adapter;
    private final List<ShortProfileResponse> selectedParticipants = new ArrayList<>();
    private List<ShortProfileResponse> searchResults = new ArrayList<>();


    private CameraHelper cameraHelper;
    private Uri selectedGroupAvatarUri;
    private ActivityResultLauncher<Intent> cameraLauncher, galleryLauncher;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);

        initViews();
        initSpinner();
        initRecyclerView();
        initCameraHelpers();
        setListeners();
        adapter.setProfiles(new ArrayList<>());
    }

    private void initViews() {
        back = findViewById(R.id.back_arrow_group);
        groupAvatar = findViewById(R.id.groupAvatar);
        groupNameInput = findViewById(R.id.groupNameInput);
        groupTypeSpinner = findViewById(R.id.groupTypeSpinner);
        searchInput = findViewById(R.id.searchInput);
        participantsRecyclerView = findViewById(R.id.participantsRecyclerView);
        createGroupButton = findViewById(R.id.createGroupButton);
        btnClearSearch = findViewById(R.id.btnClearSearch);
    }

    private void initSpinner() {
        GroupChatType[] groupChatTypes = GroupChatType.values();
        ArrayAdapter<GroupChatType> spinnerAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, groupChatTypes);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        groupTypeSpinner.setAdapter(spinnerAdapter);
    }

    private void initRecyclerView() {
        participantsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SelectableProfileAdapter();
        participantsRecyclerView.setAdapter(adapter);
    }

    private void initCameraHelpers() {
        cameraHelper = new CameraHelper(this);

        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && cameraHelper.getCameraImageUri() != null) {
                        selectedGroupAvatarUri = cameraHelper.getCameraImageUri();
                        groupAvatar.setImageURI(selectedGroupAvatarUri);
                    } else {
                        Toast.makeText(this, "Camera cancelled", Toast.LENGTH_SHORT).show();
                    }
                });

        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        if (imageUri != null) {
                            selectedGroupAvatarUri = imageUri;
                            groupAvatar.setImageURI(imageUri);
                        }
                    } else {
                        Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setListeners() {
        back.setOnClickListener(v -> onBackPressed());

        adapter.setOnProfileSelectedListener(new SelectableProfileAdapter.OnProfileSelectedListener() {
            @Override
            public void onProfileSelected(ShortProfileResponse profile) {
                if (!selectedParticipants.contains(profile)) {
                    selectedParticipants.add(profile);
                }
            }

            @Override
            public void onProfileDeselected(ShortProfileResponse profile) {
                selectedParticipants.remove(profile);
                adapter.setSelectedProfiles(selectedParticipants);
            }
        });

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String query = s.toString().trim();
                if (!query.isEmpty()) {
                    btnClearSearch.setVisibility(VISIBLE);
                    searchProfiles(query);
                } else {
                    btnClearSearch.setVisibility(GONE);
                    adapter.setProfiles(selectedParticipants);
                    adapter.setSelectedProfiles(selectedParticipants);
                }
            }
        });

        btnClearSearch.setOnClickListener(v -> {
            searchInput.setText("");
            btnClearSearch.setVisibility(GONE);
        });

        groupAvatar.setOnClickListener(v -> showImagePickerDialog());

        createGroupButton.setOnClickListener(v -> {
            String groupName = groupNameInput.getText().toString().trim();
            if (groupName.isEmpty()) {
                Toast.makeText(this, "Please enter group name", Toast.LENGTH_SHORT).show();
                return;
            }
            if (selectedParticipants.isEmpty()) {
                Toast.makeText(this, "Select at least 2 participant", Toast.LENGTH_SHORT).show();
                return;
            }

            List<Long> selectedIds = new ArrayList<>();
            for (ShortProfileResponse p : selectedParticipants) {
                selectedIds.add(p.getId());
            }

            CreateGroupChatRequest request = new CreateGroupChatRequest();
            request.setGroupName(groupName);
            request.setParticipantIds(selectedIds);
            request.setGroupChatType((GroupChatType) groupTypeSpinner.getSelectedItem());

            MultipartBody.Part avatarPart = selectedGroupAvatarUri != null ?
                    createMultipartFromUri("file", selectedGroupAvatarUri) : null;

            createGroupChat(request, avatarPart);
        });
    }

    private void showImagePickerDialog() {
        String[] options = {"Take Photo", "Choose from Gallery"};
        new MaterialAlertDialogBuilder(this)
                .setTitle("Select Group Avatar")
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

    private void searchProfiles(String query) {
        ApiService apiService = ApiClient.getApiService(getApplicationContext());
        apiService.searchProfiles(query).enqueue(new Callback<List<ShortProfileResponse>>() {
            @Override
            public void onResponse(Call<List<ShortProfileResponse>> call, Response<List<ShortProfileResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    searchResults = response.body();
                    adapter.setProfiles(searchResults);

                    List<ShortProfileResponse> selectedInSearch = new ArrayList<>();
                    for (ShortProfileResponse profile : searchResults) {
                        for (ShortProfileResponse selected : selectedParticipants) {
                            if (profile.getId().equals(selected.getId())) {
                                selectedInSearch.add(profile);
                                break;
                            }
                        }
                    }
                    adapter.setSelectedProfiles(selectedInSearch);
                } else {
                    adapter.setProfiles(new ArrayList<>());
                }
            }

            @Override
            public void onFailure(Call<List<ShortProfileResponse>> call, Throwable t) {
                Toast.makeText(CreateGroupChatActivity.this, "Search failed: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createGroupChat(CreateGroupChatRequest request, MultipartBody.Part avatarPart) {
        ApiService apiService = ApiClient.getApiService(getApplicationContext());
        RequestBody dataPart = RequestBody.create(
                new com.google.gson.Gson().toJson(request), MediaType.parse("application/json"));

        apiService.createGroupChat(dataPart, avatarPart).enqueue(new Callback<ChatResponse>() {
            @Override
            public void onResponse(Call<ChatResponse> call, Response<ChatResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(CreateGroupChatActivity.this, "Group created: " + response.body().getGroupName(), Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(CreateGroupChatActivity.this, "Select correct Group Type", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ChatResponse> call, Throwable t) {
                Toast.makeText(CreateGroupChatActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

}
