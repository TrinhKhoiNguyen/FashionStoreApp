# Hướng dẫn tạo dữ liệu mẫu trên Firebase Firestore

## 📋 Cấu trúc Collections

### 1. Collection: `users`
Mỗi document ID = userId từ Firebase Authentication

**Document mẫu:**
```json
{
  "userId": "abc123xyz",
  "email": "trinhkhoinguyen16@gmail.com",
  "fullName": "Trinh Khoi Nguyen",
  "phone": "0123456789",
  "points": 1500,
  "language": "vi",
  "createdAt": "2024-01-15T10:30:00Z"
}
```

**Subcollection: `users/{userId}/addresses`**
```json
{
  "addressId": "addr_001",
  "name": "Trinh Khoi Nguyen",
  "phone": "0123456789",
  "address": "123 Nguyễn Huệ",
  "city": "TP. Hồ Chí Minh",
  "isDefault": true
}
```

**Subcollection: `users/{userId}/paymentMethods`**
```json
{
  "paymentId": "pay_001",
  "type": "Thanh toán khi nhận hàng",
  "isDefault": true
}
```

---

### 2. Collection: `products`
**Document mẫu 1 - Áo thun:**
```json
{
  "productId": "prod_001",
  "name": "Áo thun nam basic",
  "description": "Áo thun cotton 100%, form regular fit, thoải mái cho mọi hoạt động",
  "categoryId": "cat_001",
  "categoryName": "Áo thun",
  "currentPrice": 199000,
  "originalPrice": 299000,
  "imageUrl": "https://firebasestorage.googleapis.com/...",
  "imageUrls": [
    "https://firebasestorage.googleapis.com/...",
    "https://firebasestorage.googleapis.com/..."
  ],
  "rating": 4.5,
  "reviewCount": 128,
  "stockQuantity": 45,
  "sizes": ["S", "M", "L", "XL"],
  "colors": ["Đen", "Trắng", "Xám"],
  "createdAt": "2024-01-10T08:00:00Z",
  "updatedAt": "2024-11-20T15:30:00Z"
}
```

**Document mẫu 2 - Quần jean:**
```json
{
  "productId": "prod_002",
  "name": "Quần jean nam slim fit",
  "description": "Quần jean co giãn nhẹ, form slim fit ôm dáng, chất liệu denim cao cấp",
  "categoryId": "cat_002",
  "categoryName": "Quần",
  "currentPrice": 450000,
  "originalPrice": 650000,
  "imageUrl": "https://firebasestorage.googleapis.com/...",
  "imageUrls": [
    "https://firebasestorage.googleapis.com/..."
  ],
  "rating": 4.8,
  "reviewCount": 256,
  "stockQuantity": 18,
  "sizes": ["29", "30", "31", "32", "33"],
  "colors": ["Xanh đậm", "Đen"],
  "createdAt": "2024-01-12T10:00:00Z",
  "updatedAt": "2024-11-21T12:00:00Z"
}
```

**Document mẫu 3 - Giày sneaker:**
```json
{
  "productId": "prod_003",
  "name": "Giày sneaker thể thao",
  "description": "Giày sneaker phong cách thể thao, đế cao su chống trượt, thoáng khí",
  "categoryId": "cat_003",
  "categoryName": "Giày dép",
  "currentPrice": 890000,
  "originalPrice": 1200000,
  "imageUrl": "https://firebasestorage.googleapis.com/...",
  "imageUrls": [
    "https://firebasestorage.googleapis.com/..."
  ],
  "rating": 4.7,
  "reviewCount": 89,
  "stockQuantity": 5,
  "sizes": ["39", "40", "41", "42", "43"],
  "colors": ["Trắng", "Đen/Trắng"],
  "createdAt": "2024-01-15T14:00:00Z",
  "updatedAt": "2024-11-22T09:00:00Z"
}
```

---

### 3. Collection: `categories`
```json
{
  "categoryId": "cat_001",
  "name": "Áo thun",
  "imageUrl": "https://firebasestorage.googleapis.com/...",
  "productCount": 45,
  "order": 1
}
```

```json
{
  "categoryId": "cat_002",
  "name": "Quần",
  "imageUrl": "https://firebasestorage.googleapis.com/...",
  "productCount": 38,
  "order": 2
}
```

```json
{
  "categoryId": "cat_003",
  "name": "Giày dép",
  "imageUrl": "https://firebasestorage.googleapis.com/...",
  "productCount": 22,
  "order": 3
}
```

---

