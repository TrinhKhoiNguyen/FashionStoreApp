package com.example.fashionstoreapp.api;

import com.example.fashionstoreapp.model.District;
import com.example.fashionstoreapp.model.Province;
import com.example.fashionstoreapp.model.Ward;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * API Service interface để gọi API lấy danh sách Tỉnh/Quận/Phường
 * Bạn sẽ thay đổi URL cơ bản (Base URL) trong RetrofitClient sau
 */
public interface AddressApiService {

    /**
     * Lấy danh sách tất cả Tỉnh/Thành phố
     * API: https://provinces.open-api.vn/api/p/
     * 
     * @return Danh sách Province
     */
    @GET("p/")
    Call<List<Province>> getProvinces();

    /**
     * Lấy danh sách Quận/Huyện theo Tỉnh
     * API: https://provinces.open-api.vn/api/p/{code}?depth=2
     * 
     * @param provinceCode Mã của Tỉnh
     * @param depth        Độ sâu dữ liệu (2 = có districts)
     * @return Danh sách District
     */
    @GET("p/{code}")
    Call<Province> getProvinceWithDistricts(@retrofit2.http.Path("code") int provinceCode, @Query("depth") int depth);

    /**
     * Lấy danh sách Phường/Xã theo Quận
     * API: https://provinces.open-api.vn/api/d/{code}?depth=2
     * 
     * @param districtCode Mã của Quận
     * @param depth        Độ sâu dữ liệu (2 = có wards)
     * @return Danh sách Ward
     */
    @GET("d/{code}")
    Call<District> getDistrictWithWards(@retrofit2.http.Path("code") int districtCode, @Query("depth") int depth);
}
