package com.example.fashionstoreapp;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fashionstoreapp.adapters.AddressAdapter;
import com.example.fashionstoreapp.adapters.PaymentMethodAdapter;
import com.example.fashionstoreapp.model.Address;
import com.example.fashionstoreapp.model.PaymentMethod;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddressPaymentActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private RecyclerView addressesRecyclerView, paymentMethodsRecyclerView;
    private MaterialButton btnAddAddress, btnAddPayment;
    private LinearLayout addressEmptyLayout, paymentEmptyLayout;

    private AddressAdapter addressAdapter;
    private PaymentMethodAdapter paymentAdapter;
    private List<Address> addressList;
    private List<PaymentMethod> paymentList;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address_payment);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        initViews();
        setupToolbar();
        setupRecyclerViews();
        setupListeners();
        loadAddresses();
        loadPaymentMethods();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        addressesRecyclerView = findViewById(R.id.addressesRecyclerView);
        paymentMethodsRecyclerView = findViewById(R.id.paymentMethodsRecyclerView);
        btnAddAddress = findViewById(R.id.btnAddAddress);
        btnAddPayment = findViewById(R.id.btnAddPayment);
        addressEmptyLayout = findViewById(R.id.addressEmptyLayout);
        paymentEmptyLayout = findViewById(R.id.paymentEmptyLayout);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Địa chỉ & Thanh toán");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerViews() {
        addressList = new ArrayList<>();
        addressAdapter = new AddressAdapter(this, addressList, this::onAddressDelete);
        addressesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        addressesRecyclerView.setAdapter(addressAdapter);

        paymentList = new ArrayList<>();
        paymentAdapter = new PaymentMethodAdapter(this, paymentList, this::onPaymentDelete);
        paymentMethodsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        paymentMethodsRecyclerView.setAdapter(paymentAdapter);
    }

    private void setupListeners() {
        btnAddAddress.setOnClickListener(v -> showAddAddressDialog());
        btnAddPayment.setOnClickListener(v -> showAddPaymentDialog());
    }

    private void loadAddresses() {
        if (auth.getCurrentUser() == null)
            return;

        db.collection("users")
                .document(auth.getCurrentUser().getUid())
                .collection("addresses")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    addressList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Address address = doc.toObject(Address.class);
                        addressList.add(address);
                    }
                    updateAddressUI();
                });
    }

    private void loadPaymentMethods() {
        if (auth.getCurrentUser() == null)
            return;

        db.collection("users")
                .document(auth.getCurrentUser().getUid())
                .collection("paymentMethods")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    paymentList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        PaymentMethod payment = doc.toObject(PaymentMethod.class);
                        paymentList.add(payment);
                    }
                    updatePaymentUI();
                });
    }

    private void updateAddressUI() {
        if (addressList.isEmpty()) {
            addressEmptyLayout.setVisibility(View.VISIBLE);
            addressesRecyclerView.setVisibility(View.GONE);
        } else {
            addressEmptyLayout.setVisibility(View.GONE);
            addressesRecyclerView.setVisibility(View.VISIBLE);
            addressAdapter.notifyDataSetChanged();
        }
    }

    private void updatePaymentUI() {
        if (paymentList.isEmpty()) {
            paymentEmptyLayout.setVisibility(View.VISIBLE);
            paymentMethodsRecyclerView.setVisibility(View.GONE);
        } else {
            paymentEmptyLayout.setVisibility(View.GONE);
            paymentMethodsRecyclerView.setVisibility(View.VISIBLE);
            paymentAdapter.notifyDataSetChanged();
        }
    }

    private void showAddAddressDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_address, null);
        TextInputEditText etName = dialogView.findViewById(R.id.etAddressName);
        TextInputEditText etPhone = dialogView.findViewById(R.id.etAddressPhone);
        TextInputEditText etAddress = dialogView.findViewById(R.id.etAddressDetail);
        TextInputEditText etCity = dialogView.findViewById(R.id.etAddressCity);

        new AlertDialog.Builder(this)
                .setTitle("Thêm địa chỉ")
                .setView(dialogView)
                .setPositiveButton("Thêm", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    String phone = etPhone.getText().toString().trim();
                    String address = etAddress.getText().toString().trim();
                    String city = etCity.getText().toString().trim();

                    if (TextUtils.isEmpty(name) || TextUtils.isEmpty(phone) ||
                            TextUtils.isEmpty(address) || TextUtils.isEmpty(city)) {
                        Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    saveAddress(name, phone, address, city);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void saveAddress(String name, String phone, String address, String city) {
        if (auth.getCurrentUser() == null)
            return;

        Map<String, Object> addressData = new HashMap<>();
        addressData.put("name", name);
        addressData.put("phone", phone);
        addressData.put("address", address);
        addressData.put("city", city);
        addressData.put("isDefault", addressList.isEmpty());

        db.collection("users")
                .document(auth.getCurrentUser().getUid())
                .collection("addresses")
                .add(addressData)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Đã thêm địa chỉ", Toast.LENGTH_SHORT).show();
                    loadAddresses();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void showAddPaymentDialog() {
        String[] options = { "Thanh toán khi nhận hàng", "Thẻ ngân hàng", "Ví điện tử" };

        new AlertDialog.Builder(this)
                .setTitle("Chọn phương thức")
                .setItems(options, (dialog, which) -> {
                    String type = options[which];
                    savePaymentMethod(type);
                })
                .show();
    }

    private void savePaymentMethod(String type) {
        if (auth.getCurrentUser() == null)
            return;

        Map<String, Object> paymentData = new HashMap<>();
        paymentData.put("type", type);
        paymentData.put("isDefault", paymentList.isEmpty());

        db.collection("users")
                .document(auth.getCurrentUser().getUid())
                .collection("paymentMethods")
                .add(paymentData)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Đã thêm phương thức thanh toán", Toast.LENGTH_SHORT).show();
                    loadPaymentMethods();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void onAddressDelete(Address address) {
        if (auth.getCurrentUser() == null)
            return;

        new AlertDialog.Builder(this)
                .setTitle("Xóa địa chỉ")
                .setMessage("Bạn có chắc muốn xóa địa chỉ này?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    db.collection("users")
                            .document(auth.getCurrentUser().getUid())
                            .collection("addresses")
                            .document(address.getAddressId())
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Đã xóa", Toast.LENGTH_SHORT).show();
                                loadAddresses();
                            });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void onPaymentDelete(PaymentMethod payment) {
        if (auth.getCurrentUser() == null)
            return;

        new AlertDialog.Builder(this)
                .setTitle("Xóa phương thức")
                .setMessage("Bạn có chắc muốn xóa?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    db.collection("users")
                            .document(auth.getCurrentUser().getUid())
                            .collection("paymentMethods")
                            .document(payment.getPaymentId())
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Đã xóa", Toast.LENGTH_SHORT).show();
                                loadPaymentMethods();
                            });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}
