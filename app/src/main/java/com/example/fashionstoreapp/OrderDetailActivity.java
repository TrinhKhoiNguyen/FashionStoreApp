package com.example.fashionstoreapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fashionstoreapp.adapters.OrderItemAdapter;
import com.example.fashionstoreapp.model.Order;
import com.example.fashionstoreapp.models.OrderStatus;
import com.example.fashionstoreapp.utils.FirestoreManager;
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
    private LinearLayout statusUpdateSection;
    private Button updateStatusButton;
    private ProgressBar progressBar;
    private RecyclerView rvOrderItems;

    private Order order;
    private OrderItemAdapter orderItemAdapter;
    private FirestoreManager firestoreManager;
    private boolean isAdminMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);

        // Get order from intent
        String orderId = getIntent().getStringExtra("orderId");
        isAdminMode = getIntent().getBooleanExtra("isAdminMode", false);
        if (orderId == null) {
            finish();
            return;
        }

        firestoreManager = FirestoreManager.getInstance();
        initViews();
        setupToolbar();
        setupRecyclerView();
        setupListeners();
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
        statusUpdateSection = findViewById(R.id.statusUpdateSection);
        updateStatusButton = findViewById(R.id.updateStatusButton);
        progressBar = findViewById(R.id.progressBar);
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

    private void setupListeners() {
        if (updateStatusButton != null && isAdminMode) {
            updateStatusButton.setOnClickListener(v -> showStatusUpdateDialog());
        }
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

        // Show cancel reason if status is "cancelled"
        if ("cancelled".equals(order.getStatus())) {
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
        tvRecipientName.setText(order.getRecipientName() != null ? order.getRecipientName() : "Người nhận");
        tvRecipientPhone.setText(order.getPhoneNumber());

        // Products
        if (order.getItems() != null) {
            orderItemAdapter = new OrderItemAdapter(this, order.getItems());
            rvOrderItems.setAdapter(orderItemAdapter);
        }

        // Payment Details
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

        // Use saved subtotal, shippingFee, voucherDiscount from order
        double subtotal = order.getSubtotal();
        tvSubtotal.setText(formatter.format(subtotal));

        double shippingFee = order.getShippingFee();
        if (shippingFee == 0) {
            tvShippingFee.setText("Miễn phí");
            tvShippingFee.setTextColor(getColor(android.R.color.holo_green_dark));
        } else {
            tvShippingFee.setText(formatter.format(shippingFee));
        }

        // Show voucher discount if > 0
        double voucherDiscount = order.getVoucherDiscount();
        if (voucherDiscount > 0) {
            layoutVoucherDiscount.setVisibility(View.VISIBLE);
            tvVoucherDiscount.setText("-" + formatter.format(voucherDiscount));
        } else {
            layoutVoucherDiscount.setVisibility(View.GONE);
        }

        // Hide shipping discount for now
        layoutShippingDiscount.setVisibility(View.GONE);

        // Total from saved order data
        tvTotalPayment.setText(formatter.format(order.getTotal()));

        // Payment Method
        tvPaymentMethod.setText(order.getPaymentMethod() != null ? order.getPaymentMethod() : "COD");

        // Show/hide update status section for admin
        if (isAdminMode && statusUpdateSection != null) {
            String status = order.getStatus();
            if ("cancelled".equals(status) || "delivered".equals(status)) {
                statusUpdateSection.setVisibility(View.GONE);
            } else {
                statusUpdateSection.setVisibility(View.VISIBLE);
            }
        } else if (statusUpdateSection != null) {
            statusUpdateSection.setVisibility(View.GONE);
        }
    }

    private void showStatusUpdateDialog() {
        if (order == null)
            return;

        String currentStatus = order.getStatus();
        String[] statusOptions = getNextStatusOptions(currentStatus);

        if (statusOptions.length == 0) {
            Toast.makeText(this, "Không thể cập nhật trạng thái đơn hàng này", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Cập Nhật Trạng Thái Đơn Hàng");

        builder.setItems(statusOptions, (dialog, which) -> {
            String selectedStatus = getStatusCode(statusOptions[which]);
            updateOrderStatus(selectedStatus);
        });

        builder.setNegativeButton("Hủy", null);
        builder.show();
    }

    private String[] getNextStatusOptions(String currentStatus) {
        switch (currentStatus) {
            case "pending":
                return new String[] {
                        "Xác nhận đơn hàng (Đang chuẩn bị)",
                        "Hủy đơn hàng"
                };
            case "processing":
                return new String[] {
                        "Bắt đầu giao hàng (Đang giao)",
                        "Hủy đơn hàng"
                };
            case "shipping":
                return new String[] {
                        "Đã giao hàng thành công"
                };
            default:
                return new String[0];
        }
    }

    private String getStatusCode(String statusText) {
        if (statusText.contains("Đang chuẩn bị"))
            return "processing";
        if (statusText.contains("Đang giao"))
            return "shipping";
        if (statusText.contains("Đã giao"))
            return "delivered";
        if (statusText.contains("Hủy"))
            return "cancelled";
        return "pending";
    }

    private void updateOrderStatus(String newStatus) {
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
        if (updateStatusButton != null) {
            updateStatusButton.setEnabled(false);
        }

        // If cancelling, ask for reason
        if ("cancelled".equals(newStatus) || "canceled".equals(newStatus)) {
            final android.widget.EditText input = new android.widget.EditText(this);
            input.setHint("Lý do hủy đơn (ví dụ: hết hàng, lỗi hệ thống)");
            new AlertDialog.Builder(this)
                    .setTitle("Lý do hủy đơn")
                    .setView(input)
                    .setPositiveButton("Gửi", (dialog, which) -> {
                        String reason = input.getText() != null ? input.getText().toString().trim() : "";
                        firestoreManager.updateOrderStatus(order.getOrderId(), newStatus, reason,
                                new FirestoreManager.OnOrderStatusUpdatedListener() {
                                    @Override
                                    public void onStatusUpdated() {
                                        if (progressBar != null) {
                                            progressBar.setVisibility(View.GONE);
                                        }
                                        if (updateStatusButton != null) {
                                            updateStatusButton.setEnabled(true);
                                        }
                                        Toast.makeText(OrderDetailActivity.this, "Đã cập nhật trạng thái đơn hàng",
                                                Toast.LENGTH_SHORT)
                                                .show();

                                        // Reload order detail
                                        loadOrderDetail(order.getOrderId());
                                    }

                                    @Override
                                    public void onError(String error) {
                                        if (progressBar != null) {
                                            progressBar.setVisibility(View.GONE);
                                        }
                                        if (updateStatusButton != null) {
                                            updateStatusButton.setEnabled(true);
                                        }
                                        Toast.makeText(OrderDetailActivity.this, "Lỗi cập nhật: " + error,
                                                Toast.LENGTH_SHORT).show();
                                    }
                                });
                    })
                    .setNegativeButton("Hủy", (d, w) -> {
                        if (progressBar != null) {
                            progressBar.setVisibility(View.GONE);
                        }
                        if (updateStatusButton != null) {
                            updateStatusButton.setEnabled(true);
                        }
                    })
                    .show();
        } else {
            firestoreManager.updateOrderStatus(order.getOrderId(), newStatus, null,
                    new FirestoreManager.OnOrderStatusUpdatedListener() {
                        @Override
                        public void onStatusUpdated() {
                            if (progressBar != null) {
                                progressBar.setVisibility(View.GONE);
                            }
                            if (updateStatusButton != null) {
                                updateStatusButton.setEnabled(true);
                            }
                            Toast.makeText(OrderDetailActivity.this, "Đã cập nhật trạng thái đơn hàng",
                                    Toast.LENGTH_SHORT)
                                    .show();

                            // Reload order detail
                            loadOrderDetail(order.getOrderId());
                        }

                        @Override
                        public void onError(String error) {
                            if (progressBar != null) {
                                progressBar.setVisibility(View.GONE);
                            }
                            if (updateStatusButton != null) {
                                updateStatusButton.setEnabled(true);
                            }
                            Toast.makeText(OrderDetailActivity.this, "Lỗi cập nhật: " + error, Toast.LENGTH_SHORT)
                                    .show();
                        }
                    });
        }
    }
}
