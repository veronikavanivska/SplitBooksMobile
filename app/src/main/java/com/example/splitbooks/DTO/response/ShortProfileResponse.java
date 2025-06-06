package com.example.splitbooks.DTO.response;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShortProfileResponse {
    private Long id;
    private String username;
    private String avatarUrl;


}