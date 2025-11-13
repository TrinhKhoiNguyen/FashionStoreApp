# Fashion Store App - 160Store

Ứng dụng bán hàng thời trang nam trên Android.

## 📁 Cấu trúc dự án

```
app/src/main/java/com/example/fashionstoreapp/
├── models/              # Data models
│   ├── Product.java     # Model sản phẩm
│   ├── CartItem.java    # Model item trong giỏ hàng
│   └── User.java        # Model người dùng
├── adapters/            # RecyclerView Adapters
│   ├── ProductAdapter.java  # Adapter cho danh sách sản phẩm
│   └── CartAdapter.java     # Adapter cho giỏ hàng
├── utils/               # Utility classes
│   ├── CartManager.java     # Quản lý giỏ hàng (Singleton)
│   └── SessionManager.java  # Quản lý phiên đăng nhập
├── MainActivity.java    # Màn hình chính
├── LoginActivity.java   # Màn hình đăng nhập
└── CartActivity.java    # Màn hình giỏ hàng
```

## ✨ Tính năng đã implement

### 1. **MainActivity** - Màn hình chính
- ✅ Toolbar với menu, search, account, cart icons
- ✅ Banner voucher vàng
- ✅ Hero banner "ALL ABOUT MEN'S WEAR"
- ✅ Nhiều section sản phẩm:
  - Ưu đãi dành cho bạn
  - Retro Sports Collection
  - Hàng Mới
  - Outlet
  - Áo Thun
  - Áo Polo
- ✅ RecyclerView hiển thị sản phẩm (Horizontal & Grid)
- ✅ Floating call button
- ✅ Cart badge hiển thị số lượng sản phẩm
- ✅ Thêm sản phẩm vào giỏ hàng
- ✅ Yêu thích sản phẩm

### 2. **LoginActivity** - Màn hình đăng nhập
- ✅ Đăng nhập bằng email/số điện thoại và mật khẩu
- ✅ Validation form đầy đủ
- ✅ Quên mật khẩu (UI ready)
- ✅ Đăng nhập với Google (UI ready)
- ✅ Đăng nhập với Facebook (UI ready)
- ✅ Link đăng ký tài khoản mới (UI ready)
- ✅ Session management với SharedPreferences

### 3. **CartActivity** - Màn hình giỏ hàng
- ✅ Hiển thị danh sách sản phẩm trong giỏ
- ✅ Checkbox chọn sản phẩm để thanh toán
- ✅ Tăng/giảm số lượng sản phẩm
- ✅ Xóa sản phẩm khỏi giỏ hàng
- ✅ Tính tổng giá tự động
- ✅ Empty state khi giỏ hàng trống
- ✅ Nút thanh toán

### 4. **Models** - Data classes
- ✅ **Product**: Thông tin sản phẩm đầy đủ
  - ID, tên, mô tả
  - Giá hiện tại, giá gốc, % giảm giá
  - Category, image URL
  - Badges (New, Voucher)
  - Favorite status
  - Stock quantity
  
- ✅ **CartItem**: Item trong giỏ hàng
  - Product reference
  - Quantity, size, color
  - Selected status
  - Total price calculation
  
- ✅ **User**: Thông tin người dùng
  - Email, phone, name, password
  - Profile image
  - Địa chỉ đầy đủ (address, ward, district, city)
  - Favorite product IDs
  - Created/last login timestamp

### 5. **Adapters** - RecyclerView
- ✅ **ProductAdapter**: 
  - Hiển thị product card
  - Click listeners (product, add to cart, favorite)
  - Show/hide badges
  - Price formatting
  
- ✅ **CartAdapter**:
  - Cart item display
  - Quantity controls
  - Checkbox selection
  - Remove item
  - Total price calculation

### 6. **Utils** - Utility classes
- ✅ **CartManager** (Singleton):
  - Add/remove/update cart items
  - Get cart items & count
  - Calculate total price
  - Select/deselect items
  - Clear cart
  
- ✅ **SessionManager**:
  - Login/logout
  - Save/get user data
  - Check login status
  - JSON serialization với Gson

## 🎨 UI/UX Features

- Material Design components
- Dark toolbar với logo
- Yellow voucher banner
- Product cards với:
  - Discount badge
  - New badge
  - Voucher badge
  - Favorite icon
  - Add to cart button
- Responsive layouts
- ScrollView/NestedScrollView
- CoordinatorLayout với AppBar

## 📦 Dependencies

```gradle
// Core Android
implementation(libs.appcompat)
implementation(libs.material)
implementation(libs.constraintlayout)

// RecyclerView
implementation("androidx.recyclerview:recyclerview:1.3.2")

// Gson for JSON
implementation("com.google.code.gson:gson:2.10.1")
```

## 🚀 Cách chạy

1. Clone project
2. Mở bằng Android Studio
3. Sync Gradle files
4. Chạy trên emulator hoặc thiết bị thật

## 📝 TODO - Tính năng cần bổ sung

### Priority High
- [ ] Tích hợp API backend
- [ ] Image loading với Glide/Picasso
- [ ] Product detail activity
- [ ] Checkout flow
- [ ] Order history
- [ ] Search functionality
- [ ] Filter & sort products

### Priority Medium
- [ ] Register activity
- [ ] Forgot password flow
- [ ] Google Sign In integration
- [ ] Facebook Login integration
- [ ] Profile activity
- [ ] Edit profile
- [ ] Address management

### Priority Low
- [ ] Notifications
- [ ] Wishlist screen
- [ ] Product reviews
- [ ] Rating system
- [ ] Share product
- [ ] Dark mode support

## 🔐 Login Demo

Hiện tại app chấp nhận bất kỳ email/password nào (minimum 6 characters).

**Test credentials:**
- Email: `test@example.com`
- Password: `123456`

## 📱 Screenshots

(Chạy app để xem giao diện thực tế)

## 👨‍💻 Author

Fashion Store App - 160Store Clone

## 📄 License

This is a demo project for learning purposes.
