package com.example.fashionstoreapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fashionstoreapp.adapters.ProductAdapter;
import com.example.fashionstoreapp.models.Product;
import com.example.fashionstoreapp.utils.FirestoreManager;
import com.example.fashionstoreapp.utils.SessionManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class FavoritesActivity extends AppCompatActivity implements ProductAdapter.OnProductClickListener {

    private MaterialToolbar toolbar;
    private RecyclerView favoritesRecyclerView;
    private LinearLayout emptyStateLayout;
    private ProgressBar loadingProgress;
    private Button btnShopNow;
    private BottomNavigationView bottomNavigation;

    private ProductAdapter productAdapter;
    private List<Product> favoriteProducts;
    private FirestoreManager firestoreManager;
    private SessionManager sessionManager;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        // Initialize Firebase and managers
        mAuth = FirebaseAuth.getInstance();
        firestoreManager = FirestoreManager.getInstance();
        sessionManager = new SessionManager(this);

        // Initialize views
        initViews();

        // Setup toolbar
        setupToolbar();

        // Setup RecyclerView
        setupRecyclerView();

        // Setup Bottom Navigation
        setupBottomNavigation();

        // Load favorites
        loadFavorites();

        // Setup click listeners
        setupClickListeners();
    }

    private void initViews() {
        toolbar = findViewById(R.id.favoritesToolbar);
        favoritesRecyclerView = findViewById(R.id.favoritesRecyclerView);
        emptyStateLayout = findViewById(R.id.emptyStateLayout);
        loadingProgress = findViewById(R.id.loadingProgress);
        btnShopNow = findViewById(R.id.btnShopNow);
        bottomNavigation = findViewById(R.id.bottomNavigation);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        favoriteProducts = new ArrayList<>();
        productAdapter = new ProductAdapter(this, favoriteProducts, this);

        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        favoritesRecyclerView.setLayoutManager(layoutManager);
        favoritesRecyclerView.setAdapter(productAdapter);
    }

    private void setupClickListeners() {
        btnShopNow.setOnClickListener(v -> {
            // Navigate back to MainActivity
            Intent intent = new Intent(FavoritesActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });
    }

    private void loadFavorites() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            showEmptyState();
            Toast.makeText(this, "Vui lòng đăng nhập để xem sản phẩm yêu thích", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        showLoading();

        firestoreManager.loadFavoriteProducts(userId, new FirestoreManager.OnFavoriteProductsLoadedListener() {
            @Override
            public void onFavoriteProductsLoaded(List<Product> products) {
                hideLoading();
                favoriteProducts.clear();
                favoriteProducts.addAll(products);
                productAdapter.notifyDataSetChanged();

                if (products.isEmpty()) {
                    showEmptyState();
                } else {
                    showFavorites();
                }
            }

            @Override
            public void onError(String error) {
                hideLoading();
                showEmptyState();
                Toast.makeText(FavoritesActivity.this, "Lỗi tải dữ liệu: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showLoading() {
        loadingProgress.setVisibility(View.VISIBLE);
        favoritesRecyclerView.setVisibility(View.GONE);
        emptyStateLayout.setVisibility(View.GONE);
    }

    private void hideLoading() {
        loadingProgress.setVisibility(View.GONE);
    }

    private void showFavorites() {
        favoritesRecyclerView.setVisibility(View.VISIBLE);
        emptyStateLayout.setVisibility(View.GONE);
    }

    private void showEmptyState() {
        favoritesRecyclerView.setVisibility(View.GONE);
        emptyStateLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void onProductClick(Product product) {
        Intent intent = new Intent(this, ProductDetailActivity.class);
        intent.putExtra("product", product);
        startActivity(intent);
    }

    @Override
    public void onFavoriteClick(Product product) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        String productId = product.getId();

        if (product.isFavorite()) {
            // Remove from favorites
            firestoreManager.removeFavorite(userId, productId, new FirestoreManager.OnFavoriteRemovedListener() {
                @Override
                public void onFavoriteRemoved() {
                    product.setFavorite(false);

                    // Remove from list and update UI
                    favoriteProducts.remove(product);
                    productAdapter.notifyDataSetChanged();

                    if (favoriteProducts.isEmpty()) {
                        showEmptyState();
                    }

                    Toast.makeText(FavoritesActivity.this, "Đã xóa khỏi yêu thích", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(String error) {
                    Toast.makeText(FavoritesActivity.this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // Add to favorites (shouldn't happen in this screen, but handle it)
            firestoreManager.saveFavorite(userId, productId, new FirestoreManager.OnFavoriteSavedListener() {
                @Override
                public void onFavoriteSaved() {
                    product.setFavorite(true);
                    productAdapter.notifyDataSetChanged();
                    Toast.makeText(FavoritesActivity.this, "Đã thêm vào yêu thích", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(String error) {
                    Toast.makeText(FavoritesActivity.this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public void onAddToCartClick(Product product) {
        // Handle add to cart
        Toast.makeText(this, "Đã thêm " + product.getName() + " vào giỏ hàng", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload favorites when returning to this screen
        loadFavorites();
    }

    private void setupBottomNavigation() {
        // Set Wishlist as selected
        bottomNavigation.setSelectedItemId(R.id.nav_wishlist);

        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                Intent intent = new Intent(FavoritesActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (itemId == R.id.nav_categories) {
                Intent intent = new Intent(FavoritesActivity.this, CategoriesActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (itemId == R.id.nav_wishlist) {
                // Already on wishlist
                return true;
            } else if (itemId == R.id.nav_account) {
                Intent intent = new Intent(FavoritesActivity.this, ProfileActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
                return true;
            }

            return false;
        });
    }
}
