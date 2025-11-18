# 🔐 Forgot Password - Quick Guide

## ✅ Đã hoàn thành

### 📱 Tính năng
- Email input với validation
- Firebase Auth password reset integration
- Success dialog thông báo đã gửi email
- Error handling chi tiết
- Loading state khi gửi
- Info section với hướng dẫn

---

## 🎯 Cách sử dụng

### Từ app:
1. Mở app → LoginActivity
2. Click "Quên mật khẩu?"
3. Nhập email đã đăng ký
4. Click "GỬI EMAIL ĐẶT LẠI MẬT KHẨU"
5. Kiểm tra email (inbox hoặc spam)
6. Click link trong email
7. Nhập mật khẩu mới (min 6 ký tự)
8. Xác nhận mật khẩu
9. Đăng nhập lại với mật khẩu mới

---

## 📧 Email Reset Password

**Gửi từ:** noreply@fashionstoreapp.firebaseapp.com
**Chủ đề:** Reset your password
**Nội dung:** Link reset password (có hiệu lực 1 giờ)

### Customize email (Optional):
Firebase Console → Authentication → Templates → Password reset

---

## 🔧 Testing

### Test với email thật:
```
1. Đăng ký tài khoản mới với email thật của bạn
2. Logout
3. Click "Quên mật khẩu?"
4. Nhập email vừa đăng ký
5. Check email → Click link
6. Đặt mật khẩu mới
7. Login với mật khẩu mới → Success ✅
```

---

## 🐛 Troubleshooting

### Email không đến?
- ✓ Check spam/junk folder
- ✓ Wait 5-10 minutes
- ✓ Check email đã đăng ký đúng chưa
- ✓ Firebase Authentication enabled?

### Lỗi "Email này chưa được đăng ký"?
- ✓ Đăng ký tài khoản trước
- ✓ Kiểm tra chính tả email

### Link expired?
- ✓ Link chỉ có hiệu lực 1 giờ
- ✓ Request lại email mới

---

## 📱 UI Components

**ForgotPasswordActivity:**
```
- Back button (←)
- Title: "Quên mật khẩu?"
- Description text
- Email icon (📧)
- Email input
- "Gửi Email" button
- Progress bar
- Info section (💡 Lưu ý)
- "Nhớ mật khẩu? Đăng nhập"
```

---

## 🎨 Design

- Material Design
- Black button, white text
- Clean layout
- Professional look
- User-friendly messages

---

## ✅ Files Created

1. `ForgotPasswordActivity.java`
2. `activity_forgot_password.xml`

## ✅ Files Updated

1. `LoginActivity.java` - Added navigation
2. `AndroidManifest.xml` - Registered activity

---

**Status:** 100% Complete ✅

**Build & Test:** Ready to use!
