package com.example.splitbooks.DTO;

import com.google.gson.annotations.SerializedName;

public class Language {
    @SerializedName("languageId")
    private int languageId;

    @SerializedName("languageName")
    private String languageName;

    public int getLanguageId() {
        return languageId;
    }

    public String getLanguageName() {
        return languageName;
    }
}
