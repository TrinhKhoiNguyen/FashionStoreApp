package com.example.fashionstoreapp.admin;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fashionstoreapp.R;
import com.example.fashionstoreapp.models.Product;
import com.example.fashionstoreapp.utils.FirestoreManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AdminProductActivity extends AppCompatActivity implements AdminProductAdapter.OnProductActionListener {

    private RecyclerView recyclerView;
    private AdminProductAdapter adapter;
    private ProgressBar progressBar;
    private FirestoreManager firestoreManager;
    private List<Product> allProducts = new ArrayList<>();
    private List<Product> filteredProducts = new ArrayList<>();
    
    private TextInputEditText etSearch;
    private MaterialButton btnFilter, btnSort;
    
    // Filter and Sort state
    private String selectedCategory = "Tất cả";
    private String sortBy = "Mặc định"; // Mặc định, Giá tăng, Giá giảm, Tên A-Z

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_product_list);

        firestoreManager = FirestoreManager.getInstance();

        initViews();
        loadProducts();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        loadProducts(); // Reload when coming back from Edit/Add
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        progressBar = findViewById(R.id.progressBar);
        recyclerView = findViewById(R.id.rvAdminProducts);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        etSearch = findViewById(R.id.etSearch);
        btnFilter = findViewById(R.id.btnFilter);
        btnSort = findViewById(R.id.btnSort);
        
        adapter = new AdminProductAdapter(this, filteredProducts, this);
        recyclerView.setAdapter(adapter);

        FloatingActionButton fab = findViewById(R.id.fabAddProduct);
        fab.setOnClickListener(v -> {
             Intent intent = new Intent(AdminProductActivity.this, AdminProductEditActivity.class);
             startActivity(intent);
        });
        
        setupSearch();
        setupFilter();
        setupSort();
    }
    
    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyFilters();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }
    
    private void setupFilter() {
        btnFilter.setOnClickListener(v -> showFilterDialog());
    }
    
    private void setupSort() {
        btnSort.setOnClickListener(v -> showSortDialog());
    }
    
    private void showFilterDialog() {
        // Get unique categories
        Set<String> categories = new HashSet<>();
        categories.add("Tất cả");
        for (Product p : allProducts) {
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
        filteredProducts.clear();
        
        String searchQuery = etSearch.getText().toString().toLowerCase().trim();
        
        // Filter by search and category
        for (Product product : allProducts) {
            boolean matchesSearch = searchQuery.isEmpty() || 
                    product.getName().toLowerCase().contains(searchQuery) ||
                    (product.getCategory() != null && product.getCategory().toLowerCase().contains(searchQuery));
            
            boolean matchesCategory = selectedCategory.equals("Tất cả") || 
                    (product.getCategory() != null && product.getCategory().equals(selectedCategory));
            
            if (matchesSearch && matchesCategory) {
                filteredProducts.add(product);
            }
        }
        
        // Sort
        sortProducts();
        
        adapter.notifyDataSetChanged();
    }
    
    private void sortProducts() {
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
    }

    private void loadProducts() {
        progressBar.setVisibility(View.VISIBLE);
        // Load ALL products (might need pagination later but fine for now)
        firestoreManager.loadAllProducts(new FirestoreManager.OnProductsLoadedListener() {
            @Override
            public void onProductsLoaded(List<Product> loadedProducts) {
                allProducts.clear();
                allProducts.addAll(loadedProducts);
                applyFilters(); // Apply current filters
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(AdminProductActivity.this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onEdit(Product product) {
        Intent intent = new Intent(this, AdminProductEditActivity.class);
        intent.putExtra("product", product);
        startActivity(intent);
    }

    @Override
    public void onDelete(Product product) {
        new AlertDialog.Builder(this)
                .setTitle("Xóa sản phẩm")
                .setMessage("Bạn có chắc chắn muốn xóa " + product.getName() + "?")
                .setPositiveButton("Xóa", (dialog, which) -> deleteProduct(product))
                .setNegativeButton("Hủy", null)
                .show();
    }
    
    private void deleteProduct(Product product) {
        progressBar.setVisibility(View.VISIBLE);
        firestoreManager.deleteProduct(product.getId(), new FirestoreManager.OnActionCompleteListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(AdminProductActivity.this, "Đã xóa sản phẩm", Toast.LENGTH_SHORT).show();
                loadProducts(); // Reload list
            }

            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(AdminProductActivity.this, "Lỗi xóa: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
