# Firestore Rules và Indexes Configuration

## 1. Firestore Security Rules

Vào Firebase Console → Firestore Database → Rules và paste đoạn code sau:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    
    // Allow users to read their own user document
    match /users/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
      
      // Allow users to read/write their own addresses
      match /addresses/{addressId} {
        allow read, write: if request.auth != null && request.auth.uid == userId;
      }
      
      // Allow users to read/write their own payment methods
      match /paymentMethods/{paymentId} {
        allow read, write: if request.auth != null && request.auth.uid == userId;
      }
    }
    
    // Allow authenticated users to read products
    match /products/{productId} {
      allow read: if request.auth != null;
      allow write: if false; // Only admins should write (use Firebase Admin SDK)
    }
    
    // Allow authenticated users to read categories
    match /categories/{categoryId} {
      allow read: if request.auth != null;
      allow write: if false;
    }
    
    // Allow users to read/write their own orders
    match /orders/{orderId} {
      allow read: if request.auth != null && request.auth.uid == resource.data.userId;
      allow create: if request.auth != null && request.auth.uid == request.resource.data.userId;
      allow update: if false; // Orders shouldn't be updated by users
      allow delete: if false;
    }
    
    // Allow authenticated users to read active vouchers
    match /vouchers/{voucherId} {
      allow read: if request.auth != null;
      allow write: if false;
    }
    
    // Allow users to read/write their own cart
    match /carts/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
    
    // Allow users to read/write their own favorites
    match /favorites/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
  }
}
```

## 2. Firestore Indexes

### Option A: Tự động tạo index (Khuyến nghị)

1. Chạy app và mở OrdersActivity
2. Khi xuất hiện lỗi "FAILED_PRECONDITION", click vào link trong logcat
3. Firebase sẽ tự động tạo index cho bạn
4. Đợi vài phút để index được build

### Option B: Tạo index thủ công

Vào Firebase Console → Firestore Database → Indexes → Composite:

**Index cho Orders Collection:**
- Collection ID: `orders`
- Fields to index:
  - Field: `userId` | Order: Ascending
  - Field: `createdAt` | Order: Descending
- Query scope: Collection

Click **Create Index** và đợi vài phút.

## 3. Test lại app

Sau khi cập nhật Rules và tạo Index, test lại:
- OffersActivity - Should load vouchers
- OrdersActivity - Should load orders với sorting

## 4. Lưu ý

- Firestore Rules đảm bảo user chỉ đọc được orders của chính họ
- Composite index cần thiết cho query có combine where + orderBy
- Index có thể mất vài phút để build xong
