package com.example.fashionstoreapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fashionstoreapp.adapters.OrderAdapter;
import com.example.fashionstoreapp.model.Order;
import com.example.fashionstoreapp.utils.FirestoreManager;
import com.example.fashionstoreapp.utils.SessionManager;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.List;

public class OrderHistoryActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private RecyclerView ordersRecyclerView;
    private View emptyOrdersLayout, loadingLayout;
    private Button btnStartShopping;

    private OrderAdapter orderAdapter;
    private List<Order> orders;
    private FirestoreManager firestoreManager;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_history);

        firestoreManager = FirestoreManager.getInstance();
        sessionManager = new SessionManager(this);
        orders = new ArrayList<>();

        initViews();
        setupToolbar();
        setupRecyclerView();
        loadOrders();
    }

    private void initViews() {
        toolbar = findViewById(R.id.orderHistoryToolbar);
        ordersRecyclerView = findViewById(R.id.ordersRecyclerView);
        emptyOrdersLayout = findViewById(R.id.emptyOrdersLayout);
        loadingLayout = findViewById(R.id.loadingLayout);
        btnStartShopping = findViewById(R.id.btnStartShopping);

        btnStartShopping.setOnClickListener(v -> {
            finish();
        });
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Lịch sử đơn hàng");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        ordersRecyclerView.setLayoutManager(layoutManager);

        orderAdapter = new OrderAdapter(this, orders, null);
        ordersRecyclerView.setAdapter(orderAdapter);
    }

    private void loadOrders() {
        showLoading();

        String userId = sessionManager.getUserId();
        if (userId == null) {
            showEmpty();
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        firestoreManager.loadOrders(userId, new FirestoreManager.OnOrdersLoadedListener() {
            @Override
            public void onOrdersLoaded(List<Order> loadedOrders) {
                orders.clear();
                orders.addAll(loadedOrders);
                orderAdapter.updateOrders(orders);

                if (orders.isEmpty()) {
                    showEmpty();
                } else {
                    showOrders();
                }
            }

            @Override
            public void onError(String error) {
                showEmpty();
                Toast.makeText(OrderHistoryActivity.this, "Lỗi tải đơn hàng: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showLoading() {
        loadingLayout.setVisibility(View.VISIBLE);
        ordersRecyclerView.setVisibility(View.GONE);
        emptyOrdersLayout.setVisibility(View.GONE);
    }

    private void showOrders() {
        loadingLayout.setVisibility(View.GONE);
        ordersRecyclerView.setVisibility(View.VISIBLE);
        emptyOrdersLayout.setVisibility(View.GONE);
    }

    private void showEmpty() {
        loadingLayout.setVisibility(View.GONE);
        ordersRecyclerView.setVisibility(View.GONE);
        emptyOrdersLayout.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadOrders();
    }
}
