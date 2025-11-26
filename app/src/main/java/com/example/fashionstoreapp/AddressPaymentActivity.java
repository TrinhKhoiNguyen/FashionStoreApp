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
import com.example.fashionstoreapp.model.Address;
// Payment methods removed — app now uses addresses only
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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
    private RecyclerView addressesRecyclerView;
    private MaterialButton btnAddAddress;
    private LinearLayout addressEmptyLayout;

    private AddressAdapter addressAdapter;
    private List<Address> addressList;
    private FloatingActionButton fabAddAddress;

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
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        addressesRecyclerView = findViewById(R.id.addressesRecyclerView);
        btnAddAddress = findViewById(R.id.btnAddAddress);
        fabAddAddress = findViewById(R.id.fabAddAddress);
        addressEmptyLayout = findViewById(R.id.addressEmptyLayout);
    }

    private boolean selectMode = false;

    private void setupToolbar() {
        // Determine mode from intent
        selectMode = getIntent() != null && getIntent().getBooleanExtra("select_address", false);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            // If opened as address book from profile or selection mode, show "Sổ địa chỉ"
            if (selectMode) {
                getSupportActionBar().setTitle("Chọn địa chỉ");
            } else {
                getSupportActionBar().setTitle("Sổ địa chỉ");
            }
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerViews() {
        addressList = new ArrayList<>();
        addressAdapter = new AddressAdapter(this, addressList,
                new com.example.fashionstoreapp.adapters.AddressAdapter.OnAddressActionListener() {
                    @Override
                    public void onDelete(Address address) {
                        onAddressDelete(address);
                    }

                    @Override
                    public void onSelect(Address address) {
                        // If activity started for selection, return selected address
                        if (selectMode) {
                            android.content.Intent result = new android.content.Intent();
                            result.putExtra("name", address.getName());
                            result.putExtra("phone", address.getPhone());
                            result.putExtra("address", address.getAddress());
                            result.putExtra("city", address.getCity());
                            result.putExtra("addressId", address.getAddressId());
                            setResult(RESULT_OK, result);
                            finish();
                        }
                    }
                });
        addressesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        addressesRecyclerView.setAdapter(addressAdapter);

        // Payment methods removed — nothing to setup here
    }

    private void setupListeners() {
        btnAddAddress.setOnClickListener(v -> showAddAddressDialog());
        if (fabAddAddress != null) {
            fabAddAddress.setOnClickListener(v -> showAddAddressDialog());
        }
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
                        if (address != null) {
                            // Ensure addressId is set so delete operations work and selection returns id
                            address.setAddressId(doc.getId());
                            addressList.add(address);
                        }
                    }
                    updateAddressUI();
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
}
