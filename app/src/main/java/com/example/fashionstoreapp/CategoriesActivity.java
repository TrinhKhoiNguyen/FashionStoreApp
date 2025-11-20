package com.example.fashionstoreapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fashionstoreapp.adapters.CategoryAdapter;
import com.example.fashionstoreapp.models.Category;
import com.example.fashionstoreapp.utils.FirestoreManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class CategoriesActivity extends AppCompatActivity
        implements CategoryAdapter.OnCategoryClickListener {

    private RecyclerView categoriesRecyclerView;
    private CategoryAdapter categoryAdapter;
    private ProgressBar loadingProgress;
    private FirestoreManager firestoreManager;
    private BottomNavigationView bottomNavigation;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categories);

        // Initialize Firestore manager
        firestoreManager = FirestoreManager.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Initialize views
        initViews();

        // Setup RecyclerView
        setupRecyclerView();

        // Setup Bottom Navigation
        setupBottomNavigation();

        // Load categories
        loadCategories();
    }

    private void initViews() {
        categoriesRecyclerView = findViewById(R.id.categoriesRecyclerView);
        loadingProgress = findViewById(R.id.loadingProgress);
        bottomNavigation = findViewById(R.id.bottomNavigation);
    }

    private void setupRecyclerView() {
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        categoriesRecyclerView.setLayoutManager(layoutManager);
    }

    private void loadCategories() {
        loadingProgress.setVisibility(View.VISIBLE);

        firestoreManager.loadCategories(new FirestoreManager.OnCategoriesLoadedListener() {
            @Override
            public void onCategoriesLoaded(List<Category> categories) {
                loadingProgress.setVisibility(View.GONE);

                if (!categories.isEmpty()) {
                    categoryAdapter = new CategoryAdapter(CategoriesActivity.this, categories, CategoriesActivity.this);
                    categoriesRecyclerView.setAdapter(categoryAdapter);
                } else {
                    // Load sample categories if Firestore is empty
                    loadSampleCategories();
                }
            }

            @Override
            public void onError(String error) {
                loadingProgress.setVisibility(View.GONE);
                Toast.makeText(CategoriesActivity.this, "Lỗi tải danh mục: " + error, Toast.LENGTH_SHORT).show();
                // Load sample categories as fallback
                loadSampleCategories();
            }
        });
    }

    private void loadSampleCategories() {
        List<Category> sampleCategories = createSampleCategories();
        categoryAdapter = new CategoryAdapter(this, sampleCategories, this);
        categoriesRecyclerView.setAdapter(categoryAdapter);
    }

    private List<Category> createSampleCategories() {
        List<Category> categories = new ArrayList<>();

        categories.add(new Category("ao-thun", "Áo Thun", "", "", 1));
        categories.add(new Category("ao-polo", "Áo Polo", "", "", 2));
        categories.add(new Category("ao-so-mi", "Áo Sơ Mi", "", "", 3));
        categories.add(new Category("ao-hoodie", "Áo Hoodie", "", "", 4));
        categories.add(new Category("ao-khoac", "Áo Khoác", "", "", 5));
        categories.add(new Category("quan-sot", "Quần Sọt", "", "", 6));
        categories.add(new Category("quan-tay", "Quần Tây", "", "", 7));
        categories.add(new Category("retro-sports", "Retro Sports", "", "", 8));
        categories.add(new Category("outlet", "Outlet", "", "", 9));

        return categories;
    }

    @Override
    public void onCategoryClick(Category category) {
        // Open category products activity
        Intent intent = new Intent(this, CategoryProductsActivity.class);
        intent.putExtra("categoryId", category.getId());
        intent.putExtra("categoryName", category.getName());
        startActivity(intent);
    }

    private void setupBottomNavigation() {
        // Set Categories as selected
        bottomNavigation.setSelectedItemId(R.id.nav_categories);

        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                Intent intent = new Intent(CategoriesActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (itemId == R.id.nav_categories) {
                // Already on categories
                return true;
            } else if (itemId == R.id.nav_wishlist) {
                if (mAuth.getCurrentUser() == null) {
                    Toast.makeText(this, "Vui lòng đăng nhập để xem danh sách yêu thích", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(CategoriesActivity.this, LoginActivity.class);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(CategoriesActivity.this, FavoritesActivity.class);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                    finish();
                }
                return true;
            } else if (itemId == R.id.nav_account) {
                if (mAuth.getCurrentUser() == null) {
                    Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(CategoriesActivity.this, LoginActivity.class);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(CategoriesActivity.this, ProfileActivity.class);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                    finish();
                }
                return true;
            }

            return false;
        });
    }
}
