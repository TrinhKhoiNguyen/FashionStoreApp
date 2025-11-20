package com.example.fashionstoreapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fashionstoreapp.adapters.ProductAdapter;
import com.example.fashionstoreapp.models.Product;
import com.example.fashionstoreapp.utils.FirestoreManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CategoryProductsActivity extends AppCompatActivity implements ProductAdapter.OnProductClickListener {

    private Toolbar toolbar;
    private RecyclerView productsRecyclerView;
    private ProgressBar progressBar;
    private TextView emptyStateText;
    private TextView categoryDescription;
    private MaterialCardView filterSortCard;
    private MaterialButton btnFilter, btnSort;

    private ProductAdapter productAdapter;
    private List<Product> allProducts = new ArrayList<>();
    private List<Product> filteredProducts = new ArrayList<>();

    private String categoryId;
    private String categoryName;
    private String sortBy = "Mặc định";

    private FirestoreManager firestoreManager;
    private FirebaseAuth mAuth;
    private ListenerRegistration productsListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_products);

        // Get category info from intent
        categoryId = getIntent().getStringExtra("categoryId");
        categoryName = getIntent().getStringExtra("categoryName");

        if (categoryId == null || categoryId.isEmpty()) {
            Toast.makeText(this, "Lỗi: Không tìm thấy danh mục", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        firestoreManager = FirestoreManager.getInstance();
        mAuth = FirebaseAuth.getInstance();

        initViews();
        setupToolbar();
        setupRecyclerView();
        setupRealtimeListener();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        productsRecyclerView = findViewById(R.id.productsRecyclerView);
        progressBar = findViewById(R.id.progressBar);
        emptyStateText = findViewById(R.id.emptyStateText);
        categoryDescription = findViewById(R.id.categoryDescription);
        filterSortCard = findViewById(R.id.filterSortCard);
        btnFilter = findViewById(R.id.btnFilter);
        btnSort = findViewById(R.id.btnSort);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(categoryName != null ? categoryName : "Sản phẩm");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        productsRecyclerView.setLayoutManager(layoutManager);

        productAdapter = new ProductAdapter(this, filteredProducts, this);
        productsRecyclerView.setAdapter(productAdapter);
        
        setupFilter();
        setupSort();
    }
    
    private void setupFilter() {
        btnFilter.setOnClickListener(v -> {
            // For category page, filter is less relevant since already filtered by category
            // But can add price range filter if needed
            Toast.makeText(this, "Tính năng lọc giá đang phát triển", Toast.LENGTH_SHORT).show();
        });
    }
    
    private void setupSort() {
        btnSort.setOnClickListener(v -> showSortDialog());
    }
    
    private void showSortDialog() {
        String[] sortOptions = {"Mặc định", "Giá tăng dần", "Giá giảm dần", "Tên A-Z", "Tên Z-A"};
        int selectedIndex = 0;
        for (int i = 0; i < sortOptions.length; i++) {
            if (sortOptions[i].equals(sortBy)) {
                selectedIndex = i;
                break;
            }
        }
        
        new AlertDialog.Builder(this)
                .setTitle("Sắp xếp")
                .setSingleChoiceItems(sortOptions, selectedIndex, (dialog, which) -> {
                    sortBy = sortOptions[which];
                    applySort();
                    dialog.dismiss();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
    
    private void applySort() {
        filteredProducts.clear();
        filteredProducts.addAll(allProducts);
        
        switch (sortBy) {
            case "Giá tăng dần":
                Collections.sort(filteredProducts, (p1, p2) -> 
                    Double.compare(p1.getCurrentPrice(), p2.getCurrentPrice()));
                break;
            case "Giá giảm dần":
                Collections.sort(filteredProducts, (p1, p2) -> 
                    Double.compare(p2.getCurrentPrice(), p1.getCurrentPrice()));
                break;
            case "Tên A-Z":
                Collections.sort(filteredProducts, (p1, p2) -> 
                    p1.getName().compareToIgnoreCase(p2.getName()));
                break;
            case "Tên Z-A":
                Collections.sort(filteredProducts, (p1, p2) -> 
                    p2.getName().compareToIgnoreCase(p1.getName()));
                break;
            default: // Mặc định - keep original order
                break;
        }
        
        productAdapter.notifyDataSetChanged();
    }

    private void setupRealtimeListener() {
        showLoading();

        // Setup real-time listener for products in this category
        productsListener = FirebaseFirestore.getInstance()
                .collection("products")
                .whereEqualTo("category", categoryId)
                .addSnapshotListener((snapshots, error) -> {
                    hideLoading();

                    if (error != null) {
                        Toast.makeText(this, "Lỗi tải dữ liệu: " + error.getMessage(),
                                Toast.LENGTH_SHORT).show();
                        showEmptyState();
                        return;
                    }

                    if (snapshots != null) {
                        allProducts.clear();

                        for (QueryDocumentSnapshot doc : snapshots) {
                            Product product = doc.toObject(Product.class);
                            if (product != null) {
                                product.setId(doc.getId());
                                allProducts.add(product);
                            }
                        }

                        if (allProducts.isEmpty()) {
                            showEmptyState();
                        } else {
                            hideEmptyState();
                            applySort(); // Apply current sort
                        }
                    }
                });
    }

    private void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
        productsRecyclerView.setVisibility(View.GONE);
        emptyStateText.setVisibility(View.GONE);
    }

    private void hideLoading() {
        progressBar.setVisibility(View.GONE);
        productsRecyclerView.setVisibility(View.VISIBLE);
    }

    private void showEmptyState() {
        productsRecyclerView.setVisibility(View.GONE);
        filterSortCard.setVisibility(View.GONE);
        emptyStateText.setVisibility(View.VISIBLE);
        emptyStateText.setText("Chưa có sản phẩm nào trong danh mục này");
    }

    private void hideEmptyState() {
        emptyStateText.setVisibility(View.GONE);
        filterSortCard.setVisibility(View.VISIBLE);
        productsRecyclerView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onProductClick(Product product) {
        Intent intent = new Intent(this, ProductDetailActivity.class);
        intent.putExtra("product", product);
        startActivity(intent);
    }

    @Override
    public void onAddToCartClick(Product product) {
        Toast.makeText(this, "Đã thêm " + product.getName() + " vào giỏ hàng", Toast.LENGTH_SHORT).show();
        // TODO: Implement add to cart logic if needed
    }

    @Override
    public void onFavoriteClick(Product product) {
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Vui lòng đăng nhập để sử dụng tính năng này", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = mAuth.getCurrentUser().getUid();
        boolean isFavorite = product.isFavorite();

        // Optimistic update
        product.setFavorite(!isFavorite);
        productAdapter.notifyDataSetChanged();

        if (!isFavorite) {
            // Add to favorites
            firestoreManager.saveFavorite(userId, product.getId(), new FirestoreManager.OnFavoriteSavedListener() {
                @Override
                public void onFavoriteSaved() {
                    Toast.makeText(CategoryProductsActivity.this, "Đã thêm vào yêu thích", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(String error) {
                    // Revert on error
                    product.setFavorite(isFavorite);
                    productAdapter.notifyDataSetChanged();
                    Toast.makeText(CategoryProductsActivity.this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // Remove from favorites
            firestoreManager.removeFavorite(userId, product.getId(), new FirestoreManager.OnFavoriteRemovedListener() {
                @Override
                public void onFavoriteRemoved() {
                    Toast.makeText(CategoryProductsActivity.this, "Đã xóa khỏi yêu thích", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(String error) {
                    // Revert on error
                    product.setFavorite(isFavorite);
                    productAdapter.notifyDataSetChanged();
                    Toast.makeText(CategoryProductsActivity.this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remove listener to prevent memory leaks
        if (productsListener != null) {
            productsListener.remove();
        }
    }
}
