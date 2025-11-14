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
    ├── stockQuantity: number
    ├── availableSizes: array<string> (NEW)
    ├── rating: number (NEW)
    └── reviewCount: number (NEW)
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

#### 3. **reviews** (Đánh giá sản phẩm) - NEW
```
reviews/
├── {reviewId}/
    ├── id: string
    ├── productId: string
    ├── userId: string
    ├── userName: string
    ├── rating: number (1-5)
    ├── comment: string
    ├── timestamp: number
    └── userImageUrl: string (optional)
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
  "stockQuantity": 50,
  "availableSizes": ["S", "M", "L", "XL"],
  "rating": 4.5,
  "reviewCount": 12
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
- `description`: Mô tả chi tiết sản phẩm (hiển thị trong ProductDetail)
- `currentPrice`: Giá hiện tại
- `originalPrice`: Giá gốc
- `category`: ID của category (phải khớp với category đã tạo)
- `isNew`: true/false - Sản phẩm mới
- `hasVoucher`: true/false - Có voucher không
- `imageUrl`: URL hoặc tên file ảnh trong drawable
- `availableSizes`: ["S", "M", "L", "XL"] - Các size có sẵn
- `rating`: 0.0 - 5.0 - Điểm đánh giá trung bình
- `reviewCount`: Số lượng đánh giá

### Bước 5: Thêm Reviews (Đánh giá sản phẩm)

**Ví dụ thêm review:**

#### Document: review_001
```json
{
  "id": "review_001",
  "productId": "product_001",
  "userId": "user123",
  "userName": "Nguyễn Văn A",
  "rating": 5,
  "comment": "Sản phẩm rất đẹp, chất lượng tốt. Giao hàng nhanh!",
  "timestamp": 1700000000000,
  "userImageUrl": ""
}
```

#### Document: review_002
```json
{
  "id": "review_002",
  "productId": "product_001",
  "userId": "user456",
  "userName": "Trần Thị B",
  "rating": 4,
  "comment": "Form áo đẹp nhưng hơi ôm. Nên lấy size lớn hơn 1 size.",
  "timestamp": 1700100000000,
  "userImageUrl": ""
}
```

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
   - `Product.java` - Model sản phẩm (updated with sizes, rating, reviewCount)
   - `Category.java` - Model danh mục
   - `Review.java` - Model đánh giá (NEW)

4. **Product Detail Screen** ✅ (NEW)
   - `ProductDetailActivity.java` - Màn hình chi tiết sản phẩm
   - `activity_product_detail.xml` - Layout với image gallery, size selector, description, reviews
   - `ReviewAdapter.java` - Adapter hiển thị đánh giá
   - `item_review.xml` - Layout item đánh giá

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

**⚠️ QUAN TRỌNG: Bạn cần cập nhật Firestore Rules để cho phép ghi đánh giá!**

### Cách cập nhật Rules:

1. Mở Firebase Console: https://console.firebase.google.com/
2. Chọn project của bạn
3. Vào **Firestore Database** → **Rules**
4. Copy và paste rules sau:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    
    // Products - Read: everyone, Write: authenticated users only
    match /products/{productId} {
      allow read: if true;
      allow write: if request.auth != null;
    }
    
    // Categories - Read: everyone, Write: authenticated users only
    match /categories/{categoryId} {
      allow read: if true;
      allow write: if request.auth != null;
    }
    
    // Reviews - Read: everyone, Write: authenticated users only
    match /reviews/{reviewId} {
      allow read: if true;
      allow create: if true; // Cho phép tất cả người dùng tạo review
      allow update, delete: if request.auth != null && request.auth.uid == resource.data.userId;
    }
  }
}
```

5. Click **Publish** để lưu

### Giải thích Rules:

- **products**: Mọi người đọc được, chỉ user đã đăng nhập mới ghi được
- **categories**: Mọi người đọc được, chỉ user đã đăng nhập mới ghi được
- **reviews**: 
  - Mọi người đọc được
  - **Mọi người tạo đánh giá được** (không cần đăng nhập)
  - Chỉ chủ review mới sửa/xóa được

### Lưu ý:
- Nếu muốn bắt buộc đăng nhập mới viết review: `allow create: if request.auth != null;`
- Rules hiện tại cho phép KHÔNG CẦN đăng nhập để viết review

## 🔑 Important Notes

1. **ImageUrl**: Có thể sử dụng URL (bắt đầu bằng http/https) hoặc tên drawable
2. **Category**: Phải match với category ID trong collection categories
3. **Price**: Lưu dưới dạng number, không phải string
4. **Boolean**: isNew, hasVoucher, isActive phải là boolean true/false
5. **availableSizes**: Array chứa các size ["S", "M", "L", "XL"]
6. **rating**: Number từ 0.0 đến 5.0
7. **reviewCount**: Số nguyên, số lượng đánh giá
8. **description**: String mô tả chi tiết, hiển thị trong màn hình ProductDetail

## ✨ Product Detail Features

Khi click vào sản phẩm, app sẽ hiển thị:
- ✅ Image gallery với ViewPager2
- ✅ Tên sản phẩm, loại, MSP
- ✅ Giá hiện tại, giá gốc, % giảm giá
- ✅ Đánh giá sao + số lượng review
- ✅ Chọn kích thước (S/M/L/XL) với highlight
- ✅ Mô tả chi tiết sản phẩm
- ✅ Danh sách đánh giá từ người dùng
- ✅ Nút "Thêm vào giỏ" và "Mua ngay"

---

**Happy Coding!** 🚀
