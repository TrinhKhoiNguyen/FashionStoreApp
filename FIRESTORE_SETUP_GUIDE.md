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

#### 4. **users** (Thông tin người dùng) - NEW
```
users/
├── {userId}/
    ├── name: string
    ├── birthday: string (dd/MM/yyyy)
    ├── gender: string ("Nam" hoặc "Nữ")
    ├── phone: string
    └── updatedAt: number (timestamp)
```

#### 5. **carts** (Giỏ hàng) - NEW
```
carts/
├── {userId}/
    ├── items: array [
    │     {
    │       productId: string
    │       productName: string
    │       productImage: string (URL hoặc drawable name)
    │       productPrice: number
    │       quantity: number
    │       size: string
    │       color: string
    │       isSelected: boolean
    │     }
    │   ]
    └── updatedAt: number (timestamp)
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
   - `loadProductReviews()` - Load đánh giá sản phẩm (NEW)
   - `addProduct()` - Thêm sản phẩm
   - `addCategory()` - Thêm danh mục
   - `addReview()` - Thêm đánh giá (NEW)
   - `updateProductRating()` - Cập nhật rating tự động (NEW)
   - `saveUserProfile()` - Lưu thông tin người dùng (NEW)
   - `loadUserProfile()` - Tải thông tin người dùng (NEW)
   - `saveCartItems()` - Lưu giỏ hàng (NEW)
   - `loadCartItems()` - Tải giỏ hàng (NEW)
   - `clearCart()` - Xóa giỏ hàng (NEW)

### 2. **MainActivity.java** ✅
   - `loadProductsFromFirestore()` - Load sản phẩm từ Firestore
   - `loadCategoriesFromFirestore()` - Load categories
   - `loadCartFromFirestore()` - Load giỏ hàng khi app khởi động (NEW)
   - Fallback to sample data nếu Firestore trống

3. **Models** ✅
   - `Product.java` - Model sản phẩm (updated with sizes, rating, reviewCount)
   - `Category.java` - Model danh mục
   - `Review.java` - Model đánh giá (NEW)
   - `User.java` - Model người dùng (updated with birthday, gender) (NEW)
   - `CartItem.java` - Model item giỏ hàng

4. **Product Detail Screen** ✅ (NEW)
   - `ProductDetailActivity.java` - Màn hình chi tiết sản phẩm
   - `activity_product_detail.xml` - Layout với image gallery, size selector, description, reviews
   - `ReviewAdapter.java` - Adapter hiển thị đánh giá
   - `item_review.xml` - Layout item đánh giá

5. **Profile Management** ✅ (NEW)
   - `ProfileActivity.java` - Màn hình thông tin tài khoản
   - `activity_profile.xml` - Form nhập họ, tên, sinh nhật, giới tính, SĐT
   - Lưu thông tin vào Firestore collection `users`
   - Tự động tải thông tin khi mở app

6. **Cart Management** ✅ (NEW)
   - `CartManager.java` - Quản lý giỏ hàng với Firestore integration
   - `CartActivity.java` - Màn hình giỏ hàng
   - `activity_cart.xml` - Layout giỏ hàng
   - Tự động lưu vào Firestore mỗi khi thêm/sửa/xóa
   - Tự động tải giỏ hàng khi mở app
   - Giỏ hàng không mất khi thoát app

7. **Search Feature** ✅ (NEW)
   - `SearchActivity.java` - Màn hình tìm kiếm
   - `activity_search.xml` - Layout tìm kiếm với suggestions
   - Tìm kiếm real-time theo tên, mô tả, category
   - Popular search chips (Áo thun, Áo polo, Quần jeans, ...)

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

**⚠️ QUAN TRỌNG: Bạn cần cập nhật Firestore Rules để cho phép đọc/ghi dữ liệu!**

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
    
    // Users - Read/Write: chỉ chủ tài khoản (NEW)
    match /users/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
    
    // Carts - Read/Write: chỉ chủ giỏ hàng (NEW)
    match /carts/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
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
- **users** (NEW):
  - Chỉ user đã đăng nhập mới đọc/ghi thông tin của chính mình
  - Bảo mật thông tin cá nhân (họ tên, sinh nhật, giới tính, SĐT)
- **carts** (NEW):
  - Chỉ user đã đăng nhập mới đọc/ghi giỏ hàng của chính mình
  - Mỗi user có 1 document riêng chứa giỏ hàng

### Lưu ý:
- Nếu muốn bắt buộc đăng nhập mới viết review: `allow create: if request.auth != null;`
- Rules hiện tại cho phép KHÔNG CẦN đăng nhập để viết review
- **users** và **carts** yêu cầu đăng nhập để đảm bảo bảo mật

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

## 🛒 Cart Management (Quản lý Giỏ hàng)

### Thiết lập Collection `carts`:

**⚠️ QUAN TRỌNG: Bạn KHÔNG CẦN tạo collection `carts` thủ công!**

Collection `carts` sẽ được **tự động tạo** khi user thêm sản phẩm đầu tiên vào giỏ hàng. Hệ thống hoạt động như sau:

#### 1. **Document ID = userId từ Firebase Auth**
   ```
   Collection: carts
   Document ID: {userId} ← Tự động lấy từ FirebaseAuth.getCurrentUser().getUid()
   ```

#### 2. **Luồng tự động tạo giỏ hàng**:

   **Bước 1**: User đăng nhập → Firebase Auth tạo userId (ví dụ: `abc123xyz`)
   
   **Bước 2**: User click "Thêm vào giỏ" lần đầu tiên
   
   **Bước 3**: App tự động:
   ```java
   String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
   // userId = "abc123xyz"
   
   db.collection("carts")
     .document(userId)  // ← Sử dụng userId làm Document ID
     .set(cartData);    // ← Firestore tự động tạo document nếu chưa tồn tại
   ```
   
   **Kết quả trong Firestore**:
   ```
   carts (collection)
     └── abc123xyz (document) ← Tự động tạo
         ├── items: [...]
         └── updatedAt: 1700000000000
   ```

#### 3. **Tại sao không cần tạo thủ công?**

   - ✅ App tự động lấy userId từ Firebase Authentication
   - ✅ Firestore tự động tạo document khi gọi `.set()` hoặc `.update()`
   - ✅ Mỗi user chỉ có 1 document duy nhất (userId làm key)
   - ✅ Không xung đột, không trùng lặp

#### 4. **Xem giỏ hàng trong Firebase Console**:

   Sau khi user thêm sản phẩm vào giỏ:
   
   1. Mở Firebase Console → Firestore Database
   2. Sẽ thấy collection `carts` xuất hiện tự động
   3. Click vào `carts` → Thấy document với ID = userId
   4. Click vào document → Xem chi tiết giỏ hàng:
   
   ```
   carts/
   ├── abc123xyz/          ← Document ID (userId)
   │   ├── items: [
   │   │   {
   │   │     productId: "product_001",
   │   │     productName: "Áo Khoác Bomber",
   │   │     productImage: "product1",
   │   │     productPrice: 450000,
   │   │     quantity: 2,
   │   │     size: "L",
   │   │     color: "Đen",
   │   │     isSelected: true
   │   │   }
   │   │ ]
   │   └── updatedAt: 1700000000000
   │
   ├── xyz789def/          ← User khác có giỏ hàng riêng
   │   └── ...
   ```

#### 5. **Code tự động trong app**:

   **Lưu giỏ hàng** (CartManager.java):
   ```java
   private void saveCartToFirestore() {
       FirebaseUser user = mAuth.getCurrentUser();
       if (user != null) {
           String userId = user.getUid(); // ← Lấy userId tự động
           
           firestoreManager.saveCartItems(
               userId,           // ← Truyền userId
               cartItems, 
               listener
           );
       }
   }
   ```
   
   **Trong FirestoreManager.java**:
   ```java
   public void saveCartItems(String userId, List<CartItem> cartItems, ...) {
       // Prepare data
       Map<String, Object> data = new HashMap<>();
       data.put("items", cartData);
       data.put("updatedAt", System.currentTimeMillis());
       
       // Save to Firestore - Tự động tạo document nếu chưa có
       db.collection("carts")
           .document(userId)  // ← userId làm Document ID
           .set(data)         // ← set() tự động tạo nếu chưa tồn tại
           .addOnSuccessListener(...);
   }
   ```

#### 6. **Kiểm tra userId của user hiện tại**:

   Để biết userId của user đang đăng nhập:
   
   **Cách 1 - Trong code (Log)**:
   ```java
   FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
   if (user != null) {
       Log.d("UserId", "Current userId: " + user.getUid());
       // Output: Current userId: abc123xyz
   }
   ```
   
   **Cách 2 - Firebase Console**:
   1. Mở Firebase Console
   2. Vào **Authentication** → **Users**
   3. Xem cột **User UID** → Đây là userId
   4. Copy userId này để tìm trong collection `carts`

#### 7. **Testing**:

   **Test 1 - Tạo giỏ hàng tự động**:
   ```
   1. Đăng nhập với user A
   2. Thêm 1 sản phẩm vào giỏ
   3. Mở Firebase Console → Firestore → carts
   4. ✅ Thấy document mới với ID = userId của user A
   ```
   
   **Test 2 - Mỗi user có giỏ riêng**:
   ```
   1. Đăng nhập user A → Thêm sản phẩm X
   2. Đăng xuất
   3. Đăng nhập user B → Thêm sản phẩm Y
   4. Mở Firebase Console → carts
   5. ✅ Thấy 2 documents riêng biệt:
      - carts/{userA_id} → có sản phẩm X
      - carts/{userB_id} → có sản phẩm Y
   ```
   
   **Test 3 - Tải giỏ hàng khi mở app**:
   ```
   1. Đăng nhập → Thêm sản phẩm vào giỏ
   2. Force close app
   3. Mở lại app
   4. ✅ App tự động tải giỏ hàng của user hiện tại từ Firestore
   ```

### Cách hoạt động:

1. **Tự động lưu khi thêm sản phẩm**:
   ```java
   // Khi user click "Thêm vào giỏ"
   CartItem item = new CartItem(product, quantity, size, color);
   cartManager.addItem(item);
   // → Tự động lưu vào Firestore collection "carts"
   ```

2. **Tự động tải khi mở app**:
   ```java
   // MainActivity.onCreate()
   cartManager.loadCartFromFirestore(listener);
   // → Tải giỏ hàng từ Firestore với đầy đủ thông tin sản phẩm + ảnh
   ```

3. **Cấu trúc dữ liệu giỏ hàng trong Firestore**:
   ```json
   {
     "items": [
       {
         "productId": "product_001",
         "productName": "Áo Khoác Bomber Nam",
         "productImage": "product1",
         "productPrice": 450000,
         "quantity": 2,
         "size": "L",
         "color": "Đen",
         "isSelected": true
       }
     ],
     "updatedAt": 1700000000000
   }
   ```

4. **Mỗi thao tác đều tự động lưu**:
   - ✅ Thêm sản phẩm → Lưu Firestore
   - ✅ Xóa sản phẩm → Lưu Firestore
   - ✅ Tăng/giảm số lượng → Lưu Firestore
   - ✅ Chọn/bỏ chọn sản phẩm → Lưu Firestore
   - ✅ Thoát app → Dữ liệu vẫn lưu trên Firestore
   - ✅ Vào lại app → Tự động tải giỏ hàng

### Lợi ích:

- 🔄 **Đồng bộ**: Giỏ hàng được lưu trên cloud, không mất khi thoát app
- 📱 **Cross-device**: Có thể đồng bộ giỏ hàng trên nhiều thiết bị (cùng tài khoản)
- 🖼️ **Đầy đủ thông tin**: Lưu cả ảnh sản phẩm, tên, giá để hiển thị đúng
- ⚡ **Real-time**: Cập nhật ngay lập tức khi có thay đổi

## 👤 Profile Management (Quản lý Thông tin)

### Các trường dữ liệu được lưu:

- **name**: Họ và tên đầy đủ (string)
- **birthday**: Ngày sinh (string, format: dd/MM/yyyy)
- **gender**: Giới tính ("Nam" hoặc "Nữ")
- **phone**: Số điện thoại (string)
- **updatedAt**: Thời gian cập nhật (timestamp)

### Luồng hoạt động:

1. **Lần đầu vào ProfileActivity**: Form trống
2. **Nhập thông tin và bấm "Cập nhật"**: Lưu vào Firestore collection `users` với document ID = userId
3. **Thoát ra vào lại**: Tự động tải dữ liệu từ Firestore và điền vào form
4. **Không mất dữ liệu**: Thông tin lưu vĩnh viễn trên Firestore

### Ví dụ document trong Firestore:

```json
// Collection: users
// Document ID: abc123xyz (userId từ Firebase Auth)
{
  "name": "Nguyễn Văn A",
  "birthday": "15/03/1990",
  "gender": "Nam",
  "phone": "0123456789",
  "updatedAt": 1700000000000
}
```

## 🔍 Search Feature (Tìm kiếm)

### Chức năng:

1. **Real-time search**: Tìm kiếm ngay khi gõ
2. **Search scope**: Tìm trong tên, mô tả, và category sản phẩm
3. **Popular suggestions**: Chips gợi ý (Áo thun, Áo polo, Quần jeans, Áo khoác, Quần short)
4. **Results display**: Hiển thị dạng lưới 2 cột với ảnh sản phẩm
5. **Empty state**: Hiển thị thông báo khi không tìm thấy

### Cách sử dụng:

- Click icon search trên MainActivity → Mở SearchActivity
- Gõ từ khóa → Kết quả hiển thị real-time
- Click vào chip gợi ý → Tự động tìm kiếm
- Click vào sản phẩm → Mở ProductDetailActivity

---

## 📝 Testing Checklist

### Giỏ hàng:
- [ ] Thêm sản phẩm vào giỏ → Kiểm tra trong CartActivity
- [ ] Thoát app hoàn toàn (force close)
- [ ] Mở lại app → Giỏ hàng vẫn còn đầy đủ sản phẩm + ảnh
- [ ] Xóa sản phẩm → Thoát → Mở lại → Sản phẩm đã xóa không còn

### Profile:
- [ ] Nhập họ, tên, sinh nhật, giới tính, SĐT → Bấm "Cập nhật"
- [ ] Thoát ra MainActivity
- [ ] Vào lại ProfileActivity → Thông tin vẫn còn
- [ ] Thoát app hoàn toàn → Mở lại app → Vào ProfileActivity → Thông tin vẫn còn

### Search:
- [ ] Click icon search → Mở màn hình tìm kiếm
- [ ] Gõ "áo" → Hiển thị tất cả sản phẩm có chữ "áo"
- [ ] Click chip "Áo polo" → Tìm kiếm "áo polo"
- [ ] Gõ từ không có → Hiển thị "Không tìm thấy sản phẩm"

### Reviews:
- [ ] Mở ProductDetailActivity
- [ ] Click "Viết đánh giá" → Nhập rating + comment → Submit
- [ ] Kiểm tra review hiển thị trong danh sách
- [ ] Thoát ra vào lại → Review vẫn còn
- [ ] Kiểm tra rating trung bình đã cập nhật

---

**Happy Coding!** 🚀
