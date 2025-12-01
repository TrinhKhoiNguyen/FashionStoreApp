package com.example.fashionstoreapp;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.fashionstoreapp.models.Voucher;
import com.example.fashionstoreapp.utils.FirestoreManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AdminVoucherFormActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private TextInputEditText etVoucherCode, etAmount, etMaxDiscount, etMinOrder;
    private TextInputEditText etQuantity, etStartDate, etEndDate, etDescription;
    private AutoCompleteTextView actvVoucherType;
    private TextInputLayout tilVoucherCode, tilAmount, tilMaxDiscount, tilMinOrder;
    private TextInputLayout tilQuantity, tilStartDate, tilEndDate, tilVoucherType;
    private SwitchMaterial switchActive;
    private MaterialButton btnSave, btnDelete;
    private ProgressBar progressBar;

    private FirestoreManager firestoreManager;
    private Voucher currentVoucher;
    private String voucherId;
    private boolean isEditMode = false;

    private long startDateMillis = 0;
    private long endDateMillis = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_voucher_form);

        firestoreManager = FirestoreManager.getInstance();

        // Check if edit mode
        voucherId = getIntent().getStringExtra("VOUCHER_ID");
        isEditMode = voucherId != null;

        initViews();
        setupToolbar();
        setupTypeDropdown();
        setupDatePickers();
        setupButtons();

        if (isEditMode) {
            loadVoucher();
        }
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        etVoucherCode = findViewById(R.id.etVoucherCode);
        actvVoucherType = findViewById(R.id.actvVoucherType);
        etAmount = findViewById(R.id.etAmount);
        etMaxDiscount = findViewById(R.id.etMaxDiscount);
        etMinOrder = findViewById(R.id.etMinOrder);
        etQuantity = findViewById(R.id.etQuantity);
        etStartDate = findViewById(R.id.etStartDate);
        etEndDate = findViewById(R.id.etEndDate);
        etDescription = findViewById(R.id.etDescription);

        tilVoucherCode = findViewById(R.id.tilVoucherCode);
        tilVoucherType = findViewById(R.id.tilVoucherType);
        tilAmount = findViewById(R.id.tilAmount);
        tilMaxDiscount = findViewById(R.id.tilMaxDiscount);
        tilMinOrder = findViewById(R.id.tilMinOrder);
        tilQuantity = findViewById(R.id.tilQuantity);
        tilStartDate = findViewById(R.id.tilStartDate);
        tilEndDate = findViewById(R.id.tilEndDate);

        switchActive = findViewById(R.id.switchActive);
        btnSave = findViewById(R.id.btnSave);
        btnDelete = findViewById(R.id.btnDelete);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupToolbar() {
        toolbar.setTitle(isEditMode ? "Chỉnh sửa Voucher" : "Thêm Voucher");
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupTypeDropdown() {
        String[] types = { "Giảm cố định (₫)", "Giảm theo % (Percent)" };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, types);
        actvVoucherType.setAdapter(adapter);

        // Show/hide max discount field based on type
        actvVoucherType.setOnItemClickListener((parent, view, position, id) -> {
            if (position == 1) { // Percent type
                tilMaxDiscount.setVisibility(View.VISIBLE);
            } else {
                tilMaxDiscount.setVisibility(View.GONE);
                etMaxDiscount.setText("");
            }
        });
    }

    private void setupDatePickers() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();

        // Start Date Picker
        etStartDate.setOnClickListener(v -> {
            calendar.setTimeInMillis(startDateMillis > 0 ? startDateMillis : System.currentTimeMillis());

            DatePickerDialog dialog = new DatePickerDialog(this,
                    (view, year, month, dayOfMonth) -> {
                        calendar.set(year, month, dayOfMonth, 0, 0, 0);
                        startDateMillis = calendar.getTimeInMillis();
                        etStartDate.setText(sdf.format(calendar.getTime()));
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH));

            dialog.getDatePicker().setMinDate(System.currentTimeMillis());
            dialog.show();
        });

        // End Date Picker
        etEndDate.setOnClickListener(v -> {
            calendar.setTimeInMillis(endDateMillis > 0 ? endDateMillis : System.currentTimeMillis());

            DatePickerDialog dialog = new DatePickerDialog(this,
                    (view, year, month, dayOfMonth) -> {
                        calendar.set(year, month, dayOfMonth, 23, 59, 59);
                        endDateMillis = calendar.getTimeInMillis();
                        etEndDate.setText(sdf.format(calendar.getTime()));
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH));

            if (startDateMillis > 0) {
                dialog.getDatePicker().setMinDate(startDateMillis);
            } else {
                dialog.getDatePicker().setMinDate(System.currentTimeMillis());
            }
            dialog.show();
        });
    }

    private void setupButtons() {
        btnSave.setOnClickListener(v -> saveVoucher());

        if (isEditMode) {
            btnDelete.setVisibility(View.VISIBLE);
            btnDelete.setOnClickListener(v -> showDeleteConfirmDialog());
            // Disable editing voucher code in edit mode
            etVoucherCode.setEnabled(false);
        } else {
            btnDelete.setVisibility(View.GONE);
        }
    }

    private void loadVoucher() {
        // In a real app, you'd load from Firestore, but for now we'll get from Intent
        // You need to modify the fragment to pass the full voucher object or load here
        // For simplicity, I'll show the structure:

        showLoading(true);
        firestoreManager.getAllVouchers(new FirestoreManager.OnVouchersLoadedListener() {
            @Override
            public void onVouchersLoaded(java.util.List<Voucher> vouchers) {
                for (Voucher voucher : vouchers) {
                    if (voucher.getId().equals(voucherId)) {
                        currentVoucher = voucher;
                        fillFormWithVoucher(voucher);
                        break;
                    }
                }
                showLoading(false);
            }

            @Override
            public void onError(String error) {
                showLoading(false);
                Toast.makeText(AdminVoucherFormActivity.this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void fillFormWithVoucher(Voucher voucher) {
        etVoucherCode.setText(voucher.getCode());

        // Set type
        if ("fixed".equals(voucher.getType())) {
            actvVoucherType.setText("Giảm cố định (₫)", false);
            tilMaxDiscount.setVisibility(View.GONE);
        } else {
            actvVoucherType.setText("Giảm theo % (Percent)", false);
            tilMaxDiscount.setVisibility(View.VISIBLE);
            etMaxDiscount.setText(String.valueOf((int) voucher.getMaxDiscount()));
        }

        etAmount.setText(String.valueOf((int) voucher.getAmount()));
        etMinOrder.setText(String.valueOf((int) voucher.getMinOrder()));
        etQuantity.setText(String.valueOf(voucher.getQuantity()));

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        startDateMillis = voucher.getStartAt();
        endDateMillis = voucher.getEndAt();
        etStartDate.setText(sdf.format(voucher.getStartAt()));
        etEndDate.setText(sdf.format(voucher.getEndAt()));

        etDescription.setText(voucher.getDescription());
        switchActive.setChecked(voucher.isActive());
    }

    private void saveVoucher() {
        if (!validateForm()) {
            return;
        }

        Voucher voucher = new Voucher();

        // Code
        voucher.setCode(etVoucherCode.getText().toString().trim().toUpperCase());

        // Type
        String selectedType = actvVoucherType.getText().toString();
        voucher.setType(selectedType.contains("Percent") ? "percent" : "fixed");

        // Amount
        voucher.setAmount(Double.parseDouble(etAmount.getText().toString().trim()));

        // Max Discount (for percent type)
        if ("percent".equals(voucher.getType()) && !etMaxDiscount.getText().toString().trim().isEmpty()) {
            voucher.setMaxDiscount(Double.parseDouble(etMaxDiscount.getText().toString().trim()));
        } else {
            voucher.setMaxDiscount(0);
        }

        // Min Order
        voucher.setMinOrder(Double.parseDouble(etMinOrder.getText().toString().trim()));

        // Quantity
        voucher.setQuantity(Integer.parseInt(etQuantity.getText().toString().trim()));

        // Dates
        voucher.setStartAt(startDateMillis);
        voucher.setEndAt(endDateMillis);

        // Description
        String description = etDescription.getText().toString().trim();
        voucher.setDescription(description.isEmpty() ? null : description);

        // Active
        voucher.setActive(switchActive.isChecked());

        showLoading(true);

        if (isEditMode) {
            // Keep used count from current voucher
            voucher.setUsedCount(currentVoucher.getUsedCount());

            firestoreManager.updateVoucher(voucherId, voucher, new FirestoreManager.OnVoucherSavedListener() {
                @Override
                public void onVoucherSaved(String voucherId) {
                    showLoading(false);
                    Toast.makeText(AdminVoucherFormActivity.this, "Đã cập nhật voucher", Toast.LENGTH_SHORT).show();
                    finish();
                }

                @Override
                public void onError(String error) {
                    showLoading(false);
                    Toast.makeText(AdminVoucherFormActivity.this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            firestoreManager.createVoucher(voucher, new FirestoreManager.OnVoucherSavedListener() {
                @Override
                public void onVoucherSaved(String voucherId) {
                    showLoading(false);
                    Toast.makeText(AdminVoucherFormActivity.this, "Đã tạo voucher mới", Toast.LENGTH_SHORT).show();
                    finish();
                }

                @Override
                public void onError(String error) {
                    showLoading(false);
                    Toast.makeText(AdminVoucherFormActivity.this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private boolean validateForm() {
        boolean isValid = true;

        // Code
        String code = etVoucherCode.getText().toString().trim();
        if (code.isEmpty()) {
            tilVoucherCode.setError("Vui lòng nhập mã voucher");
            isValid = false;
        } else if (code.length() < 3) {
            tilVoucherCode.setError("Mã voucher phải có ít nhất 3 ký tự");
            isValid = false;
        } else if (!code.matches("[A-Z0-9]+")) {
            tilVoucherCode.setError("Mã voucher chỉ chứa chữ hoa và số");
            isValid = false;
        } else {
            tilVoucherCode.setError(null);
        }

        // Type
        if (actvVoucherType.getText().toString().isEmpty()) {
            tilVoucherType.setError("Vui lòng chọn loại voucher");
            isValid = false;
        } else {
            tilVoucherType.setError(null);
        }

        // Amount
        String amountStr = etAmount.getText().toString().trim();
        if (amountStr.isEmpty()) {
            tilAmount.setError("Vui lòng nhập giá trị");
            isValid = false;
        } else {
            double amount = Double.parseDouble(amountStr);
            String type = actvVoucherType.getText().toString();

            if (type.contains("Percent")) {
                if (amount <= 0 || amount > 100) {
                    tilAmount.setError("Phần trăm phải từ 1 đến 100");
                    isValid = false;
                } else {
                    tilAmount.setError(null);
                }
            } else {
                if (amount <= 0) {
                    tilAmount.setError("Giá trị phải lớn hơn 0");
                    isValid = false;
                } else {
                    tilAmount.setError(null);
                }
            }
        }

        // Min Order
        String minOrderStr = etMinOrder.getText().toString().trim();
        if (minOrderStr.isEmpty()) {
            tilMinOrder.setError("Vui lòng nhập đơn hàng tối thiểu");
            isValid = false;
        } else if (Double.parseDouble(minOrderStr) < 0) {
            tilMinOrder.setError("Giá trị không hợp lệ");
            isValid = false;
        } else {
            tilMinOrder.setError(null);
        }

        // Quantity
        String quantityStr = etQuantity.getText().toString().trim();
        if (quantityStr.isEmpty()) {
            tilQuantity.setError("Vui lòng nhập số lượng");
            isValid = false;
        } else if (Integer.parseInt(quantityStr) <= 0) {
            tilQuantity.setError("Số lượng phải lớn hơn 0");
            isValid = false;
        } else {
            tilQuantity.setError(null);
        }

        // Start Date
        if (startDateMillis == 0) {
            tilStartDate.setError("Vui lòng chọn ngày bắt đầu");
            isValid = false;
        } else {
            tilStartDate.setError(null);
        }

        // End Date
        if (endDateMillis == 0) {
            tilEndDate.setError("Vui lòng chọn ngày kết thúc");
            isValid = false;
        } else if (endDateMillis <= startDateMillis) {
            tilEndDate.setError("Ngày kết thúc phải sau ngày bắt đầu");
            isValid = false;
        } else if (endDateMillis < System.currentTimeMillis()) {
            tilEndDate.setError("Ngày kết thúc phải sau ngày hiện tại");
            isValid = false;
        } else {
            tilEndDate.setError(null);
        }

        return isValid;
    }

    private void showDeleteConfirmDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Xóa voucher")
                .setMessage("Bạn có chắc muốn xóa voucher này?")
                .setPositiveButton("Xóa", (dialog, which) -> deleteVoucher())
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void deleteVoucher() {
        showLoading(true);

        firestoreManager.deleteVoucher(voucherId, new FirestoreManager.OnVoucherDeletedListener() {
            @Override
            public void onVoucherDeleted() {
                showLoading(false);
                Toast.makeText(AdminVoucherFormActivity.this, "Đã xóa voucher", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onError(String error) {
                showLoading(false);
                Toast.makeText(AdminVoucherFormActivity.this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnSave.setEnabled(!show);
    }
}
