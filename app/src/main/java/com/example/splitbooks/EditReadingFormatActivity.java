package com.example.splitbooks;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.splitbooks.DTO.request.EditReadingPreferences;
import com.example.splitbooks.DTO.response.ProfileResponse;
import com.example.splitbooks.network.ApiClient;
import com.example.splitbooks.network.ApiService;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditReadingFormatActivity extends AppCompatActivity {

    private Chip chipEbook, chipBook, chipAudioBook;
    private Button nextButton,back;

    private ArrayList<Integer> selectedLanguageIds;

    private final int FORMAT_BOOK = 1;
    private final int FORMAT_EBOOK = 2;
    private final int FORMAT_AUDIOBOOK = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_formats);

        chipEbook = findViewById(R.id.chip_ebook);
        chipBook = findViewById(R.id.chip_book);
        chipAudioBook = findViewById(R.id.chip_aubook);
        nextButton = findViewById(R.id.next_button);
        back = findViewById(R.id.back_button_edit_formats);

        selectedLanguageIds = getIntent().getIntegerArrayListExtra("selectedLanguageIds");
        if (selectedLanguageIds == null) {
            selectedLanguageIds = new ArrayList<>();
        }

        loadUserFormats();

        back.setOnClickListener(v-> {

            Intent intent = new Intent(this, EditLanguageActivity.class);
            intent.putIntegerArrayListExtra("selectedLanguages", selectedLanguageIds );
            startActivity(intent);
            finish();
        });

        nextButton.setOnClickListener(v -> {
            ArrayList<Integer> selectedFormats = getSelectedFormatIds();
            if (selectedFormats.isEmpty()) {
                Toast.makeText(this, "Please select at least one format", Toast.LENGTH_SHORT).show();
                return;
            }
            sendPatchReadingPreferences(selectedFormats);
        });
    }

    private void loadUserFormats() {
        ApiService apiService = ApiClient.getApiService(getApplicationContext());
        apiService.getProfile().enqueue(new Callback<ProfileResponse>() {
            @Override
            public void onResponse(Call<ProfileResponse> call, Response<ProfileResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<String> formatNames = response.body().getFormatNames();
                    ArrayList<Integer> formatIds = new ArrayList<>();

                    for (String format : formatNames) {
                        switch (format.toLowerCase()) {
                            case "book":
                                formatIds.add(FORMAT_BOOK);
                                break;
                            case "ebook":
                                formatIds.add(FORMAT_EBOOK);
                                break;
                            case "audiobook":
                                formatIds.add(FORMAT_AUDIOBOOK);
                                break;
                        }
                    }

                    preselectFormats(formatIds);
                } else {
                    Toast.makeText(EditReadingFormatActivity.this, "Failed to load formats", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ProfileResponse> call, Throwable t) {
                Toast.makeText(EditReadingFormatActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void preselectFormats(ArrayList<Integer> selectedFormatIds) {
        if (selectedFormatIds.contains(FORMAT_BOOK)) chipBook.setChecked(true);
        if (selectedFormatIds.contains(FORMAT_EBOOK)) chipEbook.setChecked(true);
        if (selectedFormatIds.contains(FORMAT_AUDIOBOOK)) chipAudioBook.setChecked(true);
    }

    private ArrayList<Integer> getSelectedFormatIds() {
        ArrayList<Integer> selected = new ArrayList<>();
        if (chipBook.isChecked()) selected.add(FORMAT_BOOK);
        if (chipEbook.isChecked()) selected.add(FORMAT_EBOOK);
        if (chipAudioBook.isChecked()) selected.add(FORMAT_AUDIOBOOK);
        return selected;
    }

    private void sendPatchReadingPreferences(ArrayList<Integer> selectedFormatIds) {
        ApiService apiService = ApiClient.getApiService(getApplicationContext());

        List<Long> formats = new ArrayList<>();
        for (int id : selectedFormatIds) {
            formats.add((long) id);
        }

        List<Long> languages = new ArrayList<>();
        for (int id : selectedLanguageIds) {
            languages.add((long) id);
        }

        EditReadingPreferences request = new EditReadingPreferences(formats, languages);

        apiService.editReadingPreferences(request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(EditReadingFormatActivity.this, "Preferences updated", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(EditReadingFormatActivity.this, PublicProfileActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(EditReadingFormatActivity.this, "Update failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(EditReadingFormatActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

}
