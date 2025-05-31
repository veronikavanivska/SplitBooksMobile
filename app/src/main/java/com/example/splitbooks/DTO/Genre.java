package com.example.splitbooks.DTO;

import com.google.gson.annotations.SerializedName;

public class Genre {
    @SerializedName("genreId")
    private int genreId;

    @SerializedName("genreName")
    private String genreName;

    public int getGenreId() {
        return genreId;
    }

    public String getGenreName() {
        return genreName;
    }
}