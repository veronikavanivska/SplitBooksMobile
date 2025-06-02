package com.example.splitbooks.network;


import com.example.splitbooks.DTO.request.EditGenres;
import com.example.splitbooks.DTO.request.EditReadingPreferences;
import com.example.splitbooks.DTO.request.Genre;
import com.example.splitbooks.DTO.request.Language;
import com.example.splitbooks.DTO.request.LoginRequest;
import com.example.splitbooks.DTO.response.LoginResponse;
import com.example.splitbooks.DTO.response.ProfileResponse;
import com.example.splitbooks.DTO.response.ProfileSetupResponse;
import com.example.splitbooks.DTO.request.ReadingFormat;
import com.example.splitbooks.DTO.request.RegistrationRequest;
import com.example.splitbooks.DTO.response.RegistrationResponse;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface ApiService {
    @POST("api/auth/login")  // Assuming the endpoint is "/login"
    Call<LoginResponse> login(@Body LoginRequest loginRequest);

    @POST("api/auth/register")
    Call<RegistrationResponse> register(@Body RegistrationRequest registrationRequest);

    @GET("api/profile-setup/genres")
    Call<List<Genre>> getGenres();

    @GET("api/profile-setup/languages")
    Call<List<Language>> getLanguages();

    @GET("api/profile-setup/reading-format")
    Call<List<ReadingFormat>> getReadingFormats();

    @Multipart
    @POST("/api/profile/setup")
    Call<ProfileSetupResponse> setupProfile(

            @Part("data") RequestBody data,
            @Part MultipartBody.Part avatar
    );

    @GET("/api/profile/me")
    Call<ProfileResponse> getProfile();
    @Multipart
    @PATCH("/api/profile/edit")
    Call<ResponseBody> editProfile(@Part("data") RequestBody data,
                                   @Part MultipartBody.Part avatar) ;
    @PATCH("/api/profile/edit/genres")
    Call<Void> editGenres(@Body EditGenres request);

    @PATCH("/api/profile/edit/preferences")
    Call<Void> editReadingPreferences(@Body EditReadingPreferences request);

    @POST("/api/profile/anonymous")
    Call<Void> createAnonymousProfile();

    @PATCH("/api/profile/toggle")
    Call<ResponseBody> toggleProfile();


}