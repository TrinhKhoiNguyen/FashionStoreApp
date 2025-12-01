# 📸 HƯỚNG DẪN UPLOAD NHIỀU ẢNH SẢN PHẨM LÊN FIREBASE

## ✅ Đã implement:

### 1. **Product Model đã được cập nhật**
- Thêm field `imageUrls` (List<String>) để lưu nhiều URL ảnh
- Getter `getImageUrls()` tự động fallback về `imageUrl` nếu không có
- Setter `setImageUrls()` tự động cập nhật `imageUrl` chính

### 2. **ProductDetailActivity đã hỗ trợ**
- ViewPager2 với ProductImageAdapter
- Swipe qua nhiều ảnh
- Zoom/pinch cho mỗi ảnh
- Image indicators

---

## 🔥 CÁCH UPLOAD NHIỀU ẢNH LÊN FIREBASE

### Phương pháp 1: Upload từ Admin Panel (Recommended)

#### Bước 1: Thêm Image Picker vào AddEditProductActivity

```java
// Trong AddEditProductActivity.java

private static final int PICK_IMAGES_REQUEST = 1;
private ArrayList<Uri> selectedImageUris = new ArrayList<>();
private RecyclerView rvSelectedImages;
private ImageAdapter imageAdapter;

// Setup RecyclerView cho selected images
private void setupImagePicker() {
    Button btnSelectImages = findViewById(R.id.btnSelectImages);
    rvSelectedImages = findViewById(R.id.rvSelectedImages);
    
    // Horizontal RecyclerView
    LinearLayoutManager layoutManager = new LinearLayoutManager(this, 
        LinearLayoutManager.HORIZONTAL, false);
    rvSelectedImages.setLayoutManager(layoutManager);
    
    imageAdapter = new ImageAdapter(selectedImageUris, this);
    rvSelectedImages.setAdapter(imageAdapter);
    
    btnSelectImages.setOnClickListener(v -> {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Chọn ảnh"), PICK_IMAGES_REQUEST);
    });
}

@Override
protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    
    if (requestCode == PICK_IMAGES_REQUEST && resultCode == RESULT_OK) {
        selectedImageUris.clear();
        
        if (data.getClipData() != null) {
            // Multiple images selected
            int count = data.getClipData().getItemCount();
            for (int i = 0; i < count && i < 5; i++) { // Limit to 5 images
                Uri imageUri = data.getClipData().getItemAt(i).getUri();
                selectedImageUris.add(imageUri);
            }
        } else if (data.getData() != null) {
            // Single image selected
            selectedImageUris.add(data.getData());
        }
        
        imageAdapter.notifyDataSetChanged();
    }
}
```

#### Bước 2: Upload Images lên Firebase Storage

```java
private void uploadProductWithImages() {
    if (selectedImageUris.isEmpty()) {
        Toast.makeText(this, "Vui lòng chọn ít nhất 1 ảnh", Toast.LENGTH_SHORT).show();
        return;
    }
    
    // Show loading
    ProgressDialog progressDialog = new ProgressDialog(this);
    progressDialog.setMessage("Đang upload ảnh...");
    progressDialog.show();
    
    List<String> imageUrls = new ArrayList<>();
    final int[] uploadCount = {0};
    final int totalImages = selectedImageUris.size();
    
    // Upload each image
    for (int i = 0; i < selectedImageUris.size(); i++) {
        Uri imageUri = selectedImageUris.get(i);
        String filename = "products/" + productId + "/image_" + i + "_" + 
                          System.currentTimeMillis() + ".jpg";
        
        StorageReference imageRef = FirebaseStorage.getInstance()
                                                   .getReference()
                                                   .child(filename);
        
        imageRef.putFile(imageUri)
            .addOnSuccessListener(taskSnapshot -> {
                imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    imageUrls.add(uri.toString());
                    uploadCount[0]++;
                    
                    // Update progress
                    progressDialog.setMessage("Đang upload ảnh " + uploadCount[0] + "/" + totalImages);
                    
                    // All images uploaded
                    if (uploadCount[0] == totalImages) {
                        progressDialog.dismiss();
                        saveProductToFirestore(imageUrls);
                    }
                });
            })
            .addOnFailureListener(e -> {
                progressDialog.dismiss();
                Toast.makeText(this, "Lỗi upload: " + e.getMessage(), 
                              Toast.LENGTH_SHORT).show();
            });
    }
}

private void saveProductToFirestore(List<String> imageUrls) {
    Product product = new Product();
    product.setId(productId);
    product.setName(productName);
    product.setImageUrls(imageUrls); // Set multiple images
    
    // Save to Firestore
    FirebaseFirestore.getInstance()
        .collection("products")
        .document(productId)
        .set(product)
        .addOnSuccessListener(aVoid -> {
            Toast.makeText(this, "Thêm sản phẩm thành công!", Toast.LENGTH_SHORT).show();
            finish();
        })
        .addOnFailureListener(e -> {
            Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
}
```

