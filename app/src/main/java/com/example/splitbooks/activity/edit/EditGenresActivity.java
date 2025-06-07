package com.example.splitbooks.activity.edit;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.splitbooks.DTO.request.EditGenres;
import com.example.splitbooks.DTO.request.Genre;
import com.example.splitbooks.DTO.response.ProfileResponse;
import com.example.splitbooks.activity.profile.PublicProfileActivity;
import com.example.splitbooks.R;
import com.example.splitbooks.activity.home.HomePageActivity;
import com.example.splitbooks.network.ApiClient;
import com.example.splitbooks.network.ApiService;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditGenresActivity extends AppCompatActivity {

    private AutoCompleteTextView autoCompleteGenre;
    private ArrayAdapter<String> adapter;
    private List<Genre> allGenres = new ArrayList<>();
    private ChipGroup chipGroupGenres;
    private Button saveButton;
    private MaterialToolbar back;
    private List<Long> currentUserGenreIds = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_genre);

        autoCompleteGenre = findViewById(R.id.edit_auto_complete_genre);
        chipGroupGenres = findViewById(R.id.chip_group_selected_genre);
        saveButton = findViewById(R.id.next_button);
        back = findViewById(R.id.back_arrow_edit_genre);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line);
        autoCompleteGenre.setAdapter(adapter);

        autoCompleteGenre.setOnItemClickListener((parent, view, position, id) -> {
            String selectedName = adapter.getItem(position);
            Genre selectedGenre = findGenreByName(selectedName);
            if (selectedGenre != null && !isChipAlreadyAdded(Long.valueOf(selectedGenre.getGenreId()))) {
                addGenreChip(selectedGenre);
            }
            autoCompleteGenre.setText("");
        });
        back.setOnClickListener(v-> {
            Intent intent = new Intent(this, PublicProfileActivity.class);
            startActivity(intent);
            finish();
        });

        saveButton.setOnClickListener(v -> {
            List<Long> selectedGenreIds = getSelectedGenreIds();
            if (selectedGenreIds.isEmpty()) {
                Toast.makeText(this, "Please select at least one genre", Toast.LENGTH_SHORT).show();
                return;
            }
            sendPatchGenresRequest(selectedGenreIds);

        });

        loadGenresFromApi();
    }

    private void loadGenresFromApi() {
        ApiService apiService = ApiClient.getApiService(getApplicationContext());
        apiService.getGenres().enqueue(new Callback<List<Genre>>() {
            @Override
            public void onResponse(Call<List<Genre>> call, Response<List<Genre>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allGenres = response.body();
                    List<String> names = new ArrayList<>();
                    for (Genre genre : allGenres) {
                        names.add(genre.getGenreName());
                    }
                    adapter.clear();
                    adapter.addAll(names);
                    adapter.notifyDataSetChanged();
                    loadUserGenres();
                } else {
                    Toast.makeText(EditGenresActivity.this, "Failed to load genres", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Genre>> call, Throwable t) {
                Toast.makeText(EditGenresActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadUserGenres() {
        ApiService apiService = ApiClient.getApiService(getApplicationContext());
        apiService.getProfile().enqueue(new Callback<ProfileResponse>() {
            @Override
            public void onResponse(Call<ProfileResponse> call, Response<ProfileResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<String> genreNames = response.body().getGenreNames();
                    if (genreNames != null) {
                        for (String genreName : genreNames) {
                            Genre genre = findGenreByName(genreName);
                            if (genre != null) {
                                addGenreChip(genre);
                            }
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<ProfileResponse> call, Throwable t) {
                Toast.makeText(EditGenresActivity.this, "Failed to load your genres", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendPatchGenresRequest(List<Long> selectedGenreIds) {
        ApiService apiService = ApiClient.getApiService(getApplicationContext());
        EditGenres request = new EditGenres(selectedGenreIds);

        apiService.editGenres(request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(EditGenresActivity.this, "Genres updated successfully", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(EditGenresActivity.this, HomePageActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(EditGenresActivity.this, "Update failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(EditGenresActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private Genre findGenreByName(String name) {
        for (Genre genre : allGenres) {
            if (genre.getGenreName().equalsIgnoreCase(name)) {
                return genre;
            }
        }
        return null;
    }

    private Genre findGenreById(Long id) {
        for (Genre genre : allGenres) {
            if (genre.getGenreId() == id) {
                return genre;
            }
        }
        return null;
    }
    private boolean isChipAlreadyAdded(Long genreId) {
        for (int i = 0; i < chipGroupGenres.getChildCount(); i++) {
            Chip chip = (Chip) chipGroupGenres.getChildAt(i);
            if (chip.getTag() instanceof Long && ((Long) chip.getTag()).equals(genreId)) return true;
        }
        return false;
    }


    private void addGenreChip(Genre genre) {
        Chip chip = new Chip(this);
        chip.setText(genre.getGenreName());
        chip.setTag(genre.getGenreId());
        chip.setCloseIconVisible(true);
        chip.setClickable(true);
        chip.setCheckable(false);
        chip.setChipBackgroundColor(ColorStateList.valueOf(Color.WHITE));
        chip.setChipStrokeColor(ColorStateList.valueOf(Color.parseColor("#006400")));
        chip.setChipStrokeWidth(2f);
        chip.setCloseIconTint(ColorStateList.valueOf(Color.parseColor("#4CAF50")));
        chip.setOnCloseIconClickListener(v -> chipGroupGenres.removeView(chip));

        chipGroupGenres.addView(chip);
    }

    private List<Long> getSelectedGenreIds() {
        List<Long> ids = new ArrayList<>();
        for (int i = 0; i < chipGroupGenres.getChildCount(); i++) {
            Chip chip = (Chip) chipGroupGenres.getChildAt(i);
            Object tagObj = chip.getTag();
            if (tagObj instanceof Number) {
                long id = ((Number) tagObj).longValue();
                ids.add(id);
            }
        }
        return ids;
    }
}

