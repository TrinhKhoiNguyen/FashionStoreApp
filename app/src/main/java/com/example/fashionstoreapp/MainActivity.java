package com.example.fashionstoreapp;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.fashionstoreapp.adapters.BannerAdapter;
import com.example.fashionstoreapp.adapters.ProductAdapter;
import com.example.fashionstoreapp.models.Banner;
import com.example.fashionstoreapp.models.CartItem;
import com.example.fashionstoreapp.models.Category;
import com.example.fashionstoreapp.models.Product;
import com.example.fashionstoreapp.models.User;
import com.example.fashionstoreapp.utils.CartManager;
import com.example.fashionstoreapp.utils.FirestoreManager;
import com.example.fashionstoreapp.utils.SessionManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements ProductAdapter.OnProductClickListener, BannerAdapter.OnBannerClickListener {

    private ImageView menuIcon, searchIcon, accountIcon, cartIcon;
    private TextView cartBadge;
    private FloatingActionButton fabCall;

    // Banner ViewPager
    private ViewPager2 bannerViewPager;
    private LinearLayout bannerIndicator;
    private BannerAdapter bannerAdapter;
    private Handler bannerHandler;
    private Runnable bannerRunnable;
    private int currentBannerPosition = 0;

    private RecyclerView voucherRecyclerView, retroSportsRecyclerView;
    private RecyclerView newArrivalsRecyclerView, outletRecyclerView;
    private RecyclerView shirtsRecyclerView, poloRecyclerView, somiRecyclerView;

    private ProductAdapter voucherAdapter, retroSportsAdapter;
    private ProductAdapter newArrivalsAdapter, outletAdapter;
    private ProductAdapter shirtsAdapter, poloAdapter, somiAdapter;

    private Button btnViewAllRetro, btnViewAllOutlet, btnViewAllShirts, btnViewAllPolo;

    private CartManager cartManager;
    private SessionManager sessionManager;
    private FirebaseAuth mAuth;
    private FirestoreManager firestoreManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize managers
        cartManager = CartManager.getInstance();
        sessionManager = new SessionManager(this);
        mAuth = FirebaseAuth.getInstance();
        firestoreManager = FirestoreManager.getInstance();

        // Initialize views
        initViews();

        // Setup Banner Slider
        setupBannerSlider();

        // Setup RecyclerViews
        setupRecyclerViews();

        // Load data from Firestore
        loadProductsFromFirestore();
        loadCategoriesFromFirestore();

        // Setup click listeners
        setupClickListeners();

        // Update cart badge
        updateCartBadge();
    }

    private void initViews() {
        menuIcon = findViewById(R.id.menuIcon);
        searchIcon = findViewById(R.id.searchIcon);
        accountIcon = findViewById(R.id.accountIcon);
        cartIcon = findViewById(R.id.cartIcon);
        cartBadge = findViewById(R.id.cartBadge);
        fabCall = findViewById(R.id.fabCall);

        // Banner ViewPager
        bannerViewPager = findViewById(R.id.bannerViewPager);
        bannerIndicator = findViewById(R.id.bannerIndicator);

        voucherRecyclerView = findViewById(R.id.voucherRecyclerView);
        retroSportsRecyclerView = findViewById(R.id.retroSportsRecyclerView);
        newArrivalsRecyclerView = findViewById(R.id.newArrivalsRecyclerView);
        outletRecyclerView = findViewById(R.id.outletRecyclerView);
        shirtsRecyclerView = findViewById(R.id.shirtsRecyclerView);
        poloRecyclerView = findViewById(R.id.poloRecyclerView);
        somiRecyclerView = findViewById(R.id.somiRecyclerView);

        btnViewAllRetro = findViewById(R.id.btnViewAllRetro);
        btnViewAllOutlet = findViewById(R.id.btnViewAllOutlet);
        btnViewAllShirts = findViewById(R.id.btnViewAllShirts);
        btnViewAllPolo = findViewById(R.id.btnViewAllPolo);
    }

    private void setupBannerSlider() {
        // Create sample banners
        List<Banner> banners = createSampleBanners();

        // Setup adapter
        bannerAdapter = new BannerAdapter(this, banners, this);
        bannerViewPager.setAdapter(bannerAdapter);

        // Setup indicator dots
        setupBannerIndicators(banners.size());

        // Setup page change callback
        bannerViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                currentBannerPosition = position;
                updateBannerIndicators(position);
            }
        });

        // Auto scroll banner
        startBannerAutoScroll();
    }

    private List<Banner> createSampleBanners() {
        List<Banner> banners = new ArrayList<>();

        // Banner 1 - Sử dụng ảnh banner1.png từ drawable
        Banner banner1 = new Banner("1", "ALL ABOUT", "MEN'S WEAR", "", R.drawable.banner1,
                Color.parseColor("#F5F5F5"));
        banners.add(banner1);

        // Banner 2 - Sử dụng ảnh banner2.png từ drawable
        Banner banner2 = new Banner("2", "SUMMER", "COLLECTION 2025", "", R.drawable.banner2,
                Color.parseColor("#FFE5E5"));
        banners.add(banner2);

        // Banner 3 - Sử dụng ảnh banner3.png từ drawable
        Banner banner3 = new Banner("3", "STREET", "STYLE", "", R.drawable.banner3, Color.parseColor("#E5F5FF"));
        banners.add(banner3);

        // Banner 4 - Sử dụng ảnh banner4.png từ drawable
        Banner banner4 = new Banner("4", "PREMIUM", "QUALITY", "", R.drawable.banner4, Color.parseColor("#FFF5E5"));
        banners.add(banner4);

        return banners;
    }

    private void setupBannerIndicators(int count) {
        bannerIndicator.removeAllViews();
        ImageView[] dots = new ImageView[count];

        for (int i = 0; i < count; i++) {
            dots[i] = new ImageView(this);
            dots[i].setImageResource(android.R.drawable.presence_invisible);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    20, 20);
            params.setMargins(8, 0, 8, 0);
            dots[i].setLayoutParams(params);
            bannerIndicator.addView(dots[i]);
        }

        if (count > 0) {
            dots[0].setImageResource(android.R.drawable.presence_online);
        }
    }

    private void updateBannerIndicators(int position) {
        int childCount = bannerIndicator.getChildCount();
        for (int i = 0; i < childCount; i++) {
            ImageView dot = (ImageView) bannerIndicator.getChildAt(i);
            if (i == position) {
                dot.setImageResource(android.R.drawable.presence_online);
            } else {
                dot.setImageResource(android.R.drawable.presence_invisible);
            }
        }
    }

    private void startBannerAutoScroll() {
        bannerHandler = new Handler(Looper.getMainLooper());
        bannerRunnable = new Runnable() {
            @Override
            public void run() {
                int itemCount = bannerAdapter.getItemCount();
                if (itemCount > 0) {
                    currentBannerPosition = (currentBannerPosition + 1) % itemCount;
                    bannerViewPager.setCurrentItem(currentBannerPosition, true);
                }
                bannerHandler.postDelayed(this, 3000); // Auto scroll every 3 seconds
            }
        };
        bannerHandler.postDelayed(bannerRunnable, 3000);
    }

    private void stopBannerAutoScroll() {
        if (bannerHandler != null && bannerRunnable != null) {
            bannerHandler.removeCallbacks(bannerRunnable);
        }
    }

    private void setupRecyclerViews() {
        // Horizontal RecyclerViews
        setupHorizontalRecyclerView(voucherRecyclerView);
        setupHorizontalRecyclerView(retroSportsRecyclerView);
        setupHorizontalRecyclerView(shirtsRecyclerView);
        setupHorizontalRecyclerView(poloRecyclerView);
        setupHorizontalRecyclerView(somiRecyclerView);

        // Grid RecyclerViews
        setupGridRecyclerView(newArrivalsRecyclerView);
        setupGridRecyclerView(outletRecyclerView);
    }

    private void setupHorizontalRecyclerView(RecyclerView recyclerView) {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);
    }

    private void setupGridRecyclerView(RecyclerView recyclerView) {
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(layoutManager);
    }

    private void loadSampleProducts() {
        // Sample data for voucher products
        List<Product> voucherProducts = createSampleProducts("Voucher", 5);
        for (Product p : voucherProducts) {
            p.setHasVoucher(true);
            p.setVoucherText("Voucher 15K");
        }
        voucherAdapter = new ProductAdapter(this, voucherProducts, this);
        voucherRecyclerView.setAdapter(voucherAdapter);

        // Sample data for retro sports
        List<Product> retroProducts = createSampleProducts("Retro Sports", 5);
        retroSportsAdapter = new ProductAdapter(this, retroProducts, this);
        retroSportsRecyclerView.setAdapter(retroSportsAdapter);

        // Sample data for new arrivals
        List<Product> newProducts = createSampleProducts("Hàng Mới", 6);
        for (Product p : newProducts) {
            p.setNew(true);
        }
        newArrivalsAdapter = new ProductAdapter(this, newProducts, this);
        newArrivalsRecyclerView.setAdapter(newArrivalsAdapter);

        // Sample data for outlet
        List<Product> outletProducts = createSampleProducts("Outlet", 6);
        outletAdapter = new ProductAdapter(this, outletProducts, this);
        outletRecyclerView.setAdapter(outletAdapter);

        // Sample data for shirts
        List<Product> shirtProducts = createSampleProducts("Áo Thun", 5);
        shirtsAdapter = new ProductAdapter(this, shirtProducts, this);
        shirtsRecyclerView.setAdapter(shirtsAdapter);

        // Sample data for polo
        List<Product> poloProducts = createSampleProducts("Áo Polo", 5);
        poloAdapter = new ProductAdapter(this, poloProducts, this);
        poloRecyclerView.setAdapter(poloAdapter);
    }

    // ==================== FIRESTORE METHODS ====================

    private void loadProductsFromFirestore() {
        // Load voucher products
        firestoreManager.loadVoucherProducts(10, new FirestoreManager.OnProductsLoadedListener() {
            @Override
            public void onProductsLoaded(List<Product> products) {
                if (!products.isEmpty()) {
                    voucherAdapter = new ProductAdapter(MainActivity.this, products, MainActivity.this);
                    voucherRecyclerView.setAdapter(voucherAdapter);
                } else {
                    // Fallback to sample data if Firestore is empty
                    loadSampleVoucherProducts();
                }
            }

            @Override
            public void onError(String error) {
                Toast.makeText(MainActivity.this, "Lỗi tải sản phẩm voucher: " + error, Toast.LENGTH_SHORT).show();
                loadSampleVoucherProducts();
            }
        });

        // Load new products
        firestoreManager.loadNewProducts(10, new FirestoreManager.OnProductsLoadedListener() {
            @Override
            public void onProductsLoaded(List<Product> products) {
                if (!products.isEmpty()) {
                    newArrivalsAdapter = new ProductAdapter(MainActivity.this, products, MainActivity.this);
                    newArrivalsRecyclerView.setAdapter(newArrivalsAdapter);
                } else {
                    loadSampleNewProducts();
                }
            }

            @Override
            public void onError(String error) {
                Toast.makeText(MainActivity.this, "Lỗi tải sản phẩm mới: " + error, Toast.LENGTH_SHORT).show();
                loadSampleNewProducts();
            }
        });

        // Load products by category
        loadProductsByCategory("retro-sports", retroSportsRecyclerView, products -> {
            retroSportsAdapter = new ProductAdapter(MainActivity.this, products, MainActivity.this);
            retroSportsRecyclerView.setAdapter(retroSportsAdapter);
        });

        loadProductsByCategory("outlet", outletRecyclerView, products -> {
            outletAdapter = new ProductAdapter(MainActivity.this, products, MainActivity.this);
            outletRecyclerView.setAdapter(outletAdapter);
        });

        loadProductsByCategory("ao-thun", shirtsRecyclerView, products -> {
            shirtsAdapter = new ProductAdapter(MainActivity.this, products, MainActivity.this);
            shirtsRecyclerView.setAdapter(shirtsAdapter);
        });

        loadProductsByCategory("ao-polo", poloRecyclerView, products -> {
            poloAdapter = new ProductAdapter(MainActivity.this, products, MainActivity.this);
            poloRecyclerView.setAdapter(poloAdapter);
        });

        loadProductsByCategory("ao-so-mi", somiRecyclerView, products -> {
            somiAdapter = new ProductAdapter(MainActivity.this, products, MainActivity.this);
            somiRecyclerView.setAdapter(somiAdapter);
        });
    }

    private void loadProductsByCategory(String categoryId, RecyclerView recyclerView, OnProductsLoadCallback callback) {
        firestoreManager.loadProductsByCategory(categoryId, new FirestoreManager.OnProductsLoadedListener() {
            @Override
            public void onProductsLoaded(List<Product> products) {
                if (!products.isEmpty()) {
                    callback.onLoaded(products);
                } else {
                    // Fallback to sample data
                    List<Product> sampleProducts = createSampleProducts(categoryId, 5);
                    callback.onLoaded(sampleProducts);
                }
            }

            @Override
            public void onError(String error) {
                Toast.makeText(MainActivity.this, "Lỗi tải danh mục " + categoryId, Toast.LENGTH_SHORT).show();
                List<Product> sampleProducts = createSampleProducts(categoryId, 5);
                callback.onLoaded(sampleProducts);
            }
        });
    }

    private void loadCategoriesFromFirestore() {
        firestoreManager.loadCategories(new FirestoreManager.OnCategoriesLoadedListener() {
            @Override
            public void onCategoriesLoaded(List<Category> categories) {
                // You can use categories for navigation or filtering
                // For now, just log them
                for (Category category : categories) {
                    android.util.Log.d("MainActivity", "Category: " + category.getName());
                }
            }

            @Override
            public void onError(String error) {
                Toast.makeText(MainActivity.this, "Lỗi tải danh mục: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Fallback methods to load sample data
    private void loadSampleVoucherProducts() {
        List<Product> voucherProducts = createSampleProducts("Voucher", 5);
        for (Product p : voucherProducts) {
            p.setHasVoucher(true);
            p.setVoucherText("Voucher 15K");
        }
        voucherAdapter = new ProductAdapter(this, voucherProducts, this);
        voucherRecyclerView.setAdapter(voucherAdapter);
    }

    private void loadSampleNewProducts() {
        List<Product> newProducts = createSampleProducts("Hàng Mới", 6);
        for (Product p : newProducts) {
            p.setNew(true);
        }
        newArrivalsAdapter = new ProductAdapter(this, newProducts, this);
        newArrivalsRecyclerView.setAdapter(newArrivalsAdapter);
    }

    // Callback interface
    private interface OnProductsLoadCallback {
        void onLoaded(List<Product> products);
    }

    // Keep the sample data method as fallback

    private List<Product> createSampleProducts(String category, int count) {
        List<Product> products = new ArrayList<>();
        String[] names = {
                "Áo Khoác Bomber Nam ICONDENIM",
                "Áo Thun Basic Cotton 100%",
                "Áo Polo Pique Premium",
                "Quần Jean Slim Fit",
                "Áo Hoodie Street Style",
                "Áo Sơ Mi Oxford Classic"
        };

        // Ảnh sản phẩm - Bạn có thể thay đổi tên file ảnh ở đây
        // Đặt ảnh vào: app/src/main/res/drawable/
        String[] imageUrls = {
                "product1", // Tên file ảnh trong drawable (không cần .png/.jpg)
                "product2",
                "product3",
                "product4",
                "product5",
                "product6"
        };

        for (int i = 0; i < count; i++) {
            String id = category + "_" + (i + 1);
            String name = names[i % names.length] + " " + (i + 1);
            double originalPrice = 500000 + (i * 100000);
            double currentPrice = originalPrice * 0.7; // 30% discount

            // Sử dụng tên drawable cho imageUrl
            String imageUrl = imageUrls[i % imageUrls.length];

            Product product = new Product(id, name, "Mô tả sản phẩm " + name,
                    currentPrice, originalPrice, imageUrl, category);
            products.add(product);
        }
        return products;
    }

    private void setupClickListeners() {
        menuIcon.setOnClickListener(v -> {
            Toast.makeText(this, "Menu clicked", Toast.LENGTH_SHORT).show();
        });

        searchIcon.setOnClickListener(v -> {
            Toast.makeText(this, "Search clicked", Toast.LENGTH_SHORT).show();
        });

        accountIcon.setOnClickListener(v -> {
            // Check if user is logged in
            if (sessionManager.isLoggedIn() || mAuth.getCurrentUser() != null) {
                // Show user menu
                showUserMenu();
            } else {
                // Open login activity
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

        cartIcon.setOnClickListener(v -> {
            // Open cart activity
            Intent intent = new Intent(MainActivity.this, CartActivity.class);
            startActivity(intent);
        });

        fabCall.setOnClickListener(v -> {
            Toast.makeText(this, "Call: 1900 1234", Toast.LENGTH_SHORT).show();
        });

        btnViewAllRetro.setOnClickListener(v -> {
            Toast.makeText(this, "View all Retro Sports", Toast.LENGTH_SHORT).show();
        });

        btnViewAllOutlet.setOnClickListener(v -> {
            Toast.makeText(this, "View all Outlet", Toast.LENGTH_SHORT).show();
        });

        btnViewAllShirts.setOnClickListener(v -> {
            Toast.makeText(this, "View all Shirts", Toast.LENGTH_SHORT).show();
        });

        btnViewAllPolo.setOnClickListener(v -> {
            Toast.makeText(this, "View all Polo", Toast.LENGTH_SHORT).show();
        });
    }

    private void updateCartBadge() {
        int itemCount = cartManager.getCartItemCount();
        if (itemCount > 0) {
            cartBadge.setVisibility(View.VISIBLE);
            cartBadge.setText(String.valueOf(itemCount));
        } else {
            cartBadge.setVisibility(View.GONE);
        }
    }

    private void showUserMenu() {
        User user = sessionManager.getUser();
        String userName = "Người dùng";
        String userInfo = "";

        if (user != null) {
            userName = user.getDisplayName();
            if (user.getEmail() != null) {
                userInfo = user.getEmail();
            } else if (user.getPhone() != null) {
                userInfo = user.getPhone();
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Xin chào, " + userName);
        builder.setMessage(userInfo);

        builder.setPositiveButton("Thông tin tài khoản", (dialog, which) -> {
            Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
            startActivity(intent);
        });

        builder.setNeutralButton("Đơn hàng", (dialog, which) -> {
            Toast.makeText(this, "Tính năng đang được phát triển", Toast.LENGTH_SHORT).show();
            // TODO: Navigate to orders activity
        });

        builder.setNegativeButton("Đăng xuất", (dialog, which) -> {
            logout();
        });

        builder.show();
    }

    private void logout() {
        new AlertDialog.Builder(this)
                .setTitle("Đăng xuất")
                .setMessage("Bạn có chắc chắn muốn đăng xuất?")
                .setPositiveButton("Đăng xuất", (dialog, which) -> {
                    // Sign out from Firebase
                    mAuth.signOut();

                    // Clear session
                    sessionManager.logout();

                    // Clear cart
                    cartManager.clearCart();

                    Toast.makeText(this, "Đã đăng xuất", Toast.LENGTH_SHORT).show();

                    // Refresh UI
                    updateCartBadge();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateCartBadge();
        startBannerAutoScroll();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopBannerAutoScroll();
    }

    // BannerAdapter.OnBannerClickListener implementation
    @Override
    public void onBannerClick(Banner banner) {
        Toast.makeText(this, "Banner: " + banner.getTitle(), Toast.LENGTH_SHORT).show();
        // TODO: Handle banner click action
    }

    // ProductAdapter.OnProductClickListener implementation
    @Override
    public void onProductClick(Product product) {
        Toast.makeText(this, "Clicked: " + product.getName(), Toast.LENGTH_SHORT).show();
        // TODO: Open product detail activity
    }

    @Override
    public void onAddToCartClick(Product product) {
        CartItem cartItem = new CartItem(product, 1);
        cartManager.addItem(cartItem);
        updateCartBadge();
        Toast.makeText(this, "Đã thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onFavoriteClick(Product product) {
        if (product.isFavorite()) {
            Toast.makeText(this, "Đã thêm vào yêu thích", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Đã bỏ yêu thích", Toast.LENGTH_SHORT).show();
        }
    }
}