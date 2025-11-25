# 🚀 Admin Panel Nâng Cao - Tiến Độ Cập Nhật

## ✅ ĐÃ HOÀN THÀNH (Phase 1)

### 1. **Model Enhancements** ✅
- ✅ **SizeStock.java**: Model quản lý stock theo size (S, M, L, XL)
  - Fields: size, stock
  - Methods: isAvailable(), isLowStock(threshold)

- ✅ **Product.java - Enhanced**:
  - `List<SizeStock> sizeStocks` - Stock theo từng size
  - `List<String> colors` - Màu sắc available
  - `boolean isVisible` - Ẩn/hiện sản phẩm
  - `int lowStockThreshold` - Ngưỡng cảnh báo (default: 10)
  - `int totalSold` - Tổng số lượng đã bán (dùng cho top selling)
  - Utility methods:
    - `getTotalStock()` - Tổng stock tất cả sizes
    - `isLowStock()` - Check sắp hết hàng
    - `isOutOfStock()` - Check hết hàng
    - `getStockForSize(String size)` - Lấy stock của size cụ thể
    - `updateStockForSize(String size, int newStock)` - Update stock size
    - `getStockStatusText()` - Text hiển thị status
    - `getStockStatusColor()` - Color code: 0=red, 1=orange, 2=green

- ✅ **OrderStatus.java**: Constants và helpers cho order status
  - Constants: PENDING, PROCESSING, SHIPPING, DELIVERED, CANCELLED
  - `getStatusText(status)` - Chuyển status sang tiếng Việt
  - `canUpdateTo(current, new)` - Validate status transition

### 2. **FirestoreManager - Enhanced Methods** ✅
Đã thêm 11 methods mới:

#### Dashboard Analytics:
- ✅ `getTodayRevenue(listener)` - Doanh thu hôm nay (chỉ tính delivered/shipping/processing)
- ✅ `getTodayOrders(listener)` - Số đơn hàng hôm nay
- ✅ `getTodayNewUsers(listener)` - Số user đăng ký hôm nay
- ✅ `getLowStockProducts(listener)` - Sản phẩm sắp hết hàng
- ✅ `getPendingOrders(listener)` - Đơn hàng chờ xử lý (status=pending)
- ✅ `getTopSellingProducts(limit, listener)` - Top sản phẩm bán chạy (order by totalSold DESC)

#### Product Management:
- ✅ `updateProductStock(productId, size, newStock, listener)` - Update stock theo size
- ✅ `toggleProductVisibility(productId, isVisible, listener)` - Ẩn/hiện sản phẩm
- ✅ `saveProduct(product, listener)` - Thêm hoặc update sản phẩm
- ✅ `saveCategory(category, listener)` - Thêm hoặc update category
- ✅ `deleteCategory(categoryId, listener)` - Xóa category

#### Helper Methods:
- ✅ `getTodayStartTimestamp()` - Lấy timestamp 00:00:00 hôm nay

#### New Listeners:
- OnRevenueLoadedListener
- OnTodayOrdersLoadedListener  
- OnTodayUsersLoadedListener
- OnStockUpdatedListener
- OnVisibilityToggledListener
- OnProductSavedListener
- OnCategorySavedListener
- OnCategoryDeletedListener

### 3. **AdminDashboardFragment - Completely Redesigned** ✅
Thay thế dashboard cũ với 6 metrics mới:

#### Today's Stats (3 cards):
- 💰 **Doanh thu hôm nay** - Tổng revenue từ đơn hàng hôm nay
- 📦 **Đơn hàng hôm nay** - Số lượng orders hôm nay
- 👥 **User mới hôm nay** - Số user đăng ký hôm nay

#### Alert Cards (2 cards):
- ⚠️ **Sản phẩm sắp hết** - Số lượng products có stock <= threshold
- 🔔 **Đơn chờ xử lý** - Số lượng orders status=pending

#### Data Lists (3 sections):
- 🏆 **Top Sản Phẩm Bán Chạy** - Top 5 products (horizontal RecyclerView)
- 📋 **Đơn Hàng Chờ Xử Lý** - List pending orders
- ⚠️ **Sản Phẩm Sắp Hết Hàng** - List low-stock products

