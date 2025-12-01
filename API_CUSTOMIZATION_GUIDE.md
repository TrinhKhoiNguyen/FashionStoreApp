# 🔧 Hướng Dẫn Thay Đổi API cho Chức Năng Địa Chỉ

## 📍 Vị Trí File Cần Sửa

```
app/src/main/java/com/example/fashionstoreapp/utils/RetrofitClient.java
```

## 🌐 API Đã Cấu Hình Sẵn

### provinces.open-api.vn (Mặc định)

```java
private static final String BASE_URL = "https://provinces.open-api.vn/api/";
```

**Endpoints:**
- GET `/p/` - Lấy tất cả tỉnh
- GET `/p/{province_code}/?depth=2` - Lấy quận theo tỉnh
- GET `/d/{district_code}/?depth=2` - Lấy phường theo quận

**Lưu ý:** API này có cấu trúc khác, cần điều chỉnh:

### 1. Cập nhật AddressApiService.java

```java
@GET("p/")
Call<List<Province>> getProvinces();

@GET("p/{code}")
Call<ProvinceDetail> getProvinceDetail(@Path("code") String provinceCode, @Query("depth") int depth);

@GET("d/{code}")
Call<DistrictDetail> getDistrictDetail(@Path("code") String districtCode, @Query("depth") int depth);
```

## 🔄 API Thay Thế Khuyên Dùng

### Option 1: vnappmob.com (Đơn giản hơn)

#### Bước 1: Thay BASE_URL
```java
// File: RetrofitClient.java, dòng 23
private static final String BASE_URL = "https://vapi.vnappmob.com/api/";
```

#### Bước 2: Cập nhật AddressApiService.java
```java
public interface AddressApiService {
    
    @GET("province/")
    Call<ProvinceResponse> getProvinces();

    @GET("province/district/{province_id}")
    Call<DistrictResponse> getDistricts(@Path("province_id") String provinceId);

    @GET("province/ward/{district_id}")
    Call<WardResponse> getWards(@Path("district_id") String districtId);
}
```

#### Bước 3: Tạo Response Wrapper Classes

**File mới: `model/ProvinceResponse.java`**
```java
package com.example.fashionstoreapp.model;

import java.util.List;

public class ProvinceResponse {
    private List<Province> results;

    public List<Province> getResults() {
        return results;
    }

    public void setResults(List<Province> results) {
        this.results = results;
    }
}
```

**File mới: `model/DistrictResponse.java`**
```java
package com.example.fashionstoreapp.model;

import java.util.List;

public class DistrictResponse {
    private List<District> results;

    public List<District> getResults() {
        return results;
    }

    public void setResults(List<District> results) {
        this.results = results;
    }
}
```

**File mới: `model/WardResponse.java`**
```java
package com.example.fashionstoreapp.model;

import java.util.List;

public class WardResponse {
    private List<Ward> results;

    public List<Ward> getResults() {
        return results;
    }

    public void setResults(List<Ward> results) {
        this.results = results;
    }
}
```

#### Bước 4: Cập nhật Province.java
```java
package com.example.fashionstoreapp.model;

import com.google.gson.annotations.SerializedName;

public class Province {
    @SerializedName("province_id")
    private String provinceId;  // Đổi từ int sang String

    @SerializedName("province_name")
    private String provinceName;

    @SerializedName("province_type")
    private String provinceType;

    public Province() {
    }

    public int getProvinceId() {
        return Integer.parseInt(provinceId);  // Parse về int
    }

    public void setProvinceId(String provinceId) {
        this.provinceId = provinceId;
    }

    public String getProvinceName() {
        return provinceName;
    }

    public void setProvinceName(String provinceName) {
        this.provinceName = provinceName;
    }

    @Override
    public String toString() {
        return provinceName;
    }
}
```

#### Bước 5: Cập nhật District.java
```java
package com.example.fashionstoreapp.model;

import com.google.gson.annotations.SerializedName;

public class District {
    @SerializedName("district_id")
    private String districtId;  // Đổi từ int sang String

    @SerializedName("district_name")
    private String districtName;

    @SerializedName("district_type")
    private String districtType;

    @SerializedName("province_id")
    private String provinceId;

    public District() {
    }

    public int getDistrictId() {
        return Integer.parseInt(districtId);
    }

    public void setDistrictId(String districtId) {
        this.districtId = districtId;
    }

    public String getDistrictName() {
        return districtName;
    }

    public void setDistrictName(String districtName) {
        this.districtName = districtName;
    }

    public int getProvinceId() {
        return Integer.parseInt(provinceId);
    }

    public void setProvinceId(String provinceId) {
        this.provinceId = provinceId;
    }

    @Override
    public String toString() {
        return districtName;
    }
}
```

#### Bước 6: Cập nhật Ward.java
```java
package com.example.fashionstoreapp.model;

import com.google.gson.annotations.SerializedName;

public class Ward {
    @SerializedName("ward_id")
    private String wardId;

    @SerializedName("ward_name")
    private String wardName;

    @SerializedName("ward_type")
    private String wardType;

    @SerializedName("district_id")
    private String districtId;

    public Ward() {
    }

    // Giữ nguyên wardCode cho tương thích
    public String getWardCode() {
        return wardId;
    }

    public void setWardCode(String wardCode) {
        this.wardId = wardCode;
    }

    public String getWardName() {
        return wardName;
    }

    public void setWardName(String wardName) {
        this.wardName = wardName;
    }

    public int getDistrictId() {
        return Integer.parseInt(districtId);
    }

    public void setDistrictId(String districtId) {
        this.districtId = districtId;
    }

    @Override
    public String toString() {
        return wardName;
    }
}
```

