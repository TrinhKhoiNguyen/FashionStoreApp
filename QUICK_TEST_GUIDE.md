# 🧪 Quick Test Guide - Real-time Features

## Test 1: Real-time Product Updates (5 phút)

### Chuẩn bị:
1. ✅ Mở app trên điện thoại/emulator
2. ✅ Mở Firebase Console trong browser: https://console.firebase.google.com/
3. ✅ Vào project → Firestore Database

### Test thêm sản phẩm mới:

**Bước 1:** Trong app, scroll xuống phần "ÁO THUN"

**Bước 2:** Trong Firebase Console:
- Click collection **"products"**
- Click **"Add document"**
- Document ID: để trống (auto generate) hoặc nhập: `test_realtime_001`

**Bước 3:** Copy paste dữ liệu này:
```json
{
  "name": "Áo Thun Real-time Test",
  "description": "Test tự động cập nhật",
  "currentPrice": 199000,
  "originalPrice": 399000,
  "discountPercent": 50,
  "imageUrl": "product1",
  "category": "ao-thun",
  "isNew": true,
  "hasVoucher": false,
  "voucherText": "",
  "isFavorite": false,
  "stockQuantity": 100,
  "rating": 4.8,
  "reviewCount": 15,
  "sizes": ["S", "M", "L", "XL"],
  "colors": ["Đen", "Trắng", "Xám"],
  "createdAt": 1700000000000
}
```

**Bước 4:** Click **"Save"**

**Kết quả mong đợi:**
✅ Sản phẩm mới xuất hiện NGAY LẬP TỨC trong section "ÁO THUN" (không cần refresh!)

---

## Test 2: Xem tất cả sản phẩm (2 phút)

**Bước 1:** Trong app, tìm nút **"Xem tất cả"** bên cạnh "ÁO THUN"

**Bước 2:** Click nút **"Xem tất cả"**

**Kết quả mong đợi:**
✅ Màn hình mới mở ra với title "Áo Thun"
✅ Hiển thị toàn bộ sản phẩm áo thun dạng grid 2 cột
✅ Có sản phẩm vừa thêm ở Test 1

---

## Test 3: Real-time trong màn hình "Xem tất cả" (3 phút)

**Chuẩn bị:** Ở màn hình CategoryProductsActivity (từ Test 2)

**Bước 1:** Giữ app mở (KHÔNG click back)

**Bước 2:** Trong Firebase Console, thêm sản phẩm mới:
```json
{
  "name": "Áo Thun Real-time Test 2",
  "description": "Test lần 2",
  "currentPrice": 249000,
  "originalPrice": 449000,
  "discountPercent": 45,
  "imageUrl": "product2",
  "category": "ao-thun",
  "isNew": true,
  "hasVoucher": true,
  "voucherText": "Giảm 50K",
  "isFavorite": false,
  "stockQuantity": 80,
  "rating": 4.5,
  "reviewCount": 8,
  "sizes": ["M", "L", "XL"],
  "colors": ["Đỏ", "Xanh"],
  "createdAt": 1700000100000
}
```

**Bước 3:** Click **"Save"** trong Firestore

**Kết quả mong đợi:**
✅ Sản phẩm mới xuất hiện NGAY trong danh sách (scroll xuống để thấy)
✅ Badge "MỚI" hoặc "VOUCHER" hiển thị đúng

---

## Test 4: Sửa sản phẩm (2 phút)

**Bước 1:** Trong Firebase Console:
- Click vào sản phẩm "Áo Thun Real-time Test"
- Sửa field **"name"** thành: `"Áo Thun ĐÃ SỬA"`
- Sửa field **"currentPrice"** thành: `299000`

**Bước 2:** Click **"Update"**

**Kết quả mong đợi:**
✅ Tên sản phẩm trong app đổi thành "Áo Thun ĐÃ SỬA"
✅ Giá đổi thành 299.000đ
✅ Cập nhật NGAY không cần refresh

---

## Test 5: Xóa sản phẩm (1 phút)

**Bước 1:** Trong Firebase Console:
- Click vào sản phẩm "Áo Thun Real-time Test 2"
- Click nút **"Delete document"** (icon thùng rác)
- Confirm xóa

**Kết quả mong đợi:**
✅ Sản phẩm biến mất NGAY khỏi danh sách trong app

---

## Test 6: Test nhiều categories (3 phút)

**Retro Sports:**
```json
{
  "name": "Áo Retro Test",
  "category": "retro-sports",
  "currentPrice": 399000,
  "originalPrice": 599000,
  "discountPercent": 33,
  "imageUrl": "product3",
  "isNew": false,
  "hasVoucher": false,
  "stockQuantity": 50,
  "rating": 4.6,
  "reviewCount": 12
}
```

**Outlet:**
```json
{
  "name": "Áo Outlet Sale",
  "category": "outlet",
  "currentPrice": 149000,
  "originalPrice": 499000,
  "discountPercent": 70,
  "imageUrl": "product4",
  "isNew": false,
  "hasVoucher": true,
  "voucherText": "Sale 70%",
  "stockQuantity": 30,
  "rating": 4.2,
  "reviewCount": 25
}
```

