package com.example.fashionstoreapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fashionstoreapp.adapters.OrderAdapter;
import com.example.fashionstoreapp.model.Order;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class OrdersFragment extends Fragment {

    private static final String ARG_STATUS = "status";
    private String statusFilter;

    private RecyclerView recyclerView;
    private ProgressBar loadingProgress;
    private TextView emptyText;
    private OrderAdapter adapter;
    private List<Order> orderList;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    public static OrdersFragment newInstance(String status) {
        OrdersFragment fragment = new OrdersFragment();
        Bundle args = new Bundle();
        args.putString(ARG_STATUS, status);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            statusFilter = getArguments().getString(ARG_STATUS);
        }
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_orders, container, false);

        recyclerView = view.findViewById(R.id.ordersRecyclerView);
        loadingProgress = view.findViewById(R.id.loadingProgress);
        emptyText = view.findViewById(R.id.emptyText);

        orderList = new ArrayList<>();
        adapter = new OrderAdapter(getContext(), orderList, order -> {
            // Open OrderDetailActivity when order is clicked
            Intent intent = new Intent(getContext(), OrderDetailActivity.class);
            intent.putExtra("orderId", order.getOrderId());
            startActivity(intent);
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        loadOrders();

        return view;
    }

    private void loadOrders() {
        if (auth.getCurrentUser() == null)
            return;

        loadingProgress.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        emptyText.setVisibility(View.GONE);

        // Query without orderBy to avoid needing composite index
        // We'll sort on client side instead
        db.collection("orders")
                .whereEqualTo("userId", auth.getCurrentUser().getUid())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    orderList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Order order = doc.toObject(Order.class);

                        // Filter by status (no "all" tab anymore)
                        if (statusFilter == null || order.getStatus().equals(statusFilter)) {
                            orderList.add(order);
                        }
                    }

                    // Sort by createdAt descending on client side
                    orderList.sort((o1, o2) -> {
                        if (o1.getCreatedAt() == null && o2.getCreatedAt() == null)
                            return 0;
                        if (o1.getCreatedAt() == null)
                            return 1;
                        if (o2.getCreatedAt() == null)
                            return -1;
                        return o2.getCreatedAt().compareTo(o1.getCreatedAt());
                    });

                    loadingProgress.setVisibility(View.GONE);
                    if (orderList.isEmpty()) {
                        emptyText.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    } else {
                        emptyText.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                        adapter.updateOrders(orderList);
                    }
                }).addOnFailureListener(e -> {
                    loadingProgress.setVisibility(View.GONE);
                    emptyText.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Lỗi tải đơn hàng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
