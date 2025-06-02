package com.example.splitbooks.DTO.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EditProfileRequest {
    private String firstName;
    private String lastName;
    private String phone;
    private String username;

    public EditProfileRequest(String username){
        this.username = username;
    }

    public EditProfileRequest(String firstName,String lastName, String phone){
        this.firstName =firstName;
        this.lastName = lastName;
        this.phone = phone;
    }
}