### 4. **Adapter Enhancements** ✅
- ✅ AdminProductAdapter.updateData(List<Product>) - Refresh data
- ✅ AdminOrderAdapter.updateData(List<Order>) - Refresh data

### 5. **New Layout** ✅
- ✅ fragment_admin_dashboard.xml - Redesigned với ScrollView, Material Cards, màu sắc đẹp

---

## 🔄 ĐANG LÀM (Phase 2)

### Tab-based Product Management
Cần tạo AdminProductsFragment mới với 2 tabs:

#### Tab 1: Products Management
- CRUD sản phẩm đầy đủ
- Quản lý stock theo size
- Quản lý màu sắc
- Ẩn/hiện sản phẩm
- Upload nhiều ảnh

#### Tab 2: Categories Management  
- CRUD categories
- Set displayOrder
- Active/Inactive

---

## ⏳ CÒN LẠI (Phase 3-5)

### Phase 3: AddEditProductActivity
Form nhập đầy đủ:
- [ ] Tên, mô tả sản phẩm
- [ ] Giá gốc, giá hiện tại (tự tính discount%)
- [ ] Chọn category từ Firestore
- [ ] Nhập stock cho từng size (S, M, L, XL)
- [ ] Chọn nhiều màu (color picker hoặc preset colors)
- [ ] Upload nhiều ảnh (ImagePicker + Firebase Storage)
- [ ] Checkbox isVisible (hiển thị hay ẩn)
- [ ] Nút Save (validate rồi mới lưu)

### Phase 4: OrderDetailActivity  
Chi tiết đơn hàng:
- [ ] Thông tin khách hàng (name, phone, address)
- [ ] Danh sách items (image, name, size, color, quantity, price)
- [ ] Tổng tiền (subtotal, shipping, voucher, total)
- [ ] Timeline trạng thái đơn hàng
- [ ] Nút cập nhật status với dropdown
- [ ] **Realtime sync**: Khi admin update status → user's order list cũng update

### Phase 5: AdminOrdersFragment Enhancement
- [ ] Filter chips: All, Pending, Processing, Shipping, Delivered, Cancelled
- [ ] Click order → navigate to OrderDetailActivity
- [ ] Quick status update dialog
- [ ] **Realtime listener**: Listen to Firestore changes, auto refresh list

### Phase 6: Realtime Order Status Sync
**QUAN TRỌNG**: Khi admin cập nhật status đơn hàng:
1. Update document trong Firestore collection "orders"
2. User app cần listen realtime changes:
   - Nếu user đang mở OrdersActivity/OrderHistoryActivity
   - Setup SnapshotListener cho collection "orders" where userId == currentUser
   - Khi có thay đổi → auto refresh RecyclerView
   - Show notification "Đơn hàng #123 đã được cập nhật"

**Implementation**:
```java
// In User's OrdersActivity/Fragment:
ordersListener = db.collection("orders")
    .whereEqualTo("userId", currentUserId)
    .addSnapshotListener((snapshots, error) -> {
        if (error != null) return;
        // Refresh order list
        List<Order> updatedOrders = new ArrayList<>();
        for (DocumentSnapshot doc : snapshots) {
            updatedOrders.add(doc.toObject(Order.class));
        }
        adapter.updateOrders(updatedOrders);
        // Optional: Show toast if status changed
    });
```

---

## 📊 Cấu trúc Firestore cần thiết

### Collection: products
```json
{
  "id": "product_123",
  "name": "Áo thun basic",
  "description": "Áo thun cotton 100%...",
  "currentPrice": 199000,
  "originalPrice": 299000,
  "category": "Áo",
  "imageUrl": "https://...",
  "sizeStocks": [
    {"size": "S", "stock": 20},
    {"size": "M", "stock": 15},
    {"size": "L", "stock": 8},  // Low stock (< 10)
    {"size": "XL", "stock": 5}  // Low stock
  ],
  "colors": ["Đen", "Trắng", "Xám"],
  "isVisible": true,
  "lowStockThreshold": 10,
  "totalSold": 245,  // For top selling
  "rating": 4.5,
  "reviewCount": 89
}
```

