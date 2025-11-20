package com.example.fashionstoreapp.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.fashionstoreapp.MainActivity;
import com.example.fashionstoreapp.R;
import com.google.android.material.card.MaterialCardView;

public class AdminActivity extends AppCompatActivity {

    private MaterialCardView cardManageProducts;
    private MaterialCardView cardManageOrders;
    private MaterialCardView cardManageUsers;
    private MaterialCardView cardManageReviews;
    private MaterialCardView cardExitAdmin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        setupToolbar();
        initViews();
        setupListeners();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.adminToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Quản trị viên");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void initViews() {
        cardManageProducts = findViewById(R.id.cardManageProducts);
        cardManageOrders = findViewById(R.id.cardManageOrders);
        cardManageUsers = findViewById(R.id.cardManageUsers);
        cardManageReviews = findViewById(R.id.cardManageReviews);
        cardExitAdmin = findViewById(R.id.cardExitAdmin);
    }

    private void setupListeners() {
        cardManageProducts.setOnClickListener(v -> {
            Intent intent = new Intent(AdminActivity.this, AdminProductActivity.class);
            startActivity(intent);
        });

        cardManageOrders.setOnClickListener(v -> {
            Toast.makeText(this, "Tính năng quản lý đơn hàng đang phát triển", Toast.LENGTH_SHORT).show();
            // Intent intent = new Intent(AdminActivity.this, AdminOrderActivity.class);
            // startActivity(intent);
        });

        cardManageUsers.setOnClickListener(v -> {
            Toast.makeText(this, "Tính năng quản lý người dùng đang phát triển", Toast.LENGTH_SHORT).show();
            // Intent intent = new Intent(AdminActivity.this, AdminUserActivity.class);
            // startActivity(intent);
        });

        cardManageReviews.setOnClickListener(v -> {
            Toast.makeText(this, "Tính năng quản lý đánh giá đang phát triển", Toast.LENGTH_SHORT).show();
            // Intent intent = new Intent(AdminActivity.this, AdminReviewActivity.class);
            // startActivity(intent);
        });

        cardExitAdmin.setOnClickListener(v -> {
            finish(); // Close Admin activity
        });
    }
}
