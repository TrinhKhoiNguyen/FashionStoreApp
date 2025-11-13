# Hướng dẫn cấu hình Firebase Phone Authentication

## Bước 1: Thêm SHA-1 vào Firebase Console

1. Lấy SHA-1 fingerprint bằng lệnh:
```bash
.\gradlew signingReport
```

2. Copy SHA-1 từ kết quả (ví dụ: `SHA1: XX:XX:XX:XX:...`)

3. Vào Firebase Console → Project Settings → Your apps → Android app
4. Thêm SHA certificate fingerprints
5. Download file `google-services.json` mới và thay thế trong `app/`

## Bước 2: Enable Phone Authentication

1. Vào Firebase Console → Authentication → Sign-in method
2. Enable **Phone** provider
3. Thêm test phone numbers (optional) để test mà không cần SMS thật

## Bước 3: Cấu hình reCAPTCHA (Production)

Firebase Phone Auth sử dụng reCAPTCHA để verify. Đảm bảo:
- SHA-1 đã được thêm đúng
- Package name khớp với Firebase
- Google Play services đã cài đặt trên thiết bị

## Bước 4: Test Phone Numbers (Development)

Trong Firebase Console → Authentication → Sign-in method → Phone → Test phone numbers

Thêm số test:
- Phone: +84 123 456 789
- Code: 123456

## Cấu trúc File

```
app/
├── google-services.json          # Firebase config (QUAN TRỌNG!)
├── src/main/java/.../
│   ├── RegisterActivity.java     # Màn hình đăng ký
│   ├── OTPVerificationActivity.java  # Xác thực OTP
│   └── LoginActivity.java        # Đăng nhập (đã update)
└── src/main/res/layout/
    ├── activity_register.xml     # Layout đăng ký
    └── activity_otp_verification.xml  # Layout OTP
```

## Flow Đăng Ký

1. **RegisterActivity**: Nhập số điện thoại → Gửi OTP
2. **OTPVerificationActivity**: Nhập 6 số OTP → Xác thực
3. **MainActivity**: Đăng ký thành công, chuyển về màn hình chính

## Xử lý Lỗi Thường Gặp

### 1. "This app is not authorized to use Firebase Authentication"
**Giải pháp**: 
- Kiểm tra SHA-1 đã thêm vào Firebase
- Download lại `google-services.json`
- Clean và rebuild project

### 2. "reCAPTCHA verification failed"
**Giải pháp**:
- Chạy trên thiết bị thật (không phải emulator)
- Đảm bảo Google Play Services đã cài đặt
- Kiểm tra internet connection

### 3. "Invalid phone number"
**Giải pháp**:
- Số điện thoại phải có format quốc tế (+84...)
- Code tự động thêm +84 nếu số bắt đầu bằng 0

## Test với Test Phone Number

```java
// Trong Firebase Console, thêm:
Phone: +84123456789
Code: 123456

// App sẽ không gửi SMS thật, nhập code 123456 để verify
```

## Dependencies Cần Thiết

```gradle
// Firebase
implementation(platform("com.google.firebase:firebase-bom:33.5.1"))
implementation("com.google.firebase:firebase-auth")

// Google Sign In
implementation("com.google.android.gms:play-services-auth:21.2.0")
```

## Permissions (Không cần khai báo thêm)

Firebase Phone Auth không cần permission đặc biệt. Tất cả được handle tự động.
