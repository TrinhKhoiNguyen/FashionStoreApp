package com.example.fashionstoreapp.utils;

import com.example.fashionstoreapp.api.AddressApiService;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * RetrofitClient - Singleton class để quản lý Retrofit instance
 * 
 * HƯỚNG DẪN SỬ DỤNG:
 * - Thay đổi BASE_URL bằng URL API thực tế của bạn
 * - Ví dụ: "https://provinces.open-api.vn/api/" hoặc API khác
 */
public class RetrofitClient {

    // API miễn phí cho địa chỉ Việt Nam (provinces.open-api.vn)
    // Đơn giản, không cần API key, trả về trực tiếp array
    private static final String BASE_URL = "https://provinces.open-api.vn/api/";

    // Nếu muốn đổi sang API khác, xem file API_CUSTOMIZATION_GUIDE.md

    private static RetrofitClient instance;
    private final Retrofit retrofit;
    private final AddressApiService addressApiService;

    private RetrofitClient() {
        // Logging interceptor để debug API calls
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        // OkHttpClient với timeout
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();

        // Retrofit instance
        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        // Tạo service
        addressApiService = retrofit.create(AddressApiService.class);
    }

    /**
     * Lấy singleton instance
     */
    public static synchronized RetrofitClient getInstance() {
        if (instance == null) {
            instance = new RetrofitClient();
        }
        return instance;
    }

    /**
     * Lấy AddressApiService để gọi API
     */
    public AddressApiService getAddressApiService() {
        return addressApiService;
    }
}
