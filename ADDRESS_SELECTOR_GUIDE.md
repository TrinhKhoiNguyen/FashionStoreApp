# Hướng Dẫn Chức Năng Chọn Địa Chỉ 3 Cấp (Tỉnh/Quận/Phường)

## 📋 Tổng Quan

Chức năng này cho phép người dùng chọn địa chỉ theo 3 cấp:
1. **Tỉnh/Thành phố** → Chọn trước
2. **Quận/Huyện** → Tự động load khi chọn Tỉnh
3. **Phường/Xã** → Tự động load khi chọn Quận

## 🚀 Các File Đã Tạo

### 1. Model Classes (Package: `model`)
- `Province.java` - Model cho Tỉnh/Thành phố
- `District.java` - Model cho Quận/Huyện  
- `Ward.java` - Model cho Phường/Xã
- `Address.java` - Đã cập nhật thêm các trường địa chỉ 3 cấp

### 2. API Service (Package: `api`)
- `AddressApiService.java` - Interface định nghĩa các API endpoints

### 3. Utils
- `RetrofitClient.java` - Singleton class quản lý Retrofit instance

### 4. Activity
- `AddressPaymentActivity.java` - Đã cập nhật logic xử lý 3 Spinner

### 5. Layout
- `dialog_add_address.xml` - Đã thay thế TextInput bằng 3 Spinner

## ⚙️ QUAN TRỌNG: Cấu Hình API

### Thay Đổi URL API

Mở file `RetrofitClient.java` và thay đổi `BASE_URL`:

```java
// Dòng 23-26 trong RetrofitClient.java
private static final String BASE_URL = "https://your-api-url.com/api/";
```

### API Miễn Phí Đề Xuất

**Option 1: provinces.open-api.vn** (Đã cấu hình sẵn)
```java
private static final String BASE_URL = "https://provinces.open-api.vn/api/";
```

**Option 2: vnappmob.com**
```java
private static final String BASE_URL = "https://vapi.vnappmob.com/api/";
```

### Cấu Trúc API Endpoints

File `AddressApiService.java` đã định nghĩa 3 endpoints:

```java
@GET("provinces")           // Lấy tất cả tỉnh
@GET("districts")           // Lấy quận theo tỉnh (?province_id=xxx)
@GET("wards")              // Lấy phường theo quận (?district_id=xxx)
```

### Tùy Chỉnh Endpoint

Nếu API của bạn có endpoint khác, sửa trong `AddressApiService.java`:

```java
// Ví dụ API có endpoint khác
@GET("api/provinces")  // Thay vì "provinces"
@GET("api/districts")  // Thay vì "districts"
@GET("api/wards")      // Thay vì "wards"
```

### Tùy Chỉnh Query Parameters

Nếu API yêu cầu tên parameter khác:

```java
// Thay vì province_id
@GET("districts")
Call<List<District>> getDistricts(@Query("provinceId") int provinceId);

// Thay vì district_id
@GET("wards")
Call<List<Ward>> getWards(@Query("districtId") int districtId);
```

## 📝 Cách Hoạt Động

### Luồng Xử Lý

1. **Mở Dialog Thêm Địa Chỉ**
   - Load danh sách Tỉnh từ API
   - Spinner Quận và Phường disabled

2. **Chọn Tỉnh**
   - Gọi API lấy danh sách Quận theo `provinceId`
   - Enable Spinner Quận
   - Reset Spinner Phường

3. **Chọn Quận**
   - Gọi API lấy danh sách Phường theo `districtId`
   - Enable Spinner Phường

4. **Chọn Phường**
   - Lưu thông tin đã chọn

5. **Nhấn "Thêm"**
   - Validate: Kiểm tra đã chọn đủ 3 cấp chưa
   - Lưu vào Firestore với đầy đủ thông tin

### Validation

```java
// Kiểm tra đã chọn Tỉnh
if (selectedProvince == null) {
    Toast.makeText(this, "Vui lòng chọn Tỉnh/Thành phố", ...);
    return;
}

// Kiểm tra đã chọn Quận
if (selectedDistrict == null) {
    Toast.makeText(this, "Vui lòng chọn Quận/Huyện", ...);
    return;
}

// Kiểm tra đã chọn Phường
if (selectedWard == null) {
    Toast.makeText(this, "Vui lòng chọn Phường/Xã", ...);
    return;
}
```

## 💾 Dữ Liệu Lưu Vào Firestore

Khi lưu địa chỉ, các trường sau được lưu vào Firestore:

```javascript
{
  "name": "Nguyễn Văn A",
  "phone": "0901234567",
  "address": "123 Đường ABC",
  
  // Thông tin địa chỉ 3 cấp
  "provinceId": 1,
  "provinceName": "Thành phố Hồ Chí Minh",
  "districtId": 100,
  "districtName": "Quận 1",
  "wardCode": "00001",
  "wardName": "Phường Bến Nghé",
  
  // Tương thích với code cũ
  "city": "Thành phố Hồ Chí Minh",
  "isDefault": true
}
```

