package com.example.fashionstoreapp.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fashionstoreapp.OrderDetailActivity;
import com.example.fashionstoreapp.R;
import com.example.fashionstoreapp.adapters.AdminOrderAdapter;
import com.example.fashionstoreapp.model.Order;
import com.example.fashionstoreapp.utils.FirestoreManager;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

public class AdminOrdersFragment extends Fragment implements AdminOrderAdapter.OnAdminOrderClickListener {

    private ChipGroup orderStatusChipGroup;
    private Chip chipAll, chipProcessing, chipShipping, chipCompleted, chipCancelled;
    private RecyclerView ordersRecyclerView;
    private AdminOrderAdapter adapter;
    private FirestoreManager firestoreManager;
    private List<Order> allOrders = new ArrayList<>();
    private List<Order> filteredOrders = new ArrayList<>();
    private String currentFilter = "Tất cả";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_orders, container, false);

        initViews(view);
        setupRecyclerView();
        setupListeners();
        loadOrders();

        return view;
    }

    private void initViews(View view) {
        orderStatusChipGroup = view.findViewById(R.id.orderStatusChipGroup);
        chipAll = view.findViewById(R.id.chipAll);
        chipProcessing = view.findViewById(R.id.chipProcessing);
        chipShipping = view.findViewById(R.id.chipShipping);
        chipCompleted = view.findViewById(R.id.chipCompleted);
        chipCancelled = view.findViewById(R.id.chipCancelled);
        ordersRecyclerView = view.findViewById(R.id.adminOrdersRecyclerView);
        firestoreManager = FirestoreManager.getInstance();
    }

    private void setupRecyclerView() {
        adapter = new AdminOrderAdapter(getContext(), filteredOrders, this);
        ordersRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        ordersRecyclerView.setAdapter(adapter);
    }

    private void setupListeners() {
        orderStatusChipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty())
                return;

            int checkedId = checkedIds.get(0);
            if (checkedId == R.id.chipAll) {
                currentFilter = "Tất cả";
            } else if (checkedId == R.id.chipProcessing) {
                currentFilter = "processing"; // Use English code from database
            } else if (checkedId == R.id.chipShipping) {
                currentFilter = "shipping"; // Use English code from database
            } else if (checkedId == R.id.chipCompleted) {
                currentFilter = "delivered"; // Use English code from database
            } else if (checkedId == R.id.chipCancelled) {
                currentFilter = "cancelled"; // Use English code from database
            }

            filterOrders();
        });
    }

    private void loadOrders() {
        firestoreManager.getAllOrders(new FirestoreManager.OnOrdersLoadedListener() {
            @Override
            public void onOrdersLoaded(List<Order> orders) {
                allOrders.clear();
                allOrders.addAll(orders);
                filterOrders();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(getContext(), "Lỗi tải đơn hàng: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterOrders() {
        filteredOrders.clear();
        if ("Tất cả".equals(currentFilter)) {
            filteredOrders.addAll(allOrders);
        } else {
            for (Order order : allOrders) {
                if (currentFilter.equals(order.getStatus())) {
                    filteredOrders.add(order);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onViewOrder(Order order) {
        Intent intent = new Intent(getActivity(), OrderDetailActivity.class);
        intent.putExtra("orderId", order.getOrderId());
        intent.putExtra("isAdminMode", true); // Enable admin status update features
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadOrders(); // Reload when returning from OrderDetailActivity
    }

    @Override
    public void onUpdateOrderStatus(Order order) {
        // Show dialog to update order status
        String[] statusTexts = { "Đang chuẩn bị", "Đang giao", "Đã giao", "Đã hủy" };
        String[] statusCodes = { "processing", "shipping", "delivered", "cancelled" };

        new androidx.appcompat.app.AlertDialog.Builder(getContext())
                .setTitle("Cập nhật trạng thái đơn hàng")
                .setItems(statusTexts, (dialog, which) -> {
                    String newStatus = statusCodes[which]; // Use English code
                    updateOrderStatus(order, newStatus);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void updateOrderStatus(Order order, String newStatus) {
        firestoreManager.updateOrderStatus(order.getOrderId(), newStatus,
                new FirestoreManager.OnOrderStatusUpdatedListener() {
                    @Override
                    public void onStatusUpdated() {
                        Toast.makeText(getContext(), "Đã cập nhật trạng thái", Toast.LENGTH_SHORT).show();
                        loadOrders(); // Reload list
                    }

                    @Override
                    public void onError(String error) {
                        Toast.makeText(getContext(), "Lỗi cập nhật: " + error, Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
