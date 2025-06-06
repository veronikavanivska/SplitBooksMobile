package com.example.splitbooks.DTO.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProfileResponse {
    private Long id;
    private String username;
    private String firstName;
    private String lastName;
    private String phone;
    private int followers;
    private int following;
    private List<String> genreNames;
    private List<String> formatNames;
    private List<String> languageNames;
    private String avatarUrl;
    private boolean hasAnonymous ;
    private boolean setupCompleted;
    private boolean anonymous;


}
