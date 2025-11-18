# 🆕 Hướng Dẫn Thêm 3 Danh Mục Mới

## ✅ Đã thêm vào app:

1. **Áo Khoác** - `categoryId: "ao-khoac"`
2. **Quần Sọt** - `categoryId: "quan-sot"`
3. **Quần Tây** - `categoryId: "quan-tay"`

---

## 📝 Bước 1: Thêm Categories vào Firestore

### Vào Firebase Console → Firestore Database → Collection "categories"

### 1. Thêm Áo Khoác:
```json
Document ID: ao-khoac
{
  "id": "ao-khoac",
  "name": "Áo Khoác",
  "description": "Áo khoác nam các loại",
  "imageUrl": "",
  "displayOrder": 7,
  "isActive": true
}
```

### 2. Thêm Quần Sọt:
```json
Document ID: quan-sot
{
  "id": "quan-sot",
  "name": "Quần Sọt",
  "description": "Quần sọt nam",
  "imageUrl": "",
  "displayOrder": 8,
  "isActive": true
}
```

### 3. Thêm Quần Tây:
```json
Document ID: quan-tay
{
  "id": "quan-tay",
  "name": "Quần Tây",
  "description": "Quần tây nam công sở",
  "imageUrl": "",
  "displayOrder": 9,
  "isActive": true
}
```

---

## 🛍️ Bước 2: Thêm Sản Phẩm Mẫu

### Vào Collection "products" → Add documents

### Áo Khoác - Sản phẩm 1:
```json
Document ID: (auto-generate hoặc product_aokhoac_001)
{
  "name": "Áo Khoác Bomber Nam",
  "description": "Áo khoác bomber phong cách retro",
  "currentPrice": 599000,
  "originalPrice": 899000,
  "discountPercent": 33,
  "imageUrl": "product1",
  "category": "ao-khoac",
  "isNew": true,
  "hasVoucher": false,
  "voucherText": "",
  "isFavorite": false,
  "stockQuantity": 50,
  "rating": 4.7,
  "reviewCount": 15,
  "sizes": ["M", "L", "XL"],
  "colors": ["Đen", "Xanh Navy"],
  "createdAt": 1700000000000
}
```

### Áo Khoác - Sản phẩm 2:
```json
{
  "name": "Áo Khoác Dù Gió",
  "description": "Áo khoác dù chống nước nhẹ",
  "currentPrice": 399000,
  "originalPrice": 599000,
  "discountPercent": 33,
  "imageUrl": "product2",
  "category": "ao-khoac",
  "isNew": false,
  "hasVoucher": true,
  "voucherText": "Giảm 50K",
  "isFavorite": false,
  "stockQuantity": 80,
  "rating": 4.5,
  "reviewCount": 20,
  "sizes": ["S", "M", "L", "XL"],
  "colors": ["Đen", "Xám", "Xanh"],
  "createdAt": 1700000100000
}
```

### Quần Sọt - Sản phẩm 1:
```json
{
  "name": "Quần Sọt Thể Thao Nam",
  "description": "Quần sọt thể thao thoáng mát",
  "currentPrice": 199000,
  "originalPrice": 349000,
  "discountPercent": 43,
  "imageUrl": "product3",
  "category": "quan-sot",
  "isNew": true,
  "hasVoucher": false,
  "voucherText": "",
  "isFavorite": false,
  "stockQuantity": 100,
  "rating": 4.6,
  "reviewCount": 25,
  "sizes": ["M", "L", "XL", "XXL"],
  "colors": ["Đen", "Xám", "Navy"],
  "createdAt": 1700000200000
}
```

### Quần Sọt - Sản phẩm 2:
```json
{
  "name": "Quần Sọt Jean Nam",
  "description": "Quần sọt jean phong cách",
  "currentPrice": 299000,
  "originalPrice": 499000,
  "discountPercent": 40,
  "imageUrl": "product4",
  "category": "quan-sot",
  "isNew": false,
  "hasVoucher": true,
  "voucherText": "Sale 40%",
  "isFavorite": false,
  "stockQuantity": 60,
  "rating": 4.4,
  "reviewCount": 18,
  "sizes": ["29", "30", "31", "32"],
  "colors": ["Xanh", "Đen"],
  "createdAt": 1700000300000
}
```

