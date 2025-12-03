package com.example.fashionstoreapp.services;

import com.example.fashionstoreapp.models.GeminiRequest;
import com.example.fashionstoreapp.models.GeminiResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface GeminiApiInterface {

    // Sử dụng model gemini-2.0-flash-exp
    @POST("v1beta/models/gemini-2.0-flash-exp:generateContent")
    Call<GeminiResponse> generateContent(
            @Body GeminiRequest request,
            @Query("key") String apiKey
    );
}