## 🔧 Dependencies Đã Thêm

Trong `app/build.gradle.kts`:

```kotlin
// Retrofit for API calls
implementation("com.squareup.retrofit2:retrofit:2.9.0")
implementation("com.squareup.retrofit2:converter-gson:2.9.0")
implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")
```

## 🧪 Test Chức Năng

### Bước 1: Đồng Bộ Gradle
```bash
# Trong Android Studio
File → Sync Project with Gradle Files
```

### Bước 2: Chạy Ứng Dụng
1. Vào **ProfileActivity**
2. Chọn **Sổ địa chỉ**
3. Nhấn **FAB (+)** hoặc **Thêm địa chỉ**
4. Dialog hiển thị với 3 Spinner

### Bước 3: Kiểm Tra
- [ ] Spinner Tỉnh load được danh sách
- [ ] Chọn Tỉnh → Spinner Quận tự động load
- [ ] Chọn Quận → Spinner Phường tự động load
- [ ] Không chọn đủ 3 cấp → Hiển thị lỗi
- [ ] Chọn đủ → Lưu thành công vào Firestore

## 🐛 Xử Lý Lỗi

### Lỗi "Failed to load provinces"

**Nguyên nhân:** API không hoạt động hoặc URL sai

**Giải pháp:**
1. Kiểm tra BASE_URL trong `RetrofitClient.java`
2. Test API bằng Postman/Browser
3. Xem Log trong Android Studio Logcat (filter: "AddressPaymentActivity")

### Lỗi "Cannot connect to API"

**Nguyên nhân:** Không có kết nối Internet hoặc API bị chặn

**Giải pháp:**
1. Kiểm tra quyền INTERNET trong AndroidManifest.xml (đã có)
2. Kiểm tra kết nối mạng thiết bị/emulator
3. Thử API khác

### Lỗi "Model mismatch"

**Nguyên nhân:** Cấu trúc JSON từ API khác với model

**Giải pháp:**
1. Xem response thực tế trong Logcat (có HTTP Logging Interceptor)
2. Sửa `@SerializedName` trong Province.java, District.java, Ward.java

**Ví dụ:** API trả về `province_id` thay vì `ProvinceID`
```java
// Trong Province.java
@SerializedName("province_id")  // Tên field trong JSON
private int provinceId;         // Tên biến trong Java
```

## 📱 UI/UX

### Tùy Chỉnh Giao Diện Spinner

Trong `dialog_add_address.xml`, có thể thay đổi:

```xml
<!-- Thay đổi style của Spinner -->
<Spinner
    android:background="@drawable/custom_spinner_bg"
    android:textSize="16sp"
    android:textColor="@color/black"
    ... />
```

### Thêm Progress Loading

Trong `AddressPaymentActivity.java`, thêm ProgressBar khi load API:

```java
// Trong loadProvinces()
progressBar.setVisibility(View.VISIBLE);

// Trong onResponse/onFailure
progressBar.setVisibility(View.GONE);
```

## 🔄 Tích Hợp Với Checkout

Khi sử dụng địa chỉ trong CheckoutActivity, có thể lấy địa chỉ đầy đủ:

```java
Address address = ... // Lấy từ Firestore
String fullAddress = address.getFullAddress();
// Kết quả: "123 Đường ABC, Phường Bến Nghé, Quận 1, Thành phố Hồ Chí Minh"
```

## 📞 API Khác

Nếu bạn muốn dùng API khác, đảm bảo API có:

1. **Endpoint lấy tất cả Tỉnh**
   - Method: GET
   - Response: Array of Province objects

2. **Endpoint lấy Quận theo Tỉnh**
   - Method: GET
   - Parameter: province_id (hoặc tên khác)
   - Response: Array of District objects

3. **Endpoint lấy Phường theo Quận**
   - Method: GET
   - Parameter: district_id (hoặc tên khác)
   - Response: Array of Ward objects

## ✅ Checklist Hoàn Thành

- [x] Thêm Retrofit dependencies
- [x] Tạo model classes (Province, District, Ward)
- [x] Cập nhật Address model
- [x] Tạo AddressApiService interface
- [x] Tạo RetrofitClient singleton
- [x] Cập nhật layout dialog_add_address.xml
- [x] Cập nhật AddressPaymentActivity
- [x] Thêm validation đầy đủ
- [x] Test chức năng

## 🎯 Tính Năng

✅ Chọn địa chỉ 3 cấp bằng Spinner (Dropdown)
✅ Load dữ liệu từ API (Retrofit)
✅ Auto-load Quận khi chọn Tỉnh
✅ Auto-load Phường khi chọn Quận
✅ Validate đầy đủ trước khi lưu
✅ Lưu đầy đủ thông tin vào Firestore
✅ Code đầy đủ, không rút gọn
✅ Sẵn sàng thay đổi API URL

---

**Lưu ý:** Nhớ thay đổi `BASE_URL` trong `RetrofitClient.java` trước khi chạy!
