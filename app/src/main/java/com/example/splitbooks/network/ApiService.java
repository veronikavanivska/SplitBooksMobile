package com.example.splitbooks.network;


import com.example.splitbooks.DTO.LoginRequest;
import com.example.splitbooks.DTO.LoginResponse;
import com.example.splitbooks.DTO.RegistrationRequest;
import com.example.splitbooks.DTO.RegistrationResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {
    @POST("api/auth/login")  // Assuming the endpoint is "/login"
    Call<LoginResponse> login(@Body LoginRequest loginRequest);

    @POST("api/auth/register")
    Call<RegistrationResponse> register(@Body RegistrationRequest registrationRequest);
}