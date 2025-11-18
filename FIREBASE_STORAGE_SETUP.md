# Firebase Storage Rules Setup

## Storage Rules Configuration

Để cho phép người dùng upload ảnh review, bạn cần cấu hình Firebase Storage Rules.

### Bước 1: Truy cập Firebase Console
1. Vào [Firebase Console](https://console.firebase.google.com/)
2. Chọn project của bạn
3. Vào **Storage** > **Rules**

### Bước 2: Cấu hình Rules

Paste đoạn code sau vào Firebase Storage Rules:

```
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    
    // Allow read for all authenticated users
    match /{allPaths=**} {
      allow read: if request.auth != null;
    }
    
    // Review images
    match /reviews/{productId}/{imageId} {
      // Allow write if authenticated and file is image and size < 5MB
      allow write: if request.auth != null
                   && request.resource.contentType.matches('image/.*')
                   && request.resource.size < 5 * 1024 * 1024;
      
      // Allow read for everyone
      allow read: if true;
    }
    
    // Profile images
    match /profiles/{userId}/{imageId} {
      // Allow write only to own profile
      allow write: if request.auth != null
                   && request.auth.uid == userId
                   && request.resource.contentType.matches('image/.*')
                   && request.resource.size < 5 * 1024 * 1024;
      
      // Allow read for everyone
      allow read: if true;
    }
    
    // Product images (Admin only in production)
    match /products/{productId}/{imageId} {
      allow read: if true;
      // Allow write for testing (change to admin check in production)
      allow write: if request.auth != null;
    }
  }
}
```

### Bước 3: Publish Rules

Click **Publish** để áp dụng rules.

## Storage Structure

```
storage/
├── reviews/
│   ├── {productId}/
│   │   ├── {uuid}.jpg
│   │   └── {uuid}.jpg
├── profiles/
│   ├── {userId}/
│   │   └── avatar.jpg
└── products/
    ├── {productId}/
    │   ├── image1.jpg
    │   └── image2.jpg
```

## Security Notes

- ✅ Review images: Authenticated users can upload, everyone can read
- ✅ File size limit: 5MB per image
- ✅ Only image files allowed (image/*)
- ✅ Profile images: Users can only upload to their own folder
- ⚠️ In production, consider adding rate limiting and quota management

## Testing

1. Login to the app
2. Go to product detail
3. Click "Viết đánh giá"
4. Click "+" button to add images
5. Select up to 5 images
6. Submit review
7. Images will be uploaded to Firebase Storage and URLs saved in Firestore

## Firestore Review Document Structure

```json
{
  "id": "reviewId",
  "productId": "productId",
  "userId": "userId",
  "userName": "User Name",
  "rating": 5,
  "comment": "Great product!",
  "imageUrls": [
    "https://firebasestorage.googleapis.com/.../image1.jpg",
    "https://firebasestorage.googleapis.com/.../image2.jpg"
  ],
  "timestamp": 1234567890
}
```

## Troubleshooting

### Error: "User does not have permission to access 'reviews/...'"
- Check if user is logged in
- Verify Storage Rules are published
- Check internet connection

### Error: "File size exceeds limit"
- Image must be < 5MB
- Compress images before upload

### Images not displaying
- Check image URLs in Firestore
- Verify READ_EXTERNAL_STORAGE permission granted
- Check Glide integration
