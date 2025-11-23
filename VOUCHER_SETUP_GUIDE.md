# Hướng Dẫn Tạo Voucher trong Firestore

## Cấu trúc Collection `vouchers`

Mỗi document trong collection `vouchers` cần có các field sau:

### Field bắt buộc:
- **code** (string): Mã voucher (viết hoa, VD: "NEWYEAR2025")
- **isActive** (boolean): true = còn hiệu lực, false = đã vô hiệu hóa

### Field tùy chọn (validation):
- **minOrderAmount** (number): Giá trị đơn hàng tối thiểu để áp dụng (VD: 300000)
- **expiryDate** (timestamp): Ngày hết hạn

### Field giảm giá (chọn 1 trong 2):
- **discountAmount** (number): Giảm số tiền cố định (VD: 50000 = giảm 50k)
- **discountPercent** (number): Giảm theo % (VD: 10 = giảm 10%)

### Field bổ sung:
- **maxDiscount** (number): Giảm tối đa khi dùng discountPercent (VD: 100000)

---

## Ví dụ các loại Voucher

### 1. Voucher giảm 50.000₫ cố định
```json
{
  "code": "SAVE50K",
  "isActive": true,
  "discountAmount": 50000,
  "minOrderAmount": 200000,
  "expiryDate": Timestamp(31/12/2025)
}
```
**Ý nghĩa**: Giảm 50k cho đơn hàng từ 200k trở lên, hết hạn 31/12/2025

---

### 2. Voucher giảm 15% (tối đa 100k)
```json
{
  "code": "SALE15",
  "isActive": true,
  "discountPercent": 15,
  "maxDiscount": 100000,
  "minOrderAmount": 300000,
  "expiryDate": Timestamp(31/12/2025)
}
```
**Ý nghĩa**: Giảm 15% tổng đơn (tối đa 100k), đơn hàng tối thiểu 300k

---

### 3. Voucher giảm 20% không giới hạn
```json
{
  "code": "VIP20",
  "isActive": true,
  "discountPercent": 20,
  "minOrderAmount": 500000,
  "expiryDate": Timestamp(31/12/2025)
}
```
**Ý nghĩa**: Giảm 20% không giới hạn, đơn hàng tối thiểu 500k

---

### 4. Voucher freeship (giảm 30k)
```json
{
  "code": "FREESHIP",
  "isActive": true,
  "discountAmount": 30000,
  "expiryDate": Timestamp(31/12/2025)
}
```
**Ý nghĩa**: Giảm 30k (bằng phí ship), không yêu cầu đơn hàng tối thiểu

---

## Cách thêm vào Firestore Console

1. Mở Firebase Console → Firestore Database
2. Tạo collection mới tên `vouchers`
3. Thêm document mới với Auto-ID hoặc custom ID
4. Thêm các field theo mẫu trên
5. Với `expiryDate`: Chọn type = **timestamp**, nhập ngày/giờ hết hạn

---

## Logic Ưu Tiên trong Code

**discountAmount** có ưu tiên cao hơn **discountPercent**

Nếu document có cả 2 field:
- Code sẽ dùng `discountAmount` (giảm cố định)
- Bỏ qua `discountPercent`

**Khuyến nghị**: Chỉ dùng 1 trong 2 field để tránh nhầm lẫn

---

## Test Voucher

### Mã voucher test sẵn (thêm vào Firestore):

#### NEWYEAR2025
```json
{
  "code": "NEWYEAR2025",
  "isActive": true,
  "discountPercent": 10,
  "maxDiscount": 100000,
  "minOrderAmount": 500000,
  "expiryDate": Timestamp(31/12/2025 23:59:59)
}
```

#### WELCOME50
```json
{
  "code": "WELCOME50",
  "isActive": true,
  "discountAmount": 50000,
  "minOrderAmount": 300000,
  "expiryDate": Timestamp(31/12/2025 23:59:59)
}
```

#### FREESHIP30
```json
{
  "code": "FREESHIP30",
  "isActive": true,
  "discountAmount": 30000,
  "expiryDate": Timestamp(31/12/2025 23:59:59)
}
```

---

## Validation Messages trong App

- **Không có mã**: "Vui lòng nhập mã voucher"
- **Mã sai/hết hạn**: "Mã voucher không hợp lệ hoặc đã hết hạn"
- **Đơn hàng < min**: "Đơn hàng tối thiểu XXX₫ để áp dụng voucher này"
- **Hết hạn**: "Voucher đã hết hạn"
- **Không có discount**: "Voucher không hợp lệ"
- **Thành công**: "✓ Áp dụng voucher thành công!"

---

## Debug

Nếu voucher không áp dụng được, kiểm tra Logcat:
```
Filter: CheckoutActivity
```

Xem log:
- "Voucher applied: CODE, Discount: XXXX"
- "Voucher error: ERROR_MESSAGE"
