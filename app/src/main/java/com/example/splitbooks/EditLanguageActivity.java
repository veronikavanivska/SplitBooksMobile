package com.example.splitbooks;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.splitbooks.DTO.request.Language;
import com.example.splitbooks.DTO.response.ProfileResponse;
import com.example.splitbooks.network.ApiClient;
import com.example.splitbooks.network.ApiService;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditLanguageActivity extends AppCompatActivity {

    private AutoCompleteTextView autoCompleteLanguage;
    private ArrayAdapter<String> adapter;
    private List<Language> allLanguages = new ArrayList<>();
    private ChipGroup chipGroupLanguage;
    private Button nextButton,  backButton ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_language);

        autoCompleteLanguage = findViewById(R.id.auto_complete_language);
        chipGroupLanguage = findViewById(R.id.chip_group_selected_language);
        backButton = findViewById(R.id.back_button_edit_languages);
        nextButton = findViewById(R.id.next_button);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line);
        autoCompleteLanguage.setAdapter(adapter);

        autoCompleteLanguage.setOnItemClickListener((parent, view, position, id) -> {
            String selectedName = adapter.getItem(position);
            Language selectedLang = findLanguageByName(selectedName);
            if (selectedLang != null && !isChipAlreadyAdded(selectedLang.getLanguageId())) {
                addLanguageChip(selectedLang);
            }
            autoCompleteLanguage.setText("");
        });


        backButton.setOnClickListener(v-> {
            Intent intent = new Intent(this, PublicProfileActivity.class);
            startActivity(intent);
            finish();
        });

        nextButton.setOnClickListener(v -> {
            List<Integer> selectedIds = getSelectedLanguageIds();
            if (selectedIds.isEmpty()) {
                Toast.makeText(this, "Please select at least one language", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(this, EditReadingFormatActivity.class);
            intent.putIntegerArrayListExtra("selectedLanguageIds", new ArrayList<>(selectedIds));
            startActivity(intent);
            finish();
        });

        loadLanguagesFromApi();
    }

    private void loadLanguagesFromApi() {
        ApiService apiService = ApiClient.getApiService(getApplicationContext());
        apiService.getLanguages().enqueue(new Callback<List<Language>>() {
            @Override
            public void onResponse(Call<List<Language>> call, Response<List<Language>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allLanguages = response.body();
                    List<String> names = new ArrayList<>();
                    for (Language lang : allLanguages) {
                        names.add(lang.getLanguageName());
                    }
                    adapter.clear();
                    adapter.addAll(names);
                    adapter.notifyDataSetChanged();
                    loadUserLanguages();
                } else {
                    Toast.makeText(EditLanguageActivity.this, "Failed to load languages", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Language>> call, Throwable t) {
                Toast.makeText(EditLanguageActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadUserLanguages() {
        ApiService apiService = ApiClient.getApiService(getApplicationContext());
        apiService.getProfile().enqueue(new Callback<ProfileResponse>() {
            @Override
            public void onResponse(Call<ProfileResponse> call, Response<ProfileResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<String> userLangNames = response.body().getLanguageNames();
                    if (userLangNames != null) {
                        for (String name : userLangNames) {
                            Language lang = findLanguageByName(name);
                            if (lang != null) {
                                addLanguageChip(lang);
                            }
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<ProfileResponse> call, Throwable t) {
                Toast.makeText(EditLanguageActivity.this, "Failed to load your languages", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private Language findLanguageByName(String name) {
        for (Language lang : allLanguages) {
            if (lang.getLanguageName().equalsIgnoreCase(name)) {
                return lang;
            }
        }
        return null;
    }

    private boolean isChipAlreadyAdded(int languageId) {
        for (int i = 0; i < chipGroupLanguage.getChildCount(); i++) {
            Chip chip = (Chip) chipGroupLanguage.getChildAt(i);
            if (chip.getTag() instanceof Integer && ((Integer) chip.getTag()) == languageId) return true;
        }
        return false;
    }

    private void addLanguageChip(Language language) {
        Chip chip = new Chip(this);
        chip.setText(language.getLanguageName());
        chip.setTag(language.getLanguageId());
        chip.setCloseIconVisible(true);
        chip.setChipBackgroundColor(ColorStateList.valueOf(Color.WHITE));
        chip.setChipStrokeColor(ColorStateList.valueOf(Color.parseColor("#006400")));
        chip.setChipStrokeWidth(2f);
        chip.setCloseIconTint(ColorStateList.valueOf(Color.parseColor("#4CAF50")));
        chip.setOnCloseIconClickListener(v -> chipGroupLanguage.removeView(chip));
        chipGroupLanguage.addView(chip);
    }

    private List<Integer> getSelectedLanguageIds() {
        List<Integer> ids = new ArrayList<>();
        for (int i = 0; i < chipGroupLanguage.getChildCount(); i++) {
            Chip chip = (Chip) chipGroupLanguage.getChildAt(i);
            Object tagObj = chip.getTag();
            if (tagObj instanceof Integer) {
                ids.add((Integer) tagObj);
            }
        }
        return ids;
    }
}
