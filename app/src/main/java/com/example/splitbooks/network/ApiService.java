package com.example.splitbooks.network;


import com.example.splitbooks.DTO.request.BooksSearchRequest;
import com.example.splitbooks.DTO.request.CreatePrivateChatRequest;
import com.example.splitbooks.DTO.request.EditGenres;
import com.example.splitbooks.DTO.request.EditReadingPreferences;
import com.example.splitbooks.DTO.request.Genre;
import com.example.splitbooks.DTO.request.Language;
import com.example.splitbooks.DTO.request.LoginRequest;
import com.example.splitbooks.DTO.request.ReviewRequest;
import com.example.splitbooks.DTO.response.BookResponse;
import com.example.splitbooks.DTO.response.BookWithReviewsResponse;
import com.example.splitbooks.DTO.response.ChatResponse;
import com.example.splitbooks.DTO.response.LoginResponse;
import com.example.splitbooks.DTO.response.PageResponse;
import com.example.splitbooks.DTO.response.ProfileResponse;
import com.example.splitbooks.DTO.response.ProfileSetupResponse;
import com.example.splitbooks.DTO.request.ReadingFormat;
import com.example.splitbooks.DTO.request.RegistrationRequest;
import com.example.splitbooks.DTO.response.RegistrationResponse;
import com.example.splitbooks.DTO.response.ReviewResponse;
import com.example.splitbooks.DTO.response.SendMessageResponse;
import com.example.splitbooks.DTO.response.ShortChatResponse;
import com.example.splitbooks.DTO.response.ShortProfileResponse;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {
    @POST("api/auth/login")
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

    @GET("/api/follow/followers/{profileId}")
    Call<List<ShortProfileResponse>> getFollowers(@Path("profileId") Long profileId);

    @GET("/api/follow/following/{profileId}")
    Call<List<ShortProfileResponse>> getFollowing(@Path("profileId") Long profileId);

    @GET("/api/profile/{profileId}")
    Call<ProfileResponse> getProfileById(@Path("profileId") Long profileId);

    @GET("/api/follow/is-following/{targetProfileId}")
    Call<Boolean> isFollowing(@Path("targetProfileId") Long targetProfileId);

    @POST("/api/follow/{targetProfileId}")
    Call<ResponseBody> follow(@Path("targetProfileId") Long targetProfileId);

    @DELETE("/api/follow/unfollow/{targetProfileId}")
    Call<ResponseBody> unfollow(@Path("targetProfileId") Long targetProfileId);

    @GET("/api/follow/search")
    Call<List<ShortProfileResponse>> searchProfiles(@Query("username") String username);

    @GET("/api/suggestions/best")
    Call<List<ShortProfileResponse>> getBestFriendSuggestions();


    @POST("/api/books/search")
    Call<BookResponse> searchBooks(
            @Body BooksSearchRequest request,
            @Query("startIndex") int startIndex,
            @Query("maxResults") int maxResults
    );
    @GET("/api/books/{volumeId}")
    Call<BookWithReviewsResponse> getBookById(@Path("volumeId") String volumeId);
    @POST("/api/books/{volumeId}/addReview")
    Call<ReviewResponse> addReview(
            @Path("volumeId") String volumeId,
            @Body ReviewRequest request
    );
    @DELETE("/api/books/removeReview/{reviewId}")
    Call<Void> deleteReview( @Path("reviewId") Long reviewId);

    @DELETE("/api/books/{volumeId}/remove")
    Call<ResponseBody> removeBookFromLibrary(@Path("volumeId") String volumeId);

    @POST("/api/books/{volumeId}/add")
    Call<ResponseBody>  addBookToLibrary(@Path("volumeId") String volumeId);

    @GET("/api/books/mybooks/{profileId}")
    Call<BookResponse> getBooksByProfileId(@Path("profileId") Long profileId);

    @GET("/api/chat/allChats")
    Call<PageResponse<ShortChatResponse>> getAllChats(@Query("page") int page, @Query("size") int size);

    @GET("/api/chat/{chatId}/messages")
    Call<PageResponse<SendMessageResponse>> getChatMessages(
            @Path("chatId") Long chatId,
            @Query("page") int page,
            @Query("size") int size,
            @Query("sort") String sort
    );

    @Multipart
    @POST("/api/chat/group")
    Call<ChatResponse> createGroupChat(
            @Part("data") RequestBody data,
            @Part MultipartBody.Part file
    );

    @POST("/api/chat/private")
    Call<ChatResponse> createPrivateChat(
            @Body CreatePrivateChatRequest request);

    @GET("/api/chat/{chatId}")
    Call<ChatResponse> getChatInfo(@Path("chatId") Long chatId);

    @DELETE("/api/chat/{chatId}")
    Call<Void> deleteChat(@Path("chatId") Long chatId);

    @PATCH("/api/chat/{chatId}")
    Call<Void>  updateChat(
            @Path("chatId") Long chatId,
            @Part("name") RequestBody name,
            @Part MultipartBody.Part file
    );


}