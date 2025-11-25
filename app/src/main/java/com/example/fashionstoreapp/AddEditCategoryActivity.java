package com.example.fashionstoreapp;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.example.fashionstoreapp.models.Category;
import com.example.fashionstoreapp.utils.FirestoreManager;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

/**
 * Activity for adding new category or editing existing category
 */
public class AddEditCategoryActivity extends AppCompatActivity {

    // UI Components
    private Toolbar toolbar;
    private ImageView categoryImageView;
    private TextInputEditText nameInput, descriptionInput, imageUrlInput, displayOrderInput;
    private CheckBox isActiveCheckbox;
    private Button saveButton;
    private ProgressBar progressBar;

    // Data
    private FirestoreManager firestoreManager;
    private String categoryId;
    private boolean isEditMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_category);

        initViews();
        setupToolbar();
        firestoreManager = FirestoreManager.getInstance();

        // Check if edit mode
        categoryId = getIntent().getStringExtra("CATEGORY_ID");
        isEditMode = getIntent().getBooleanExtra("EDIT_MODE", false);

        setupListeners();

        if (isEditMode && categoryId != null) {
            toolbar.setTitle("Sửa Danh Mục");
            loadCategoryData();
        } else {
            toolbar.setTitle("Thêm Danh Mục Mới");
        }
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        categoryImageView = findViewById(R.id.categoryImageView);
        nameInput = findViewById(R.id.categoryNameInput);
        descriptionInput = findViewById(R.id.categoryDescriptionInput);
        imageUrlInput = findViewById(R.id.imageUrlInput);
        displayOrderInput = findViewById(R.id.displayOrderInput);
        isActiveCheckbox = findViewById(R.id.isActiveCheckbox);
        saveButton = findViewById(R.id.saveCategoryButton);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupListeners() {
        // Image URL input - show preview
        imageUrlInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String url = imageUrlInput.getText().toString().trim();
                if (!url.isEmpty()) {
                    loadImagePreview(url);
                }
            }
        });

        // Save button
        saveButton.setOnClickListener(v -> validateAndSaveCategory());
    }

    private void loadImagePreview(String url) {
        Glide.with(this)
                .load(url)
                .placeholder(R.drawable.baseline_category_24)
                .into(categoryImageView);
    }

    private void loadCategoryData() {
        progressBar.setVisibility(View.VISIBLE);

        firestoreManager.loadCategories(new FirestoreManager.OnCategoriesLoadedListener() {
            @Override
            public void onCategoriesLoaded(List<Category> categories) {
                for (Category category : categories) {
                    if (category.getId().equals(categoryId)) {
                        fillFormWithCategory(category);
                        break;
                    }
                }
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(AddEditCategoryActivity.this, "Lỗi tải danh mục: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fillFormWithCategory(Category category) {
        nameInput.setText(category.getName());
        descriptionInput.setText(category.getDescription());
        imageUrlInput.setText(category.getImageUrl());
        displayOrderInput.setText(String.valueOf(category.getDisplayOrder()));
        isActiveCheckbox.setChecked(category.isActive());

        loadImagePreview(category.getImageUrl());
    }

    private void validateAndSaveCategory() {
        // Validate inputs
        String name = nameInput.getText().toString().trim();
        String description = descriptionInput.getText().toString().trim();
        String imageUrl = imageUrlInput.getText().toString().trim();
        String displayOrderStr = displayOrderInput.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            nameInput.setError("Vui lòng nhập tên danh mục");
            return;
        }

        int displayOrder = 0;
        if (!TextUtils.isEmpty(displayOrderStr)) {
            try {
                displayOrder = Integer.parseInt(displayOrderStr);
            } catch (NumberFormatException e) {
                displayOrderInput.setError("Số thứ tự không hợp lệ");
                return;
            }
        }

        // Create or update category
        Category category = new Category();
        if (isEditMode) {
            category.setId(categoryId);
        }

        category.setName(name);
        category.setDescription(description);
        category.setImageUrl(imageUrl);
        category.setDisplayOrder(displayOrder);
        category.setIsActive(isActiveCheckbox.isChecked());

        saveCategory(category);
    }

    private void saveCategory(Category category) {
        progressBar.setVisibility(View.VISIBLE);
        saveButton.setEnabled(false);

        firestoreManager.saveCategory(category, new FirestoreManager.OnCategorySavedListener() {
            @Override
            public void onCategorySaved(String categoryId) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(AddEditCategoryActivity.this,
                        isEditMode ? "Đã cập nhật danh mục" : "Đã thêm danh mục mới",
                        Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);
                saveButton.setEnabled(true);
                Toast.makeText(AddEditCategoryActivity.this, "Lỗi lưu danh mục: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
