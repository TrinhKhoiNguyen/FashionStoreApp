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
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
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
    // Keep a copy of the full list so we can apply client-side filters
    private List<Product> allProducts = new ArrayList<>();

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
                        allProducts.clear();

                        for (QueryDocumentSnapshot doc : snapshots) {
                            Product product = doc.toObject(Product.class);
                            if (product != null) {
                                product.setId(doc.getId());
                                allProducts.add(product);
                            }
                        }

                        // Apply currently selected filters (none by default)
                        applyFiltersAndUpdate(allProducts);
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.menu_category_products, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        if (item.getItemId() == R.id.action_filter_products) {
            showFilterSheet();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showFilterSheet() {
        com.google.android.material.bottomsheet.BottomSheetDialog dialog = new com.google.android.material.bottomsheet.BottomSheetDialog(
                this);
        android.view.View sheet = getLayoutInflater().inflate(R.layout.dialog_filter_products, null);

        android.widget.EditText minPriceEt = sheet.findViewById(R.id.minPriceEt);
        android.widget.EditText maxPriceEt = sheet.findViewById(R.id.maxPriceEt);
        android.widget.CheckBox inStockCb = sheet.findViewById(R.id.inStockCb);
        ChipGroup categoryChipGroup = sheet.findViewById(R.id.categoryChipGroup);
        ChipGroup sizeChipGroup = sheet.findViewById(R.id.sizeChipGroup);
        android.widget.Button applyBtn = sheet.findViewById(R.id.applyFilterBtn);
        android.widget.Button resetBtn = sheet.findViewById(R.id.resetFilterBtn);

        applyBtn.setOnClickListener(v -> {
            String minText = minPriceEt.getText().toString().trim();
            String maxText = maxPriceEt.getText().toString().trim();
            Double min = null, max = null;
            try {
                if (!minText.isEmpty())
                    min = Double.parseDouble(minText);
            } catch (NumberFormatException ignored) {
            }
            try {
                if (!maxText.isEmpty())
                    max = Double.parseDouble(maxText);
            } catch (NumberFormatException ignored) {
            }

            // Default sort type (no spinner anymore)
            int sortType = 0;
            boolean inStockOnly = inStockCb.isChecked();

            // Collect selected categories and sizes
            java.util.Set<String> selectedCategories = new java.util.HashSet<>();
            for (int i = 0; i < categoryChipGroup.getChildCount(); i++) {
                android.view.View child = categoryChipGroup.getChildAt(i);
                if (child instanceof Chip) {
                    Chip c = (Chip) child;
                    if (c.isChecked() && c.getTag() != null) {
                        selectedCategories.add(String.valueOf(c.getTag()));
                    }
                }
            }

            java.util.Set<String> selectedSizes = new java.util.HashSet<>();
            for (int i = 0; i < sizeChipGroup.getChildCount(); i++) {
                android.view.View child = sizeChipGroup.getChildAt(i);
                if (child instanceof Chip) {
                    Chip c = (Chip) child;
                    if (c.isChecked() && c.getTag() != null) {
                        selectedSizes.add(String.valueOf(c.getTag()));
                    }
                }
            }

            filterAndApply(min, max, sortType, inStockOnly, selectedCategories, selectedSizes);
            dialog.dismiss();
        });

        resetBtn.setOnClickListener(v -> {
            // Reset filters
            // clear chips
            for (int i = 0; i < categoryChipGroup.getChildCount(); i++) {
                android.view.View child = categoryChipGroup.getChildAt(i);
                if (child instanceof Chip)
                    ((Chip) child).setChecked(false);
            }
            for (int i = 0; i < sizeChipGroup.getChildCount(); i++) {
                android.view.View child = sizeChipGroup.getChildAt(i);
                if (child instanceof Chip)
                    ((Chip) child).setChecked(false);
            }
            applyFiltersAndUpdate(allProducts);
            dialog.dismiss();
        });

        // Populate size chips from allProducts (unique sizes)
        java.util.Set<String> sizesSet = new java.util.LinkedHashSet<>();
        for (Product p : allProducts) {
            List<String> sizes = p.getAvailableSizes();
            if (sizes != null)
                sizesSet.addAll(sizes);
        }
        sizeChipGroup.removeAllViews();
        for (String s : sizesSet) {
            Chip chip = new Chip(this);
            chip.setText(s);
            chip.setCheckable(true);
            chip.setId(android.view.View.generateViewId());
            chip.setTag(s);
            sizeChipGroup.addView(chip);
        }

        // Populate category chips by querying Firestore categories collection
        categoryChipGroup.removeAllViews();
        FirebaseFirestore.getInstance()
                .collection("categories")
                .whereEqualTo("active", true)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String cid = doc.getId();
                        String name = doc.contains("name") ? doc.getString("name") : (doc.getId());
                        Chip chip = new Chip(this);
                        chip.setText(name);
                        chip.setCheckable(true);
                        chip.setId(android.view.View.generateViewId());
                        chip.setTag(cid);
                        categoryChipGroup.addView(chip);
                    }
                })
                .addOnFailureListener(e -> {
                    // ignore; categories optional
                });

        dialog.setContentView(sheet);
        dialog.show();
    }

    private void filterAndApply(Double minPrice, Double maxPrice, int sortType, boolean inStockOnly,
            java.util.Set<String> categories, java.util.Set<String> sizes) {
        List<Product> filtered = new ArrayList<>();
        for (Product p : allProducts) {
            double price = p.getCurrentPrice();
            if (minPrice != null && price < minPrice)
                continue;
            if (maxPrice != null && price > maxPrice)
                continue;
            if (inStockOnly && p.getStockQuantity() <= 0)
                continue;

            // Filter by categories (if selected)
            if (categories != null && !categories.isEmpty()) {
                String prodCat = p.getCategory();
                if (prodCat == null || !categories.contains(prodCat))
                    continue;
            }

            // Filter by sizes (if selected)
            if (sizes != null && !sizes.isEmpty()) {
                List<String> avail = p.getAvailableSizes();
                boolean any = false;
                if (avail != null) {
                    for (String s : avail) {
                        if (sizes.contains(s)) {
                            any = true;
                            break;
                        }
                    }
                }
                if (!any)
                    continue;
            }
            filtered.add(p);
        }

        // Sort
        switch (sortType) {
            case 1: // Price low -> high
                java.util.Collections.sort(filtered,
                        (a, b) -> Double.compare(a.getCurrentPrice(), b.getCurrentPrice()));
                break;
            case 2: // Price high -> low
                java.util.Collections.sort(filtered,
                        (a, b) -> Double.compare(b.getCurrentPrice(), a.getCurrentPrice()));
                break;
            case 3: // Newest - if there's a timestamp field, sort by it; fallback leave order
                // No timestamp field in Product model; keep as-is
                break;
            default:
                break;
        }

        applyFiltersAndUpdate(filtered);
    }

    private void applyFiltersAndUpdate(List<Product> newList) {
        productList.clear();
        if (newList != null) {
            productList.addAll(newList);
        }

        // The adapter holds a reference to productList, so we just need to notify it
        // that the data has changed.
        productAdapter.notifyDataSetChanged();

        if (productList.isEmpty()) {
            showEmptyState();
        } else {
            hideEmptyState();
        }
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
