package com.example.fashionstoreapp.services;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.fashionstoreapp.BuildConfig;
import com.example.fashionstoreapp.models.GeminiRequest;
import com.example.fashionstoreapp.models.GeminiResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class GeminiAIService {
    private static final String TAG = "GeminiAIService";
    private static final String BASE_URL = "https://generativelanguage.googleapis.com/";
    private static final String PREFS_NAME = "GeminiPrefs";
    private static final String KEY_REQUEST_COUNT = "request_count";
    private static final String KEY_LAST_RESET = "last_reset";
    private static final int MAX_REQUESTS_PER_MINUTE = 15;
    private static final int RETRY_MAX_ATTEMPTS = 3;

    private GeminiApiInterface apiInterface;
    private SharedPreferences prefs;
    private Map<String, String> responseCache;

    // System prompt cho assistant bán quần áo
    private static final String SYSTEM_PROMPT =
            "Bạn là trợ lý ảo thông minh của cửa hàng thời trang FashionStore. " +
                    "Nhiệm vụ của bạn là tư vấn quần áo, giúp khách hàng chọn size, " +
                    "phối đồ, và trả lời các câu hỏi về sản phẩm. " +
                    "Hãy thân thiện, nhiệt tình và chuyên nghiệp. " +
                    "Nếu khách hỏi về giá hoặc tồn kho, hãy đề xuất họ liên hệ nhân viên để biết thông tin chính xác nhất.";

    public GeminiAIService(Context context) {
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.responseCache = new HashMap<>();

        // Setup OkHttp với logging
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();

        // Setup Retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiInterface = retrofit.create(GeminiApiInterface.class);
    }

    // Kiểm tra rate limit
    private boolean checkRateLimit() {
        long currentTime = System.currentTimeMillis();
        long lastReset = prefs.getLong(KEY_LAST_RESET, 0);
        int requestCount = prefs.getInt(KEY_REQUEST_COUNT, 0);

        // Reset sau 1 phút
        if (currentTime - lastReset > 60000) {
            prefs.edit()
                    .putLong(KEY_LAST_RESET, currentTime)
                    .putInt(KEY_REQUEST_COUNT, 0)
                    .apply();
            requestCount = 0;
        }

        if (requestCount >= MAX_REQUESTS_PER_MINUTE) {
            Log.w(TAG, "Rate limit exceeded");
            return false;
        }

        prefs.edit()
                .putInt(KEY_REQUEST_COUNT, requestCount + 1)
                .apply();

        return true;
    }

    // Gửi message với retry logic
    public void sendMessage(String userMessage, List<String> conversationHistory, AIResponseCallback callback) {
        if (!checkRateLimit()) {
            callback.onError("Vui lòng đợi một chút trước khi gửi tin nhắn tiếp theo.");
            return;
        }

        // Kiểm tra cache
        String cacheKey = generateCacheKey(userMessage, conversationHistory);
        if (responseCache.containsKey(cacheKey)) {
            Log.d(TAG, "Returning cached response");
            callback.onSuccess(responseCache.get(cacheKey));
            return;
        }

        sendMessageWithRetry(userMessage, conversationHistory, callback, 0);
    }

    private void sendMessageWithRetry(String userMessage, List<String> conversationHistory,
                                      AIResponseCallback callback, int attemptCount) {
        // Build request với context
        List<GeminiRequest.Part> parts = new ArrayList<>();

        // Thêm system prompt
        parts.add(new GeminiRequest.Part(SYSTEM_PROMPT));

        // Thêm conversation history (giới hạn 10 tin nhắn gần nhất)
        if (conversationHistory != null && !conversationHistory.isEmpty()) {
            int startIndex = Math.max(0, conversationHistory.size() - 10);
            for (int i = startIndex; i < conversationHistory.size(); i++) {
                parts.add(new GeminiRequest.Part(conversationHistory.get(i)));
            }
        }

        // Thêm message hiện tại
        parts.add(new GeminiRequest.Part(userMessage));

        GeminiRequest.Content content = new GeminiRequest.Content(parts);
        List<GeminiRequest.Content> contents = new ArrayList<>();
        contents.add(content);

        GeminiRequest request = new GeminiRequest(contents);

        // Gọi API
        Call<GeminiResponse> call = apiInterface.generateContent(request, BuildConfig.GEMINI_API_KEY);

        call.enqueue(new Callback<GeminiResponse>() {
            @Override
            public void onResponse(Call<GeminiResponse> call, Response<GeminiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String aiResponse = extractResponse(response.body());

                        // Lưu vào cache
                        String cacheKey = generateCacheKey(userMessage, conversationHistory);
                        responseCache.put(cacheKey, aiResponse);

                        callback.onSuccess(aiResponse);
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing response", e);
                        callback.onError("Lỗi xử lý phản hồi từ AI.");
                    }
                } else {
                    // Retry nếu failed
                    if (attemptCount < RETRY_MAX_ATTEMPTS) {
                        Log.w(TAG, "Retrying... Attempt " + (attemptCount + 1));
                        try {
                            Thread.sleep(1000 * (attemptCount + 1)); // Exponential backoff
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        sendMessageWithRetry(userMessage, conversationHistory, callback, attemptCount + 1);
                    } else {
                        String errorMsg = "Lỗi: " + response.code();
                        try {
                            if (response.errorBody() != null) {
                                errorMsg += " - " + response.errorBody().string();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        Log.e(TAG, errorMsg);
                        callback.onError("Không thể kết nối với AI. Vui lòng thử lại sau.");
                    }
                }
            }

            @Override
            public void onFailure(Call<GeminiResponse> call, Throwable t) {
                Log.e(TAG, "API call failed", t);

                if (attemptCount < RETRY_MAX_ATTEMPTS) {
                    Log.w(TAG, "Retrying after failure... Attempt " + (attemptCount + 1));
                    try {
                        Thread.sleep(1000 * (attemptCount + 1));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    sendMessageWithRetry(userMessage, conversationHistory, callback, attemptCount + 1);
                } else {
                    callback.onError("Lỗi kết nối: " + t.getMessage());
                }
            }
        });
    }

    private String extractResponse(GeminiResponse response) {
        if (response.getCandidates() != null && !response.getCandidates().isEmpty()) {
            GeminiResponse.Candidate candidate = response.getCandidates().get(0);
            if (candidate.getContent() != null &&
                    candidate.getContent().getParts() != null &&
                    !candidate.getContent().getParts().isEmpty()) {
                return candidate.getContent().getParts().get(0).getText();
            }
        }
        return "Xin lỗi, tôi không thể trả lời lúc này.";
    }

    private String generateCacheKey(String message, List<String> history) {
        StringBuilder key = new StringBuilder(message);
        if (history != null && !history.isEmpty()) {
            key.append("_");
            key.append(history.get(history.size() - 1));
        }
        return String.valueOf(key.toString().hashCode());
    }

    public void clearCache() {
        responseCache.clear();
    }

    // Callback interface
    public interface AIResponseCallback {
        void onSuccess(String response);
        void onError(String error);
    }
}