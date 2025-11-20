# Hướng dẫn cập nhật dữ liệu sản phẩm - Rating & Stock Status

## Các field mới cần thêm vào Firestore

Để hiển thị **rating (đánh giá)** và **stock status (trạng thái hàng)** trên card sản phẩm, bạn cần thêm các field sau vào mỗi document trong collection `products`:

### 1. Rating (Đánh giá sao)
```
rating: 4.5 (kiểu Number/Double)
reviewCount: 128 (kiểu Number/Integer)
```

### 2. Stock Quantity (Số lượng tồn kho)
```
stockQuantity: 50 (kiểu Number/Integer)
```

## Cách thêm vào Firebase Console

### Bước 1: Mở Firebase Console
1. Truy cập https://console.firebase.google.com/
2. Chọn project **FashionStoreApp**
3. Vào **Firestore Database** ở menu bên trái

### Bước 2: Cập nhật collection "products"
1. Click vào collection `products`
2. Chọn một document sản phẩm bất kỳ
3. Click nút **"Add field"** (hoặc icon dấu +)

### Bước 3: Thêm field "rating"
- **Field name**: `rating`
- **Type**: `number`
- **Value**: `4.5` (hoặc giá trị từ 0.0 đến 5.0)

### Bước 4: Thêm field "reviewCount"
- **Field name**: `reviewCount`
- **Type**: `number`
- **Value**: `128` (số lượng đánh giá)

### Bước 5: Thêm field "stockQuantity"
- **Field name**: `stockQuantity`
- **Type**: `number`
- **Value**: `50` (số lượng tồn kho)

### Bước 6: Lặp lại cho tất cả sản phẩm
- Áp dụng tương tự cho tất cả các document trong collection `products`

## Logic hiển thị Stock Status

App sẽ tự động hiển thị trạng thái dựa trên `stockQuantity`:

| Số lượng tồn kho | Hiển thị | Màu sắc | Background |
|------------------|----------|---------|------------|
| > 20 | **Còn hàng** | Xanh lá (#4CAF50) | #E8F5E9 |
| 1-20 | **Sắp hết** | Cam (#FF9800) | #FFF3E0 |
| 0 | **Hết hàng** | Đỏ (#F44336) | #FFEBEE |

## Logic hiển thị Rating

- **Nếu rating > 0**: Hiển thị sao vàng ⭐, điểm số và số lượng đánh giá
- **Nếu rating = 0**: Ẩn toàn bộ phần rating

## Ví dụ document hoàn chỉnh

```json
{
  "id": "prod001",
  "name": "Áo Khoác Bomber Nam ICONDENIM",
  "description": "Áo khoác bomber phong cách Hàn Quốc",
  "currentPrice": 649000,
  "originalPrice": 900000,
  "imageUrl": "https://example.com/image.jpg",
  "category": "cat001",
  "isNew": true,
  "hasVoucher": false,
  "voucherText": "",
  "rating": 4.5,
  "reviewCount": 128,
  "stockQuantity": 35
}
```

## Cách cập nhật hàng loạt (Advanced)

Nếu bạn muốn cập nhật nhiều sản phẩm cùng lúc, có thể sử dụng script hoặc import/export:

### Option 1: Export/Import CSV
1. Export dữ liệu hiện tại
2. Thêm 3 cột mới: `rating`, `reviewCount`, `stockQuantity`
3. Import lại vào Firestore

### Option 2: Sử dụng Firebase Admin SDK (Node.js)
```javascript
const admin = require('firebase-admin');
const db = admin.firestore();

async function updateAllProducts() {
  const snapshot = await db.collection('products').get();
  
  const batch = db.batch();
  snapshot.docs.forEach(doc => {
    batch.update(doc.ref, {
      rating: 4.0,  // Giá trị mặc định
      reviewCount: 0,
      stockQuantity: 100
    });
  });
  
  await batch.commit();
  console.log('Updated all products!');
}
```

## Lưu ý quan trọng

1. **Rating phải từ 0.0 đến 5.0** - App sẽ hiển thị 1 chữ số thập phân (4.5, 3.8, v.v.)
2. **ReviewCount nên >= 0** - Số lượng người đánh giá
3. **StockQuantity nên >= 0** - Số lượng tồn kho
4. **Các field này KHÔNG bắt buộc** - Nếu không có, app sẽ ẩn rating và hiển thị "Còn hàng" mặc định
5. **Đồng bộ với hệ thống đánh giá thực tế** - Nên tạo collection `reviews` riêng để lưu đánh giá chi tiết của người dùng

## Tạo hệ thống đánh giá thực tế (Optional)

Để người dùng thực sự đánh giá sản phẩm, bạn cần:

1. **Tạo collection "reviews"**:
```json
{
  "productId": "prod001",
  "userId": "user123",
  "rating": 5,
  "comment": "Sản phẩm rất tốt!",
  "createdAt": "2025-01-15T10:30:00Z"
}
```

2. **Tính toán rating trung bình** bằng Cloud Functions hoặc trong app
3. **Cập nhật rating và reviewCount** trong document sản phẩm mỗi khi có đánh giá mới

## Hỗ trợ

Nếu cần thêm chức năng:
- Cho phép người dùng đánh giá sản phẩm
- Hiển thị chi tiết các đánh giá
- Lọc theo rating

Hãy cho tôi biết để tôi hỗ trợ thêm!
