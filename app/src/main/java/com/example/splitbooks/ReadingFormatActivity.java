package com.example.splitbooks;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.splitbooks.DTO.request.ReadingFormat;
import com.example.splitbooks.network.ApiClient;
import com.example.splitbooks.network.ApiService;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReadingFormatActivity extends AppCompatActivity {

    private Chip chipBook, chipEbook, chipAudiobook;
    private Button next;

    private Map<String, Integer> formatNameToId = new HashMap<>();
    private ArrayList<Integer> selectedGenres;
    private ArrayList<Integer> selectedLanguages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reading_format);


        selectedGenres = getIntent().getIntegerArrayListExtra("selectedGenreIds");
        selectedLanguages = getIntent().getIntegerArrayListExtra("selectedLanguageIds");
        chipBook = findViewById(R.id.chip_book);
        chipEbook = findViewById(R.id.chip_ebook);
        chipAudiobook = findViewById(R.id.chip_aubook);
        next = findViewById(R.id.next_button);


        loadReadingFormatsApi();



        next.setOnClickListener(v -> {
            ArrayList<Integer> selectedFormatIds = new ArrayList<>();

            if (chipBook.isChecked() && formatNameToId.containsKey("book")) {
                selectedFormatIds.add(formatNameToId.get("book"));
            }
            if (chipEbook.isChecked() && formatNameToId.containsKey("ebook")) {
                selectedFormatIds.add(formatNameToId.get("ebook"));
            }
            if (chipAudiobook.isChecked() && formatNameToId.containsKey("audiobook")) {
                selectedFormatIds.add(formatNameToId.get("audiobook"));
            }

            if (selectedFormatIds.isEmpty()) {
                Toast.makeText(this, "Please select at least one format", Toast.LENGTH_SHORT).show();
                return;
            }

            Log.d("SelectedFormatIds", selectedFormatIds.toString());

            Intent intent = new Intent(this, ProfileSetupActivity.class);
            intent.putIntegerArrayListExtra("selectedGenres", selectedGenres);
            intent.putIntegerArrayListExtra("selectedLanguages", selectedLanguages);
            intent.putIntegerArrayListExtra("selectedFormatIds", selectedFormatIds);

            Intent previousIntent = getIntent();
            if (previousIntent != null) {
                intent.putExtras(previousIntent);
            }

            startActivity(intent);
            finish();
        });
    }

    private void loadReadingFormatsApi() {
        ApiService apiService = ApiClient.getApiService(getApplicationContext());
        Call<List<ReadingFormat>> call = apiService.getReadingFormats();

        call.enqueue(new Callback<List<ReadingFormat>>() {
            @Override
            public void onResponse(Call<List<ReadingFormat>> call, Response<List<ReadingFormat>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<ReadingFormat> formats = response.body();

                    for (ReadingFormat format : formats) {
                        String formatName = format.getFormatName().toLowerCase();
                        formatNameToId.put(formatName, format.getFormatId());
                    }
                } else {
                    Toast.makeText(ReadingFormatActivity.this, "Failed to load formats", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<ReadingFormat>> call, Throwable t) {
                Toast.makeText(ReadingFormatActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