### 4. Collection: `orders`
**Document mẫu 1 - Đơn đang xử lý:**
```json
{
  "orderId": "ORD20241122001",
  "userId": "abc123xyz",
  "items": [
    {
      "productId": "prod_001",
      "productName": "Áo thun nam basic",
      "imageUrl": "https://firebasestorage.googleapis.com/...",
      "quantity": 2,
      "price": 199000,
      "size": "L",
      "color": "Đen"
    },
    {
      "productId": "prod_002",
      "productName": "Quần jean nam slim fit",
      "imageUrl": "https://firebasestorage.googleapis.com/...",
      "quantity": 1,
      "price": 450000,
      "size": "31",
      "color": "Xanh đậm"
    }
  ],
  "total": 848000,
  "status": "Đang xử lý",
  "createdAt": "2024-11-22T10:30:00Z",
  "shippingAddress": "123 Nguyễn Huệ, TP. Hồ Chí Minh",
  "phoneNumber": "0123456789",
  "paymentMethod": "Thanh toán khi nhận hàng"
}
```

**Document mẫu 2 - Đơn đang giao:**
```json
{
  "orderId": "ORD20241120002",
  "userId": "abc123xyz",
  "items": [
    {
      "productId": "prod_003",
      "productName": "Giày sneaker thể thao",
      "imageUrl": "https://firebasestorage.googleapis.com/...",
      "quantity": 1,
      "price": 890000,
      "size": "42",
      "color": "Trắng"
    }
  ],
  "total": 890000,
  "status": "Đang giao",
  "createdAt": "2024-11-20T14:15:00Z",
  "shippingAddress": "456 Lê Lợi, Quận 1, TP. HCM",
  "phoneNumber": "0987654321",
  "paymentMethod": "Thẻ ngân hàng"
}
```

**Document mẫu 3 - Đơn hoàn thành:**
```json
{
  "orderId": "ORD20241118003",
  "userId": "abc123xyz",
  "items": [
    {
      "productId": "prod_001",
      "productName": "Áo thun nam basic",
      "imageUrl": "https://firebasestorage.googleapis.com/...",
      "quantity": 3,
      "price": 199000,
      "size": "M",
      "color": "Trắng"
    }
  ],
  "total": 597000,
  "status": "Hoàn thành",
  "createdAt": "2024-11-18T09:00:00Z",
  "shippingAddress": "789 Trần Hưng Đạo, TP. HCM",
  "phoneNumber": "0123456789",
  "paymentMethod": "Ví điện tử"
}
```

**Document mẫu 4 - Đơn đã hủy:**
```json
{
  "orderId": "ORD20241115004",
  "userId": "abc123xyz",
  "items": [
    {
      "productId": "prod_002",
      "productName": "Quần jean nam slim fit",
      "imageUrl": "https://firebasestorage.googleapis.com/...",
      "quantity": 1,
      "price": 450000,
      "size": "32",
      "color": "Đen"
    }
  ],
  "total": 450000,
  "status": "Đã hủy",
  "createdAt": "2024-11-15T16:45:00Z",
  "shippingAddress": "123 Nguyễn Huệ, TP. Hồ Chí Minh",
  "phoneNumber": "0123456789",
  "paymentMethod": "Thanh toán khi nhận hàng"
}
```

---

### 5. Collection: `vouchers`
**Document mẫu 1:**
```json
{
  "voucherId": "VOUCHER001",
  "code": "NEWYEAR2024",
  "title": "Giảm 20% cho đơn hàng đầu tiên",
  "description": "Áp dụng cho đơn hàng từ 500.000₫",
  "discountPercent": 20,
  "discountAmount": 0,
  "minOrderAmount": 500000,
  "expiryDate": "2024-12-31T23:59:59Z",
  "isActive": true
}
```

**Document mẫu 2:**
```json
{
  "voucherId": "VOUCHER002",
  "code": "FREESHIP50K",
  "title": "Miễn phí vận chuyển",
  "description": "Giảm 50.000₫ phí ship cho đơn từ 300.000₫",
  "discountPercent": 0,
  "discountAmount": 50000,
  "minOrderAmount": 300000,
  "expiryDate": "2024-12-25T23:59:59Z",
  "isActive": true
}
```

**Document mẫu 3:**
```json
{
  "voucherId": "VOUCHER003",
  "code": "FLASH100K",
  "title": "Giảm 100.000₫",
  "description": "Cho đơn hàng từ 1.000.000₫",
  "discountPercent": 0,
  "discountAmount": 100000,
  "minOrderAmount": 1000000,
  "expiryDate": "2024-11-30T23:59:59Z",
  "isActive": true
}
```

**Document mẫu 4:**
```json
{
  "voucherId": "VOUCHER004",
  "code": "MEGA30",
  "title": "Giảm 30% tối đa 200K",
  "description": "Áp dụng cho tất cả sản phẩm",
  "discountPercent": 30,
  "discountAmount": 0,
  "minOrderAmount": 800000,
  "expiryDate": "2024-12-15T23:59:59Z",
  "isActive": true
}
```

