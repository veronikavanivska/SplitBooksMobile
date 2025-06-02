package com.example.splitbooks.DTO.request;

import com.google.gson.annotations.SerializedName;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReadingFormat {

    @SerializedName("formatId")
    private int formatId;

    @SerializedName("formatName")
    private String formatName;



}
