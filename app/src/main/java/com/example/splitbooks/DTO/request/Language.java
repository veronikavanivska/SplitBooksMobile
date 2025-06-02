package com.example.splitbooks.DTO.request;

import com.google.gson.annotations.SerializedName;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Language {
    @SerializedName("languageId")
    private int languageId;

    @SerializedName("languageName")
    private String languageName;


}
