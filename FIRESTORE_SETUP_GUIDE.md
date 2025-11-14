# Firestore Database Setup Guide

## 📚 Cấu Trúc Database

### Collections

#### 1. **products** (Sản phẩm)
```
products/
├── {productId}/
    ├── id: string
    ├── name: string
    ├── description: string
    ├── currentPrice: number
    ├── originalPrice: number
    ├── discountPercent: number
    ├── imageUrl: string
    ├── category: string (categoryId)
    ├── isNew: boolean
    ├── hasVoucher: boolean
    ├── voucherText: string
    ├── isFavorite: boolean
    └── stockQuantity: number
```

#### 2. **categories** (Danh mục)
```
categories/
├── {categoryId}/
    ├── id: string
    ├── name: string
    ├── description: string
    ├── imageUrl: string
    ├── displayOrder: number
    └── isActive: boolean
```

## 🚀 Cách Thêm Dữ Liệu Vào Firestore

### Bước 1: Mở Firebase Console
1. Truy cập: https://console.firebase.google.com/
2. Chọn project "FashionStoreApp"
3. Click vào "Firestore Database" trong menu bên trái

### Bước 2: Tạo Collection "categories"

**Thêm các documents sau:**

#### Document 1: retro-sports
```json
{
  "id": "retro-sports",
  "name": "Retro Sports",
  "description": "Bộ sưu tập thể thao retro",
  "imageUrl": "",
  "displayOrder": 1,
  "isActive": true
}
```

#### Document 2: ao-thun
```json
{
  "id": "ao-thun",
  "name": "Áo Thun",
  "description": "Áo thun nam các loại",
  "imageUrl": "",
  "displayOrder": 2,
  "isActive": true
}
```

#### Document 3: ao-polo
```json
{
  "id": "ao-polo",
  "name": "Áo Polo",
  "description": "Áo polo nam cao cấp",
  "imageUrl": "",
  "displayOrder": 3,
  "isActive": true
}
```

#### Document 4: outlet
```json
{
  "id": "outlet",
  "name": "Outlet",
  "description": "Sản phẩm giảm giá",
  "imageUrl": "",
  "displayOrder": 4,
  "isActive": true
}
```

### Bước 3: Tạo Collection "products"

**Ví dụ thêm sản phẩm:**

#### Document 1: product_001
```json
{
  "id": "product_001",
  "name": "Áo Khoác Bomber Nam ICONDENIM",
  "description": "Áo khoác bomber phong cách Hàn Quốc, chất liệu dù cao cấp",
  "currentPrice": 450000,
  "originalPrice": 650000,
  "discountPercent": 30,
  "imageUrl": "product1",
  "category": "retro-sports",
  "isNew": true,
  "hasVoucher": false,
  "voucherText": "",
  "isFavorite": false,
  "stockQuantity": 50
}
```

#### Document 2: product_002
```json
{
  "id": "product_002",
  "name": "Áo Thun Basic Cotton 100%",
  "description": "Áo thun cotton 100% form rộng thoải mái",
  "currentPrice": 199000,
  "originalPrice": 299000,
  "discountPercent": 33,
  "imageUrl": "product2",
  "category": "ao-thun",
  "isNew": false,
  "hasVoucher": true,
  "voucherText": "Voucher 15K",
  "isFavorite": false,
  "stockQuantity": 100
}
```

#### Document 3: product_003
```json
{
  "id": "product_003",
  "name": "Áo Polo Pique Premium",
  "description": "Áo polo vải pique cao cấp, thấm hút mồ hôi tốt",
  "currentPrice": 350000,
  "originalPrice": 500000,
  "discountPercent": 30,
  "imageUrl": "product3",
  "category": "ao-polo",
  "isNew": true,
  "hasVoucher": true,
  "voucherText": "Voucher 15K",
  "isFavorite": false,
  "stockQuantity": 75
}
```

