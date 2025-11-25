# Admin Panel - Hướng dẫn sử dụng

## 🔐 Thiết lập Admin trên Firebase

### Bước 1: Tạo tài khoản Admin
1. Đăng ký tài khoản mới trên app hoặc sử dụng tài khoản hiện có
2. Lấy User UID từ Firebase Authentication

### Bước 2: Cấp quyền Admin trên Firestore
1. Vào **Firebase Console** > **Firestore Database**
2. Tìm collection `users`
3. Tìm document có ID = User UID của tài khoản cần cấp quyền admin
4. Thêm/sửa field:
   - Field name: `role`
   - Type: `string`
   - Value: `admin`
5. Click **Update**

### Ví dụ Document User Admin:
```json
{
  "userId": "abc123xyz",
  "email": "admin@fashionstore.com",
  "name": "Admin Store",
  "phone": "0123456789",
  "role": "admin",  // <-- Field quan trọng
  "createdAt": 1700000000000
}
```

---

## 📱 Tính năng Admin Panel

### 1. **Dashboard (Thống kê)**
- 📊 Tổng doanh thu
- 📦 Tổng đơn hàng
- 🛍️ Tổng sản phẩm
- 👥 Tổng người dùng
- 📋 Danh sách đơn hàng gần đây

### 2. **Quản lý Sản phẩm**
- ✅ Xem danh sách tất cả sản phẩm
- 🔍 Tìm kiếm sản phẩm theo tên, danh mục
- ➕ Thêm sản phẩm mới
- ✏️ Chỉnh sửa thông tin sản phẩm
- 🗑️ Xóa sản phẩm
- 📊 Hiển thị số lượng tồn kho với màu cảnh báo:
  - 🟢 Xanh: >= 20 sản phẩm
  - 🟠 Cam: 10-19 sản phẩm
  - 🔴 Đỏ: < 10 sản phẩm

### 3. **Quản lý Đơn hàng**
- ✅ Xem tất cả đơn hàng
- 🔍 Lọc đơn hàng theo trạng thái:
  - Tất cả
  - Đang xử lý
  - Đang giao
  - Hoàn thành
  - Đã hủy
- 👁️ Xem chi tiết đơn hàng
- 🔄 Cập nhật trạng thái đơn hàng
- 📅 Sắp xếp theo thời gian tạo

### 4. **Quản lý Người dùng**
- ✅ Xem danh sách tất cả người dùng
- 🔍 Tìm kiếm người dùng theo tên, email, SĐT
- 👤 Xem chi tiết thông tin người dùng
- 🔐 Thay đổi quyền (user ↔ admin)
- 🚫 Vô hiệu hóa tài khoản (nếu cần)
- 🏅 Hiển thị badge Admin cho tài khoản admin

---

## 🎯 Cách sử dụng

### Truy cập Admin Panel:
1. Đăng nhập bằng tài khoản có `role = "admin"`
2. Vào **Tài khoản** (Profile)
3. Sẽ thấy card **⚡ Admin Panel** màu vàng
4. Click vào để mở Admin Panel

### Quản lý Sản phẩm:
1. Vào tab **Sản phẩm**
2. Tìm kiếm hoặc scroll để xem danh sách
3. Click nút **✏️** để chỉnh sửa
4. Click nút **🗑️** để xóa (có xác nhận)
5. Click nút **➕** (FAB) để thêm sản phẩm mới

### Quản lý Đơn hàng:
1. Vào tab **Đơn hàng**
2. Chọn chip filter để lọc theo trạng thái
3. Click **Xem chi tiết** để xem thông tin đơn hàng
4. Click **Cập nhật** để thay đổi trạng thái:
   - Đang xử lý → Đang giao
   - Đang giao → Hoàn thành
   - Hoặc Hủy đơn hàng

### Quản lý Người dùng:
1. Vào tab **Người dùng**
2. Tìm kiếm người dùng cần quản lý
3. Click vào người dùng hoặc nút **⋮** để xem tùy chọn:
   - **Xem chi tiết**: Thông tin đầy đủ
   - **Thay đổi quyền**: Chuyển user ↔ admin
   - **Vô hiệu hóa**: Khóa tài khoản

---

## ⚠️ Lưu ý quan trọng

### Bảo mật:
- ⚠️ Chỉ cấp quyền admin cho người đáng tin cậy
- ⚠️ Không chia sẻ thông tin đăng nhập admin
- ⚠️ Thường xuyên kiểm tra danh sách admin

### Firestore Rules:
Cần thiết lập rules để bảo vệ admin operations:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    
    // Helper function to check if user is admin
    function isAdmin() {
      return request.auth != null && 
             get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role == 'admin';
    }
    
    // Products - Admin can write, all can read
    match /products/{productId} {
      allow read: if true;
      allow write: if isAdmin();
    }
    
    // Orders - Admin can read all, users can read their own
    match /orders/{orderId} {
      allow read: if isAdmin() || 
                     (request.auth != null && resource.data.userId == request.auth.uid);
      allow create: if request.auth != null;
      allow update: if isAdmin();
      allow delete: if isAdmin();
    }
    
    // Users - Admin can read/update all
    match /users/{userId} {
      allow read: if isAdmin() || request.auth.uid == userId;
      allow write: if request.auth.uid == userId;
      allow update: if isAdmin(); // Admin can update any user
    }
    
    // Categories - Admin can write
    match /categories/{categoryId} {
      allow read: if true;
      allow write: if isAdmin();
    }
  }
}
```

### Permissions:
- ✅ Admin có thể CRUD tất cả products
- ✅ Admin có thể xem và update tất cả orders
- ✅ Admin có thể xem và update user roles
- ❌ Admin KHÔNG thể xóa users (chỉ vô hiệu hóa)

---

## 🚀 Tính năng nâng cao (Tương lai)

### Thêm sản phẩm:
- Form nhập đầy đủ thông tin sản phẩm
- Upload multiple images
- Chọn danh mục, sizes, colors
- Thiết lập giá, giảm giá, stock

### Thống kê nâng cao:
- Biểu đồ doanh thu theo ngày/tháng
- Top sản phẩm bán chạy
- Tỷ lệ chuyển đổi đơn hàng
- Phân tích khách hàng

### Notifications:
- Thông báo đơn hàng mới
- Cảnh báo sản phẩm sắp hết hàng
- Báo cáo hàng ngày/tuần

### Export Data:
- Xuất báo cáo Excel/CSV
- In hóa đơn, phiếu giao hàng

---

## 📞 Hỗ trợ

Nếu gặp vấn đề với Admin Panel, vui lòng:
1. Kiểm tra lại quyền admin trong Firestore
2. Đăng xuất và đăng nhập lại
3. Kiểm tra Firestore Rules
4. Xem logs trong Logcat

---

**Version**: 1.0  
**Last Updated**: November 2024
