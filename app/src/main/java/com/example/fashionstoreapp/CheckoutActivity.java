package com.example.fashionstoreapp;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fashionstoreapp.adapters.CheckoutItemAdapter;
import com.example.fashionstoreapp.models.CartItem;
import com.example.fashionstoreapp.models.Order;
import com.example.fashionstoreapp.utils.CartManager;
import com.example.fashionstoreapp.utils.FirestoreManager;
import com.example.fashionstoreapp.utils.SessionManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class CheckoutActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private TextInputEditText etRecipientName, etRecipientPhone, etShippingAddress, etNote, etVoucherCode;
    private RadioGroup paymentMethodGroup;
    private RadioButton rbCOD, rbBankTransfer, rbMomo;
    private RecyclerView rvCheckoutItems;
    private TextView tvSubtotal, tvShippingFee, tvTotal, tvVoucherDiscount, tvAppliedVoucherInfo;
    private Button btnPlaceOrder, btnApplyVoucher;
    private android.view.View layoutVoucherDiscount, layoutAppliedVoucher;
    private android.widget.ImageView ivRemoveVoucher;

    private CheckoutItemAdapter checkoutItemAdapter;

    private CartManager cartManager;
    private FirestoreManager firestoreManager;
    private SessionManager sessionManager;

    private List<CartItem> orderItems;
    private double totalAmount;
    private double subtotalAmount = 0;
    private double shippingFeeAmount = 0;
    private double voucherDiscount = 0;
    private String appliedVoucherCode = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        cartManager = CartManager.getInstance();
        firestoreManager = FirestoreManager.getInstance();
        sessionManager = new SessionManager(this);

        initViews();
        setupToolbar();
        loadOrderData();
        setupClickListeners();
        setupVoucherListeners();
    }

    private void initViews() {
        toolbar = findViewById(R.id.checkoutToolbar);
        etRecipientName = findViewById(R.id.etRecipientName);
        etRecipientPhone = findViewById(R.id.etRecipientPhone);
        etShippingAddress = findViewById(R.id.etShippingAddress);
        etNote = findViewById(R.id.etNote);
        paymentMethodGroup = findViewById(R.id.paymentMethodGroup);
        rbCOD = findViewById(R.id.rbCOD);
        rbBankTransfer = findViewById(R.id.rbBankTransfer);
        rbMomo = findViewById(R.id.rbMomo);
        rvCheckoutItems = findViewById(R.id.rvCheckoutItems);
        tvSubtotal = findViewById(R.id.tvSubtotal);
        tvShippingFee = findViewById(R.id.tvShippingFee);
        tvTotal = findViewById(R.id.tvTotal);
        btnPlaceOrder = findViewById(R.id.btnPlaceOrder);

        // Voucher components
        etVoucherCode = findViewById(R.id.etVoucherCode);
        btnApplyVoucher = findViewById(R.id.btnApplyVoucher);
        layoutVoucherDiscount = findViewById(R.id.layoutVoucherDiscount);
        tvVoucherDiscount = findViewById(R.id.tvVoucherDiscount);
        layoutAppliedVoucher = findViewById(R.id.layoutAppliedVoucher);
        tvAppliedVoucherInfo = findViewById(R.id.tvAppliedVoucherInfo);
        ivRemoveVoucher = findViewById(R.id.ivRemoveVoucher);

        // Debug: Check if views are found
        if (layoutVoucherDiscount == null) {
            android.util.Log.e("CheckoutActivity", "layoutVoucherDiscount is NULL!");
        }
        if (tvVoucherDiscount == null) {
            android.util.Log.e("CheckoutActivity", "tvVoucherDiscount is NULL!");
        }
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Thanh toán");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void loadOrderData() {
        // Get selected items from cart
        orderItems = cartManager.getSelectedItems();

        if (orderItems.isEmpty()) {
            Toast.makeText(this, "Giỏ hàng trống", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Setup RecyclerView for product list with images
        rvCheckoutItems.setLayoutManager(new LinearLayoutManager(this));
        checkoutItemAdapter = new CheckoutItemAdapter(this, orderItems);
        rvCheckoutItems.setAdapter(checkoutItemAdapter);

        // Calculate and display totals
        updateTotals();

        // Load user info if available
        loadUserInfo();
    }

    private void loadUserInfo() {
        String userId = sessionManager.getUserId();
        if (userId != null) {
            firestoreManager.loadUserProfile(userId, new FirestoreManager.OnUserProfileLoadedListener() {
                @Override
                public void onProfileLoaded(String name, String birthday, String gender, String phone) {
                    if (name != null) {
                        etRecipientName.setText(name);
                    }
                    if (phone != null) {
                        etRecipientPhone.setText(phone);
                    }
                }

                @Override
                public void onError(String error) {
                    // Ignore error, user can still manually enter info
                }
            });

            // Load shipping address from profile
            loadShippingAddress(userId);
        }
    }

    private void loadShippingAddress(String userId) {
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .collection("addresses")
                .whereEqualTo("isDefault", true)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        String address = queryDocumentSnapshots.getDocuments().get(0).getString("address");
                        String city = queryDocumentSnapshots.getDocuments().get(0).getString("city");
                        if (address != null && city != null) {
                            etShippingAddress.setText(address + ", " + city);
                        }
                    }
                });
    }

    private void setupClickListeners() {
        btnPlaceOrder.setOnClickListener(v -> validateAndPlaceOrder());
    }

    private void setupVoucherListeners() {
        btnApplyVoucher.setOnClickListener(v -> applyVoucher());
        ivRemoveVoucher.setOnClickListener(v -> removeVoucher());
    }

    private void applyVoucher() {
        String voucherCode = etVoucherCode.getText().toString().trim().toUpperCase();

        if (TextUtils.isEmpty(voucherCode)) {
            Toast.makeText(this, "Vui lòng nhập mã voucher", Toast.LENGTH_SHORT).show();
            return;
        }

        // Calculate current subtotal for validation
        double subtotal = 0;
        for (CartItem item : orderItems) {
            subtotal += item.getTotalPrice();
        }
        final double currentSubtotal = subtotal;

        // Show loading
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Đang kiểm tra voucher...");
        progressDialog.show();

        // Check voucher in Firestore
        FirebaseFirestore.getInstance()
                .collection("vouchers")
                .whereEqualTo("code", voucherCode)
                .whereEqualTo("isActive", true)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    progressDialog.dismiss();

                    if (queryDocumentSnapshots.isEmpty()) {
                        Toast.makeText(this, "Mã voucher không hợp lệ hoặc đã hết hạn", Toast.LENGTH_LONG).show();
                        return;
                    }

                    com.google.firebase.firestore.DocumentSnapshot voucherDoc = queryDocumentSnapshots.getDocuments()
                            .get(0);

                    // Get voucher data
                    Double minOrderAmount = voucherDoc.getDouble("minOrderAmount");
                    com.google.firebase.Timestamp expiryTimestamp = voucherDoc.getTimestamp("expiryDate");
                    Double discountPercent = voucherDoc.getDouble("discountPercent");
                    Double discountAmount = voucherDoc.getDouble("discountAmount");
                    Double maxDiscount = voucherDoc.getDouble("maxDiscount");

                    // Validation 1: Check minimum order amount
                    if (minOrderAmount != null && currentSubtotal < minOrderAmount) {
                        Toast.makeText(this,
                                "Đơn hàng tối thiểu " + String.format("%,.0f₫", minOrderAmount)
                                        + " để áp dụng voucher này",
                                Toast.LENGTH_LONG).show();
                        return;
                    }

                    // Validation 2: Check expiry date
                    if (expiryTimestamp != null) {
                        long expiryMillis = expiryTimestamp.toDate().getTime();
                        long currentMillis = System.currentTimeMillis();
                        if (currentMillis > expiryMillis) {
                            Toast.makeText(this, "Voucher đã hết hạn", Toast.LENGTH_LONG).show();
                            return;
                        }
                    }

                    // Calculate discount
                    double calculatedDiscount = 0;
                    String discountInfo = "";

                    // Priority: discountAmount > discountPercent
                    if (discountAmount != null && discountAmount > 0) {
                        // Fixed amount discount
                        calculatedDiscount = discountAmount;
                        discountInfo = "Giảm " + String.format("%,.0f₫", discountAmount);
                    } else if (discountPercent != null && discountPercent > 0) {
                        // Percentage discount
                        calculatedDiscount = currentSubtotal * (discountPercent / 100.0);

                        // Apply max discount cap if exists
                        if (maxDiscount != null && calculatedDiscount > maxDiscount) {
                            calculatedDiscount = maxDiscount;
                            discountInfo = "Giảm " + String.format("%.0f%%", discountPercent) +
                                    " (tối đa " + String.format("%,.0f₫", maxDiscount) + ")";
                        } else {
                            discountInfo = "Giảm " + String.format("%.0f%%", discountPercent) +
                                    " (" + String.format("%,.0f₫", calculatedDiscount) + ")";
                        }
                    } else {
                        Toast.makeText(this, "Voucher không hợp lệ", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Apply voucher
                    voucherDiscount = calculatedDiscount;
                    appliedVoucherCode = voucherCode;

                    // Update UI
                    layoutAppliedVoucher.setVisibility(android.view.View.VISIBLE);
                    tvAppliedVoucherInfo.setText("Mã " + voucherCode + " - " + discountInfo);
                    etVoucherCode.setText("");

                    // Update totals
                    updateTotals();

                    Toast.makeText(this, "✓ Áp dụng voucher thành công!", Toast.LENGTH_SHORT).show();

                    android.util.Log.d("CheckoutActivity", "Voucher applied: " + voucherCode +
                            ", Discount: " + calculatedDiscount);
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    android.util.Log.e("CheckoutActivity", "Voucher error: " + e.getMessage());
                });
    }

    private void removeVoucher() {
        voucherDiscount = 0;
        appliedVoucherCode = null;
        layoutAppliedVoucher.setVisibility(android.view.View.GONE);
        updateTotals();
        Toast.makeText(this, "Đã hủy voucher", Toast.LENGTH_SHORT).show();
    }

    private void updateTotals() {
        // Calculate subtotal
        subtotalAmount = 0;
        for (CartItem item : orderItems) {
            subtotalAmount += item.getTotalPrice();
        }

        // Shipping fee (free if > 500k, else 30k)
        shippingFeeAmount = subtotalAmount >= 500000 ? 0 : 30000;

        // Total after voucher
        double finalTotal = subtotalAmount + shippingFeeAmount - voucherDiscount;
        if (finalTotal < 0)
            finalTotal = 0;

        // Update UI
        tvSubtotal.setText(String.format("%,.0f₫", subtotalAmount));

        if (shippingFeeAmount == 0) {
            tvShippingFee.setText("Miễn phí");
            tvShippingFee.setTextColor(0xFF4CAF50);
        } else {
            tvShippingFee.setText(String.format("%,.0f₫", shippingFeeAmount));
            tvShippingFee.setTextColor(0xFF000000);
        }

        if (voucherDiscount > 0) {
            if (layoutVoucherDiscount != null && tvVoucherDiscount != null) {
                layoutVoucherDiscount.setVisibility(android.view.View.VISIBLE);
                tvVoucherDiscount.setText("-" + String.format("%,.0f₫", voucherDiscount));
                android.util.Log.d("CheckoutActivity", "Voucher discount visible: " + voucherDiscount);

                // Force UI update
                layoutVoucherDiscount.requestLayout();
                layoutVoucherDiscount.invalidate();
            } else {
                android.util.Log.e("CheckoutActivity",
                        "layoutVoucherDiscount or tvVoucherDiscount is NULL in updateTotals!");
            }
        } else {
            if (layoutVoucherDiscount != null) {
                layoutVoucherDiscount.setVisibility(android.view.View.GONE);
                android.util.Log.d("CheckoutActivity", "Voucher discount hidden");
            }
        }

        tvTotal.setText(String.format("%,.0f₫", finalTotal));
        totalAmount = finalTotal;
    }

    private void validateAndPlaceOrder() {
        String recipientName = etRecipientName.getText().toString().trim();
        String recipientPhone = etRecipientPhone.getText().toString().trim();
        String shippingAddress = etShippingAddress.getText().toString().trim();
        String note = etNote.getText().toString().trim();

        // Validate input
        if (TextUtils.isEmpty(recipientName)) {
            etRecipientName.setError("Vui lòng nhập tên người nhận");
            etRecipientName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(recipientPhone)) {
            etRecipientPhone.setError("Vui lòng nhập số điện thoại");
            etRecipientPhone.requestFocus();
            return;
        }

        if (recipientPhone.length() < 10) {
            etRecipientPhone.setError("Số điện thoại không hợp lệ");
            etRecipientPhone.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(shippingAddress)) {
            etShippingAddress.setError("Vui lòng nhập địa chỉ nhận hàng");
            etShippingAddress.requestFocus();
            return;
        }

        // Get payment method
        String paymentMethod = "cod";
        int selectedPaymentId = paymentMethodGroup.getCheckedRadioButtonId();
        if (selectedPaymentId == R.id.rbBankTransfer) {
            paymentMethod = "bank_transfer";
        } else if (selectedPaymentId == R.id.rbMomo) {
            paymentMethod = "momo";
        }

        // Show confirmation dialog
        showConfirmationDialog(recipientName, recipientPhone, shippingAddress, note, paymentMethod);
    }

    private void showConfirmationDialog(String recipientName, String recipientPhone,
            String shippingAddress, String note, String paymentMethod) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Xác nhận đặt hàng");

        String message = "Người nhận: " + recipientName + "\n" +
                "SĐT: " + recipientPhone + "\n" +
                "Địa chỉ: " + shippingAddress + "\n" +
                "Tổng tiền: " + String.format("%,.0f₫", totalAmount);

        builder.setMessage(message);
        builder.setPositiveButton("Đặt hàng", (dialog, which) -> {
            placeOrder(recipientName, recipientPhone, shippingAddress, note, paymentMethod);
        });
        builder.setNegativeButton("Hủy", null);
        builder.show();
    }

    private void placeOrder(String recipientName, String recipientPhone,
            String shippingAddress, String note, String paymentMethod) {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Đang đặt hàng...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        String userId = sessionManager.getUserId();

        // Create order with payment breakdown
        Order order = new Order(userId, orderItems, totalAmount,
                subtotalAmount, shippingFeeAmount, voucherDiscount,
                paymentMethod, recipientName, recipientPhone, shippingAddress, note);

        // Save order to Firestore
        firestoreManager.saveOrder(order, new FirestoreManager.OnOrderSavedListener() {
            @Override
            public void onOrderSaved(String orderId) {
                progressDialog.dismiss();

                // Remove ordered items from cart
                cartManager.clearSelectedItems();

                // Show success dialog
                showSuccessDialog(orderId);
            }

            @Override
            public void onError(String error) {
                progressDialog.dismiss();
                Toast.makeText(CheckoutActivity.this, "Lỗi đặt hàng: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showSuccessDialog(String orderId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Đặt hàng thành công!");
        builder.setMessage("Mã đơn hàng: " + orderId + "\n\nCảm ơn bạn đã mua hàng!");
        builder.setPositiveButton("OK", (dialog, which) -> {
            // Go back to main screen
            finish();
        });
        builder.setCancelable(false);
        builder.show();
    }
}
