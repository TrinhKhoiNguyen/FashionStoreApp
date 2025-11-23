# Hướng Dẫn Cập Nhật Trạng Thái Đơn Hàng

## Mapping Trạng Thái Mới

### Tabs trong OrdersActivity:
1. **Chờ xác nhận** → `status = "pending"`
2. **Đang chuẩn bị** → `status = "processing"`
3. **Đang giao** → `status = "shipping"`
4. **Đã giao** → `status = "delivered"`
5. **Đã hủy** → `status = "cancelled"`

---

## Màu Sắc Trạng Thái

| Trạng Thái | Tên Hiển Thị | Màu | Hex Code |
|------------|--------------|-----|----------|
| pending | Chờ xác nhận | Cam | #FFA726 |
| processing | Đang chuẩn bị | Xanh nhạt | #42A5F5 |
| shipping | Đang giao | Xanh dương | #2196F3 |
| delivered | Đã giao | Xanh lá | #4CAF50 |
| cancelled | Đã hủy | Đỏ | #F44336 |

---

## Cập Nhật Dữ Liệu Firestore

### 1. Đơn hàng cũ đang dùng status tiếng Việt
Nếu bạn có đơn hàng cũ với status:
- "Đang xử lý" → Cần đổi thành `"processing"`
- "Đang giao" → Cần đổi thành `"shipping"`  
- "Hoàn thành" → Cần đổi thành `"delivered"`
- "Đã hủy" → Cần đổi thành `"cancelled"`

### 2. Script cập nhật (chạy trong Firebase Console > Firestore > Rules tab):

```javascript
// Không thể chạy script trực tiếp trong Firestore
// Phải cập nhật thủ công hoặc qua Cloud Functions
```

### 3. Cập nhật thủ công:
1. Mở Firebase Console → Firestore Database
2. Vào collection `orders`
3. Với mỗi document, đổi field `status`:
   - "Đang xử lý" → "processing"
   - "Đang giao" → "shipping"
   - "Hoàn thành" → "delivered"
   - "Đã hủy" → "cancelled"

### 4. Đơn hàng mới:
- Tự động có `status = "pending"` khi tạo
- Admin có thể cập nhật qua app (chức năng quản lý đơn hàng)

---

## Testing

### Test case 1: Tạo đơn hàng mới
1. Thêm sản phẩm vào giỏ
2. Checkout
3. Kiểm tra orders collection:
   - `status` phải là `"pending"`
   - Hiển thị tab "Chờ xác nhận"
   - Badge màu cam (#FFA726)
   - Text "Chờ xác nhận"

### Test case 2: Cập nhật trạng thái
Trong Firestore, đổi `status` của 1 đơn:
- `"pending"` → Hiện tab "Chờ xác nhận", màu cam
- `"processing"` → Hiện tab "Đang chuẩn bị", màu xanh nhạt
- `"shipping"` → Hiện tab "Đang giao", màu xanh dương
- `"delivered"` → Hiện tab "Đã giao", màu xanh lá
- `"cancelled"` → Hiện tab "Đã hủy", màu đỏ

### Test case 3: Hiển thị tổng tiền
- Mỗi order card phải có "Tổng tiền: XXX₫"
- Format: `750.000₫` (có dấu phẩy ngăn cách nghìn)
- Màu đỏ (#FF0000)
- Font size: 18sp, bold

---

## Fix "Tổng tiền = 0₫"

### Nguyên nhân:
Field trong Firestore là `total` (model.Order) nhưng save là `totalAmount` (models.Order)

### Giải pháp:
Đã thống nhất dùng `totalAmount` trong cả 2 model.

### Kiểm tra:
```
orders/{orderId}/totalAmount = 750000 (number)
```

NOT:
```
orders/{orderId}/total = 750000 (sai field name)
```

---

## Code Changes Summary

### 1. OrdersActivity.java
- Tab "Tất cả" → "Chờ xác nhận"
- Status array: `["pending", "processing", "shipping", "delivered", "cancelled"]`

### 2. OrdersFragment.java
- Xóa logic filter "all"
- Filter theo status chính xác

### 3. model/Order.java & models/Order.java
- `getStatusText()`: Trả về tiếng Việt
- `getStatusColor()`: Trả về màu tương ứng
- `getFormattedTotalAmount()`: Format số tiền

### 4. OrderAdapter.java
- Hiển thị badge màu động
- Hiển thị tổng tiền đúng format

---

## Admin Panel (Tương lai)

Để quản lý đơn hàng, admin cần app/web có chức năng:
1. Xem tất cả đơn hàng
2. Cập nhật trạng thái:
   - pending → processing
   - processing → shipping
   - shipping → delivered
   - Bất kỳ → cancelled
3. Xem chi tiết đơn hàng
4. In hóa đơn

---

## Notes

- **Không xóa tab "Tất cả"**: Đã thay bằng "Chờ xác nhận"
- **Status là tiếng Anh**: Dễ quản lý, mở rộng
- **Hiển thị tiếng Việt**: Qua `getStatusText()`
- **Màu động**: Qua `getStatusColor()`
