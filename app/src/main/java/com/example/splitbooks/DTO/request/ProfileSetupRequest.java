package com.example.splitbooks.DTO.request;


import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProfileSetupRequest {

    private List<Long> selectedGenres;
    private List<Long> preferredFormat;
    private List<Long> preferredLanguages;
    private String phone;
    private String firstName;
    private String lastName;
    private String anonimousUsername;



    public ProfileSetupRequest(String firstName, String lastName, String phoneNumber,
                               List<Long> genreIds, List<Long> languageIds, List<Long> formatIds) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phoneNumber;
        this.selectedGenres = genreIds;
        this.preferredLanguages = languageIds;
        this.preferredFormat = formatIds;
    }

    public ProfileSetupRequest(String anonimousUsername,
                               List<Long> genreIds, List<Long> languageIds, List<Long> formatIds) {
        this.anonimousUsername = anonimousUsername;
        this.selectedGenres = genreIds;
        this.preferredLanguages = languageIds;
        this.preferredFormat = formatIds;
    }
}

