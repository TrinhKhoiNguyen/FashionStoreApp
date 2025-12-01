# 🎁 Admin Panel - Quản lý Voucher

## Tổng quan

Module quản lý voucher cho phép admin tạo, chỉnh sửa, xóa và theo dõi các voucher giảm giá. Hệ thống hỗ trợ đầy đủ các loại voucher phổ biến trong thương mại điện tử.

---

## 🎯 Tính năng chính

### 1. **Danh sách Voucher**
- ✅ Hiển thị tất cả voucher với đầy đủ thông tin
- 🔍 Tìm kiếm theo mã voucher
- 🏷️ Lọc theo:
  - Tất cả
  - Active (đang hoạt động)
  - Inactive (đã tắt)
  - Hết hạn
  - Giảm % (Percent)
  - Giảm cố định (Fixed)
- 📊 Hiển thị trạng thái với màu sắc:
  - 🟢 **Active**: Voucher đang hoạt động
  - 🔴 **Hết hạn**: Voucher đã quá hạn sử dụng
  - 🟠 **Hết lượt**: Voucher đã hết số lượng
  - ⚪ **Inactive**: Voucher bị vô hiệu hóa

### 2. **Thông tin Voucher**
Mỗi voucher hiển thị:
- **Mã voucher**: CODE (viết hoa)
- **Loại**: Giảm cố định hoặc giảm %
- **Giá trị**: Số tiền hoặc % giảm
- **Giảm tối đa**: Chỉ áp dụng cho loại giảm %
- **Đơn tối thiểu**: Giá trị đơn hàng tối thiểu để áp dụng
- **Số lượng**: Còn lại / Tổng số (với màu cảnh báo)
- **Thời hạn**: Ngày bắt đầu - Ngày kết thúc
- **Mô tả**: Mô tả chi tiết voucher

### 3. **Thêm Voucher Mới**

**Form nhập liệu:**
- **Mã voucher** *(bắt buộc)*
  - Viết hoa, không dấu
  - Ít nhất 3 ký tự
  - Chỉ chứa chữ cái và số
  - Không trùng với voucher khác

- **Loại voucher** *(bắt buộc)*
  - Giảm cố định (₫)
  - Giảm theo % (Percent)

- **Giá trị** *(bắt buộc)*
  - Với loại cố định: Số tiền > 0
  - Với loại %: Từ 1-100

- **Giảm tối đa**
  - Chỉ hiển thị khi chọn loại %
  - Giới hạn số tiền giảm tối đa

- **Đơn hàng tối thiểu** *(bắt buộc)*
  - Giá trị đơn hàng tối thiểu để áp dụng voucher
  - Có thể = 0 (không yêu cầu)

- **Số lượng** *(bắt buộc)*
  - Số lượng voucher có thể sử dụng
  - Phải > 0

- **Ngày bắt đầu** *(bắt buộc)*
  - Chọn từ DatePicker
  - Không được trước ngày hiện tại

- **Ngày kết thúc** *(bắt buộc)*
  - Chọn từ DatePicker
  - Phải sau ngày bắt đầu
  - Phải sau ngày hiện tại

- **Mô tả**
  - Mô tả chi tiết về voucher (tùy chọn)
  - Tối đa 200 ký tự

- **Trạng thái**
  - Switch Active/Inactive
  - Mặc định: Active

**Validation:**
- ❌ Mã voucher trùng
- ❌ Giá trị không hợp lệ
- ❌ Ngày không hợp lý
- ❌ Số lượng <= 0

### 4. **Chỉnh sửa Voucher**

**Lưu ý:**
- ✅ Có thể sửa tất cả thông tin NGOẠI TRỪ mã voucher
- ✅ Giữ nguyên số lượng đã sử dụng
- ✅ Có thể kích hoạt/vô hiệu hóa
- ✅ Có nút xóa voucher

### 5. **Xóa Voucher**

**Quy trình:**
1. Click nút **Xóa** hoặc menu **⋮** → Xóa
2. Xác nhận trong dialog
3. Voucher bị xóa vĩnh viễn khỏi Firestore

**⚠️ Cảnh báo:**
- Không thể khôi phục sau khi xóa
- Nên vô hiệu hóa thay vì xóa nếu voucher đã được sử dụng

### 6. **Kích hoạt / Vô hiệu hóa**

**Cách thực hiện:**
- Click nút **Kích hoạt** / **Vô hiệu hóa**
- Hoặc menu **⋮** → Đổi trạng thái
- Voucher inactive sẽ không thể sử dụng khi checkout

---

## 📋 Cấu trúc Dữ liệu Firestore

### Collection: `vouchers`

```json
{
  "code": "NEWYEAR2025",
  "type": "percent",
  "amount": 15,
  "maxDiscount": 100000,
  "minOrder": 299000,
  "quantity": 500,
  "usedCount": 123,
  "startAt": 1733011200000,
  "endAt": 1735689599000,
  "active": true,
  "description": "Voucher giảm giá đầu năm",
  "createdAt": 1733011200000,
  "updatedAt": 1733011200000
}
```

**Field Types:**
- `code`: String (uppercase, unique)
- `type`: String ("fixed" | "percent")
- `amount`: Number (tiền hoặc %)
- `maxDiscount`: Number (chỉ cho percent)
- `minOrder`: Number (đơn tối thiểu)
- `quantity`: Number (tổng số voucher)
- `usedCount`: Number (số đã dùng)
- `startAt`: Number (epoch milliseconds)
- `endAt`: Number (epoch milliseconds)
- `active`: Boolean
- `description`: String (optional)
- `createdAt`: Number (epoch milliseconds)
- `updatedAt`: Number (epoch milliseconds)

