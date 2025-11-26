package com.example.fashionstoreapp;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.fashionstoreapp.adapters.BannerAdapter;
import com.example.fashionstoreapp.adapters.ProductAdapter;
import com.example.fashionstoreapp.adapters.SearchSuggestionAdapter;
import com.example.fashionstoreapp.models.Banner;
import com.example.fashionstoreapp.models.CartItem;
import com.example.fashionstoreapp.models.Category;
import com.example.fashionstoreapp.models.FilterCriteria;
import com.example.fashionstoreapp.models.Product;
import com.example.fashionstoreapp.models.User;
import com.example.fashionstoreapp.utils.CartManager;
import com.example.fashionstoreapp.utils.FirestoreManager;
import com.example.fashionstoreapp.utils.SessionManager;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity
        implements ProductAdapter.OnProductClickListener, BannerAdapter.OnBannerClickListener,
        SearchSuggestionAdapter.OnSuggestionClickListener {

    private ImageView menuIcon, notificationIcon, cartIcon;
    private LinearLayout toolbarSearchContainer;
    private ImageView toolbarSearchIcon;
    private EditText toolbarSearchInput;
    private TextView cartBadge;
    private TextView notificationBadge;
    private BottomNavigationView bottomNavigation;

    // Search Panel
    private MaterialCardView searchPanel;
    private EditText searchInput;
    private ImageView closeSearchIcon;
    private RecyclerView searchSuggestionsRecyclerView;
    private LinearLayout searchResultsHeader, noResultsLayout;
    private TextView viewAllSearchResults;
    private ProgressBar searchLoadingProgress;
    private NestedScrollView mainScrollView;
    private SearchSuggestionAdapter searchSuggestionAdapter;
    private List<Product> allProducts = new ArrayList<>();
    private Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;

    // Banner ViewPager
    private ViewPager2 bannerViewPager;
    private LinearLayout bannerIndicator;
    private BannerAdapter bannerAdapter;
    private Handler bannerHandler;
    private Runnable bannerRunnable;
    private int currentBannerPosition = 0;

    private RecyclerView voucherRecyclerView, retroSportsRecyclerView;
    private RecyclerView featuredCategoriesRecyclerView;
    private RecyclerView newArrivalsRecyclerView, outletRecyclerView;
    private RecyclerView shirtsRecyclerView, poloRecyclerView, somiRecyclerView, hoodiesRecyclerView;
    private RecyclerView aoKhoacRecyclerView, quanSotRecyclerView, quanTayRecyclerView;
    private RecyclerView filteredResultsRecyclerView;
    private LinearLayout filteredResultsHeader;
    private LinearLayout filteredNoResultsLayout;
    private TextView viewAllFilteredResults;

    private ProductAdapter voucherAdapter, retroSportsAdapter;
    private com.example.fashionstoreapp.adapters.FeaturedCategoryAdapter featuredAdapter;
    private ProductAdapter newArrivalsAdapter, outletAdapter;
    private ProductAdapter shirtsAdapter, poloAdapter, somiAdapter, hoodiesAdapter;
    private ProductAdapter aoKhoacAdapter, quanSotAdapter, quanTayAdapter;
    private ProductAdapter filteredResultsAdapter;

    private Button btnViewAllRetro, btnViewAllOutlet, btnViewAllShirts, btnViewAllPolo;
    private Button btnViewAllSomi, btnViewAllHoodies;
    private Button btnViewAllAoKhoac, btnViewAllQuanSot, btnViewAllQuanTay;

    private CartManager cartManager;
    private SessionManager sessionManager;
    private FirebaseAuth mAuth;
    private FirestoreManager firestoreManager;

    // Filter
    private LinearLayout filterButton, sortButton;
    private TextView filterBadge;
    private FilterCriteria currentFilter;

    // Store original product lists for filtering
    private List<Product> originalVoucherProducts = new ArrayList<>();
    private List<Product> originalRetroProducts = new ArrayList<>();
    private List<Product> originalNewProducts = new ArrayList<>();
    private List<Product> originalOutletProducts = new ArrayList<>();
    private List<Product> originalShirtsProducts = new ArrayList<>();
    private List<Product> originalPoloProducts = new ArrayList<>();
    private List<Product> originalSomiProducts = new ArrayList<>();
    private List<Product> originalHoodiesProducts = new ArrayList<>();
    private List<Product> originalAoKhoacProducts = new ArrayList<>();
    private List<Product> originalQuanSotProducts = new ArrayList<>();
    private List<Product> originalQuanTayProducts = new ArrayList<>();

    // Real-time listeners
    private Map<String, ListenerRegistration> categoryListeners = new HashMap<>();
    private ListenerRegistration userNotifListener;
    private ListenerRegistration globalNotifListener;
    private static final long NOTIF_LOCAL_THRESHOLD_MS = 2000; // skip very-recent notifications (we show local)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize managers
        cartManager = CartManager.getInstance();
        sessionManager = new SessionManager(this);
        mAuth = FirebaseAuth.getInstance();
        firestoreManager = FirestoreManager.getInstance();

        // Initialize filter
        currentFilter = new FilterCriteria();

        // Initialize views
        initViews();

        // Setup Banner Slider
        setupBannerSlider();

        // Setup RecyclerViews
        setupRecyclerViews();

        // Load cart from Firestore
        loadCartFromFirestore();

        // Load data from Firestore
        loadProductsFromFirestore();
        loadCategoriesFromFirestore();

        // Setup search functionality
        setupSearchPanel();

        // Setup click listeners
        setupClickListeners();

        // Update cart badge
        updateCartBadge();
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String uid = user.getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            // Listen to user-specific notifications
            userNotifListener = db.collection("notifications")
                    .whereEqualTo("userId", uid)
                    .addSnapshotListener((snapshots, e) -> {
                        if (e != null || snapshots == null)
                            return;
                        for (DocumentChange dc : snapshots.getDocumentChanges()) {
                            if (dc.getType() == DocumentChange.Type.ADDED) {
                                try {
                                    java.util.Date ts = dc.getDocument().getDate("timestamp");
                                    long now = System.currentTimeMillis();
                                    if (ts != null && (now - ts.getTime()) < NOTIF_LOCAL_THRESHOLD_MS) {
                                        // likely created by this device just now — skip to avoid duplicate
                                        continue;
                                    }
                                    String title = dc.getDocument().getString("title");
                                    String message = dc.getDocument().getString("message");
                                    String id = dc.getDocument().getId();
                                    if (title != null && message != null) {
                                        new com.example.fashionstoreapp.NotificationHelper(MainActivity.this)
                                                .showNotification(title, message, id.hashCode());
                                    }
                                } catch (Exception ignored) {
                                }
                            }
                        }
                        // Refresh badge after processing changes
                        refreshUnreadCount();
                    });

            // Listen to global notifications (userId == "")
            globalNotifListener = db.collection("notifications")
                    .whereEqualTo("userId", "")
                    .addSnapshotListener((snapshots, e) -> {
                        if (e != null || snapshots == null)
                            return;
                        for (DocumentChange dc : snapshots.getDocumentChanges()) {
                            if (dc.getType() == DocumentChange.Type.ADDED) {
                                try {
                                    java.util.Date ts = dc.getDocument().getDate("timestamp");
                                    long now = System.currentTimeMillis();
                                    if (ts != null && (now - ts.getTime()) < NOTIF_LOCAL_THRESHOLD_MS) {
                                        continue;
                                    }
                                    String title = dc.getDocument().getString("title");
                                    String message = dc.getDocument().getString("message");
                                    String id = dc.getDocument().getId();
                                    if (title != null && message != null) {
                                        new com.example.fashionstoreapp.NotificationHelper(MainActivity.this)
                                                .showNotification(title, message, id.hashCode());
                                    }
                                } catch (Exception ignored) {
                                }
                            }
                        }
                        // Refresh badge after processing changes
                        refreshUnreadCount();
                    });
            // Initial badge refresh
            refreshUnreadCount();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (userNotifListener != null)
            userNotifListener.remove();
        if (globalNotifListener != null)
            globalNotifListener.remove();
    }

    private void initViews() {
        // Toolbar search pill
        toolbarSearchContainer = findViewById(R.id.toolbarSearchContainer);
        toolbarSearchIcon = findViewById(R.id.toolbarSearchIcon);
        toolbarSearchInput = findViewById(R.id.toolbarSearchInput);
        notificationIcon = findViewById(R.id.notificationIcon);
        notificationBadge = findViewById(R.id.notificationBadge);
        cartIcon = findViewById(R.id.cartIcon);
        cartBadge = findViewById(R.id.cartBadge);
        // fabCall removed - no longer in layout
        bottomNavigation = findViewById(R.id.bottomNavigation);

        // Search Panel
        searchPanel = findViewById(R.id.searchPanel);
        searchInput = findViewById(R.id.searchInput);
        closeSearchIcon = findViewById(R.id.closeSearchIcon);
        searchSuggestionsRecyclerView = findViewById(R.id.searchSuggestionsRecyclerView);
        searchResultsHeader = findViewById(R.id.searchResultsHeader);
        noResultsLayout = findViewById(R.id.noResultsLayout);
        viewAllSearchResults = findViewById(R.id.viewAllSearchResults);
        searchLoadingProgress = findViewById(R.id.searchLoadingProgress);
        mainScrollView = findViewById(R.id.mainScrollView);

        // Banner ViewPager
        bannerViewPager = findViewById(R.id.bannerViewPager);
        bannerIndicator = findViewById(R.id.bannerIndicator);

        voucherRecyclerView = findViewById(R.id.voucherRecyclerView);
        featuredCategoriesRecyclerView = findViewById(R.id.featuredCategoriesRecyclerView);
        retroSportsRecyclerView = findViewById(R.id.retroSportsRecyclerView);
        newArrivalsRecyclerView = findViewById(R.id.newArrivalsRecyclerView);
        outletRecyclerView = findViewById(R.id.outletRecyclerView);
        shirtsRecyclerView = findViewById(R.id.shirtsRecyclerView);
        poloRecyclerView = findViewById(R.id.poloRecyclerView);
        somiRecyclerView = findViewById(R.id.somiRecyclerView);
        hoodiesRecyclerView = findViewById(R.id.hoodiesRecyclerView);

        btnViewAllRetro = findViewById(R.id.btnViewAllRetro);
        btnViewAllOutlet = findViewById(R.id.btnViewAllOutlet);
        btnViewAllShirts = findViewById(R.id.btnViewAllShirts);
        btnViewAllPolo = findViewById(R.id.btnViewAllPolo);
        btnViewAllSomi = findViewById(R.id.btnViewAllSomi);
        btnViewAllHoodies = findViewById(R.id.btnViewAllHoodies);

        // New categories
        aoKhoacRecyclerView = findViewById(R.id.aoKhoacRecyclerView);
        quanSotRecyclerView = findViewById(R.id.quanSotRecyclerView);
        quanTayRecyclerView = findViewById(R.id.quanTayRecyclerView);

        // Filtered combined results list (hidden by default)
        filteredResultsRecyclerView = findViewById(R.id.filteredResultsRecyclerView);
        filteredResultsHeader = findViewById(R.id.filteredResultsHeader);
        filteredNoResultsLayout = findViewById(R.id.filteredNoResultsLayout);
        viewAllFilteredResults = findViewById(R.id.viewAllFilteredResults);

        btnViewAllAoKhoac = findViewById(R.id.btnViewAllAoKhoac);
        btnViewAllQuanSot = findViewById(R.id.btnViewAllQuanSot);
        btnViewAllQuanTay = findViewById(R.id.btnViewAllQuanTay);

        // Filter bar
        filterButton = findViewById(R.id.filterButton);
        sortButton = findViewById(R.id.sortButton);
        filterBadge = findViewById(R.id.filterBadge);
    }

    private void updateNotificationBadge(int count) {
        if (notificationBadge == null)
            return;
        if (count <= 0) {
            notificationBadge.setVisibility(View.GONE);
            return;
        }
        notificationBadge.setVisibility(View.VISIBLE);
        if (count > 99) {
            notificationBadge.setText("99+");
        } else {
            notificationBadge.setText(String.valueOf(count));
        }
    }

    private void refreshUnreadCount() {
        String uid = null;
        if (mAuth != null && mAuth.getCurrentUser() != null) {
            uid = mAuth.getCurrentUser().getUid();
        }
        // If user not logged in, pass empty string to count only global notifications
        String userIdParam = uid != null ? uid : "";
        firestoreManager.getUnreadNotificationsCount(userIdParam, new FirestoreManager.OnUnreadCountLoadedListener() {
            @Override
            public void onCountLoaded(int count) {
                runOnUiThread(() -> updateNotificationBadge(count));
            }

            @Override
            public void onError(String error) {
                // On error, hide badge
                runOnUiThread(() -> updateNotificationBadge(0));
            }
        });
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
        setupHorizontalRecyclerView(featuredCategoriesRecyclerView);
        setupHorizontalRecyclerView(retroSportsRecyclerView);
        setupHorizontalRecyclerView(shirtsRecyclerView);
        setupHorizontalRecyclerView(poloRecyclerView);
        setupHorizontalRecyclerView(somiRecyclerView);
        setupHorizontalRecyclerView(hoodiesRecyclerView);
        setupHorizontalRecyclerView(aoKhoacRecyclerView);
        setupHorizontalRecyclerView(quanSotRecyclerView);
        setupHorizontalRecyclerView(quanTayRecyclerView);

        // Grid RecyclerViews
        setupGridRecyclerView(newArrivalsRecyclerView);
        setupGridRecyclerView(outletRecyclerView);

        // Setup filtered results (vertical list)
        if (filteredResultsRecyclerView != null) {
            LinearLayoutManager lm = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
            filteredResultsRecyclerView.setLayoutManager(lm);
            filteredResultsAdapter = new ProductAdapter(this, new ArrayList<>(), this);
            filteredResultsRecyclerView.setAdapter(filteredResultsAdapter);
        }
    }

    private List<Product> filterAndSortList(List<Product> originalProducts) {
        List<Product> filteredProducts = new ArrayList<>();
        if (originalProducts == null || originalProducts.isEmpty())
            return filteredProducts;

        for (Product product : originalProducts) {
            boolean passesFilter = true;

            // Price filter
            if (currentFilter.getMinPrice() != null && product.getCurrentPrice() < currentFilter.getMinPrice()) {
                passesFilter = false;
            }
            if (currentFilter.getMaxPrice() != null && product.getCurrentPrice() > currentFilter.getMaxPrice()) {
                passesFilter = false;
            }

            // Stock filter
            if (currentFilter.isInStockOnly() && product.getStockQuantity() <= 0) {
                passesFilter = false;
            }

            // Category filter
            if (!currentFilter.getCategories().isEmpty()) {
                boolean matchesCategory = false;
                for (String category : currentFilter.getCategories()) {
                    if (product.getCategory() != null
                            && product.getCategory().toLowerCase().contains(category.toLowerCase())) {
                        matchesCategory = true;
                        break;
                    }
                }
                if (!matchesCategory)
                    passesFilter = false;
            }

            // Size filter
            if (!currentFilter.getSizes().isEmpty()) {
                boolean matchesSize = false;
                if (product.getAvailableSizes() != null) {
                    for (String size : currentFilter.getSizes()) {
                        if (product.getAvailableSizes().contains(size)) {
                            matchesSize = true;
                            break;
                        }
                    }
                }
                if (!matchesSize)
                    passesFilter = false;
            }

            if (passesFilter)
                filteredProducts.add(product);
        }

        // Apply sorting
        String sortBy = currentFilter.getSortBy();
        if (sortBy != null) {
            switch (sortBy) {
                case "name_asc":
                    filteredProducts.sort((p1, p2) -> p1.getName().compareToIgnoreCase(p2.getName()));
                    break;
                case "name_desc":
                    filteredProducts.sort((p1, p2) -> p2.getName().compareToIgnoreCase(p1.getName()));
                    break;
                case "price_asc":
                    filteredProducts.sort((p1, p2) -> Double.compare(p1.getCurrentPrice(), p2.getCurrentPrice()));
                    break;
                case "price_desc":
                    filteredProducts.sort((p1, p2) -> Double.compare(p2.getCurrentPrice(), p1.getCurrentPrice()));
                    break;
                case "newest":
                    // keep original order
                    break;
            }
        }

        return filteredProducts;
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

    private void loadCartFromFirestore() {
        if (mAuth.getCurrentUser() != null) {
            cartManager.loadCartFromFirestore(new CartManager.OnCartLoadedListener() {
                @Override
                public void onCartLoaded() {
                    updateCartBadge();
                }

                @Override
                public void onError(String error) {
                    // Handle error silently or show a toast if needed
                }
            });
        }
    }

    private void loadProductsFromFirestore() {
        // Load all products for search
        firestoreManager.loadProducts(new FirestoreManager.OnProductsLoadedListener() {
            @Override
            public void onProductsLoaded(List<Product> products) {
                allProducts.clear();
                allProducts.addAll(products);
            }

            @Override
            public void onError(String error) {
                // Keep existing products if error
            }
        });

        // Load voucher products
        firestoreManager.loadVoucherProducts(10, new FirestoreManager.OnProductsLoadedListener() {
            @Override
            public void onProductsLoaded(List<Product> products) {
                if (!products.isEmpty()) {
                    originalVoucherProducts = new ArrayList<>(products);
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
                    originalNewProducts = new ArrayList<>(products);
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
            originalRetroProducts = new ArrayList<>(products);
            retroSportsAdapter = new ProductAdapter(MainActivity.this, products, MainActivity.this);
            retroSportsRecyclerView.setAdapter(retroSportsAdapter);
        });

        loadProductsByCategory("outlet", outletRecyclerView, products -> {
            originalOutletProducts = new ArrayList<>(products);
            outletAdapter = new ProductAdapter(MainActivity.this, products, MainActivity.this);
            outletRecyclerView.setAdapter(outletAdapter);
        });

        loadProductsByCategory("ao-thun", shirtsRecyclerView, products -> {
            originalShirtsProducts = new ArrayList<>(products);
            shirtsAdapter = new ProductAdapter(MainActivity.this, products, MainActivity.this);
            shirtsRecyclerView.setAdapter(shirtsAdapter);
        });

        loadProductsByCategory("ao-polo", poloRecyclerView, products -> {
            originalPoloProducts = new ArrayList<>(products);
            poloAdapter = new ProductAdapter(MainActivity.this, products, MainActivity.this);
            poloRecyclerView.setAdapter(poloAdapter);
        });

        loadProductsByCategory("ao-so-mi", somiRecyclerView, products -> {
            originalSomiProducts = new ArrayList<>(products);
            somiAdapter = new ProductAdapter(MainActivity.this, products, MainActivity.this);
            somiRecyclerView.setAdapter(somiAdapter);
        });

        loadProductsByCategory("ao-hoodie", hoodiesRecyclerView, products -> {
            originalHoodiesProducts = new ArrayList<>(products);
            hoodiesAdapter = new ProductAdapter(MainActivity.this, products, MainActivity.this);
            hoodiesRecyclerView.setAdapter(hoodiesAdapter);
        });

        loadProductsByCategory("ao-khoac", aoKhoacRecyclerView, products -> {
            originalAoKhoacProducts = new ArrayList<>(products);
            aoKhoacAdapter = new ProductAdapter(MainActivity.this, products, MainActivity.this);
            aoKhoacRecyclerView.setAdapter(aoKhoacAdapter);
        });

        loadProductsByCategory("quan-sot", quanSotRecyclerView, products -> {
            originalQuanSotProducts = new ArrayList<>(products);
            quanSotAdapter = new ProductAdapter(MainActivity.this, products, MainActivity.this);
            quanSotRecyclerView.setAdapter(quanSotAdapter);
        });

        loadProductsByCategory("quan-tay", quanTayRecyclerView, products -> {
            originalQuanTayProducts = new ArrayList<>(products);
            quanTayAdapter = new ProductAdapter(MainActivity.this, products, MainActivity.this);
            quanTayRecyclerView.setAdapter(quanTayAdapter);
        });
    }

    private void loadProductsByCategory(String categoryId, RecyclerView recyclerView, OnProductsLoadCallback callback) {
        // Setup real-time listener for this category
        ListenerRegistration listener = FirebaseFirestore.getInstance()
                .collection("products")
                .whereEqualTo("category", categoryId)
                .limit(5) // Limit to 5 items for home screen
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        Toast.makeText(MainActivity.this, "Lỗi tải danh mục " + categoryId, Toast.LENGTH_SHORT).show();
                        List<Product> sampleProducts = createSampleProducts(categoryId, 5);
                        callback.onLoaded(sampleProducts);
                        return;
                    }

                    if (snapshots != null) {
                        List<Product> products = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : snapshots) {
                            Product product = doc.toObject(Product.class);
                            if (product != null) {
                                product.setId(doc.getId());
                                products.add(product);
                            }
                        }

                        if (!products.isEmpty()) {
                            callback.onLoaded(products);
                        } else {
                            List<Product> sampleProducts = createSampleProducts(categoryId, 5);
                            callback.onLoaded(sampleProducts);
                        }
                    }
                });

        // Store listener for cleanup
        categoryListeners.put(categoryId, listener);
    }

    private void loadCategoriesFromFirestore() {
        firestoreManager.loadCategories(new FirestoreManager.OnCategoriesLoadedListener() {
            @Override
            public void onCategoriesLoaded(List<Category> categories) {
                // Setup featured categories horizontal list
                if (categories != null && !categories.isEmpty()) {
                    featuredAdapter = new com.example.fashionstoreapp.adapters.FeaturedCategoryAdapter(
                            MainActivity.this, categories, category -> {
                                // Open category products when clicked
                                openCategoryProducts(category.getId(), category.getName());
                            });
                    featuredCategoriesRecyclerView.setAdapter(featuredAdapter);
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
        originalVoucherProducts = new ArrayList<>(voucherProducts);
        voucherAdapter = new ProductAdapter(this, voucherProducts, this);
        voucherRecyclerView.setAdapter(voucherAdapter);
    }

    private void loadSampleNewProducts() {
        List<Product> newProducts = createSampleProducts("Hàng Mới", 6);
        for (Product p : newProducts) {
            p.setNew(true);
        }
        originalNewProducts = new ArrayList<>(newProducts);
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

            // Normalize category -> use internal id (ao-thun, ao-polo, etc.) to keep
            // consistency with Firestore data
            String normalizedCategory = normalizeCategoryId(category);

            Product product = new Product(id, name, "Mô tả sản phẩm " + name,
                    currentPrice, originalPrice, imageUrl, normalizedCategory);
            products.add(product);
        }
        return products;
    }

    // Helper to convert display category or id into normalized internal category id
    private String normalizeCategoryId(String category) {
        if (category == null)
            return "";
        String c = category.trim().toLowerCase();
        // Common mapping for Vietnamese display names
        switch (c) {
            case "áo thun":
            case "ao thun":
            case "ao-thun":
                return "ao-thun";
            case "áo polo":
            case "ao polo":
            case "ao-polo":
                return "ao-polo";
            case "áo sơ mi":
            case "áo sơ-mi":
            case "ao sơ mi":
            case "ao-so-mi":
                return "ao-so-mi";
            case "áo khoác":
            case "ao khoac":
            case "ao-khoac":
                return "ao-khoac";
            case "áo hoodie":
            case "ao hoodie":
            case "ao-hoodie":
                return "ao-hoodie";
            case "quần sọt":
            case "quan sot":
            case "quan-sot":
                return "quan-sot";
            case "quần tây":
            case "quan tay":
            case "quan-tay":
                return "quan-tay";
            case "outlet":
                return "outlet";
            case "voucher":
            case "hàng mới":
            case "hang moi":
                return c.replaceAll("\\s+", "-");
            default:
                // Fallback: slugify by replacing spaces with '-' and remove diacritics
                String slug = c.replaceAll("\\s+", "-");
                // remove Vietnamese diacritics approx by basic replacements
                slug = slug.replaceAll("[áàảãạăắằẳẵặâấầẩẫậ]", "a");
                slug = slug.replaceAll("[đ]", "d");
                slug = slug.replaceAll("[éèẻẽẹêếềểễệ]", "e");
                slug = slug.replaceAll("[íìỉĩị]", "i");
                slug = slug.replaceAll("[óòỏõọôốồổỗộơớờởỡợ]", "o");
                slug = slug.replaceAll("[úùủũụưứừửữự]", "u");
                slug = slug.replaceAll("[ýỳỷỹỵ]", "y");
                slug = slug.replaceAll("[^a-z0-9-]", "");
                return slug;
        }
    }

    private void setupClickListeners() {

        View.OnClickListener openSearch = v -> showSearchPanel();
        if (toolbarSearchContainer != null)
            toolbarSearchContainer.setOnClickListener(openSearch);
        if (toolbarSearchIcon != null)
            toolbarSearchIcon.setOnClickListener(openSearch);
        if (toolbarSearchInput != null)
            toolbarSearchInput.setOnClickListener(openSearch);

        notificationIcon.setOnClickListener(v -> {
            // Open notifications activity
            Intent intent = new Intent(MainActivity.this, NotificationsActivity.class);
            startActivity(intent);
        });

        cartIcon.setOnClickListener(v -> {
            // Open cart activity
            Intent intent = new Intent(MainActivity.this, CartActivity.class);
            startActivity(intent);
        });

        // Filter button click
        filterButton.setOnClickListener(v -> {
            showFilterDialog();
        });

        // Sort button click
        sortButton.setOnClickListener(v -> {
            showSortDialog();
        });

        btnViewAllRetro.setOnClickListener(v -> {
            openCategoryProducts("retro-sports", "Retro Sports");
        });

        btnViewAllOutlet.setOnClickListener(v -> {
            openCategoryProducts("outlet", "Outlet");
        });

        btnViewAllShirts.setOnClickListener(v -> {
            openCategoryProducts("ao-thun", "Áo Thun");
        });

        btnViewAllPolo.setOnClickListener(v -> {
            openCategoryProducts("ao-polo", "Áo Polo");
        });

        btnViewAllSomi.setOnClickListener(v -> {
            openCategoryProducts("ao-so-mi", "Áo Sơ Mi");
        });

        btnViewAllHoodies.setOnClickListener(v -> {
            openCategoryProducts("ao-hoodie", "Áo Hoodie");
        });

        btnViewAllAoKhoac.setOnClickListener(v -> {
            openCategoryProducts("ao-khoac", "Áo Khoác");
        });

        btnViewAllQuanSot.setOnClickListener(v -> {
            openCategoryProducts("quan-sot", "Quần Sọt");
        });

        btnViewAllQuanTay.setOnClickListener(v -> {
            openCategoryProducts("quan-tay", "Quần Tây");
        });

        // Setup Bottom Navigation
        setupBottomNavigation();
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

    private void setupBottomNavigation() {
        // Set Home as selected by default
        bottomNavigation.setSelectedItemId(R.id.nav_home);

        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                // Already on home, do nothing or scroll to top
                return true;
            } else if (itemId == R.id.nav_categories) {
                // Navigate to Categories
                Intent intent = new Intent(MainActivity.this, CategoriesActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.nav_wishlist) {
                // Navigate to Wishlist
                if (mAuth.getCurrentUser() == null) {
                    Toast.makeText(this, "Vui lòng đăng nhập để xem danh sách yêu thích", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(MainActivity.this, FavoritesActivity.class);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                }
                return true;
            } else if (itemId == R.id.nav_account) {
                // Navigate to Account/Profile
                if (mAuth.getCurrentUser() == null) {
                    Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                }
                return true;
            }

            return false;
        });
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
        refreshUnreadCount();
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
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Vui lòng đăng nhập để thêm vào yêu thích", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        String productId = product.getId();

        if (product.isFavorite()) {
            // Remove from favorites
            firestoreManager.removeFavorite(userId, productId, new FirestoreManager.OnFavoriteRemovedListener() {
                @Override
                public void onFavoriteRemoved() {
                    Toast.makeText(MainActivity.this, "Đã xóa khỏi yêu thích", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(String error) {
                    product.setFavorite(true); // Revert on error
                    notifyAdaptersDataChanged();
                    Toast.makeText(MainActivity.this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // Add to favorites
            firestoreManager.saveFavorite(userId, productId, new FirestoreManager.OnFavoriteSavedListener() {
                @Override
                public void onFavoriteSaved() {
                    Toast.makeText(MainActivity.this, "Đã thêm vào yêu thích", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(String error) {
                    product.setFavorite(false); // Revert on error
                    notifyAdaptersDataChanged();
                    Toast.makeText(MainActivity.this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void notifyAdaptersDataChanged() {
        if (voucherAdapter != null)
            voucherAdapter.notifyDataSetChanged();
        if (retroSportsAdapter != null)
            retroSportsAdapter.notifyDataSetChanged();
        if (newArrivalsAdapter != null)
            newArrivalsAdapter.notifyDataSetChanged();
        if (outletAdapter != null)
            outletAdapter.notifyDataSetChanged();
        if (shirtsAdapter != null)
            shirtsAdapter.notifyDataSetChanged();
        if (poloAdapter != null)
            poloAdapter.notifyDataSetChanged();
        if (somiAdapter != null)
            somiAdapter.notifyDataSetChanged();
        if (hoodiesAdapter != null)
            hoodiesAdapter.notifyDataSetChanged();
        if (aoKhoacAdapter != null)
            aoKhoacAdapter.notifyDataSetChanged();
        if (quanSotAdapter != null)
            quanSotAdapter.notifyDataSetChanged();
        if (quanTayAdapter != null)
            quanTayAdapter.notifyDataSetChanged();
    }

    private void openCategoryProducts(String categoryId, String categoryName) {
        Intent intent = new Intent(this, CategoryProductsActivity.class);
        intent.putExtra("categoryId", categoryId);
        intent.putExtra("categoryName", categoryName);
        startActivity(intent);
    }

    // Search Panel Methods
    private void setupSearchPanel() {
        // Setup search suggestions adapter
        searchSuggestionAdapter = new SearchSuggestionAdapter(this, new ArrayList<>(), this);
        searchSuggestionsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        searchSuggestionsRecyclerView.setAdapter(searchSuggestionAdapter);

        // Close search panel
        closeSearchIcon.setOnClickListener(v -> hideSearchPanel());

        // Search input text watcher
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Cancel previous search
                if (searchRunnable != null) {
                    searchHandler.removeCallbacks(searchRunnable);
                }

                // Schedule new search with delay
                searchRunnable = () -> performSearch(s.toString());
                searchHandler.postDelayed(searchRunnable, 300); // 300ms delay
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // View all search results
        viewAllSearchResults.setOnClickListener(v -> {
            String query = searchInput.getText().toString();
            if (!query.isEmpty()) {
                Intent intent = new Intent(MainActivity.this, SearchActivity.class);
                intent.putExtra("query", query);
                startActivity(intent);
                hideSearchPanel();
            }
        });
    }

    private void showSearchPanel() {
        searchPanel.setVisibility(View.VISIBLE);
        mainScrollView.setVisibility(View.GONE);
        searchInput.requestFocus();

        // Show keyboard
        searchInput.postDelayed(() -> {
            android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) getSystemService(
                    INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(searchInput, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);
            }
        }, 100);
    }

    private void hideSearchPanel() {
        searchPanel.setVisibility(View.GONE);
        mainScrollView.setVisibility(View.VISIBLE);
        searchInput.setText("");

        // Hide keyboard
        android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) getSystemService(
                INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(searchInput.getWindowToken(), 0);
        }
    }

    private void performSearch(String query) {
        if (query.isEmpty()) {
            searchSuggestionsRecyclerView.setVisibility(View.GONE);
            searchResultsHeader.setVisibility(View.GONE);
            noResultsLayout.setVisibility(View.GONE);
            return;
        }

        // Show loading
        searchLoadingProgress.setVisibility(View.VISIBLE);
        searchSuggestionsRecyclerView.setVisibility(View.GONE);
        noResultsLayout.setVisibility(View.GONE);

        // Search in all products
        List<Product> results = new ArrayList<>();
        String lowerQuery = query.toLowerCase();

        for (Product product : allProducts) {
            if (product.getName().toLowerCase().contains(lowerQuery) ||
                    (product.getDescription() != null && product.getDescription().toLowerCase().contains(lowerQuery))) {
                results.add(product);
            }
        }

        // Update UI with results
        searchLoadingProgress.setVisibility(View.GONE);

        if (results.isEmpty()) {
            noResultsLayout.setVisibility(View.VISIBLE);
            searchResultsHeader.setVisibility(View.GONE);
            searchSuggestionsRecyclerView.setVisibility(View.GONE);
        } else {
            // Show limited results (first 10)
            int maxResults = Math.min(results.size(), 10);
            List<Product> limitedResults = results.subList(0, maxResults);

            searchSuggestionAdapter.updateSuggestions(limitedResults);
            searchSuggestionsRecyclerView.setVisibility(View.VISIBLE);
            searchResultsHeader.setVisibility(View.VISIBLE);
            noResultsLayout.setVisibility(View.GONE);

            // Update "View All" text
            viewAllSearchResults.setText("Xem tất cả " + results.size() + " sản phẩm");
        }
    }

    @Override
    public void onSuggestionClick(Product product) {
        hideSearchPanel();
    }

    // ==================== FILTER METHODS ====================

    private void showFilterDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_filter_products, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        // Get views from dialog
        EditText minPriceEt = dialogView.findViewById(R.id.minPriceEt);
        EditText maxPriceEt = dialogView.findViewById(R.id.maxPriceEt);
        android.widget.CheckBox inStockCb = dialogView.findViewById(R.id.inStockCb);
        com.google.android.material.chip.ChipGroup categoryChipGroup = dialogView.findViewById(R.id.categoryChipGroup);
        com.google.android.material.chip.ChipGroup sizeChipGroup = dialogView.findViewById(R.id.sizeChipGroup);
        Button resetFilterBtn = dialogView.findViewById(R.id.resetFilterBtn);
        Button applyFilterBtn = dialogView.findViewById(R.id.applyFilterBtn);

        // Populate current filter values
        if (currentFilter.getMinPrice() != null) {
            minPriceEt.setText(String.valueOf(currentFilter.getMinPrice().intValue()));
        }
        if (currentFilter.getMaxPrice() != null) {
            maxPriceEt.setText(String.valueOf(currentFilter.getMaxPrice().intValue()));
        }
        inStockCb.setChecked(currentFilter.isInStockOnly());

        // Add category chips (display name vs internal id)
        String[] categoryDisplay = { "Áo thun", "Áo sơ mi", "Quần jean", "Áo khoác" };
        String[] categoryIds = { "ao-thun", "ao-so-mi", "quan-jean", "ao-khoac" };
        for (int i = 0; i < categoryDisplay.length; i++) {
            com.google.android.material.chip.Chip chip = new com.google.android.material.chip.Chip(this);
            chip.setText(categoryDisplay[i]);
            chip.setTag(categoryIds[i]);
            chip.setCheckable(true);
            chip.setChecked(currentFilter.getCategories().contains(categoryIds[i]));
            categoryChipGroup.addView(chip);
        }

        // Add size chips
        String[] sizes = { "S", "M", "L", "XL" };
        for (String size : sizes) {
            com.google.android.material.chip.Chip chip = new com.google.android.material.chip.Chip(this);
            chip.setText(size);
            chip.setCheckable(true);
            chip.setChecked(currentFilter.getSizes().contains(size));
            sizeChipGroup.addView(chip);
        }

        // Reset button
        resetFilterBtn.setOnClickListener(v -> {
            currentFilter.reset();
            updateFilterBadge();
            applyFilters();
            dialog.dismiss();
            Toast.makeText(this, "Đã xóa tất cả bộ lọc", Toast.LENGTH_SHORT).show();
        });

        // Apply button
        applyFilterBtn.setOnClickListener(v -> {
            // Get price range
            String minPriceStr = minPriceEt.getText().toString().trim();
            String maxPriceStr = maxPriceEt.getText().toString().trim();

            if (!minPriceStr.isEmpty()) {
                currentFilter.setMinPrice(Double.parseDouble(minPriceStr));
            } else {
                currentFilter.setMinPrice(null);
            }

            if (!maxPriceStr.isEmpty()) {
                currentFilter.setMaxPrice(Double.parseDouble(maxPriceStr));
            } else {
                currentFilter.setMaxPrice(null);
            }

            // Get in stock only
            currentFilter.setInStockOnly(inStockCb.isChecked());

            // Get selected categories (use internal ids stored in tag)
            currentFilter.getCategories().clear();
            for (int i = 0; i < categoryChipGroup.getChildCount(); i++) {
                com.google.android.material.chip.Chip chip = (com.google.android.material.chip.Chip) categoryChipGroup
                        .getChildAt(i);
                if (chip.isChecked()) {
                    Object tag = chip.getTag();
                    if (tag != null) {
                        currentFilter.getCategories().add(tag.toString());
                    } else {
                        currentFilter.getCategories().add(chip.getText().toString());
                    }
                }
            }

            // Get selected sizes
            currentFilter.getSizes().clear();
            for (int i = 0; i < sizeChipGroup.getChildCount(); i++) {
                com.google.android.material.chip.Chip chip = (com.google.android.material.chip.Chip) sizeChipGroup
                        .getChildAt(i);
                if (chip.isChecked()) {
                    currentFilter.getSizes().add(chip.getText().toString());
                }
            }

            updateFilterBadge();
            applyFilters();
            dialog.dismiss();

            String message = currentFilter.hasActiveFilters()
                    ? "Đã áp dụng " + currentFilter.getActiveFilterCount() + " bộ lọc"
                    : "Đã xóa tất cả bộ lọc";
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        });

        dialog.show();
    }

    private void showSortDialog() {
        String[] sortOptions = {
                "Tên: A-Z",
                "Tên: Z-A",
                "Giá: Thấp đến cao",
                "Giá: Cao đến thấp",
                "Mới nhất"
        };

        String[] sortValues = {
                "name_asc",
                "name_desc",
                "price_asc",
                "price_desc",
                "newest"
        };

        // Find current selection
        int currentSelection = 0;
        for (int i = 0; i < sortValues.length; i++) {
            if (sortValues[i].equals(currentFilter.getSortBy())) {
                currentSelection = i;
                break;
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Sắp xếp theo")
                .setSingleChoiceItems(sortOptions, currentSelection, (dialog, which) -> {
                    currentFilter.setSortBy(sortValues[which]);
                    applyFilters();
                    dialog.dismiss();
                    Toast.makeText(this, "Đã sắp xếp: " + sortOptions[which], Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Hủy", null);

        builder.create().show();
    }

    private void updateFilterBadge() {
        int count = currentFilter.getActiveFilterCount();
        if (count > 0) {
            filterBadge.setVisibility(View.VISIBLE);
            filterBadge.setText(String.valueOf(count));
        } else {
            filterBadge.setVisibility(View.GONE);
        }
    }

    private void applyFilters() {
        // If filters are active, show combined filtered results inline under filter bar
        if (currentFilter.hasActiveFilters()) {
            List<Product> combined = new ArrayList<>();
            combined.addAll(filterAndSortList(originalVoucherProducts));
            combined.addAll(filterAndSortList(originalRetroProducts));
            combined.addAll(filterAndSortList(originalNewProducts));
            combined.addAll(filterAndSortList(originalOutletProducts));
            combined.addAll(filterAndSortList(originalShirtsProducts));
            combined.addAll(filterAndSortList(originalPoloProducts));
            combined.addAll(filterAndSortList(originalSomiProducts));
            combined.addAll(filterAndSortList(originalHoodiesProducts));
            combined.addAll(filterAndSortList(originalAoKhoacProducts));
            combined.addAll(filterAndSortList(originalQuanSotProducts));
            combined.addAll(filterAndSortList(originalQuanTayProducts));

            // If combined result is empty, keep sectioned lists visible and show a message
            if (combined.isEmpty()) {
                Toast.makeText(this, "Không tìm thấy sản phẩm phù hợp với bộ lọc", Toast.LENGTH_SHORT).show();
                if (filteredResultsRecyclerView != null)
                    filteredResultsRecyclerView.setVisibility(View.GONE);
                if (filteredResultsHeader != null)
                    filteredResultsHeader.setVisibility(View.GONE);
                if (viewAllFilteredResults != null)
                    viewAllFilteredResults.setVisibility(View.GONE);
                if (filteredNoResultsLayout != null)
                    filteredNoResultsLayout.setVisibility(View.GONE);

                // Ensure main content sections are visible
                if (mainScrollView != null)
                    mainScrollView.setVisibility(View.VISIBLE);

                voucherRecyclerView.setVisibility(View.VISIBLE);
                retroSportsRecyclerView.setVisibility(View.VISIBLE);
                newArrivalsRecyclerView.setVisibility(View.VISIBLE);
                outletRecyclerView.setVisibility(View.VISIBLE);
                shirtsRecyclerView.setVisibility(View.VISIBLE);
                poloRecyclerView.setVisibility(View.VISIBLE);
                somiRecyclerView.setVisibility(View.VISIBLE);
                hoodiesRecyclerView.setVisibility(View.VISIBLE);
                aoKhoacRecyclerView.setVisibility(View.VISIBLE);
                quanSotRecyclerView.setVisibility(View.VISIBLE);
                quanTayRecyclerView.setVisibility(View.VISIBLE);
                return;
            }

            // Apply global sorting across the combined list
            String sortBy = currentFilter.getSortBy();
            if (sortBy != null) {
                switch (sortBy) {
                    case "name_asc":
                        combined.sort((p1, p2) -> p1.getName().compareToIgnoreCase(p2.getName()));
                        break;
                    case "name_desc":
                        combined.sort((p1, p2) -> p2.getName().compareToIgnoreCase(p1.getName()));
                        break;
                    case "price_asc":
                        combined.sort((p1, p2) -> Double.compare(p1.getCurrentPrice(), p2.getCurrentPrice()));
                        break;
                    case "price_desc":
                        combined.sort((p1, p2) -> Double.compare(p2.getCurrentPrice(), p1.getCurrentPrice()));
                        break;
                    case "newest":
                        // keep current order
                        break;
                }
            }

            if (filteredResultsAdapter != null) {
                filteredResultsAdapter.updateProducts(combined);
                filteredResultsRecyclerView.setVisibility(View.VISIBLE);
            }

            // Show inline filtered header and update counts
            if (filteredResultsHeader != null)
                filteredResultsHeader.setVisibility(View.VISIBLE);
            if (viewAllFilteredResults != null)
                viewAllFilteredResults.setText("Xem tất cả " + combined.size() + " sản phẩm");
            if (viewAllFilteredResults != null)
                viewAllFilteredResults.setVisibility(View.VISIBLE);
            if (filteredNoResultsLayout != null)
                filteredNoResultsLayout.setVisibility(View.GONE);

            // Hide sectioned lists to avoid duplicate items showing
            voucherRecyclerView.setVisibility(View.GONE);
            retroSportsRecyclerView.setVisibility(View.GONE);
            newArrivalsRecyclerView.setVisibility(View.GONE);
            outletRecyclerView.setVisibility(View.GONE);
            shirtsRecyclerView.setVisibility(View.GONE);
            poloRecyclerView.setVisibility(View.GONE);
            somiRecyclerView.setVisibility(View.GONE);
            hoodiesRecyclerView.setVisibility(View.GONE);
            aoKhoacRecyclerView.setVisibility(View.GONE);
            quanSotRecyclerView.setVisibility(View.GONE);
            quanTayRecyclerView.setVisibility(View.GONE);
        } else {
            // No active filters: hide combined results and restore sectioned lists
            if (filteredResultsRecyclerView != null)
                filteredResultsRecyclerView.setVisibility(View.GONE);
            if (filteredResultsHeader != null)
                filteredResultsHeader.setVisibility(View.GONE);
            if (viewAllFilteredResults != null)
                viewAllFilteredResults.setVisibility(View.GONE);
            if (filteredNoResultsLayout != null)
                filteredNoResultsLayout.setVisibility(View.GONE);

            voucherRecyclerView.setVisibility(View.VISIBLE);
            retroSportsRecyclerView.setVisibility(View.VISIBLE);
            newArrivalsRecyclerView.setVisibility(View.VISIBLE);
            outletRecyclerView.setVisibility(View.VISIBLE);
            shirtsRecyclerView.setVisibility(View.VISIBLE);
            poloRecyclerView.setVisibility(View.VISIBLE);
            somiRecyclerView.setVisibility(View.VISIBLE);
            hoodiesRecyclerView.setVisibility(View.VISIBLE);
            aoKhoacRecyclerView.setVisibility(View.VISIBLE);
            quanSotRecyclerView.setVisibility(View.VISIBLE);
            quanTayRecyclerView.setVisibility(View.VISIBLE);

            // Restore original lists in each adapter but apply current sort (if any)
            if (voucherAdapter != null)
                applyFilterToAdapter(voucherAdapter,
                        originalVoucherProducts != null ? originalVoucherProducts : new ArrayList<>());
            if (retroSportsAdapter != null)
                applyFilterToAdapter(retroSportsAdapter,
                        originalRetroProducts != null ? originalRetroProducts : new ArrayList<>());
            if (newArrivalsAdapter != null)
                applyFilterToAdapter(newArrivalsAdapter,
                        originalNewProducts != null ? originalNewProducts : new ArrayList<>());
            if (outletAdapter != null)
                applyFilterToAdapter(outletAdapter,
                        originalOutletProducts != null ? originalOutletProducts : new ArrayList<>());
            if (shirtsAdapter != null)
                applyFilterToAdapter(shirtsAdapter,
                        originalShirtsProducts != null ? originalShirtsProducts : new ArrayList<>());
            if (poloAdapter != null)
                applyFilterToAdapter(poloAdapter,
                        originalPoloProducts != null ? originalPoloProducts : new ArrayList<>());
            if (somiAdapter != null)
                applyFilterToAdapter(somiAdapter,
                        originalSomiProducts != null ? originalSomiProducts : new ArrayList<>());
            if (hoodiesAdapter != null)
                applyFilterToAdapter(hoodiesAdapter,
                        originalHoodiesProducts != null ? originalHoodiesProducts : new ArrayList<>());
            if (aoKhoacAdapter != null)
                applyFilterToAdapter(aoKhoacAdapter,
                        originalAoKhoacProducts != null ? originalAoKhoacProducts : new ArrayList<>());
            if (quanSotAdapter != null)
                applyFilterToAdapter(quanSotAdapter,
                        originalQuanSotProducts != null ? originalQuanSotProducts : new ArrayList<>());
            if (quanTayAdapter != null)
                applyFilterToAdapter(quanTayAdapter,
                        originalQuanTayProducts != null ? originalQuanTayProducts : new ArrayList<>());
        }
    }

    private void applyFilterToAdapter(ProductAdapter adapter, List<Product> originalProducts) {
        List<Product> filteredProducts = new ArrayList<>();

        // Apply filters
        for (Product product : originalProducts) {
            boolean passesFilter = true;

            // Price filter
            if (currentFilter.getMinPrice() != null && product.getCurrentPrice() < currentFilter.getMinPrice()) {
                passesFilter = false;
            }
            if (currentFilter.getMaxPrice() != null && product.getCurrentPrice() > currentFilter.getMaxPrice()) {
                passesFilter = false;
            }

            // Stock filter
            if (currentFilter.isInStockOnly() && product.getStockQuantity() <= 0) {
                passesFilter = false;
            }

            // Category filter (if categories selected)
            if (!currentFilter.getCategories().isEmpty()) {
                boolean matchesCategory = false;
                for (String category : currentFilter.getCategories()) {
                    if (product.getCategory() != null &&
                            product.getCategory().toLowerCase().contains(category.toLowerCase())) {
                        matchesCategory = true;
                        break;
                    }
                }
                if (!matchesCategory) {
                    passesFilter = false;
                }
            }

            // Size filter (if sizes selected)
            if (!currentFilter.getSizes().isEmpty()) {
                boolean matchesSize = false;
                if (product.getAvailableSizes() != null) {
                    for (String size : currentFilter.getSizes()) {
                        if (product.getAvailableSizes().contains(size)) {
                            matchesSize = true;
                            break;
                        }
                    }
                }
                if (!matchesSize) {
                    passesFilter = false;
                }
            }

            if (passesFilter) {
                filteredProducts.add(product);
            }
        }

        // Apply sorting
        String sortBy = currentFilter.getSortBy();
        if (sortBy != null) {
            switch (sortBy) {
                case "name_asc":
                    filteredProducts.sort((p1, p2) -> p1.getName().compareToIgnoreCase(p2.getName()));
                    break;
                case "name_desc":
                    filteredProducts.sort((p1, p2) -> p2.getName().compareToIgnoreCase(p1.getName()));
                    break;
                case "price_asc":
                    filteredProducts.sort((p1, p2) -> Double.compare(p1.getCurrentPrice(), p2.getCurrentPrice()));
                    break;
                case "price_desc":
                    filteredProducts.sort((p1, p2) -> Double.compare(p2.getCurrentPrice(), p1.getCurrentPrice()));
                    break;
                case "newest":
                    // Assuming products are already sorted by newest from Firestore
                    break;
            }
        }

        // Update adapter
        adapter.updateProducts(filteredProducts);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remove all real-time listeners to prevent memory leaks
        for (ListenerRegistration listener : categoryListeners.values()) {
            if (listener != null) {
                listener.remove();
            }
        }
        categoryListeners.clear();
    }
}