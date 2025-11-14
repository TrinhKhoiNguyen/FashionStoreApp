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

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fashionstoreapp.adapters.ProductAdapter;
import com.example.fashionstoreapp.models.Product;
import com.example.fashionstoreapp.utils.FirestoreManager;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity implements ProductAdapter.OnProductClickListener {

    private ImageView backButton, searchButton;
    private EditText searchInput;
    private LinearLayout suggestionsLayout, resultsLayout, noResultsLayout;
    private TextView resultsCount;
    private RecyclerView searchResultsRecyclerView;
    private ProductAdapter searchResultsAdapter;

    private Chip chipShirt, chipPolo, chipJeans, chipJacket, chipShorts;

    private FirestoreManager firestoreManager;
    private List<Product> allProducts;
    private List<Product> searchResults;

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

        // Chips
        chipShirt = findViewById(R.id.chipShirt);
        chipPolo = findViewById(R.id.chipPolo);
        chipJeans = findViewById(R.id.chipJeans);
        chipJacket = findViewById(R.id.chipJacket);
        chipShorts = findViewById(R.id.chipShorts);

        // Auto focus on search input
        searchInput.requestFocus();
    }

    private void setupRecyclerView() {
        searchResultsAdapter = new ProductAdapter(this, searchResults, this);
        searchResultsRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        searchResultsRecyclerView.setAdapter(searchResultsAdapter);
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

        // Update UI
        if (searchResults.isEmpty()) {
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

        resultsCount.setText("Tìm thấy " + searchResults.size() + " kết quả");
        searchResultsAdapter.notifyDataSetChanged();
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
