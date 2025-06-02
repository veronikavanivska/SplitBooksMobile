package com.example.splitbooks.DTO.response;

import java.util.List;

public class ProfileResponse {
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

    public boolean isAnonymous() {
        return anonymous;
    }
    public boolean isSetupCompleted(){
        return setupCompleted;
    }
    public boolean isHasAnonymous(){
        return hasAnonymous;
    }

    public String getUsername() {
        return username;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getPhone() {
        return phone;
    }

    public int getFollowers() {
        return followers;
    }

    public int getFollowing() {
        return following;
    }

    public List<String> getGenreNames() {
        return genreNames;
    }

    public List<String> getFormatNames() {
        return formatNames;
    }

    public List<String> getLanguageNames() {
        return languageNames;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

}