#### Document 4: product_004
```json
{
  "id": "product_004",
  "name": "Quần Jean Slim Fit",
  "description": "Quần jean nam slim fit co giãn nhẹ",
  "currentPrice": 420000,
  "originalPrice": 600000,
  "discountPercent": 30,
  "imageUrl": "product4",
  "category": "outlet",
  "isNew": false,
  "hasVoucher": false,
  "voucherText": "",
  "isFavorite": false,
  "stockQuantity": 60
}
```

#### Document 5: product_005
```json
{
  "id": "product_005",
  "name": "Áo Hoodie Street Style",
  "description": "Áo hoodie unisex phong cách đường phố",
  "currentPrice": 550000,
  "originalPrice": 750000,
  "discountPercent": 26,
  "imageUrl": "product5",
  "category": "retro-sports",
  "isNew": true,
  "hasVoucher": true,
  "voucherText": "Voucher 20K",
  "isFavorite": false,
  "stockQuantity": 40
}
```

### Bước 4: Thêm Nhiều Sản Phẩm Hơn

Bạn có thể copy/paste và chỉnh sửa các trường:
- `id`: unique identifier
- `name`: Tên sản phẩm
- `currentPrice`: Giá hiện tại
- `originalPrice`: Giá gốc
- `category`: ID của category (phải khớp với category đã tạo)
- `isNew`: true/false - Sản phẩm mới
- `hasVoucher`: true/false - Có voucher không
- `imageUrl`: Tên file ảnh trong drawable (không cần extension)

## 📱 Testing

### 1. Kiểm tra kết nối
Sau khi thêm dữ liệu vào Firestore:
1. Build và chạy app
2. Mở MainActivity
3. Xem log để kiểm tra dữ liệu được load:
   ```
   Logcat filter: "MainActivity"
   hoặc "FirestoreManager"
   ```

### 2. Fallback Data
Nếu Firestore trống hoặc lỗi, app sẽ tự động hiển thị dữ liệu mẫu (sample data).

## 🔧 Code Integration

### Đã Implement:

1. **FirestoreManager.java** ✅
   - `loadProducts()` - Load tất cả sản phẩm
   - `loadProductsByCategory()` - Load theo category
   - `loadNewProducts()` - Load sản phẩm mới
   - `loadVoucherProducts()` - Load sản phẩm có voucher
   - `loadCategories()` - Load danh mục
   - `addProduct()` - Thêm sản phẩm
   - `addCategory()` - Thêm danh mục

2. **MainActivity.java** ✅
   - `loadProductsFromFirestore()` - Load sản phẩm từ Firestore
   - `loadCategoriesFromFirestore()` - Load categories
   - Fallback to sample data nếu Firestore trống

3. **Models** ✅
   - `Product.java` - Model sản phẩm
   - `Category.java` - Model danh mục

## 🎯 Next Steps

### 1. Thêm Real-time Updates
```java
// Listen to real-time changes
db.collection("products")
  .addSnapshotListener((value, error) -> {
      // Update UI when data changes
  });
```

### 2. Pagination
```java
// Load products with pagination
db.collection("products")
  .orderBy("name")
  .limit(20)
  .startAfter(lastVisible)
  .get();
```

### 3. Search
```java
// Search products by name
db.collection("products")
  .orderBy("name")
  .startAt(searchText)
  .endAt(searchText + "\uf8ff")
  .get();
```

## 📊 Firestore Rules

Thêm rules để bảo mật:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Allow read for all users
    match /products/{productId} {
      allow read: if true;
      allow write: if request.auth != null; // Only authenticated users
    }
    
    match /categories/{categoryId} {
      allow read: if true;
      allow write: if request.auth != null;
    }
  }
}
```

## 🔑 Important Notes

1. **ImageUrl**: Sử dụng tên drawable (không cần .png/.jpg)
2. **Category**: Phải match với category ID trong collection categories
3. **Price**: Lưu dưới dạng number, không phải string
4. **Boolean**: isNew, hasVoucher, isActive phải là boolean true/false

---

**Happy Coding!** 🚀
