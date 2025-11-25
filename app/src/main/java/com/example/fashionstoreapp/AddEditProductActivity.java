package com.example.fashionstoreapp;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.example.fashionstoreapp.models.Category;
import com.example.fashionstoreapp.models.Product;
import com.example.fashionstoreapp.models.SizeStock;
import com.example.fashionstoreapp.utils.FirestoreManager;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Activity for adding new product or editing existing product
 */
public class AddEditProductActivity extends AppCompatActivity {

    // UI Components
    private Toolbar toolbar;
    private ImageView productImageView;
    private TextInputEditText nameInput, descriptionInput, currentPriceInput, originalPriceInput, imageUrlInput;
    private AutoCompleteTextView categoryDropdown;
    private CheckBox isVisibleCheckbox;
    private ChipGroup colorsChipGroup;
    private LinearLayout sizeStockContainer;
    private Button saveButton, addColorButton;
    private ProgressBar progressBar;

    // Data
    private FirestoreManager firestoreManager;
    private String productId;
    private boolean isEditMode = false;
    private List<Category> categoriesList = new ArrayList<>();
    private List<String> selectedColors = new ArrayList<>();
    private List<SizeStock> sizeStocks = new ArrayList<>();

    // Default sizes
    private final String[] DEFAULT_SIZES = { "S", "M", "L", "XL" };

    // Preset colors
    private final String[] PRESET_COLORS = {
            "Đen", "Trắng", "Xám", "Đỏ", "Xanh dương",
            "Xanh lá", "Vàng", "Hồng", "Nâu", "Be"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_product);

        initViews();
        setupToolbar();
        firestoreManager = FirestoreManager.getInstance();

        // Check if edit mode
        productId = getIntent().getStringExtra("PRODUCT_ID");
        isEditMode = getIntent().getBooleanExtra("EDIT_MODE", false);

        loadCategories();
        setupSizeStocks();
        setupListeners();

        if (isEditMode && productId != null) {
            toolbar.setTitle("Sửa Sản Phẩm");
            loadProductData();
        } else {
            toolbar.setTitle("Thêm Sản Phẩm Mới");
        }
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        productImageView = findViewById(R.id.productImageView);
        nameInput = findViewById(R.id.productNameInput);
        descriptionInput = findViewById(R.id.productDescriptionInput);
        currentPriceInput = findViewById(R.id.currentPriceInput);
        originalPriceInput = findViewById(R.id.originalPriceInput);
        imageUrlInput = findViewById(R.id.imageUrlInput);
        categoryDropdown = findViewById(R.id.categoryDropdown);
        isVisibleCheckbox = findViewById(R.id.isVisibleCheckbox);
        colorsChipGroup = findViewById(R.id.colorsChipGroup);
        sizeStockContainer = findViewById(R.id.sizeStockContainer);
        saveButton = findViewById(R.id.saveProductButton);
        addColorButton = findViewById(R.id.addColorButton);
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

        // Add color button
        addColorButton.setOnClickListener(v -> showColorPickerDialog());

