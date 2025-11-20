package com.example.fashionstoreapp;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
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
    private TextInputEditText etRecipientName, etRecipientPhone, etShippingAddress, etNote, etVoucherCode;
    private RadioGroup paymentMethodGroup;
    private RadioButton rbCOD, rbBankTransfer, rbMomo;
    private TextView tvItemCount, tvSubtotal, tvShippingFee, tvTotal;
    private TextView tvAppliedVoucherCode, tvVoucherDiscount, tvVoucherDiscountAmount;
    private LinearLayout voucherInputLayout, appliedVoucherLayout, voucherDiscountLayout;
    private Button btnPlaceOrder, btnApplyVoucher;
    private View btnRemoveVoucher;

    private CartManager cartManager;
    private FirestoreManager firestoreManager;
    private SessionManager sessionManager;

    private List<CartItem> orderItems;
    private double totalAmount;
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
    }

    private void initViews() {
        toolbar = findViewById(R.id.checkoutToolbar);
        etRecipientName = findViewById(R.id.etRecipientName);
        etRecipientPhone = findViewById(R.id.etRecipientPhone);
        etShippingAddress = findViewById(R.id.etShippingAddress);
        etNote = findViewById(R.id.etNote);
        etVoucherCode = findViewById(R.id.etVoucherCode);
        paymentMethodGroup = findViewById(R.id.paymentMethodGroup);
        rbCOD = findViewById(R.id.rbCOD);
        rbBankTransfer = findViewById(R.id.rbBankTransfer);
        rbMomo = findViewById(R.id.rbMomo);
        tvItemCount = findViewById(R.id.tvItemCount);
        tvSubtotal = findViewById(R.id.tvSubtotal);
        tvShippingFee = findViewById(R.id.tvShippingFee);
        tvTotal = findViewById(R.id.tvTotal);
        btnPlaceOrder = findViewById(R.id.btnPlaceOrder);
        btnApplyVoucher = findViewById(R.id.btnApplyVoucher);
        voucherInputLayout = findViewById(R.id.voucherInputLayout);
        appliedVoucherLayout = findViewById(R.id.appliedVoucherLayout);
        voucherDiscountLayout = findViewById(R.id.voucherDiscountLayout);
        tvAppliedVoucherCode = findViewById(R.id.tvAppliedVoucherCode);
        tvVoucherDiscount = findViewById(R.id.tvVoucherDiscount);
        tvVoucherDiscountAmount = findViewById(R.id.tvVoucherDiscountAmount);
        btnRemoveVoucher = findViewById(R.id.btnRemoveVoucher);
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
        updateTotal();

        // Load user info if available
        loadUserInfo();
    }

    private void updateTotal() {
        double finalTotal = totalAmount - voucherDiscount;
        if (finalTotal < 0) finalTotal = 0;
        tvTotal.setText(String.format("%,.0f₫", finalTotal));
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
        btnApplyVoucher.setOnClickListener(v -> applyVoucher());
        btnRemoveVoucher.setOnClickListener(v -> removeVoucher());
    }

    private void applyVoucher() {
        String voucherCode = etVoucherCode.getText().toString().trim().toUpperCase();
        
        if (TextUtils.isEmpty(voucherCode)) {
            Toast.makeText(this, "Vui lòng nhập mã giảm giá", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate voucher code (you can implement real validation with Firestore)
        // For now, using sample vouchers
        double discount = validateVoucherCode(voucherCode);
        
        if (discount > 0) {
            appliedVoucherCode = voucherCode;
            voucherDiscount = discount;
            
            // Hide input, show applied voucher
            voucherInputLayout.setVisibility(View.GONE);
            appliedVoucherLayout.setVisibility(View.VISIBLE);
            voucherDiscountLayout.setVisibility(View.VISIBLE);
            
            tvAppliedVoucherCode.setText(voucherCode);
            tvVoucherDiscount.setText("Giảm " + String.format("%,.0f₫", discount));
            tvVoucherDiscountAmount.setText("-" + String.format("%,.0f₫", discount));
            
            updateTotal();
            Toast.makeText(this, "Áp dụng mã giảm giá thành công!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Mã giảm giá không hợp lệ hoặc đã hết hạn", Toast.LENGTH_SHORT).show();
        }
    }

    private double validateVoucherCode(String code) {
        // Sample voucher codes - replace with Firestore validation
        switch (code) {
            case "VOUCHER15K":
                return 15000;
            case "VOUCHER30K":
                return 30000;
            case "VOUCHER50K":
                return 50000;
            case "FREESHIP":
                // Free shipping is handled separately
                return 0;
            case "SALE50":
                // 50% discount, max 100k
                double discount = totalAmount * 0.5;
                return Math.min(discount, 100000);
            default:
                return 0;
        }
    }

    private void removeVoucher() {
        appliedVoucherCode = null;
        voucherDiscount = 0;
        
        // Show input, hide applied voucher
        voucherInputLayout.setVisibility(View.VISIBLE);
        appliedVoucherLayout.setVisibility(View.GONE);
        voucherDiscountLayout.setVisibility(View.GONE);
        
        etVoucherCode.setText("");
        updateTotal();
        Toast.makeText(this, "Đã xóa mã giảm giá", Toast.LENGTH_SHORT).show();
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

        double finalTotal = totalAmount - voucherDiscount;
        if (finalTotal < 0) finalTotal = 0;
        
        String message = "Người nhận: " + recipientName + "\n" +
                "SĐT: " + recipientPhone + "\n" +
                "Địa chỉ: " + shippingAddress;
        
        if (appliedVoucherCode != null) {
            message += "\nMã giảm giá: " + appliedVoucherCode + " (-" + String.format("%,.0f₫", voucherDiscount) + ")";
        }
        
        message += "\nTổng tiền: " + String.format("%,.0f₫", finalTotal);

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

        // Calculate final total with voucher
        double finalTotal = totalAmount - voucherDiscount;
        if (finalTotal < 0) finalTotal = 0;
        
        // Add voucher info to note if applied
        String finalNote = note;
        if (appliedVoucherCode != null) {
            if (finalNote.isEmpty()) {
                finalNote = "Mã giảm giá: " + appliedVoucherCode;
            } else {
                finalNote += "\nMã giảm giá: " + appliedVoucherCode;
            }
        }
        
        // Create order
        Order order = new Order(userId, orderItems, finalTotal, paymentMethod,
                recipientName, recipientPhone, shippingAddress, finalNote);

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
