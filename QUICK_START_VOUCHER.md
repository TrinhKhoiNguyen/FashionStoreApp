# 🚀 Quick Start - Admin Voucher Management

## 1️⃣ Thiết lập Firestore Rules

Thêm vào Firestore Rules:
```javascript
match /vouchers/{voucherId} {
  allow read: if true;
  allow create, update, delete: if isAdmin();
}
```

## 2️⃣ Tạo Voucher mẫu

Vào Firebase Console → Firestore → Tạo collection `vouchers`:

**Voucher 1: SAVE50K**
```json
{
  "code": "SAVE50K",
  "type": "fixed",
  "amount": 50000,
  "maxDiscount": 0,
  "minOrder": 299000,
  "quantity": 100,
  "usedCount": 0,
  "startAt": 1733011200000,
  "endAt": 1767225599000,
  "active": true,
  "description": "Giảm 50K cho đơn từ 299K",
  "createdAt": 1733011200000,
  "updatedAt": 1733011200000
}
```

**Voucher 2: SALE15**
```json
{
  "code": "SALE15",
  "type": "percent",
  "amount": 15,
  "maxDiscount": 100000,
  "minOrder": 500000,
  "quantity": 200,
  "usedCount": 0,
  "startAt": 1733011200000,
  "endAt": 1767225599000,
  "active": true,
  "description": "Giảm 15% tối đa 100K",
  "createdAt": 1733011200000,
  "updatedAt": 1733011200000
}
```

## 3️⃣ Build & Run

```bash
# Clean build
./gradlew clean

# Build APK
./gradlew assembleDebug

# Install
adb install app/build/outputs/apk/debug/app-debug.apk
```

## 4️⃣ Truy cập Admin Panel

1. Đăng nhập với tài khoản **admin**
2. Profile → **⚡ Admin Panel**
3. Chọn tab **Vouchers**
4. Thấy danh sách vouchers!

## 5️⃣ Test Features

### ✅ Tìm kiếm
- Gõ "SAVE" → Thấy voucher SAVE50K

### ✅ Lọc
- Click chip **Active** → Chỉ vouchers đang hoạt động
- Click chip **Percent** → Chỉ vouchers giảm %

### ✅ Thêm voucher mới
1. Click nút **+**
2. Điền form:
   - Code: **FREESHIP**
   - Type: **Giảm cố định**
   - Amount: **30000**
   - Min Order: **0**
   - Quantity: **500**
   - Dates: Hôm nay → 1 năm sau
   - Active: **ON**
3. Click **Lưu**

### ✅ Chỉnh sửa
1. Click **Chỉnh sửa** trên voucher
2. Thay đổi quantity → **300**
3. Click **Lưu**

### ✅ Xóa
1. Click menu **⋮**
2. Chọn **Xóa**
3. Confirm

### ✅ Toggle status
- Click **Vô hiệu hóa** → Voucher inactive
- Click **Kích hoạt** → Voucher active lại

## 6️⃣ Common Issues

### ❌ "Voucher không load"
**Fix**: Kiểm tra Firestore Rules, đảm bảo có quyền read

### ❌ "Không tạo được voucher"
**Fix**: 
- Kiểm tra quyền admin
- Kiểm tra mã voucher không trùng

### ❌ "Layout lỗi"
**Fix**: Rebuild project (`Build → Rebuild Project`)

## 7️⃣ Next Actions

- [ ] Test trên thiết bị thật
- [ ] Tích hợp với Checkout
- [ ] Thêm Analytics
- [ ] Export vouchers

---

**Happy Coding! 🎉**