**Áo Polo:**
```json
{
  "name": "Áo Polo Test",
  "category": "ao-polo",
  "currentPrice": 349000,
  "originalPrice": 499000,
  "discountPercent": 30,
  "imageUrl": "product5",
  "isNew": true,
  "hasVoucher": false,
  "stockQuantity": 60,
  "rating": 4.7,
  "reviewCount": 18
}
```

**Kết quả mong đợi:**
✅ Mỗi sản phẩm xuất hiện trong section đúng của nó
✅ Tất cả đều real-time (không cần refresh)
✅ Nút "Xem tất cả" của mỗi category hoạt động

---

## Test 7: Test Favorites trong CategoryProductsActivity (2 phút)

**Bước 1:** Vào "Xem tất cả Áo Thun"

**Bước 2:** Click icon ❤️ trên một sản phẩm

**Kết quả mong đợi:**
✅ Icon đổi màu (filled heart)
✅ Toast: "Đã thêm vào yêu thích"
✅ Check trong ProfileActivity → Favorites → sản phẩm có trong list

**Bước 3:** Click lại icon ❤️ để bỏ favorite

**Kết quả mong đợi:**
✅ Icon đổi về outline heart
✅ Toast: "Đã xóa khỏi yêu thích"

---

## Test 8: Test Performance - Nhiều sản phẩm (5 phút)

**Bước 1:** Thêm 20 sản phẩm vào category "ao-thun"
- Có thể copy paste và sửa name: "Áo Thun 1", "Áo Thun 2", ...

**Bước 2:** Mở app:
- MainActivity chỉ hiển thị 5 items (limit 5)
- Click "Xem tất cả" → hiển thị toàn bộ 20 items

**Kết quả mong đợi:**
✅ MainActivity load nhanh (chỉ 5 items)
✅ CategoryProductsActivity hiển thị đủ 20 items
✅ Scroll mượt mà
✅ Không lag

---

## Test 9: Test Offline Mode (2 phút)

**Bước 1:** Mở app, load dữ liệu

**Bước 2:** Tắt WiFi/Mobile data

**Bước 3:** Navigate trong app

**Kết quả mong đợi:**
✅ Dữ liệu cũ vẫn hiển thị (Firestore cache)
✅ Không crash
✅ Có thể xem sản phẩm đã load

**Bước 4:** Bật lại WiFi

**Kết quả mong đợi:**
✅ Tự động sync lại với Firestore
✅ Hiển thị dữ liệu mới nhất

---

## Test 10: Test Memory Leaks (3 phút)

**Bước 1:** Mở app → MainActivity

**Bước 2:** Click "Xem tất cả" nhiều lần:
- Áo Thun → Back
- Retro Sports → Back
- Outlet → Back
- Áo Polo → Back

**Bước 3:** Repeat 10 lần

**Kết quả mong đợi:**
✅ Không crash
✅ Không lag dần
✅ Memory không tăng liên tục (check Android Profiler nếu có)

---

## ✅ Checklist Tổng

- [ ] Test 1: Real-time thêm sản phẩm
- [ ] Test 2: Xem tất cả
- [ ] Test 3: Real-time trong màn hình Xem tất cả
- [ ] Test 4: Sửa sản phẩm real-time
- [ ] Test 5: Xóa sản phẩm real-time
- [ ] Test 6: Test nhiều categories
- [ ] Test 7: Favorites trong CategoryProductsActivity
- [ ] Test 8: Performance với nhiều sản phẩm
- [ ] Test 9: Offline mode
- [ ] Test 10: Memory leaks

---

## 🐛 Common Issues

### Issue 1: Không thấy real-time updates
**Solution:**
1. Check Firestore Rules: `allow read: if true;`
2. Check internet connection
3. Restart app

### Issue 2: App crash khi mở "Xem tất cả"
**Solution:**
```bash
.\gradlew clean build
```

### Issue 3: Classpath errors
**Solution:**
- File → Sync Project with Gradle Files
- File → Invalidate Caches and Restart

---

## 📊 Expected Results Summary

| Feature | Expected Behavior | Status |
|---------|-------------------|--------|
| Add product on Firestore | Auto appear in app | ✅ |
| Edit product | Auto update in app | ✅ |
| Delete product | Auto remove from app | ✅ |
| View All button | Open CategoryProductsActivity | ✅ |
| Real-time in CategoryProducts | Auto sync | ✅ |
| Favorites | Save to Firestore | ✅ |
| Performance (5 items limit) | Fast home screen | ✅ |
| Offline mode | Show cached data | ✅ |
| Memory management | No leaks | ✅ |

---

**⏱️ Tổng thời gian test: ~30 phút**

**🎉 Nếu tất cả test đều PASS → App hoạt động hoàn hảo!**
