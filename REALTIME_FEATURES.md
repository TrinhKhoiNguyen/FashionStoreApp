# 🔄 Real-time Firestore Integration Guide

## Tính năng đã implement

### 1. ✅ Real-time Product Updates (Cập nhật sản phẩm tự động)

**Cách hoạt động:**
- Khi bạn thêm/sửa/xóa sản phẩm trên Firestore Console, app sẽ **tự động cập nhật** không cần refresh
- Áp dụng cho tất cả categories: Retro Sports, Outlet, Áo Thun, Áo Polo, Áo Sơ Mi, Áo Hoodie

**Code location:**
- `MainActivity.java` - method `loadProductsByCategory()` sử dụng `addSnapshotListener()`
- `CategoryProductsActivity.java` - method `setupRealtimeListener()`

### 2. ✅ View All Products by Category (Xem tất cả sản phẩm)

**Activity mới:** `CategoryProductsActivity`
- Hiển thị toàn bộ sản phẩm của một category
- Layout: Grid 2 cột
- Real-time updates: Tự động sync với Firestore

**Cách sử dụng:**
- Click nút "Xem tất cả" trên MainActivity
- Mỗi category có nút riêng:
  - "Xem tất cả Retro Sports" → categoryId: `retro-sports`
  - "Xem tất cả Outlet" → categoryId: `outlet`
  - "Xem tất cả Áo Thun" → categoryId: `ao-thun`
  - "Xem tất cả Áo Polo" → categoryId: `ao-polo`

## 📱 Test Real-time Updates

### Bước 1: Thêm sản phẩm mới trên Firestore

1. Mở Firebase Console: https://console.firebase.google.com/
2. Vào **Firestore Database** → **products** collection
3. Click **"Add document"**
4. Nhập dữ liệu:

```json
{
  "id": "product_new_001",
  "name": "Áo Thun Mới Real-time",
  "description": "Test real-time sync",
  "currentPrice": 299000,
  "originalPrice": 499000,
  "discountPercent": 40,
  "imageUrl": "product1",
  "category": "ao-thun",
  "isNew": true,
  "hasVoucher": false,
  "voucherText": "",
  "isFavorite": false,
  "stockQuantity": 100,
  "rating": 4.5,
  "reviewCount": 10,
  "sizes": ["M", "L", "XL"],
  "colors": ["Đen", "Trắng"],
  "createdAt": 1700000000000
}
```

5. Click **"Save"**

### Bước 2: Xem app tự động cập nhật

- **Không cần restart app!**
- Sản phẩm mới sẽ xuất hiện ngay trong list "Áo Thun" trên MainActivity
- Vào "Xem tất cả Áo Thun" → sản phẩm mới cũng hiển thị

### Bước 3: Sửa sản phẩm

1. Trong Firestore Console, click vào sản phẩm vừa tạo
2. Sửa field `name`: "Áo Thun Đã Sửa"
3. Click **"Update"**
4. **App tự động cập nhật** tên sản phẩm

### Bước 4: Xóa sản phẩm

1. Click vào sản phẩm trong Firestore Console
2. Click nút **"Delete document"**
3. **App tự động xóa** sản phẩm khỏi list

## 🔥 Real-time Features

### Products (Sản phẩm)
✅ Tự động thêm sản phẩm mới  
✅ Tự động cập nhật thông tin sản phẩm  
✅ Tự động xóa sản phẩm  
✅ Áp dụng cho tất cả categories  
✅ Hoạt động trên MainActivity (5 items)  
✅ Hoạt động trên CategoryProductsActivity (toàn bộ)  

### Favorites (Yêu thích)
✅ Tự động sync khi thêm/xóa favorite  
✅ Real-time trong FavoritesActivity  
✅ Sync across devices (cùng user)  

### Cart (Giỏ hàng)
✅ Tự động sync khi thêm/xóa item  
✅ Real-time updates badge count  

## 🎯 Category IDs Reference

| Category Name | Category ID | Button | Activity |
|---------------|-------------|--------|----------|
| Retro Sports | `retro-sports` | btnViewAllRetro | MainActivity |
| Outlet | `outlet` | btnViewAllOutlet | MainActivity |
| Áo Thun | `ao-thun` | btnViewAllShirts | MainActivity |
| Áo Polo | `ao-polo` | btnViewAllPolo | MainActivity |
| Áo Sơ Mi | `ao-so-mi` | - | (No button yet) |
| Áo Hoodie | `ao-hoodie` | - | (No button yet) |

## 💡 Cách thêm Category mới

### Bước 1: Thêm category trong Firestore

```json
// Collection: categories
// Document ID: ao-khoac
{
  "id": "ao-khoac",
  "name": "Áo Khoác",
  "description": "Áo khoác các loại",
  "imageUrl": "",
  "displayOrder": 5,
  "isActive": true
}
```

### Bước 2: Thêm sản phẩm với category mới

```json
// Collection: products
{
  ...
  "category": "ao-khoac",
  ...
}
```

### Bước 3: Thêm RecyclerView trong MainActivity

