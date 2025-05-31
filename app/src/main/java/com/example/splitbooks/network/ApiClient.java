package com.example.splitbooks.network;

import android.content.Context;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    //private static final String BASE_URL = "http://10.0.2.2:8888/";  // Replace with your actual base URL
    private static final String BASE_URL = "http://172.20.10.3:8888/";
    private static Retrofit retrofit;

    public static Retrofit getRetrofitInstance(Context context) {
        if (retrofit == null) {
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(chain -> {
                        String token = JwtManager.getToken(context);
                        Request.Builder builder = chain.request().newBuilder();
                        if (token != null && !token.isEmpty()) {
                            builder.header("Authorization", "Bearer " + token);
                        }
                        return chain.proceed(builder.build());
                    })
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    public static ApiService getApiService(Context context) {
        return getRetrofitInstance(context).create(ApiService.class);
    }
}