### Quần Tây - Sản phẩm 1:
```json
{
  "name": "Quần Tây Công Sở Slim Fit",
  "description": "Quần tây công sở cao cấp",
  "currentPrice": 499000,
  "originalPrice": 799000,
  "discountPercent": 38,
  "imageUrl": "product5",
  "category": "quan-tay",
  "isNew": true,
  "hasVoucher": false,
  "voucherText": "",
  "isFavorite": false,
  "stockQuantity": 40,
  "rating": 4.8,
  "reviewCount": 12,
  "sizes": ["29", "30", "31", "32", "33"],
  "colors": ["Đen", "Xanh Navy", "Xám"],
  "createdAt": 1700000400000
}
```

### Quần Tây - Sản phẩm 2:
```json
{
  "name": "Quần Tây Âu Phong Cách",
  "description": "Quần tây âu lịch lãm",
  "currentPrice": 599000,
  "originalPrice": 999000,
  "discountPercent": 40,
  "imageUrl": "product6",
  "category": "quan-tay",
  "isNew": false,
  "hasVoucher": true,
  "voucherText": "Giảm 100K",
  "isFavorite": false,
  "stockQuantity": 35,
  "rating": 4.7,
  "reviewCount": 10,
  "sizes": ["29", "30", "31", "32"],
  "colors": ["Đen", "Xám Đậm"],
  "createdAt": 1700000500000
}
```

---

## 🔥 Bước 3: Test Real-time

1. **Mở app** (không cần restart)
2. **Scroll xuống** → Thấy 3 sections mới:
   - Áo Khoác
   - Quần Sọt
   - Quần Tây
3. **Sản phẩm tự động hiển thị** (real-time)
4. **Click "Xem tất cả"** → Mở CategoryProductsActivity

---

## ✨ Tính năng đã có:

### ✅ Real-time Updates
- Thêm sản phẩm trên Firestore → Tự động hiển thị trong app
- Sửa sản phẩm → Tự động cập nhật
- Xóa sản phẩm → Tự động biến mất

### ✅ View All
- Click "Xem tất cả" → Mở màn hình CategoryProductsActivity
- Hiển thị toàn bộ sản phẩm của category
- Real-time sync

### ✅ Favorites
- Click icon ❤️ → Lưu vào Firestore
- Sync across app

---

## 📊 Tổng số danh mục hiện tại: **9 categories**

1. ✅ Retro Sports (`retro-sports`)
2. ✅ Outlet (`outlet`)
3. ✅ Áo Thun (`ao-thun`)
4. ✅ Áo Polo (`ao-polo`)
5. ✅ Áo Sơ Mi (`ao-so-mi`)
6. ✅ Áo Hoodie (`ao-hoodie`)
7. ✅ **Áo Khoác (`ao-khoac`)** 🆕
8. ✅ **Quần Sọt (`quan-sot`)** 🆕
9. ✅ **Quần Tây (`quan-tay`)** 🆕

---

## 🎯 Quick Copy-Paste cho Firestore

### Categories (Copy từng document):
```
Document ID: ao-khoac
{"id":"ao-khoac","name":"Áo Khoác","description":"Áo khoác nam các loại","imageUrl":"","displayOrder":7,"isActive":true}

Document ID: quan-sot
{"id":"quan-sot","name":"Quần Sọt","description":"Quần sọt nam","imageUrl":"","displayOrder":8,"isActive":true}

Document ID: quan-tay
{"id":"quan-tay","name":"Quần Tây","description":"Quần tây nam công sở","imageUrl":"","displayOrder":9,"isActive":true}
```

---

## 🚀 Kết quả:

**Sau khi thêm dữ liệu vào Firestore:**
- ✅ 3 sections mới xuất hiện trên MainActivity
- ✅ Mỗi section có nút "Xem tất cả"
- ✅ Real-time sync hoạt động
- ✅ Click sản phẩm → ProductDetailActivity
- ✅ Favorites hoạt động

**Thời gian**: ~5-10 phút để thêm tất cả dữ liệu vào Firestore

---

## 💡 Lưu ý:

- **Category ID phải khớp**: `ao-khoac`, `quan-sot`, `quan-tay`
- **Field `category` trong products** phải match với category ID
- App sẽ tự động load khi có dữ liệu (real-time)
- Không cần restart app!

🎉 **Xong! App của bạn giờ có 9 danh mục với real-time updates!**
