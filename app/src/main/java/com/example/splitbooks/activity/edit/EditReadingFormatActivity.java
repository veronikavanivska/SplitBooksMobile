package com.example.splitbooks.activity.edit;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.splitbooks.DTO.request.EditReadingPreferences;
import com.example.splitbooks.DTO.request.ReadingFormat;
import com.example.splitbooks.DTO.response.ProfileResponse;
import com.example.splitbooks.activity.profile.PublicProfileActivity;
import com.example.splitbooks.R;
import com.example.splitbooks.network.ApiClient;
import com.example.splitbooks.network.ApiService;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditReadingFormatActivity extends AppCompatActivity {

    private Chip chipEbook, chipBook, chipAudioBook;
    private Button nextButton;

    private MaterialToolbar back;

    private ArrayList<Integer> selectedLanguageIds;

    List<ReadingFormat> allFormats = new ArrayList<>();
    private final Map<String, Integer> formatNameToId = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_formats);

        chipEbook = findViewById(R.id.chip_ebook);
        chipBook = findViewById(R.id.chip_book);
        chipAudioBook = findViewById(R.id.chip_aubook);
        nextButton = findViewById(R.id.next_button);
        back = findViewById(R.id.back_arrow_edit_formats);

        selectedLanguageIds = getIntent().getIntegerArrayListExtra("selectedLanguageIds");
        if (selectedLanguageIds == null) {
            selectedLanguageIds = new ArrayList<>();
        }

        loadReadingFormatsApi();


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
            Log.d("EditReadingFormat", "Sending formats: " + selectedFormats + ", languages: " + selectedLanguageIds);

            sendPatchReadingPreferences(selectedFormats);

        });
    }
    private void loadReadingFormatsApi() {
        ApiService apiService = ApiClient.getApiService(getApplicationContext());
        apiService.getReadingFormats().enqueue(new Callback<List<ReadingFormat>>() {
            @Override
            public void onResponse(Call<List<ReadingFormat>> call, Response<List<ReadingFormat>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allFormats = response.body();
                    for (ReadingFormat format : allFormats) {
                        formatNameToId.put(format.getFormatName().toUpperCase(), format.getFormatId());
                    }
                    loadUserFormats();
                } else {
                    Toast.makeText(EditReadingFormatActivity.this, "Failed to load formats", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<ReadingFormat>> call, Throwable t) {
                Toast.makeText(EditReadingFormatActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
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
                        Integer id = getFormatIdByName(format);
                        if (id != null) formatIds.add(id);

                    }

                    Log.d("EditReadingFormat", "chipBook=" + chipBook.isChecked() + ", chipEbook=" + chipEbook.isChecked() + ", chipAudioBook=" + chipAudioBook.isChecked());
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

    private Integer getFormatIdByName(String name) {
        if (name == null) return null;
        return formatNameToId.get(name.trim().toUpperCase());
    }



    private void preselectFormats(ArrayList<Integer> selectedFormatIds) {
        Integer bookId = formatNameToId.get("BOOK");
        Integer ebookId = formatNameToId.get("EBOOK");
        Integer audiobookId = formatNameToId.get("AUDIOBOOK");

        chipBook.setChecked(bookId != null && selectedFormatIds.contains(bookId));
        chipEbook.setChecked(ebookId != null && selectedFormatIds.contains(ebookId));
        chipAudioBook.setChecked(audiobookId != null && selectedFormatIds.contains(audiobookId));
    }



    private ArrayList<Integer> getSelectedFormatIds() {
        ArrayList<Integer> selected = new ArrayList<>();
        if (chipBook.isChecked() && formatNameToId.containsKey("BOOK")) {
            selected.add(formatNameToId.get("BOOK"));
        }
        if (chipEbook.isChecked() && formatNameToId.containsKey("EBOOK")) {
            selected.add(formatNameToId.get("EBOOK"));
        }
        if (chipAudioBook.isChecked() && formatNameToId.containsKey("AUDIOBOOK")) {
            selected.add(formatNameToId.get("AUDIOBOOK"));
        }
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

        EditReadingPreferences request = new EditReadingPreferences(languages,formats);
        Log.d("EditReadingFormat", "Sending formats: " + formats + ", languages: " + languages);
        apiService.editReadingPreferences(request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(EditReadingFormatActivity.this, "Preferences updated", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(EditReadingFormatActivity.this, PublicProfileActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
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