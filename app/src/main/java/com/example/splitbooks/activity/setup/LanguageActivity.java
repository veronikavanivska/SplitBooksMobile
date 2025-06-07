package com.example.splitbooks.activity.setup;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.splitbooks.DTO.request.Language;
import com.example.splitbooks.R;
import com.example.splitbooks.network.ApiClient;
import com.example.splitbooks.network.ApiService;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class LanguageActivity extends AppCompatActivity {

    private AutoCompleteTextView autoCompleteLanguage;
    private ArrayAdapter<String> adapter;
    private List<Language> languageObjectList = new ArrayList<>();
    private List<String> languageNames = new ArrayList<>();

    private ChipGroup chipGroupLanguage;
    private Button next;
    private ArrayList<Integer> selectedGenres;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_language);

        selectedGenres = getIntent().getIntegerArrayListExtra("selectedGenreIds");

        autoCompleteLanguage = findViewById(R.id.auto_complete_language);
        chipGroupLanguage = findViewById(R.id.chip_group_selected_language);
        next = findViewById(R.id.next_button);


        adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, languageNames);
        autoCompleteLanguage.setAdapter(adapter);

        autoCompleteLanguage.setOnItemClickListener((parent, view, position, id) -> {
            String selectedName = adapter.getItem(position);
            Language selectedLanguage = findLanguageByName(selectedName);
            if (selectedLanguage != null && !isChipAlreadyAdded(selectedLanguage.getLanguageId())) {
                addLanguageChip(selectedLanguage);
            }
            autoCompleteLanguage.setText("");
        });


        next.setOnClickListener(v -> {
            List<Integer> selectedLanguageIds = getSelectedLanguageIds();

            if (selectedLanguageIds.isEmpty()) {
                Toast.makeText(this, "Please select at least one language", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(this, ReadingFormatActivity.class);
            intent.putIntegerArrayListExtra("selectedGenres", selectedGenres);
            intent.putIntegerArrayListExtra("selectedLanguageIds", new ArrayList<>(selectedLanguageIds));

            Log.d("LanguageActivity", "Selected genres: " + selectedGenres);
            Log.d("LanguageActivity", "Selected language IDs: " + selectedLanguageIds);

            Intent previousIntent = getIntent();
            if (previousIntent != null) {
                intent.putExtras(previousIntent);
            }

            startActivity(intent);
            finish();
        });

        loadLanguagesApi();
    }

    private Language findLanguageByName(String name) {
        for (Language lang : languageObjectList) {
            if (lang.getLanguageName().equalsIgnoreCase(name)) {
                return lang;
            }
        }
        return null;
    }

    private boolean isChipAlreadyAdded(int languageId) {
        int count = chipGroupLanguage.getChildCount();
        for (int i = 0; i < count; i++) {
            Chip chip = (Chip) chipGroupLanguage.getChildAt(i);
            Integer chipLanguageId = (Integer) chip.getTag();
            if (chipLanguageId != null && chipLanguageId == languageId) {
                return true;
            }
        }
        return false;
    }

    private void addLanguageChip(Language language) {
        Chip chip = new Chip(this);
        chip.setText(language.getLanguageName());
        chip.setTag(language.getLanguageId());
        chip.setCloseIconVisible(true);
        chip.setClickable(true);
        chip.setCheckable(false);
        chip.setChipBackgroundColor(ColorStateList.valueOf(Color.parseColor("#FFFFFF")));
        chip.setChipStrokeColor(ColorStateList.valueOf(Color.parseColor("#006400")));
        chip.setChipStrokeWidth(2f);
        chip.setCloseIconTint(ColorStateList.valueOf(Color.parseColor("#4CAF50")));
        chip.setOnCloseIconClickListener(v -> chipGroupLanguage.removeView(chip));
        chipGroupLanguage.addView(chip);
    }

    private void loadLanguagesApi() {
        ApiService apiService = ApiClient.getApiService(getApplicationContext());
        Call<List<Language>> call = apiService.getLanguages();

        call.enqueue(new Callback<List<Language>>() {
            @Override
            public void onResponse(Call<List<Language>> call, Response<List<Language>> response) {
                if (response.isSuccessful()) {
                    List<Language> languages = response.body();

                    if (languages != null && !languages.isEmpty()) {
                        languageObjectList.clear();
                        languageNames.clear();

                        for (Language lang : languages) {
                            languageObjectList.add(lang);
                            languageNames.add(lang.getLanguageName());
                        }

                        runOnUiThread(() -> adapter.notifyDataSetChanged());
                    } else {
                        Log.e("Language Activity", "Empty or null language list from API");
                        Toast.makeText(LanguageActivity.this, "No languages found", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e("Language Activity", "Response unsuccessful: " + response.code());
                    Toast.makeText(LanguageActivity.this, "Failed to load languages", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Language>> call, Throwable t) {
                Log.e("Language Activity", "API call failed: " + t.getMessage());
                Toast.makeText(LanguageActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private List<Integer> getSelectedLanguageIds() {
        List<Integer> ids = new ArrayList<>();
        int count = chipGroupLanguage.getChildCount();
        for (int i = 0; i < count; i++) {
            Chip chip = (Chip) chipGroupLanguage.getChildAt(i);
            Integer id = (Integer) chip.getTag();
            if (id != null) {
                ids.add(id);
            }
        }
        return ids;
    }
}