#### Bước 7: Cập nhật AddressPaymentActivity.java

Tìm và thay thế các hàm load API:

**loadProvinces():**
```java
private void loadProvinces() {
    addressApiService.getProvinces().enqueue(new Callback<ProvinceResponse>() {
        @Override
        public void onResponse(Call<ProvinceResponse> call, Response<ProvinceResponse> response) {
            if (response.isSuccessful() && response.body() != null) {
                provinceList = response.body().getResults();  // Lấy từ results
                List<String> provinceNames = new ArrayList<>();
                provinceNames.add("Chọn Tỉnh/Thành phố");
                for (Province province : provinceList) {
                    provinceNames.add(province.getProvinceName());
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        AddressPaymentActivity.this,
                        android.R.layout.simple_spinner_item,
                        provinceNames
                );
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerProvince.setAdapter(adapter);
            } else {
                Log.e(TAG, "Failed to load provinces: " + response.code());
                Toast.makeText(AddressPaymentActivity.this, 
                        "Không thể tải danh sách tỉnh/thành phố", 
                        Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onFailure(Call<ProvinceResponse> call, Throwable t) {
            Log.e(TAG, "Error loading provinces", t);
            Toast.makeText(AddressPaymentActivity.this, 
                    "Lỗi kết nối: " + t.getMessage(), 
                    Toast.LENGTH_SHORT).show();
        }
    });
}
```

**loadDistricts():**
```java
private void loadDistricts(int provinceId) {
    addressApiService.getDistricts(String.valueOf(provinceId)).enqueue(new Callback<DistrictResponse>() {
        @Override
        public void onResponse(Call<DistrictResponse> call, Response<DistrictResponse> response) {
            if (response.isSuccessful() && response.body() != null) {
                districtList = response.body().getResults();  // Lấy từ results
                List<String> districtNames = new ArrayList<>();
                districtNames.add("Chọn Quận/Huyện");
                for (District district : districtList) {
                    districtNames.add(district.getDistrictName());
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        AddressPaymentActivity.this,
                        android.R.layout.simple_spinner_item,
                        districtNames
                );
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerDistrict.setAdapter(adapter);
                spinnerDistrict.setEnabled(true);
            } else {
                Log.e(TAG, "Failed to load districts: " + response.code());
                Toast.makeText(AddressPaymentActivity.this, 
                        "Không thể tải danh sách quận/huyện", 
                        Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onFailure(Call<DistrictResponse> call, Throwable t) {
            Log.e(TAG, "Error loading districts", t);
            Toast.makeText(AddressPaymentActivity.this, 
                    "Lỗi kết nối: " + t.getMessage(), 
                    Toast.LENGTH_SHORT).show();
        }
    });
}
```

**loadWards():**
```java
private void loadWards(int districtId) {
    addressApiService.getWards(String.valueOf(districtId)).enqueue(new Callback<WardResponse>() {
        @Override
        public void onResponse(Call<WardResponse> call, Response<WardResponse> response) {
            if (response.isSuccessful() && response.body() != null) {
                wardList = response.body().getResults();  // Lấy từ results
                List<String> wardNames = new ArrayList<>();
                wardNames.add("Chọn Phường/Xã");
                for (Ward ward : wardList) {
                    wardNames.add(ward.getWardName());
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        AddressPaymentActivity.this,
                        android.R.layout.simple_spinner_item,
                        wardNames
                );
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerWard.setAdapter(adapter);
                spinnerWard.setEnabled(true);
            } else {
                Log.e(TAG, "Failed to load wards: " + response.code());
                Toast.makeText(AddressPaymentActivity.this, 
                        "Không thể tải danh sách phường/xã", 
                        Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onFailure(Call<WardResponse> call, Throwable t) {
            Log.e(TAG, "Error loading wards", t);
            Toast.makeText(AddressPaymentActivity.this, 
                    "Lỗi kết nối: " + t.getMessage(), 
                    Toast.LENGTH_SHORT).show();
        }
    });
}
```

## 🧪 Test API Bằng Browser

### Test provinces.open-api.vn
```
https://provinces.open-api.vn/api/p/
```

### Test vnappmob.com
```
https://vapi.vnappmob.com/api/province/
https://vapi.vnappmob.com/api/province/district/01
https://vapi.vnappmob.com/api/province/ward/001
```

## 🔍 Debug API Response

Xem log trong Android Studio Logcat với filter:
```
AddressPaymentActivity
```

Hoặc xem HTTP request/response chi tiết:
```
OkHttp
```

## 💡 Tips

1. **Luôn test API trước trong browser/Postman**
2. **Kiểm tra cấu trúc JSON response**
3. **Match @SerializedName với field name trong JSON**
4. **Thêm try-catch khi parse int từ String**

## ⚠️ Lưu Ý

- ID trong vnappmob.com là String, cần parse sang int
- Response được wrap trong object `results`
- Endpoint dùng path parameter thay vì query parameter

---

**Khuyến nghị:** Sử dụng vnappmob.com vì API đơn giản và ổn định hơn!
