package com.example.fashionstoreapp;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fashionstoreapp.adapters.OrderItemAdapter;
import com.example.fashionstoreapp.model.Order;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.NumberFormat;
import java.util.Locale;

public class OrderDetailActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private TextView tvOrderStatus, tvCancelReason, tvOrderId, tvOrderDate;
    private TextView tvRecipientName, tvRecipientPhone, tvShippingAddress;
    private TextView tvSubtotal, tvShippingFee, tvShippingDiscount, tvVoucherDiscount;
    private TextView tvTotalPayment, tvPaymentMethod;
    private LinearLayout layoutCancelReason, layoutShippingDiscount, layoutVoucherDiscount;
    private RecyclerView rvOrderItems;

    private Order order;
    private OrderItemAdapter orderItemAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);

        // Get order from intent
        String orderId = getIntent().getStringExtra("orderId");
        if (orderId == null) {
            finish();
            return;
        }

        initViews();
        setupToolbar();
        setupRecyclerView();
        loadOrderDetail(orderId);
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tvOrderStatus = findViewById(R.id.tvOrderStatus);
        tvCancelReason = findViewById(R.id.tvCancelReason);
        tvOrderId = findViewById(R.id.tvOrderId);
        tvOrderDate = findViewById(R.id.tvOrderDate);
        tvRecipientName = findViewById(R.id.tvRecipientName);
        tvRecipientPhone = findViewById(R.id.tvRecipientPhone);
        tvShippingAddress = findViewById(R.id.tvShippingAddress);
        tvSubtotal = findViewById(R.id.tvSubtotal);
        tvShippingFee = findViewById(R.id.tvShippingFee);
        tvShippingDiscount = findViewById(R.id.tvShippingDiscount);
        tvVoucherDiscount = findViewById(R.id.tvVoucherDiscount);
        tvTotalPayment = findViewById(R.id.tvTotalPayment);
        tvPaymentMethod = findViewById(R.id.tvPaymentMethod);
        layoutCancelReason = findViewById(R.id.layoutCancelReason);
        layoutShippingDiscount = findViewById(R.id.layoutShippingDiscount);
        layoutVoucherDiscount = findViewById(R.id.layoutVoucherDiscount);
        rvOrderItems = findViewById(R.id.rvOrderItems);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        rvOrderItems.setLayoutManager(new LinearLayoutManager(this));
    }

    private void loadOrderDetail(String orderId) {
        FirebaseFirestore.getInstance()
                .collection("orders")
                .document(orderId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        order = documentSnapshot.toObject(Order.class);
                        if (order != null) {
                            displayOrderDetail();
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle error
                    finish();
                });
    }

    private void displayOrderDetail() {
        // Status
        tvOrderStatus.setText(order.getStatusText());
        android.graphics.drawable.GradientDrawable background = (android.graphics.drawable.GradientDrawable) tvOrderStatus
                .getBackground();
        if (background != null) {
            background.setColor(order.getStatusColor());
        }

        // Show cancel reason if status is "Đã hủy"
        if ("Đã hủy".equals(order.getStatus())) {
            layoutCancelReason.setVisibility(View.VISIBLE);
            // You can add cancelReason field to Order model if needed
            tvCancelReason.setText("Đơn hàng đã bị hủy");
        } else {
            layoutCancelReason.setVisibility(View.GONE);
        }

        // Order Info
        tvOrderId.setText(order.getOrderId());
        tvOrderDate.setText(order.getFormattedCreatedDate());

        // Shipping Address (extract from order fields)
        tvShippingAddress.setText(order.getShippingAddress());
        tvRecipientPhone.setText(order.getPhoneNumber());
        // You may need to add recipientName to Order model
        tvRecipientName.setText("Người nhận");

        // Products
        if (order.getItems() != null) {
            orderItemAdapter = new OrderItemAdapter(this, order.getItems());
            rvOrderItems.setAdapter(orderItemAdapter);
        }

        // Payment Details
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

        double subtotal = order.getTotal();
        tvSubtotal.setText(formatter.format(subtotal));

        // Shipping fee (default 30k, free if order > 500k)
        double shippingFee = subtotal >= 500000 ? 0 : 30000;
        if (shippingFee == 0) {
            tvShippingFee.setText("Miễn phí");
            tvShippingFee.setTextColor(getColor(android.R.color.holo_green_dark));
        } else {
            tvShippingFee.setText(formatter.format(shippingFee));
        }

        // For now, hide discount sections (can be added later)
        layoutShippingDiscount.setVisibility(View.GONE);
        layoutVoucherDiscount.setVisibility(View.GONE);

        // Total
        double total = subtotal + shippingFee;
        tvTotalPayment.setText(formatter.format(total));

        // Payment Method
        tvPaymentMethod.setText(order.getPaymentMethod() != null ? order.getPaymentMethod() : "COD");
    }
}
