package com.example.splitbooks.DTO;

import androidx.lifecycle.ViewModel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class ProfileSetupRequest extends ViewModel {
    private List<Long> selectedGenreIds = new ArrayList<>();

    public List<Long> getSelectedGenreIds() {
        return selectedGenreIds;
    }

    public void addGenreId(Long genreId) {
        if (!selectedGenreIds.contains(genreId)) {
            selectedGenreIds.add(genreId);
        }
    }

    public void removeGenreId(Long genreId) {
        selectedGenreIds.remove(genreId);
    }
}
