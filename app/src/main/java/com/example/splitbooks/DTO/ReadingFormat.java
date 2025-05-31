package com.example.splitbooks.DTO;

import com.google.gson.annotations.SerializedName;

public class ReadingFormat {

    @SerializedName("formatId")
    private int formatId;

    @SerializedName("formatName")
    private String formatName;


    public int getFormatId() {
        return formatId;
    }

    public String getFormatName() {
        return formatName;
    }

}