---

## 🔒 Firestore Security Rules

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    
    function isAdmin() {
      return request.auth != null && 
             get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role == 'admin';
    }
    
    // Vouchers
    match /vouchers/{voucherId} {
      // Everyone can read active vouchers (for checkout)
      allow read: if true;
      
      // Only admin can write
      allow create, update, delete: if isAdmin();
    }
  }
}
```

---

## 🎨 UI Components

### Fragment: `AdminVouchersFragment`
- SearchBar với real-time filter
- ChipGroup cho filter nhanh
- RecyclerView với LinearLayoutManager
- FloatingActionButton để thêm voucher
- Empty state khi không có voucher

### Activity: `AdminVoucherFormActivity`
- Material Design TextInputLayout
- AutoCompleteTextView cho dropdown loại voucher
- DatePickerDialog cho chọn ngày
- SwitchMaterial cho active/inactive
- Validation realtime khi nhập

### Adapter: `AdminVoucherAdapter`
- Filter theo query và type
- Popup menu cho actions
- Color-coded status badges
- Dynamic UI dựa trên voucher type

---

## 💡 Ví dụ Voucher

### 1. Voucher giảm 50.000₫
```
Mã: SAVE50K
Loại: Giảm cố định
Giá trị: 50000
Đơn tối thiểu: 299000
Số lượng: 100
```

### 2. Voucher giảm 15%
```
Mã: SALE15
Loại: Giảm %
Giá trị: 15
Giảm tối đa: 100000
Đơn tối thiểu: 500000
Số lượng: 200
```

### 3. Voucher Freeship
```
Mã: FREESHIP
Loại: Giảm cố định
Giá trị: 30000
Đơn tối thiểu: 0
Số lượng: 500
```

---

## 🚀 Cách sử dụng

### Bước 1: Truy cập Admin Panel
1. Đăng nhập với tài khoản admin
2. Vào **Profile** → **Admin Panel**
3. Chọn tab **Vouchers**

### Bước 2: Thêm Voucher
1. Click nút **+** (FAB)
2. Điền đầy đủ thông tin
3. Click **Lưu Voucher**

### Bước 3: Quản lý Voucher
- **Tìm kiếm**: Gõ mã voucher vào search bar
- **Lọc**: Click chip để lọc nhanh
- **Chỉnh sửa**: Click nút **Chỉnh sửa**
- **Xóa**: Click menu **⋮** → Xóa
- **Đổi trạng thái**: Click nút **Kích hoạt/Vô hiệu hóa**

---

## ⚠️ Lưu ý quan trọng

### Bảo mật:
- ✅ Chỉ admin mới có quyền CRUD voucher
- ✅ Mã voucher tự động chuyển thành chữ hoa
- ✅ Validate tất cả input trước khi lưu
- ❌ Không cho phép mã voucher trùng

### Best Practices:
- 📝 Đặt tên mã voucher dễ nhớ, có ý nghĩa
- 📅 Thiết lập thời hạn hợp lý
- 💰 Cân nhắc giá trị giảm và đơn tối thiểu
- 📊 Theo dõi số lượng còn lại
- 🔄 Vô hiệu hóa thay vì xóa voucher đã dùng

### Troubleshooting:
- **Voucher không áp dụng được?**
  - Kiểm tra trạng thái Active
  - Kiểm tra thời hạn
  - Kiểm tra số lượng còn lại
  - Kiểm tra đơn hàng tối thiểu

- **Không thể tạo voucher?**
  - Mã voucher đã tồn tại
  - Thiếu thông tin bắt buộc
  - Giá trị không hợp lệ
  - Ngày không hợp lý

---

## 🔧 Tích hợp với Checkout

Khi user áp dụng voucher:
```java
firestoreManager.getVoucherByCode("NEWYEAR2025", new OnVoucherLoadedListener() {
    @Override
    public void onVoucherLoaded(Voucher voucher) {
        // Tính giảm giá
        double discount = 0;
        if ("fixed".equals(voucher.getType())) {
            discount = voucher.getAmount();
        } else if ("percent".equals(voucher.getType())) {
            discount = orderTotal * voucher.getAmount() / 100;
            if (discount > voucher.getMaxDiscount()) {
                discount = voucher.getMaxDiscount();
            }
        }
        
        // Áp dụng và increment usedCount
        applyDiscount(discount);
        firestoreManager.incrementVoucherUsedCount(voucher.getId());
    }
    
    @Override
    public void onError(String error) {
        showError(error);
    }
});
```

---

## 📊 Báo cáo & Thống kê (Tương lai)

### Tính năng mở rộng:
- 📈 Thống kê voucher được sử dụng nhiều nhất
- 💰 Tổng tiền giảm giá theo voucher
- 👥 Số người dùng sử dụng mỗi voucher
- 📅 Biểu đồ sử dụng theo thời gian
- 🎯 Tỷ lệ chuyển đổi của voucher

---

**Version**: 1.0  
**Last Updated**: December 2024  
**Author**: Fashion Store Team

---

## 📞 Hỗ trợ

Nếu gặp vấn đề:
1. Kiểm tra Firestore Rules
2. Xem Logcat để debug
3. Đảm bảo có quyền admin
4. Kiểm tra kết nối Firebase

**Happy Coding! 🎉**