---

## 🚀 Cách import vào Firebase Console

### Bước 1: Vào Firebase Console
1. Truy cập: https://console.firebase.google.com
2. Chọn project **FashionStoreApp**
3. Vào **Firestore Database** ở menu bên trái

### Bước 2: Tạo Collection `products`
1. Click **Start collection**
2. Collection ID: `products`
3. Click **Next**
4. Document ID: `prod_001` (hoặc **Auto-ID**)
5. Copy/paste các field từ JSON mẫu ở trên:
   - Field: `productId` | Type: **string** | Value: `prod_001`
   - Field: `name` | Type: **string** | Value: `Áo thun nam basic`
   - Field: `currentPrice` | Type: **number** | Value: `199000`
   - Field: `originalPrice` | Type: **number** | Value: `299000`
   - Field: `rating` | Type: **number** | Value: `4.5`
   - Field: `stockQuantity` | Type: **number** | Value: `45`
   - Field: `sizes` | Type: **array** | Value: `["S", "M", "L", "XL"]`
   - ...và các field khác
6. Click **Save**
7. Lặp lại với các sản phẩm khác

### Bước 3: Tạo Collection `categories`
1. Click **Start collection**
2. Collection ID: `categories`
3. Tạo 3 documents với dữ liệu mẫu ở trên

### Bước 4: Tạo Collection `orders`
1. Click **Start collection**
2. Collection ID: `orders`
3. Tạo documents với các status khác nhau
4. **Lưu ý**: Field `items` là **array**, mỗi phần tử là **map** với các field như mẫu

### Bước 5: Tạo Collection `vouchers`
1. Click **Start collection**
2. Collection ID: `vouchers`
3. Tạo 4 documents voucher
4. **Lưu ý**: 
   - `expiryDate` dùng type **timestamp**
   - `isActive` dùng type **boolean**

### Bước 6: Tạo Collection `users`
1. Click **Start collection**
2. Collection ID: `users`
3. Document ID: Sử dụng **User UID** từ Firebase Authentication
4. Tạo document với các field như mẫu
5. **Tạo Subcollections**:
   - Vào document vừa tạo
   - Click **Start collection** bên trong
   - Collection ID: `addresses`
   - Thêm document địa chỉ
   - Lặp lại cho `paymentMethods`

---

## 📝 Lưu ý quan trọng

### Timestamp Format
Khi tạo field `createdAt`, `expiryDate`:
- Chọn type: **timestamp**
- Click vào field để mở date picker
- Chọn ngày/giờ mong muốn

### Array & Map
- **sizes**, **colors**, **imageUrls**: Type **array**, click **Add item** để thêm từng giá trị
- **items** trong orders: Type **array**, mỗi item là **map**:
  1. Click **Add item**
  2. Chọn type: **map**
  3. Thêm các field: productId, productName, quantity, price...

### Images URL
- Upload ảnh vào **Firebase Storage** trước
- Copy URL của ảnh
- Paste vào field `imageUrl` hoặc `imageUrls`

### User Authentication
- Tạo user test trong **Authentication** trước
- Copy UID của user
- Dùng UID này làm document ID trong collection `users`

---

## 🔧 Import nhanh bằng Script (Tuỳ chọn)

Nếu muốn import hàng loạt, tạo file JavaScript và chạy trong Firebase Console:

```javascript
// Vào Firestore > Rules > Console
// Paste đoạn code này và run

const db = firebase.firestore();

// Import Products
const products = [
  {
    productId: "prod_001",
    name: "Áo thun nam basic",
    currentPrice: 199000,
    originalPrice: 299000,
    rating: 4.5,
    reviewCount: 128,
    stockQuantity: 45
    // ... các field khác
  },
  // ... thêm products
];

products.forEach(product => {
  db.collection('products').doc(product.productId).set(product);
});

console.log('✅ Import thành công!');
```

---

## ✅ Checklist sau khi import

- [ ] Collection `products` có ít nhất 10 sản phẩm
- [ ] Collection `categories` có 3-5 danh mục
- [ ] Collection `orders` có đơn với 4 trạng thái khác nhau
- [ ] Collection `vouchers` có ít nhất 4 voucher active
- [ ] Collection `users` có subcollections `addresses` và `paymentMethods`
- [ ] Tất cả field types đúng (string, number, array, map, timestamp, boolean)
- [ ] Images URLs trỏ đến Firebase Storage hoặc URL hợp lệ
- [ ] Timestamp expiryDate của vouchers chưa hết hạn

---

**Hoàn thành!** Bây giờ app sẽ load được dữ liệu từ Firebase. 🎉
