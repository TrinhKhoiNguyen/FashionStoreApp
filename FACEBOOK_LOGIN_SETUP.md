# Hướng Dẫn Thiết Lập Facebook Login

## Tổng Quan
Đã tích hợp đầy đủ Facebook Login cho ứng dụng Fashion Store. Tính năng này cho phép người dùng đăng nhập nhanh chóng bằng tài khoản Facebook của họ.

## Các Bước Đã Thực Hiện

### 1. Thêm Facebook SDK
**File**: `app/build.gradle.kts`
```kotlin
implementation("com.facebook.android:facebook-login:17.0.2")
```

### 2. Cập Nhật LoginActivity.java

#### 2.1 Import Facebook Classes
```java
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.firebase.auth.FacebookAuthProvider;
import java.util.Arrays;
```

#### 2.2 Khai Báo CallbackManager
```java
private CallbackManager callbackManager;
```

#### 2.3 Khởi Tạo Facebook SDK trong onCreate()
```java
configureFacebookSignIn();
```

#### 2.4 Phương Thức configureFacebookSignIn()
- Tạo `CallbackManager` để xử lý callback
- Đăng ký callback với `LoginManager`
- Xử lý 3 trường hợp: success, cancel, error

#### 2.5 Phương Thức signInWithFacebook()
```java
private void signInWithFacebook() {
    showLoading();
    LoginManager.getInstance().logInWithReadPermissions(this, 
        Arrays.asList("email", "public_profile"));
}
```

#### 2.6 Phương Thức handleFacebookAccessToken()
- Nhận `AccessToken` từ Facebook
- Chuyển đổi thành `AuthCredential` của Firebase
- Đăng nhập vào Firebase Authentication
- Lưu thông tin user vào session
- Điều hướng đến MainActivity

#### 2.7 Override onActivityResult()
```java
@Override
protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (callbackManager != null) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }
}
```

### 3. Cập Nhật strings.xml
**File**: `app/src/main/res/values/strings.xml`
```xml
<string name="facebook_app_id">YOUR_FACEBOOK_APP_ID</string>
<string name="fb_login_protocol_scheme">fbYOUR_FACEBOOK_APP_ID</string>
```

### 4. Cập Nhật AndroidManifest.xml

#### 4.1 Thêm Permission
```xml
<uses-permission android:name="android.permission.INTERNET" />
```

#### 4.2 Thêm Facebook Meta-data và Activities
```xml
<!-- Facebook App ID -->
<meta-data android:name="com.facebook.sdk.ApplicationId"
    android:value="@string/facebook_app_id"/>

<!-- Facebook Activity for login -->
<activity android:name="com.facebook.FacebookActivity"
    android:configChanges="keyboard|keyboardHidden|screenLayout|screenSize|orientation"
    android:label="@string/app_name" />

<activity android:name="com.facebook.CustomTabActivity"
    android:exported="true">
    <intent-filter>
        <action android:name="android.intent.action.VIEW" />
        <category android:name="android.intent.category.DEFAULT" />
        <category android:name="android.intent.category.BROWSABLE" />
        <data android:scheme="@string/fb_login_protocol_scheme" />
    </intent-filter>
</activity>
```

## Cấu Hình Bắt Buộc