---

### Phương pháp 2: Upload trực tiếp qua Firebase Console

#### Bước 1: Upload ảnh lên Firebase Storage

1. Mở [Firebase Console](https://console.firebase.google.com/)
2. Chọn project của bạn
3. Vào **Storage** → **Files**
4. Tạo folder: `products/{productId}/`
5. Upload nhiều ảnh vào folder này
6. Click vào từng ảnh → Copy **Download URL**

#### Bước 2: Cập nhật Firestore

Vào **Firestore Database** → collection `products` → chọn product:

```javascript
// Thêm field imageUrls (type: array)
imageUrls: [
  "https://firebasestorage.googleapis.com/.../image1.jpg",
  "https://firebasestorage.googleapis.com/.../image2.jpg",
  "https://firebasestorage.googleapis.com/.../image3.jpg",
  "https://firebasestorage.googleapis.com/.../image4.jpg"
]
```

---

### Phương pháp 3: Sử dụng Script để import data

```javascript
// upload-product-images.js
const admin = require('firebase-admin');
const serviceAccount = require('./serviceAccountKey.json');

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount),
  storageBucket: 'your-project-id.appspot.com'
});

const db = admin.firestore();
const bucket = admin.storage().bucket();

async function uploadProductImages(productId, imagePaths) {
  const imageUrls = [];
  
  for (let i = 0; i < imagePaths.length; i++) {
    const localPath = imagePaths[i];
    const remotePath = `products/${productId}/image_${i}.jpg`;
    
    // Upload file
    await bucket.upload(localPath, {
      destination: remotePath,
      metadata: {
        contentType: 'image/jpeg'
      }
    });
    
    // Get download URL
    const file = bucket.file(remotePath);
    const [url] = await file.getSignedUrl({
      action: 'read',
      expires: '03-01-2500'
    });
    
    imageUrls.push(url);
  }
  
  // Update Firestore
  await db.collection('products').doc(productId).update({
    imageUrls: imageUrls,
    imageUrl: imageUrls[0] // Main image
  });
  
  console.log(`Uploaded ${imageUrls.length} images for product ${productId}`);
}

// Usage
uploadProductImages('product1', [
  './images/product1_img1.jpg',
  './images/product1_img2.jpg',
  './images/product1_img3.jpg'
]);
```

---

## 📱 CẤU TRÚC DỮ LIỆU FIRESTORE

### Collection: `products`

```json
{
  "id": "product_001",
  "name": "Áo Polo Premium",
  "description": "Áo polo cao cấp...",
  "currentPrice": 299000,
  "originalPrice": 499000,
  "category": "ao-polo",
  
  // Single image (backward compatible)
  "imageUrl": "https://firebasestorage.googleapis.com/.../main.jpg",
  
  // Multiple images (NEW)
  "imageUrls": [
    "https://firebasestorage.googleapis.com/.../image1.jpg",
    "https://firebasestorage.googleapis.com/.../image2.jpg",
    "https://firebasestorage.googleapis.com/.../image3.jpg",
    "https://firebasestorage.googleapis.com/.../image4.jpg"
  ],
  
  "stockQuantity": 50,
  "availableSizes": ["S", "M", "L", "XL"],
  "rating": 4.5,
  "reviewCount": 23
}
```

---

## 🎯 BEST PRACTICES

### 1. **Số lượng ảnh**
- Khuyến nghị: 3-5 ảnh mỗi sản phẩm
- Tối đa: 10 ảnh
- Ít nhất: 1 ảnh

### 2. **Kích thước ảnh**
- Resolution: 1000x1000px hoặc 1200x1200px
- Format: JPEG (tốt nhất cho web)
- File size: < 500KB mỗi ảnh (nén trước khi upload)

### 3. **Đặt tên file**
```
products/
  ├── product_001/
  │   ├── image_0.jpg  (ảnh chính - mặt trước)
  │   ├── image_1.jpg  (góc nghiêng)
  │   ├── image_2.jpg  (mặt sau)
  │   ├── image_3.jpg  (chi tiết)
  │   └── image_4.jpg  (model mặc)
```

### 4. **Thứ tự ảnh quan trọng**
- Ảnh đầu tiên (`imageUrls[0]`) = Ảnh chính hiển thị trong list
- Các ảnh sau theo thứ tự: trước → sau → nghiêng → chi tiết

### 5. **Optimize Images trước khi upload**

```java
// Compress image before upload
private Bitmap compressImage(Uri imageUri) throws IOException {
    Bitmap bitmap = MediaStore.Images.Media.getBitmap(
        getContentResolver(), imageUri);
    
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
    
    byte[] data = baos.toByteArray();
    return BitmapFactory.decodeByteArray(data, 0, data.length);
}
```

---

## 🔧 TESTING

### Test với sample data:

```java
// Trong MainActivity hoặc ProductDetailActivity
Product testProduct = new Product();
testProduct.setId("test_001");
testProduct.setName("Test Product");

List<String> testImages = Arrays.asList(
    "https://via.placeholder.com/1000/FF0000/FFFFFF?text=Image+1",
    "https://via.placeholder.com/1000/00FF00/FFFFFF?text=Image+2",
    "https://via.placeholder.com/1000/0000FF/FFFFFF?text=Image+3"
);
testProduct.setImageUrls(testImages);
```

---

## 📊 FIRESTORE SECURITY RULES

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /products/{productId} {
      // Anyone can read
      allow read: if true;
      
      // Only authenticated users can write
      allow create, update, delete: if request.auth != null;
    }
  }
}
```

## 🗂️ STORAGE SECURITY RULES

```javascript
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    match /products/{productId}/{allPaths=**} {
      // Anyone can read product images
      allow read: if true;
      
      // Only authenticated users can upload
      allow write: if request.auth != null
                   && request.resource.size < 5 * 1024 * 1024  // Max 5MB
                   && request.resource.contentType.matches('image/.*');
    }
  }
}
```

---

## ✅ CHECKLIST

- [x] Product model có field `imageUrls`
- [x] Getter/Setter cho `imageUrls` 
- [x] ProductDetailActivity hỗ trợ nhiều ảnh
- [x] ViewPager2 với zoom support
- [x] Image indicators
- [ ] Admin panel để upload nhiều ảnh (tùy chọn)
- [ ] Image compression trước upload (tùy chọn)
- [ ] Progress indicator khi upload (tùy chọn)

---

## 🚀 NEXT STEPS

1. Build & test app:
```bash
.\gradlew assembleDebug
```

2. Upload test images lên Firebase Storage

3. Cập nhật Firestore với imageUrls array

4. Test swipe qua nhiều ảnh trong ProductDetailActivity

5. Test zoom/pinch cho từng ảnh

---

Bây giờ app của bạn đã sẵn sàng hiển thị nhiều ảnh sản phẩm! 📸✨
