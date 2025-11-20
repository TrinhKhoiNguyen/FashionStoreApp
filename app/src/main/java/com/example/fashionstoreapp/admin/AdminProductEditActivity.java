package com.example.fashionstoreapp.admin;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.example.fashionstoreapp.R;
import com.example.fashionstoreapp.models.Product;
import com.example.fashionstoreapp.utils.FirestoreManager;
import com.google.android.material.textfield.TextInputEditText;

public class AdminProductEditActivity extends AppCompatActivity {

    private TextInputEditText etImageUrl, etProductName, etCurrentPrice, etOriginalPrice, etCategory, etDescription;
    private CheckBox cbIsNew, cbHasVoucher;
    private ImageView ivProductPreview;
    private Button btnSaveProduct, btnPreviewImage;
    private ProgressBar progressBar;

    private FirestoreManager firestoreManager;
    private Product product;
    private boolean isEditMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_product_edit);

        firestoreManager = FirestoreManager.getInstance();
        
        // Check if we are editing an existing product
        if (getIntent().hasExtra("product")) {
            product = (Product) getIntent().getSerializableExtra("product");
            isEditMode = true;
        }

        initViews();
        setupListeners();
        
        if (isEditMode && product != null) {
            populateData();
        }
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(isEditMode ? "Sửa sản phẩm" : "Thêm sản phẩm");
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        etImageUrl = findViewById(R.id.etImageUrl);
        etProductName = findViewById(R.id.etProductName);
        etCurrentPrice = findViewById(R.id.etCurrentPrice);
        etOriginalPrice = findViewById(R.id.etOriginalPrice);
        etCategory = findViewById(R.id.etCategory);
        etDescription = findViewById(R.id.etDescription);
        cbIsNew = findViewById(R.id.cbIsNew);
        cbHasVoucher = findViewById(R.id.cbHasVoucher);
        ivProductPreview = findViewById(R.id.ivProductPreview);
        btnSaveProduct = findViewById(R.id.btnSaveProduct);
        btnPreviewImage = findViewById(R.id.btnPreviewImage);
        progressBar = findViewById(R.id.progressBar);
    }

    private void populateData() {
        etProductName.setText(product.getName());
        etCurrentPrice.setText(String.valueOf(product.getCurrentPrice()));
        etOriginalPrice.setText(String.valueOf(product.getOriginalPrice()));
        etCategory.setText(product.getCategory());
        etDescription.setText(product.getDescription());
        etImageUrl.setText(product.getImageUrl());
        cbIsNew.setChecked(product.isNew());
        cbHasVoucher.setChecked(product.isHasVoucher());

        loadImage(product.getImageUrl());
    }

    private void setupListeners() {
        btnPreviewImage.setOnClickListener(v -> {
            String url = etImageUrl.getText().toString().trim();
            loadImage(url);
        });

        btnSaveProduct.setOnClickListener(v -> saveProduct());
    }

    private void loadImage(String url) {
        if (url != null && !url.isEmpty()) {
            Glide.with(this)
                    .load(url)
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .error(android.R.drawable.stat_notify_error)
                    .into(ivProductPreview);
        }
    }

    private void saveProduct() {
        String name = etProductName.getText().toString().trim();
        String priceStr = etCurrentPrice.getText().toString().trim();
        String originalPriceStr = etOriginalPrice.getText().toString().trim();
        String category = etCategory.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String imageUrl = etImageUrl.getText().toString().trim();

        if (name.isEmpty() || priceStr.isEmpty() || category.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin bắt buộc", Toast.LENGTH_SHORT).show();
            return;
        }

        double currentPrice = Double.parseDouble(priceStr);
        double originalPrice = originalPriceStr.isEmpty() ? currentPrice : Double.parseDouble(originalPriceStr);

        if (product == null) {
            product = new Product();
        }

        product.setName(name);
        product.setCurrentPrice(currentPrice);
        product.setOriginalPrice(originalPrice);
        product.setCategory(category);
        product.setDescription(description);
        product.setImageUrl(imageUrl);
        product.setIsNew(cbIsNew.isChecked()); // Using setIsNew for Firestore compatibility
        product.setNew(cbIsNew.isChecked());
        product.setHasVoucher(cbHasVoucher.isChecked());
        
        // Default values if new
        if (!isEditMode) {
            product.setStockQuantity(100); // Default stock
        }

        progressBar.setVisibility(View.VISIBLE);
        btnSaveProduct.setEnabled(false);

        FirestoreManager.OnActionCompleteListener listener = new FirestoreManager.OnActionCompleteListener() {
            @Override
            public void onSuccess() {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(AdminProductEditActivity.this, isEditMode ? "Cập nhật thành công" : "Thêm mới thành công", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);
                btnSaveProduct.setEnabled(true);
                Toast.makeText(AdminProductEditActivity.this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
            }
        };

        if (isEditMode) {
            firestoreManager.updateProduct(product, listener);
        } else {
            firestoreManager.addProduct(product, new FirestoreManager.OnProductAddedListener() {
                @Override
                public void onProductAdded(String productId) {
                    listener.onSuccess();
                }

                @Override
                public void onError(String error) {
                    listener.onError(error);
                }
            });
        }
    }
}
