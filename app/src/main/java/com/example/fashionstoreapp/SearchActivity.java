package com.example.fashionstoreapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fashionstoreapp.adapters.ProductAdapter;
import com.example.fashionstoreapp.models.Product;
import com.example.fashionstoreapp.utils.FirestoreManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SearchActivity extends AppCompatActivity implements ProductAdapter.OnProductClickListener {

    private ImageView backButton, searchButton;
    private EditText searchInput;
    private LinearLayout suggestionsLayout, resultsLayout, noResultsLayout;
    private TextView resultsCount;
    private RecyclerView searchResultsRecyclerView;
    private ProductAdapter searchResultsAdapter;
    private MaterialButton btnFilter, btnSort;

    private Chip chipShirt, chipPolo, chipJeans, chipJacket, chipShorts;

    private FirestoreManager firestoreManager;
    private List<Product> allProducts;
    private List<Product> searchResults;
    private List<Product> filteredResults;
    
    // Filter and Sort state
    private String selectedCategory = "Tất cả";
    private String sortBy = "Mặc định";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        firestoreManager = FirestoreManager.getInstance();
        allProducts = new ArrayList<>();
        searchResults = new ArrayList<>();

        initViews();
        setupRecyclerView();
        setupClickListeners();
        loadAllProducts();
    }

    private void initViews() {
        backButton = findViewById(R.id.backButton);
        searchButton = findViewById(R.id.searchButton);
        searchInput = findViewById(R.id.searchInput);

        suggestionsLayout = findViewById(R.id.suggestionsLayout);
        resultsLayout = findViewById(R.id.resultsLayout);
        noResultsLayout = findViewById(R.id.noResultsLayout);

        resultsCount = findViewById(R.id.resultsCount);
        searchResultsRecyclerView = findViewById(R.id.searchResultsRecyclerView);
        btnFilter = findViewById(R.id.btnFilter);
        btnSort = findViewById(R.id.btnSort);

        // Chips
        chipShirt = findViewById(R.id.chipShirt);
        chipPolo = findViewById(R.id.chipPolo);
        chipJeans = findViewById(R.id.chipJeans);
        chipJacket = findViewById(R.id.chipJacket);
        chipShorts = findViewById(R.id.chipShorts);

        // Auto focus on search input
        searchInput.requestFocus();
        
        filteredResults = new ArrayList<>();
    }

    private void setupRecyclerView() {
        searchResultsAdapter = new ProductAdapter(this, filteredResults, this);
        searchResultsRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        searchResultsRecyclerView.setAdapter(searchResultsAdapter);
        
        setupFilter();
        setupSort();
    }
    
    private void setupFilter() {
        btnFilter.setOnClickListener(v -> showFilterDialog());
    }
    
    private void setupSort() {
        btnSort.setOnClickListener(v -> showSortDialog());
    }
    
    private void showFilterDialog() {
        // Get unique categories from search results
        Set<String> categories = new HashSet<>();
        categories.add("Tất cả");
        for (Product p : searchResults) {
            if (p.getCategory() != null) {
                categories.add(p.getCategory());
            }
        }
        String[] categoryArray = categories.toArray(new String[0]);
        
        int selectedIndex = 0;
        for (int i = 0; i < categoryArray.length; i++) {
            if (categoryArray[i].equals(selectedCategory)) {
                selectedIndex = i;
                break;
            }
        }
        
        new AlertDialog.Builder(this)
                .setTitle("Lọc theo danh mục")
                .setSingleChoiceItems(categoryArray, selectedIndex, (dialog, which) -> {
                    selectedCategory = categoryArray[which];
                    applyFilters();
                    dialog.dismiss();
                })
                .setNegativeButton("Hủy", null)
                .show();
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
                    applyFilters();
                    dialog.dismiss();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
    
    private void applyFilters() {
        filteredResults.clear();
        
        // Filter by category
        for (Product product : searchResults) {
            boolean matchesCategory = selectedCategory.equals("Tất cả") || 
                    (product.getCategory() != null && product.getCategory().equals(selectedCategory));
            
            if (matchesCategory) {
                filteredResults.add(product);
            }
        }
        
        // Sort
        sortProducts();
        
        searchResultsAdapter.notifyDataSetChanged();
        resultsCount.setText("Tìm thấy " + filteredResults.size() + " kết quả");
    }
    
    private void sortProducts() {
        switch (sortBy) {
            case "Giá tăng dần":
                Collections.sort(filteredResults, (p1, p2) -> 
                    Double.compare(p1.getCurrentPrice(), p2.getCurrentPrice()));
                break;
            case "Giá giảm dần":
                Collections.sort(filteredResults, (p1, p2) -> 
                    Double.compare(p2.getCurrentPrice(), p1.getCurrentPrice()));
                break;
            case "Tên A-Z":
                Collections.sort(filteredResults, (p1, p2) -> 
                    p1.getName().compareToIgnoreCase(p2.getName()));
                break;
            case "Tên Z-A":
                Collections.sort(filteredResults, (p1, p2) -> 
                    p2.getName().compareToIgnoreCase(p1.getName()));
                break;
            default: // Mặc định - keep original order
                break;
        }
    }

    private void setupClickListeners() {
        backButton.setOnClickListener(v -> finish());

        searchButton.setOnClickListener(v -> performSearch());

        // Search when user presses Enter
        searchInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                performSearch();
                return true;
            }
            return false;
        });

        // Real-time search as user types
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    performSearch();
                } else {
                    showSuggestions();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // Popular search chips
        chipShirt.setOnClickListener(v -> searchByKeyword("áo thun"));
        chipPolo.setOnClickListener(v -> searchByKeyword("áo polo"));
        chipJeans.setOnClickListener(v -> searchByKeyword("quần jeans"));
        chipJacket.setOnClickListener(v -> searchByKeyword("áo khoác"));
        chipShorts.setOnClickListener(v -> searchByKeyword("quần short"));
    }

    private void searchByKeyword(String keyword) {
        searchInput.setText(keyword);
        searchInput.setSelection(keyword.length());
        performSearch();
    }

    private void loadAllProducts() {
        firestoreManager.loadProducts(new FirestoreManager.OnProductsLoadedListener() {
            @Override
            public void onProductsLoaded(List<Product> products) {
                allProducts.clear();
                allProducts.addAll(products);
            }

            @Override
            public void onError(String error) {
                // Handle error silently or show a toast
            }
        });
    }

    private void performSearch() {
        String query = searchInput.getText().toString().trim().toLowerCase();

        if (query.isEmpty()) {
            showSuggestions();
            return;
        }

        // Filter products
        searchResults.clear();
        for (Product product : allProducts) {
            if (matchesQuery(product, query)) {
                searchResults.add(product);
            }
        }

        // Reset filter state
        selectedCategory = "Tất cả";
        sortBy = "Mặc định";
        
        // Apply filters and sort
        applyFilters();

        // Update UI
        if (filteredResults.isEmpty()) {
            showNoResults();
        } else {
            showResults();
        }
    }

    private boolean matchesQuery(Product product, String query) {
        // Search in product name
        if (product.getName() != null && product.getName().toLowerCase().contains(query)) {
            return true;
        }

        // Search in product description
        if (product.getDescription() != null && product.getDescription().toLowerCase().contains(query)) {
            return true;
        }

        // Search in category
        if (product.getCategory() != null && product.getCategory().toLowerCase().contains(query)) {
            return true;
        }

        return false;
    }

    private void showSuggestions() {
        suggestionsLayout.setVisibility(View.VISIBLE);
        resultsLayout.setVisibility(View.GONE);
        noResultsLayout.setVisibility(View.GONE);
    }

    private void showResults() {
        suggestionsLayout.setVisibility(View.GONE);
        resultsLayout.setVisibility(View.VISIBLE);
        noResultsLayout.setVisibility(View.GONE);
    }

    private void showNoResults() {
        suggestionsLayout.setVisibility(View.GONE);
        resultsLayout.setVisibility(View.GONE);
        noResultsLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void onProductClick(Product product) {
        Intent intent = new Intent(this, ProductDetailActivity.class);
        intent.putExtra("product", product);
        startActivity(intent);
    }

    @Override
    public void onAddToCartClick(Product product) {
        // Add to cart functionality can be implemented here
    }

    @Override
    public void onFavoriteClick(Product product) {
        // Favorite functionality can be implemented here
    }
}