**activity_main.xml:**
```xml
<androidx.recyclerview.widget.RecyclerView
    android:id="@+id/aoKhoacRecyclerView"
    android:layout_width="match_parent"
    android:layout_height="wrap_content" />

<Button
    android:id="@+id/btnViewAllAoKhoac"
    android:text="Xem tất cả Áo Khoác" />
```

**MainActivity.java:**
```java
// Khai báo
private RecyclerView aoKhoacRecyclerView;
private ProductAdapter aoKhoacAdapter;
private Button btnViewAllAoKhoac;

// initViews()
aoKhoacRecyclerView = findViewById(R.id.aoKhoacRecyclerView);
btnViewAllAoKhoac = findViewById(R.id.btnViewAllAoKhoac);

// setupRecyclerViews()
setupHorizontalRecyclerView(aoKhoacRecyclerView);

// loadProductsFromFirestore()
loadProductsByCategory("ao-khoac", aoKhoacRecyclerView, products -> {
    aoKhoacAdapter = new ProductAdapter(MainActivity.this, products, MainActivity.this);
    aoKhoacRecyclerView.setAdapter(aoKhoacAdapter);
});

// setupClickListeners()
btnViewAllAoKhoac.setOnClickListener(v -> {
    openCategoryProducts("ao-khoac", "Áo Khoác");
});

// notifyAdaptersDataChanged()
if (aoKhoacAdapter != null)
    aoKhoacAdapter.notifyDataSetChanged();
```

## 🛠️ Technical Details

### Firestore Listeners

**MainActivity.java:**
```java
// Lưu trữ listeners
private Map<String, ListenerRegistration> categoryListeners = new HashMap<>();

// Setup listener
ListenerRegistration listener = FirebaseFirestore.getInstance()
    .collection("products")
    .whereEqualTo("category", categoryId)
    .limit(5)
    .addSnapshotListener((snapshots, error) -> {
        // Auto update UI when data changes
    });

// Cleanup khi activity destroy
@Override
protected void onDestroy() {
    for (ListenerRegistration listener : categoryListeners.values()) {
        listener.remove();
    }
}
```

**CategoryProductsActivity.java:**
```java
private ListenerRegistration productsListener;

productsListener = FirebaseFirestore.getInstance()
    .collection("products")
    .whereEqualTo("category", categoryId)
    .addSnapshotListener((snapshots, error) -> {
        // Real-time updates cho tất cả sản phẩm
    });

@Override
protected void onDestroy() {
    if (productsListener != null) {
        productsListener.remove();
    }
}
```

## 🔐 Firestore Security Rules

**Quan trọng:** Đảm bảo rules cho phép read products:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    
    // Products - Public read for real-time updates
    match /products/{productId} {
      allow read: if true;  // ← Quan trọng!
      allow write: if request.auth != null;
    }
    
    // Categories - Public read
    match /categories/{categoryId} {
      allow read: if true;
      allow write: if request.auth != null;
    }
  }
}
```

## 📊 Performance Optimization

### Limits đã áp dụng:
- **MainActivity**: Giới hạn 5 sản phẩm mỗi category (`.limit(5)`)
- **CategoryProductsActivity**: Load toàn bộ sản phẩm (no limit)

### Memory Management:
- ✅ Tất cả listeners được cleanup trong `onDestroy()`
- ✅ Không memory leaks
- ✅ Listeners tự động reconnect khi app resume

## 🎉 Kết quả

### Trước khi có Real-time:
❌ Phải restart app để thấy sản phẩm mới  
❌ Dữ liệu cũ khi Firestore thay đổi  
❌ Không sync giữa các devices  

### Sau khi có Real-time:
✅ Thêm sản phẩm trên Firestore → App hiển thị ngay lập tức  
✅ Sửa/Xóa sản phẩm → UI tự động cập nhật  
✅ Sync real-time across devices  
✅ Không cần pull-to-refresh  
✅ UX mượt mà, chuyên nghiệp  

## 🧪 Testing Checklist

- [ ] Thêm sản phẩm mới trên Firestore → Kiểm tra MainActivity tự động hiển thị
- [ ] Sửa tên sản phẩm → Kiểm tra tên thay đổi trong app
- [ ] Xóa sản phẩm → Kiểm tra sản phẩm biến mất khỏi list
- [ ] Click "Xem tất cả" → Kiểm tra CategoryProductsActivity mở đúng
- [ ] Thêm sản phẩm khi đang ở CategoryProductsActivity → Kiểm tra tự động thêm vào list
- [ ] Tắt WiFi → Kiểm tra app vẫn hiển thị dữ liệu cache
- [ ] Bật lại WiFi → Kiểm tra sync lại với Firestore

## 📝 Notes

- Real-time listeners sử dụng **network bandwidth**, nên chỉ dùng khi cần
- Firestore có **quota miễn phí**: 50,000 reads/day
- Mỗi listener update = 1 read operation
- Nếu vượt quota, có thể giảm số lượng listeners hoặc thêm cache strategy
