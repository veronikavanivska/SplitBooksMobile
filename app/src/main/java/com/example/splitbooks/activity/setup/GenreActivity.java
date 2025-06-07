package com.example.splitbooks.activity.setup;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.splitbooks.R;
import com.example.splitbooks.network.ApiClient;
import com.example.splitbooks.network.ApiService;
import com.example.splitbooks.DTO.request.Genre;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GenreActivity extends AppCompatActivity {

    private AutoCompleteTextView autoCompleteGenre;
    private ArrayAdapter<String> adapter;
    private List<Genre> allGenres = new ArrayList<>();
    private ChipGroup chipGroupGenres;
    private Button next , back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_genre);


        autoCompleteGenre = findViewById(R.id.auto_complete_genre);
        chipGroupGenres = findViewById(R.id.chip_group_selected_genre);
        next = findViewById(R.id.next_button);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line);
        autoCompleteGenre.setAdapter(adapter);


        autoCompleteGenre.setOnItemClickListener((parent, view, position, id) -> {
            String selectedName = adapter.getItem(position);
            Genre selectedGenre = findGenreByName(selectedName);
            if (selectedGenre != null && !isChipAlreadyAdded(selectedGenre.getGenreId())) {
                addGenreChip(selectedGenre);
            }
            autoCompleteGenre.setText("");
        });


        next.setOnClickListener(v -> {
            List<Integer> selectedGenreIds = getSelectedGenreIds();
            if (selectedGenreIds.isEmpty()) {
                Toast.makeText(this, "Please select at least one genre", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(GenreActivity.this, LanguageActivity.class);
            intent.putIntegerArrayListExtra("selectedGenreIds", new ArrayList<>(selectedGenreIds));
            intent.putExtras(getIntent());
            startActivity(intent);
            finish();
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

                } else {
                    Toast.makeText(GenreActivity.this, "Failed to load genres", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Genre>> call, Throwable t) {
                Toast.makeText(GenreActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
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

    private boolean isChipAlreadyAdded(int genreId) {
        for (int i = 0; i < chipGroupGenres.getChildCount(); i++) {
            Chip chip = (Chip) chipGroupGenres.getChildAt(i);
            if ((int) chip.getTag() == genreId) return true;
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

    private List<Integer> getSelectedGenreIds() {
        List<Integer> ids = new ArrayList<>();
        for (int i = 0; i < chipGroupGenres.getChildCount(); i++) {
            Chip chip = (Chip) chipGroupGenres.getChildAt(i);
            ids.add((Integer) chip.getTag());
        }
        return ids;
    }
}

