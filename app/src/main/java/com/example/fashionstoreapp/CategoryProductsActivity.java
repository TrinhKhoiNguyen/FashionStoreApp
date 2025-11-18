package com.example.fashionstoreapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fashionstoreapp.adapters.ProductAdapter;
import com.example.fashionstoreapp.models.Product;
import com.example.fashionstoreapp.utils.FirestoreManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class CategoryProductsActivity extends AppCompatActivity implements ProductAdapter.OnProductClickListener {

    private Toolbar toolbar;
    private RecyclerView productsRecyclerView;
    private ProgressBar progressBar;
    private TextView emptyStateText;
    private TextView categoryDescription;

    private ProductAdapter productAdapter;
    private List<Product> productList = new ArrayList<>();

    private String categoryId;
    private String categoryName;

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

        productAdapter = new ProductAdapter(this, productList, this);
        productsRecyclerView.setAdapter(productAdapter);
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
                        productList.clear();

                        for (QueryDocumentSnapshot doc : snapshots) {
                            Product product = doc.toObject(Product.class);
                            if (product != null) {
                                product.setId(doc.getId());
                                productList.add(product);
                            }
                        }

                        if (productList.isEmpty()) {
                            showEmptyState();
                        } else {
                            hideEmptyState();
                            productAdapter.notifyDataSetChanged();
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
        emptyStateText.setVisibility(View.VISIBLE);
        emptyStateText.setText("Chưa có sản phẩm nào trong danh mục này");
    }

    private void hideEmptyState() {
        emptyStateText.setVisibility(View.GONE);
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
