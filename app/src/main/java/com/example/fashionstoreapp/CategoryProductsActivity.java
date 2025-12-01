package com.example.fashionstoreapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fashionstoreapp.adapters.ProductAdapter;
import com.example.fashionstoreapp.models.FilterCriteria;
import com.example.fashionstoreapp.models.Product;
import com.example.fashionstoreapp.models.SortOption;
import com.example.fashionstoreapp.utils.EndlessRecyclerOnScrollListener;
import com.example.fashionstoreapp.utils.FilterPreferenceManager;
import com.example.fashionstoreapp.utils.FirestoreManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CategoryProductsActivity extends AppCompatActivity implements ProductAdapter.OnProductClickListener {

    private static final String TAG = "CategoryProductsActivity";
    private static final int PAGE_SIZE = 20; // Load 20 products per page

    private Toolbar toolbar;
    private RecyclerView productsRecyclerView;
    private ProgressBar progressBar;
    private TextView emptyStateText;
    private TextView categoryDescription;
    private FloatingActionButton filterFab;

    private ProductAdapter productAdapter;
    private List<Product> productList = new ArrayList<>();
    private List<Product> allLoadedProducts = new ArrayList<>(); // All products loaded from Firestore

    private String categoryId;
    private String categoryName;

    private FirestoreManager firestoreManager;
    private FirebaseAuth mAuth;
    private FilterPreferenceManager filterPreferenceManager;

    // Pagination
    private DocumentSnapshot lastVisible;
    private boolean isLastPage = false;
    private boolean isLoading = false;
    private EndlessRecyclerOnScrollListener scrollListener;

    // Filter & Sort
    private FilterCriteria currentFilter;
    private SortOption currentSort = SortOption.DEFAULT;

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
        filterPreferenceManager = new FilterPreferenceManager(this);

        initViews();
        setupToolbar();
        setupRecyclerView();
        loadSavedFilters();
        loadInitialData();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        productsRecyclerView = findViewById(R.id.productsRecyclerView);
        progressBar = findViewById(R.id.progressBar);
        emptyStateText = findViewById(R.id.emptyStateText);
        categoryDescription = findViewById(R.id.categoryDescription);
        filterFab = findViewById(R.id.filterFab);

        if (filterFab != null) {
            filterFab.setOnClickListener(v -> showFilterBottomSheet());
        }
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

        // Setup endless scroll for pagination
        scrollListener = new EndlessRecyclerOnScrollListener(layoutManager) {
            @Override
            public void onLoadMore() {
                if (!isLastPage && !isLoading) {
                    loadNextPage();
                }
            }
        };
        productsRecyclerView.addOnScrollListener(scrollListener);
    }

    /**
     * Load saved filter preferences if available
     */
    private void loadSavedFilters() {
        if (filterPreferenceManager.hasSavedPreferences()) {
            currentFilter = filterPreferenceManager.loadFilterCriteria();
            // Parse sort option
            try {
                currentSort = SortOption.valueOf(currentFilter.getSortBy());
            } catch (IllegalArgumentException e) {
                currentSort = SortOption.DEFAULT;
            }
        } else {
            currentFilter = new FilterCriteria();
        }
    }

    /**
     * Load initial page of products with server-side filtering
     */
    private void loadInitialData() {
        showLoading();
        isLoading = true;
        lastVisible = null;
        allLoadedProducts.clear();
        productList.clear();
        productAdapter.notifyDataSetChanged();

        if (scrollListener != null) {
            scrollListener.reset();
        }

        loadProductsPage();
    }

    /**
     * Load next page of products (pagination)
     */
    private void loadNextPage() {
        if (isLoading || isLastPage)
            return;
        isLoading = true;
        loadProductsPage();
    }

    /**
     * Core method: Load products with hybrid filtering
     * - Server-side: category, price range (if possible), sort
     * - Client-side: size, multiple criteria combinations
     */
    private void loadProductsPage() {
        Query query = FirebaseFirestore.getInstance()
                .collection("products")
                .whereEqualTo("category", categoryId);

        // SERVER-SIDE FILTERS

        // Price filter (if min/max set)
        if (currentFilter.getMinPrice() != null && currentFilter.getMinPrice() > 0) {
            query = query.whereGreaterThanOrEqualTo("currentPrice", currentFilter.getMinPrice());
        }
        if (currentFilter.getMaxPrice() != null && currentFilter.getMaxPrice() < Double.MAX_VALUE) {
            query = query.whereLessThanOrEqualTo("currentPrice", currentFilter.getMaxPrice());
        }

        // Stock filter (server-side if possible)
        // Note: Firestore doesn't support complex queries like stockQuantity > 0
        // combined with price
        // So we'll do stock filtering client-side

        // SERVER-SIDE SORT
        if (currentSort != SortOption.DEFAULT && currentSort.isServerSideSort()) {
            Query.Direction direction = currentSort.getFirestoreDirection().equals("ASCENDING")
                    ? Query.Direction.ASCENDING
                    : Query.Direction.DESCENDING;
            query = query.orderBy(currentSort.getFirestoreField(), direction);
        }

        // Pagination
        query = query.limit(PAGE_SIZE);
        if (lastVisible != null) {
            query = query.startAfter(lastVisible);
        }

        query.get()
                .addOnSuccessListener(querySnapshot -> {
                    hideLoading();
                    isLoading = false;

                    if (querySnapshot.isEmpty()) {
                        isLastPage = true;
                        if (allLoadedProducts.isEmpty()) {
                            showEmptyState();
                        }
                        return;
                    }

                    // Get last document for next page
                    List<DocumentSnapshot> documents = querySnapshot.getDocuments();
                    if (documents.size() < PAGE_SIZE) {
                        isLastPage = true;
                    }
                    if (!documents.isEmpty()) {
                        lastVisible = documents.get(documents.size() - 1);
                    }

                    // Parse products
                    List<Product> newProducts = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Product product = doc.toObject(Product.class);
                        if (product != null) {
                            product.setId(doc.getId());
                            newProducts.add(product);
                        }
                    }

                    // Add to all loaded products
                    allLoadedProducts.addAll(newProducts);

                    // Apply client-side filters and update display
                    applyClientSideFiltersAndDisplay();
                })
                .addOnFailureListener(e -> {
                    hideLoading();
                    isLoading = false;
                    Toast.makeText(this, "Lỗi tải dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error loading products", e);
                });
    }

    /**
     * Apply CLIENT-SIDE filters (size, stock status, rating)
     * Then sort and display results
     */
    private void applyClientSideFiltersAndDisplay() {
        List<Product> filtered = new ArrayList<>();

        for (Product product : allLoadedProducts) {
            if (currentFilter.matches(product)) {
                filtered.add(product);
            }
        }

        // CLIENT-SIDE SORTING (if not done server-side or for complex sorts)
        if (currentSort != SortOption.DEFAULT) {
            sortProducts(filtered);
        }

        productList.clear();
        productList.addAll(filtered);
        productAdapter.notifyDataSetChanged();

        if (productList.isEmpty()) {
            showEmptyState();
        } else {
            hideEmptyState();
        }
    }

    /**
     * Sort products based on current sort option
     */
    private void sortProducts(List<Product> products) {
        switch (currentSort) {
            case PRICE_LOW_TO_HIGH:
                Collections.sort(products, Comparator.comparingDouble(Product::getCurrentPrice));
                break;
            case PRICE_HIGH_TO_LOW:
                Collections.sort(products, (p1, p2) -> Double.compare(p2.getCurrentPrice(), p1.getCurrentPrice()));
                break;
            case NEWEST:
                Collections.sort(products, (p1, p2) -> {
                    if (p1.getCreatedAt() == null)
                        return 1;
                    if (p2.getCreatedAt() == null)
                        return -1;
                    return p2.getCreatedAt().compareTo(p1.getCreatedAt());
                });
                break;
            case POPULARITY:
                Collections.sort(products, (p1, p2) -> Integer.compare(p2.getPopularity(), p1.getPopularity()));
                break;
            case RATING:
                Collections.sort(products, (p1, p2) -> Double.compare(p2.getAverageRating(), p1.getAverageRating()));
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.menu_category_products, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        if (item.getItemId() == R.id.action_filter_products) {
            showFilterBottomSheet();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Show ProductFilterBottomSheet dialog
     */
    private void showFilterBottomSheet() {
        ProductFilterBottomSheet bottomSheet = ProductFilterBottomSheet.newInstance(
                allLoadedProducts,
                currentFilter);

        bottomSheet.setOnFilterAppliedListener(new ProductFilterBottomSheet.OnFilterAppliedListener() {
            @Override
            public void onFilterApplied(FilterCriteria criteria) {
                currentFilter = criteria;

                // Parse sort option
                try {
                    currentSort = SortOption.valueOf(criteria.getSortBy());
                } catch (IllegalArgumentException e) {
                    currentSort = SortOption.DEFAULT;
                }

                // Save preferences
                filterPreferenceManager.saveFilterCriteria(criteria);

                // Reload data with new filters
                loadInitialData();
            }

            @Override
            public void onFilterReset() {
                currentFilter = new FilterCriteria();
                currentSort = SortOption.DEFAULT;
                filterPreferenceManager.clearPreferences();
                loadInitialData();
            }
        });

        // Load categories for ChipGroup
        FirebaseFirestore.getInstance()
                .collection("categories")
                .whereEqualTo("active", true)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<String> categoryIds = new ArrayList<>();
                    List<String> categoryNames = new ArrayList<>();

                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        categoryIds.add(doc.getId());
                        String name = doc.contains("name") ? doc.getString("name") : doc.getId();
                        categoryNames.add(name);
                    }

                    bottomSheet.setCategoryChips(categoryIds, categoryNames);
                });

        bottomSheet.show(getSupportFragmentManager(), "ProductFilterBottomSheet");
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
        // Clean up scroll listener
        if (scrollListener != null && productsRecyclerView != null) {
            productsRecyclerView.removeOnScrollListener(scrollListener);
        }
    }
}