### Bước 1: Lấy Facebook App ID
1. Truy cập [Firebase Console](https://console.firebase.google.com/)
2. Chọn project của bạn
3. Vào **Authentication** > **Sign-in method**
4. Tìm **Facebook** trong danh sách providers
5. Sao chép **App ID** và **App secret**

### Bước 2: Cấu Hình Facebook App
1. Truy cập [Facebook Developers](https://developers.facebook.com/)
2. Vào **My Apps** > chọn app của bạn
3. Vào **Settings** > **Basic**
4. Thêm **Android Platform**:
   - **Package Name**: `com.example.fashionstoreapp`
   - **Class Name**: `com.example.fashionstoreapp.LoginActivity`
   - **Key Hashes**: Tạo bằng lệnh keytool (xem bên dưới)

### Bước 3: Tạo Key Hash
Chạy lệnh sau trong PowerShell:

**Debug Key Hash**:
```powershell
keytool -exportcert -alias androiddebugkey -keystore "$env:USERPROFILE\.android\debug.keystore" | openssl sha1 -binary | openssl base64
```
Mật khẩu mặc định: `android`

**Release Key Hash** (nếu có):
```powershell
keytool -exportcert -alias YOUR_ALIAS -keystore PATH_TO_YOUR_KEYSTORE | openssl sha1 -binary | openssl base64
```

### Bước 4: Cập Nhật strings.xml
Thay thế `YOUR_FACEBOOK_APP_ID` trong file `strings.xml`:
```xml
<string name="facebook_app_id">123456789012345</string>
<string name="fb_login_protocol_scheme">fb123456789012345</string>
```

### Bước 5: Thêm OAuth Redirect URI
1. Trong Firebase Console > Authentication > Sign-in method > Facebook
2. Sao chép **OAuth redirect URI**
3. Dán vào Facebook Developer Console > Facebook Login > Settings > Valid OAuth Redirect URIs

## Luồng Hoạt Động

```
User nhấn button "Đăng nhập với Facebook"
    ↓
signInWithFacebook() được gọi
    ↓
LoginManager mở Facebook Login Dialog
    ↓
User đăng nhập trên Facebook
    ↓
FacebookCallback.onSuccess() nhận LoginResult
    ↓
handleFacebookAccessToken() chuyển đổi token
    ↓
Firebase Authentication với FacebookAuthProvider
    ↓
saveUserToSession() lưu thông tin user
    ↓
navigateToMain() chuyển đến màn hình chính
```

## Quyền Được Yêu Cầu
- `email`: Lấy email của user
- `public_profile`: Lấy tên, ảnh đại diện của user

## Xử Lý Lỗi
- **onCancel()**: User hủy đăng nhập → Hiện thông báo "Đăng nhập Facebook đã bị hủy"
- **onError()**: Có lỗi xảy ra → Hiện thông báo "Lỗi đăng nhập Facebook: [error message]"
- **signInWithCredential fails**: Firebase auth thất bại → Hiện thông báo "Xác thực thất bại: [error message]"

## Kiểm Tra
1. **Build project**: `./gradlew build`
2. **Nhấn button Facebook** trong LoginActivity
3. **Kiểm tra logs** với tag "LoginActivity"
4. **Xác minh** user được lưu vào Firebase Authentication và Firestore

## Lưu Ý
- Facebook SDK tự động xử lý việc lưu token và quản lý session
- CallbackManager phải được khởi tạo trước khi đăng ký callback
- onActivityResult() cần được override để truyền kết quả cho CallbackManager
- Đảm bảo Key Hash chính xác, nếu sai sẽ không thể đăng nhập

## Tích Hợp Với Hệ Thống Hiện Tại
- ✅ Tích hợp với `SessionManager` để lưu thông tin user
- ✅ Tích hợp với `FirestoreManager` để lưu user vào Firestore collection `users`
- ✅ Sử dụng cùng phương thức `saveUserToSession()` như Google Sign In
- ✅ Điều hướng đến `MainActivity` sau khi đăng nhập thành công
- ✅ Hiển thị ProgressBar trong quá trình xử lý

## Troubleshooting

### Lỗi: "App Not Setup"
**Nguyên nhân**: Chưa thêm Android platform trong Facebook Developer Console
**Giải pháp**: Thêm package name, class name và key hash

### Lỗi: "Invalid Key Hash"
**Nguyên nhân**: Key hash không chính xác
**Giải pháp**: Tạo lại key hash và cập nhật trong Facebook Developer Console

### Lỗi: "Given String is empty or null"
**Nguyên nhân**: Chưa cập nhật facebook_app_id trong strings.xml
**Giải pháp**: Thay thế YOUR_FACEBOOK_APP_ID bằng App ID thực

### Facebook Login Dialog không mở
**Nguyên nhân**: Chưa thêm permission INTERNET hoặc thiếu FacebookActivity trong manifest
**Giải pháp**: Kiểm tra lại AndroidManifest.xml

## Tài Nguyên
- [Facebook Login for Android - Official Docs](https://developers.facebook.com/docs/facebook-login/android)
- [Firebase Authentication with Facebook](https://firebase.google.com/docs/auth/android/facebook-login)
- [Facebook SDK for Android](https://developers.facebook.com/docs/android)