### Collection: orders
```json
{
  "orderId": "order_abc",
  "userId": "user_123",
  "items": [
    {
      "productId": "product_123",
      "productName": "Áo thun basic",
      "imageUrl": "https://...",
      "size": "M",
      "color": "Đen",
      "quantity": 2,
      "price": 199000
    }
  ],
  "total": 428000,
  "subtotal": 398000,
  "shippingFee": 30000,
  "voucherDiscount": 0,
  "status": "pending",  // pending → processing → shipping → delivered
  "createdAt": 1732521600000,
  "shippingAddress": "123 Nguyễn Huệ, Q1, HCM",
  "recipientName": "Nguyễn Văn A",
  "phoneNumber": "0901234567",
  "paymentMethod": "COD"
}
```

### Collection: users
```json
{
  "userId": "user_123",
  "email": "user@example.com",
  "name": "Nguyễn Văn A",
  "phone": "0901234567",
  "role": "user",  // or "admin"
  "createdAt": 1732521600000  // For today's new users
}
```

### Collection: categories
```json
{
  "id": "cat_123",
  "name": "Áo",
  "description": "Các loại áo",
  "imageUrl": "https://...",
  "displayOrder": 1,
  "isActive": true
}
```

---

## 🎯 Priority Roadmap

### HIGH PRIORITY (Làm ngay):
1. ✅ Dashboard với 6 metrics mới
2. ⏳ AdminProductsFragment với 2 tabs (Products + Categories)
3. ⏳ AddEditProductActivity (form CRUD product đầy đủ)

### MEDIUM PRIORITY (Tuần sau):
4. OrderDetailActivity
5. AdminOrdersFragment với filters
6. Realtime order status sync

### LOW PRIORITY (Optional):
7. Image upload to Firebase Storage
8. Push notifications
9. Export reports (Excel/PDF)
10. Analytics charts (MPAndroidChart)

---

## 🐛 Known Issues / Notes

### Build Status:
✅ **BUILD SUCCESSFUL** - Tất cả code compile không lỗi

### Missing Resources:
- Cần có color `background_light` trong colors.xml (hoặc thay bằng #F5F5F5)
- Cần có drawable `ic_orders.xml` (hoặc dùng icon khác)

### Data Requirements:
- Products phải có field `totalSold` để sort top selling
- Users phải có field `createdAt` để đếm new users hôm nay
- Orders phải có field `createdAt` để filter hôm nay

### Testing Checklist:
- [ ] Tạo sample data trên Firestore với fields mới
- [ ] Test getTodayRevenue() với orders hôm nay
- [ ] Test getLowStockProducts() với products có stock <= threshold
- [ ] Test getTopSellingProducts() với totalSold
- [ ] Test dashboard UI hiển thị đúng
- [ ] Test realtime sync order status (chưa implement)

---

## 📝 Next Steps

### Bước 1: Test Dashboard
1. Vào Firebase Console > Firestore
2. Thêm field `totalSold` vào một số products (VD: 100, 50, 30...)
3. Thêm field `createdAt` vào users (set timestamp hôm nay)
4. Tạo vài orders với createdAt hôm nay
5. Run app, login admin, vào Dashboard
6. Verify các số liệu hiển thị đúng

### Bước 2: Implement Products Tab
1. Read AdminProductsFragment.java hiện tại
2. Redesign thành TabLayout với 2 tabs
3. ProductsTabFragment: List products với CRUD
4. CategoriesTabFragment: List categories với CRUD

### Bước 3: Implement AddEditProduct
1. Create activity_add_edit_product.xml
2. Form nhập đầy đủ fields
3. Size stock management (dynamic EditTexts)
4. Color selection (Chips hoặc dropdown)
5. Image upload (optional, có thể để URL manual trước)

### Bước 4: Order Detail & Realtime Sync
1. Create OrderDetailActivity
2. Display full order info
3. Status update với validation
4. Implement SnapshotListener cho realtime
5. Test: Admin update → User nhận realtime

---

**Last Updated**: 2025-11-25  
**Build Status**: ✅ SUCCESS  
**APK**: app/build/outputs/apk/debug/app-debug.apk  
**Next Task**: Implement AdminProductsFragment với 2 tabs

