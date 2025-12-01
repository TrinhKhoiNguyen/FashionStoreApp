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
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
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
    private com.google.android.material.button.MaterialButton btnSelectAddress;

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

    // Notification permission handling
    private static final int REQ_POST_NOTIF = 1001;
    private String pendingNotifTitle, pendingNotifMessage;

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
        setupAddressSelection();
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
        btnSelectAddress = findViewById(R.id.btnSelectAddress);

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
        // Check if we're in single-item (Buy Now) mode via Intent extra
        if (getIntent() != null && getIntent().hasExtra("single_item")) {
            CartItem single = (CartItem) getIntent().getSerializableExtra("single_item");
            orderItems = new java.util.ArrayList<>();
            if (single != null) {
                orderItems.add(single);
            }
        } else {
            // Get selected items from cart
            orderItems = cartManager.getSelectedItems();
        }

        if (orderItems == null || orderItems.isEmpty()) {
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

    private androidx.activity.result.ActivityResultLauncher<android.content.Intent> selectAddressLauncher;

    private void setupAddressSelection() {
        // Register ActivityResult launcher to receive selected address
        selectAddressLauncher = registerForActivityResult(
                new androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result != null && result.getResultCode() == RESULT_OK && result.getData() != null) {
                        android.content.Intent data = result.getData();
                        String name = data.getStringExtra("name");
                        String phone = data.getStringExtra("phone");
                        String address = data.getStringExtra("address");
                        String city = data.getStringExtra("city");

                        if (name != null)
                            etRecipientName.setText(name);
                        if (phone != null)
                            etRecipientPhone.setText(phone);
                        if (address != null && city != null)
                            etShippingAddress.setText(address + ", " + city);
                    }
                });

        if (btnSelectAddress != null) {
            btnSelectAddress.setOnClickListener(v -> {
                android.content.Intent intent = new android.content.Intent(CheckoutActivity.this,
                        AddressPaymentActivity.class);
                intent.putExtra("select_address", true);
                selectAddressLauncher.launch(intent);
            });
        }
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

        // Check voucher in Firestore using new model
        FirebaseFirestore.getInstance()
                .collection("vouchers")
                .whereEqualTo("code", voucherCode)
                .whereEqualTo("active", true)
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

                    // Get voucher data from new model
                    Double minOrder = voucherDoc.getDouble("minOrder");
                    Long startAt = voucherDoc.getLong("startAt");
                    Long endAt = voucherDoc.getLong("endAt");
                    String type = voucherDoc.getString("type");
                    Double amount = voucherDoc.getDouble("amount");
                    Double maxDiscount = voucherDoc.getDouble("maxDiscount");
                    Long quantity = voucherDoc.getLong("quantity");
                    Long usedCount = voucherDoc.getLong("usedCount");

                    // Validation 1: Check minimum order amount
                    if (minOrder != null && currentSubtotal < minOrder) {
                        Toast.makeText(this,
                                "Đơn hàng tối thiểu " + String.format("%,.0f₫", minOrder)
                                        + " để áp dụng voucher này",
                                Toast.LENGTH_LONG).show();
                        return;
                    }

                    // Validation 2: Check time validity
                    long currentTime = System.currentTimeMillis();
                    if (startAt != null && currentTime < startAt) {
                        Toast.makeText(this, "Voucher chưa đến thời gian sử dụng", Toast.LENGTH_LONG).show();
                        return;
                    }
                    if (endAt != null && currentTime > endAt) {
                        Toast.makeText(this, "Voucher đã hết hạn", Toast.LENGTH_LONG).show();
                        return;
                    }

                    // Validation 3: Check quantity
                    if (quantity != null && usedCount != null) {
                        if (usedCount >= quantity) {
                            Toast.makeText(this, "Voucher đã hết lượt sử dụng", Toast.LENGTH_LONG).show();
                            return;
                        }
                    }

                    // Calculate discount based on type
                    double calculatedDiscount = 0;
                    String discountInfo = "";

                    if (amount == null || amount <= 0) {
                        Toast.makeText(this, "Voucher không hợp lệ", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if ("percent".equals(type)) {
                        // Percentage discount
                        calculatedDiscount = currentSubtotal * (amount / 100.0);

                        // Apply max discount cap if exists
                        if (maxDiscount != null && maxDiscount > 0 && calculatedDiscount > maxDiscount) {
                            calculatedDiscount = maxDiscount;
                            discountInfo = "Giảm " + String.format("%.0f%%", amount) +
                                    " (tối đa " + String.format("%,.0f₫", maxDiscount) + ")";
                        } else {
                            discountInfo = "Giảm " + String.format("%.0f%%", amount) +
                                    " (" + String.format("%,.0f₫", calculatedDiscount) + ")";
                        }
                    } else if ("fixed".equals(type)) {
                        // Fixed amount discount
                        calculatedDiscount = amount;
                        discountInfo = "Giảm " + String.format("%,.0f₫", amount);
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

                // Update totalSold for each product (client-side increment) via
                // FirestoreManager
                try {
                    if (orderItems != null) {
                        firestoreManager.incrementProductsSold(orderItems);
                    }
                } catch (Exception e) {
                    android.util.Log.e("CheckoutActivity",
                            "Exception updating totalSold via manager: " + e.getMessage());
                }

                // Increment voucher usedCount if voucher was applied
                if (appliedVoucherCode != null && !appliedVoucherCode.isEmpty()) {
                    incrementVoucherUsedCount(appliedVoucherCode);
                }

                // Remove ordered items from cart
                cartManager.clearSelectedItems();

                // Show success dialog
                showSuccessDialog(orderId);

                // Show immediate local notification for feedback (request permission on Android
                // 13+)
                String title = "Đặt hàng thành công";
                String message = "Mã đơn hàng: " + orderId + " - Cảm ơn bạn đã mua hàng!";
                showOrRequestNotification(title, message, orderId.hashCode());
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

    private void showOrRequestNotification(String title, String message, int id) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                // Request permission and save pending payload
                pendingNotifTitle = title;
                pendingNotifMessage = message;
                ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.POST_NOTIFICATIONS },
                        REQ_POST_NOTIF);
                return;
            }
        }

        // Permission granted or not required
        try {
            new com.example.fashionstoreapp.NotificationHelper(this).showNotification(title, message, id);
        } catch (Exception ignored) {
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_POST_NOTIF) {
            if (grantResults.length > 0 && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                if (pendingNotifTitle != null && pendingNotifMessage != null) {
                    try {
                        new com.example.fashionstoreapp.NotificationHelper(this)
                                .showNotification(pendingNotifTitle, pendingNotifMessage, pendingNotifTitle.hashCode());
                    } catch (Exception ignored) {
                    }
                }
            }
            // Clear pending
            pendingNotifTitle = null;
            pendingNotifMessage = null;
        }
    }

    /**
     * Increment voucher usedCount in Firestore
     */
    private void incrementVoucherUsedCount(String voucherCode) {
        FirebaseFirestore.getInstance()
                .collection("vouchers")
                .whereEqualTo("code", voucherCode)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        com.google.firebase.firestore.DocumentSnapshot doc = queryDocumentSnapshots.getDocuments()
                                .get(0);
                        String voucherId = doc.getId();

                        // Get current usedCount
                        Long currentUsedCount = doc.getLong("usedCount");
                        long newUsedCount = (currentUsedCount != null ? currentUsedCount : 0) + 1;

                        // Update usedCount
                        FirebaseFirestore.getInstance()
                                .collection("vouchers")
                                .document(voucherId)
                                .update("usedCount", newUsedCount)
                                .addOnSuccessListener(aVoid -> {
                                    android.util.Log.d("CheckoutActivity",
                                            "Voucher usedCount incremented: " + voucherCode);
                                })
                                .addOnFailureListener(e -> {
                                    android.util.Log.e("CheckoutActivity",
                                            "Failed to increment voucher usedCount: " + e.getMessage());
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("CheckoutActivity", "Failed to find voucher: " + e.getMessage());
                });
    }
}
