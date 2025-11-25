package com.example.fashionstoreapp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fashionstoreapp.R;
import com.example.fashionstoreapp.adapters.AdminOrderAdapter;
import com.example.fashionstoreapp.adapters.AdminProductAdapter;
import com.example.fashionstoreapp.model.Order;
import com.example.fashionstoreapp.models.Product;
import com.example.fashionstoreapp.utils.FirestoreManager;
import com.google.firebase.auth.FirebaseAuth;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AdminDashboardFragment extends Fragment {

    // Today's stats
    private TextView todayRevenueText, todayOrdersText, todayUsersText;

    // Alerts
    private TextView lowStockCountText, pendingOrdersCountText;

    // Top products
    private RecyclerView topProductsRecyclerView;
    private AdminProductAdapter topProductsAdapter;

    // Pending orders
    private RecyclerView pendingOrdersRecyclerView;
    private AdminOrderAdapter pendingOrdersAdapter;

    // Low stock products
    private RecyclerView lowStockRecyclerView;
    private AdminProductAdapter lowStockAdapter;

    private FirestoreManager firestoreManager;
    private NumberFormat currencyFormatter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_dashboard, container, false);

        initViews(view);
        firestoreManager = FirestoreManager.getInstance();
        currencyFormatter = NumberFormat.getInstance(new Locale("vi", "VN"));

        loadDashboardData();

        return view;
    }

    private void initViews(View view) {
        // Today's stats
        todayRevenueText = view.findViewById(R.id.todayRevenueText);
        todayOrdersText = view.findViewById(R.id.todayOrdersText);
        todayUsersText = view.findViewById(R.id.todayUsersText);

        // Alert counts
        lowStockCountText = view.findViewById(R.id.lowStockCountText);
        pendingOrdersCountText = view.findViewById(R.id.pendingOrdersCountText);

        // Top selling products
        topProductsRecyclerView = view.findViewById(R.id.topProductsRecyclerView);
        topProductsRecyclerView
                .setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        topProductsAdapter = new AdminProductAdapter(getContext(), new ArrayList<>(), createProductClickListener());
        topProductsRecyclerView.setAdapter(topProductsAdapter);

        // Pending orders
        pendingOrdersRecyclerView = view.findViewById(R.id.pendingOrdersRecyclerView);
        pendingOrdersRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        pendingOrdersAdapter = new AdminOrderAdapter(getContext(), new ArrayList<>(), createOrderClickListener());
        pendingOrdersRecyclerView.setAdapter(pendingOrdersAdapter);

        // Low stock products
        lowStockRecyclerView = view.findViewById(R.id.lowStockRecyclerView);
        lowStockRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        lowStockAdapter = new AdminProductAdapter(getContext(), new ArrayList<>(), createProductClickListener());
        lowStockRecyclerView.setAdapter(lowStockAdapter);
    }

    private void loadDashboardData() {
        // Load today's revenue
        firestoreManager.getTodayRevenue(new FirestoreManager.OnRevenueLoadedListener() {
            @Override
            public void onRevenueLoaded(double revenue) {
                todayRevenueText.setText(currencyFormatter.format(revenue) + "₫");
            }

            @Override
            public void onError(String error) {
                todayRevenueText.setText("0₫");
            }
        });

        // Load today's orders count
        firestoreManager.getTodayOrders(new FirestoreManager.OnTodayOrdersLoadedListener() {
            @Override
            public void onOrdersLoaded(int count) {
                todayOrdersText.setText(String.valueOf(count));
            }

            @Override
            public void onError(String error) {
                todayOrdersText.setText("0");
            }
        });

        // Load today's new users
        firestoreManager.getTodayNewUsers(new FirestoreManager.OnTodayUsersLoadedListener() {
            @Override
            public void onUsersLoaded(int count) {
                todayUsersText.setText(String.valueOf(count));
            }

            @Override
            public void onError(String error) {
                todayUsersText.setText("0");
            }
        });

        // Load low stock products
        firestoreManager.getLowStockProducts(new FirestoreManager.OnProductsLoadedListener() {
            @Override
            public void onProductsLoaded(List<Product> products) {
                lowStockCountText.setText(String.valueOf(products.size()));
                lowStockAdapter.updateData(products);
            }

            @Override
            public void onError(String error) {
                lowStockCountText.setText("0");
            }
        });

        // Load pending orders
        firestoreManager.getPendingOrders(new FirestoreManager.OnOrdersLoadedListener() {
            @Override
            public void onOrdersLoaded(List<Order> orders) {
                pendingOrdersCountText.setText(String.valueOf(orders.size()));
                pendingOrdersAdapter.updateData(orders);
            }

            @Override
            public void onError(String error) {
                pendingOrdersCountText.setText("0");
            }
        });

        // Load top selling products (top 3)
        firestoreManager.getTopSellingProducts(3, new FirestoreManager.OnProductsLoadedListener() {
            @Override
            public void onProductsLoaded(List<Product> products) {
                topProductsAdapter.updateData(products);
            }

            @Override
            public void onError(String error) {
                // Show empty list
            }
        });
    }

    private AdminProductAdapter.OnAdminProductClickListener createProductClickListener() {
        return new AdminProductAdapter.OnAdminProductClickListener() {
            @Override
            public void onEditProduct(Product product) {
                // Navigate to edit product (will implement later)
                if (getContext() != null) {
                    android.widget.Toast
                            .makeText(getContext(), "Sửa: " + product.getName(), android.widget.Toast.LENGTH_SHORT)
                            .show();
                }
            }

            @Override
            public void onDeleteProduct(Product product) {
                // Handle delete (will implement later)
            }
        };
    }

    private AdminOrderAdapter.OnAdminOrderClickListener createOrderClickListener() {
        return new AdminOrderAdapter.OnAdminOrderClickListener() {
            @Override
            public void onViewOrder(Order order) {
                // Navigate to order detail (will implement later)
                if (getContext() != null) {
                    android.widget.Toast
                            .makeText(getContext(), "Xem đơn: " + order.getOrderId(), android.widget.Toast.LENGTH_SHORT)
                            .show();
                }
            }

            @Override
            public void onUpdateOrderStatus(Order order) {
                // Show status update dialog (will implement later)
            }
        };
    }
}
