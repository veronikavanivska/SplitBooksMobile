package com.example.splitbooks.DTO.request;

import com.google.gson.annotations.SerializedName;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Genre {
    @SerializedName("genreId")
    private int genreId;

    @SerializedName("genreName")
    private String genreName;


}