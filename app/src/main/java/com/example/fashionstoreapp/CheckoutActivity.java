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

import com.example.fashionstoreapp.models.CartItem;
import com.example.fashionstoreapp.models.Order;
import com.example.fashionstoreapp.utils.CartManager;
import com.example.fashionstoreapp.utils.FirestoreManager;
import com.example.fashionstoreapp.utils.SessionManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

public class CheckoutActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private TextInputEditText etRecipientName, etRecipientPhone, etShippingAddress, etNote;
    private RadioGroup paymentMethodGroup;
    private RadioButton rbCOD, rbBankTransfer, rbMomo;
    private TextView tvItemCount, tvSubtotal, tvShippingFee, tvTotal;
    private Button btnPlaceOrder;

    private CartManager cartManager;
    private FirestoreManager firestoreManager;
    private SessionManager sessionManager;

    private List<CartItem> orderItems;
    private double totalAmount;

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
        tvItemCount = findViewById(R.id.tvItemCount);
        tvSubtotal = findViewById(R.id.tvSubtotal);
        tvShippingFee = findViewById(R.id.tvShippingFee);
        tvTotal = findViewById(R.id.tvTotal);
        btnPlaceOrder = findViewById(R.id.btnPlaceOrder);
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

        // Calculate total
        totalAmount = 0;
        int itemCount = 0;
        for (CartItem item : orderItems) {
            totalAmount += item.getTotalPrice();
            itemCount += item.getQuantity();
        }

        // Display order info
        tvItemCount.setText(itemCount + " sản phẩm");
        tvSubtotal.setText(String.format("%,.0f₫", totalAmount));
        tvTotal.setText(String.format("%,.0f₫", totalAmount));

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
        }
    }

    private void setupClickListeners() {
        btnPlaceOrder.setOnClickListener(v -> validateAndPlaceOrder());
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

        // Create order
        Order order = new Order(userId, orderItems, totalAmount, paymentMethod,
                recipientName, recipientPhone, shippingAddress, note);

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