        // Save button
        saveButton.setOnClickListener(v -> validateAndSaveProduct());
    }

    private void loadCategories() {
        firestoreManager.loadCategories(new FirestoreManager.OnCategoriesLoadedListener() {
            @Override
            public void onCategoriesLoaded(List<Category> categories) {
                categoriesList = categories;
                setupCategoryDropdown();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(AddEditProductActivity.this, "Lỗi tải danh mục: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupCategoryDropdown() {
        String[] categoryNames = new String[categoriesList.size()];
        for (int i = 0; i < categoriesList.size(); i++) {
            categoryNames[i] = categoriesList.get(i).getName();
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                categoryNames);
        categoryDropdown.setAdapter(adapter);
    }

    private void setupSizeStocks() {
        sizeStockContainer.removeAllViews();

        // Initialize default sizes with stock 0
        if (sizeStocks.isEmpty()) {
            for (String size : DEFAULT_SIZES) {
                sizeStocks.add(new SizeStock(size, 0));
            }
        }

        // Create input rows for each size
        for (int i = 0; i < sizeStocks.size(); i++) {
            final int index = i;
            SizeStock sizeStock = sizeStocks.get(i);

            View sizeRow = getLayoutInflater().inflate(R.layout.item_size_stock_input, sizeStockContainer, false);

            TextInputEditText sizeInput = sizeRow.findViewById(R.id.sizeInput);
            TextInputEditText stockInput = sizeRow.findViewById(R.id.stockInput);

            sizeInput.setText(sizeStock.getSize());
            stockInput.setText(String.valueOf(sizeStock.getStock()));

            sizeInput.setEnabled(false); // Size is fixed

            // Update stock when changed
            stockInput.setOnFocusChangeListener((v, hasFocus) -> {
                if (!hasFocus) {
                    try {
                        int stock = Integer.parseInt(stockInput.getText().toString());
                        sizeStocks.get(index).setStock(stock);
                    } catch (NumberFormatException e) {
                        stockInput.setText("0");
                    }
                }
            });

            sizeStockContainer.addView(sizeRow);
        }
    }

    private void showColorPickerDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Chọn Màu Sắc");

        boolean[] checkedItems = new boolean[PRESET_COLORS.length];
        for (int i = 0; i < PRESET_COLORS.length; i++) {
            checkedItems[i] = selectedColors.contains(PRESET_COLORS[i]);
        }

        builder.setMultiChoiceItems(PRESET_COLORS, checkedItems, (dialog, which, isChecked) -> {
            if (isChecked) {
                if (!selectedColors.contains(PRESET_COLORS[which])) {
                    selectedColors.add(PRESET_COLORS[which]);
                }
            } else {
                selectedColors.remove(PRESET_COLORS[which]);
            }
        });

        builder.setPositiveButton("OK", (dialog, which) -> updateColorsChips());
        builder.setNegativeButton("Hủy", null);
        builder.show();
    }

    private void updateColorsChips() {
        colorsChipGroup.removeAllViews();

        for (String color : selectedColors) {
            Chip chip = new Chip(this);
            chip.setText(color);
            chip.setCloseIconVisible(true);
            chip.setOnCloseIconClickListener(v -> {
                selectedColors.remove(color);
                updateColorsChips();
            });
            colorsChipGroup.addView(chip);
        }
    }

    private void loadImagePreview(String url) {
        Glide.with(this)
                .load(url)
                .placeholder(R.drawable.baseline_category_24)
                .into(productImageView);
    }

    private void loadProductData() {
        progressBar.setVisibility(View.VISIBLE);

        firestoreManager.getAllProducts(new FirestoreManager.OnProductsLoadedListener() {
            @Override
            public void onProductsLoaded(List<Product> products) {
                for (Product product : products) {
                    if (product.getId().equals(productId)) {
                        fillFormWithProduct(product);
                        break;
                    }
                }
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(AddEditProductActivity.this, "Lỗi tải sản phẩm: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fillFormWithProduct(Product product) {
        nameInput.setText(product.getName());
        descriptionInput.setText(product.getDescription());
        currentPriceInput.setText(String.valueOf((int) product.getCurrentPrice()));
        originalPriceInput.setText(String.valueOf((int) product.getOriginalPrice()));
        imageUrlInput.setText(product.getImageUrl());
        categoryDropdown.setText(product.getCategory(), false);
        isVisibleCheckbox.setChecked(product.isVisible());

        loadImagePreview(product.getImageUrl());

        // Load size stocks
        if (product.getSizeStocks() != null && !product.getSizeStocks().isEmpty()) {
            sizeStocks = product.getSizeStocks();
            setupSizeStocks();
        }

        // Load colors
        if (product.getColors() != null) {
            selectedColors = new ArrayList<>(product.getColors());
            updateColorsChips();
        }
    }

    private void validateAndSaveProduct() {
        // Validate inputs
        String name = nameInput.getText().toString().trim();
        String description = descriptionInput.getText().toString().trim();
        String currentPriceStr = currentPriceInput.getText().toString().trim();
        String originalPriceStr = originalPriceInput.getText().toString().trim();
        String imageUrl = imageUrlInput.getText().toString().trim();
        String category = categoryDropdown.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            nameInput.setError("Vui lòng nhập tên sản phẩm");
            return;
        }

        if (TextUtils.isEmpty(currentPriceStr)) {
            currentPriceInput.setError("Vui lòng nhập giá hiện tại");
            return;
        }

        if (TextUtils.isEmpty(originalPriceStr)) {
            originalPriceInput.setError("Vui lòng nhập giá gốc");
            return;
        }

        if (TextUtils.isEmpty(category)) {
            categoryDropdown.setError("Vui lòng chọn danh mục");
            return;
        }

        double currentPrice = Double.parseDouble(currentPriceStr);
        double originalPrice = Double.parseDouble(originalPriceStr);

        // Create or update product
        Product product = new Product();
        if (isEditMode) {
            product.setId(productId);
        }

        product.setName(name);
        product.setDescription(description);
        product.setCurrentPrice(currentPrice);
        product.setOriginalPrice(originalPrice);
        product.setImageUrl(imageUrl);
        product.setCategory(category);
        product.setVisible(isVisibleCheckbox.isChecked());
        product.setSizeStocks(sizeStocks);
        product.setColors(selectedColors);

        // Calculate total stock from size stocks
        int totalStock = 0;
        for (SizeStock ss : sizeStocks) {
            totalStock += ss.getStock();
        }
        product.setStockQuantity(totalStock);

        saveProduct(product);
    }

    private void saveProduct(Product product) {
        progressBar.setVisibility(View.VISIBLE);
        saveButton.setEnabled(false);

        firestoreManager.saveProduct(product, new FirestoreManager.OnProductSavedListener() {
            @Override
            public void onProductSaved(String productId) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(AddEditProductActivity.this,
                        isEditMode ? "Đã cập nhật sản phẩm" : "Đã thêm sản phẩm mới",
                        Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);
                saveButton.setEnabled(true);
                Toast.makeText(AddEditProductActivity.this, "Lỗi lưu sản phẩm: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
