package com.example.fashionstoreapp;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fashionstoreapp.adapters.AddressAdapter;
import com.example.fashionstoreapp.api.AddressApiService;
import com.example.fashionstoreapp.model.Address;
import com.example.fashionstoreapp.model.District;
import com.example.fashionstoreapp.model.Province;
import com.example.fashionstoreapp.model.Ward;
import com.example.fashionstoreapp.utils.RetrofitClient;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddressPaymentActivity extends AppCompatActivity {

    private static final String TAG = "AddressPaymentActivity";

    private MaterialToolbar toolbar;
    private RecyclerView addressesRecyclerView;
    private MaterialButton btnAddAddress;
    private LinearLayout addressEmptyLayout;

    private AddressAdapter addressAdapter;
    private List<Address> addressList;
    private ExtendedFloatingActionButton fabAddAddress;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    // API Service
    private AddressApiService addressApiService;

    // Dialog components
    private Spinner spinnerProvince, spinnerDistrict, spinnerWard;
    private List<Province> provinceList = new ArrayList<>();
    private List<District> districtList = new ArrayList<>();
    private List<Ward> wardList = new ArrayList<>();
    private Province selectedProvince;
    private District selectedDistrict;
    private Ward selectedWard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address_payment);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        addressApiService = RetrofitClient.getInstance().getAddressApiService();

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

                    @Override
                    public void onViewDetails(Address address) {
                        showAddressDetailsDialog(address);
                    }

                    @Override
                    public void onEdit(Address address) {
                        showEditAddressDialog(address);
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

        // Initialize Spinners
        spinnerProvince = dialogView.findViewById(R.id.spinnerProvince);
        spinnerDistrict = dialogView.findViewById(R.id.spinnerDistrict);
        spinnerWard = dialogView.findViewById(R.id.spinnerWard);

        // Reset selections
        selectedProvince = null;
        selectedDistrict = null;
        selectedWard = null;

        // Setup Spinners
        setupProvinceSpinner();
        setupDistrictSpinner();
        setupWardSpinner();

        // Load provinces
        loadProvinces();

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Thêm địa chỉ")
                .setView(dialogView)
                .setPositiveButton("Thêm", null) // Set to null to override later
                .setNegativeButton("Hủy", null)
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            MaterialButton positiveButton = (MaterialButton) dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(v -> {
                String name = etName.getText().toString().trim();
                String phone = etPhone.getText().toString().trim();
                String address = etAddress.getText().toString().trim();

                // Validation
                if (TextUtils.isEmpty(name)) {
                    etName.setError("Vui lòng nhập họ và tên");
                    etName.requestFocus();
                    return;
                }

                if (TextUtils.isEmpty(phone)) {
                    etPhone.setError("Vui lòng nhập số điện thoại");
                    etPhone.requestFocus();
                    return;
                }

                if (selectedProvince == null) {
                    Toast.makeText(this, "Vui lòng chọn Tỉnh/Thành phố", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (selectedDistrict == null) {
                    Toast.makeText(this, "Vui lòng chọn Quận/Huyện", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (selectedWard == null) {
                    Toast.makeText(this, "Vui lòng chọn Phường/Xã", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(address)) {
                    etAddress.setError("Vui lòng nhập địa chỉ chi tiết");
                    etAddress.requestFocus();
                    return;
                }

                // All validations passed, save address
                saveAddress(name, phone, address, selectedProvince, selectedDistrict, selectedWard);
                dialog.dismiss();
            });
        });

        dialog.show();
    }

    /**
     * Setup Province Spinner with listener
     */
    private void setupProvinceSpinner() {
        spinnerProvince.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) { // Skip "Chọn Tỉnh/Thành phố" at position 0
                    selectedProvince = provinceList.get(position - 1);
                    selectedDistrict = null;
                    selectedWard = null;
                    loadDistricts(selectedProvince.getProvinceId());
                } else {
                    selectedProvince = null;
                    selectedDistrict = null;
                    selectedWard = null;
                    spinnerDistrict.setEnabled(false);
                    spinnerWard.setEnabled(false);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    /**
     * Setup District Spinner with listener
     */
    private void setupDistrictSpinner() {
        spinnerDistrict.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) { // Skip "Chọn Quận/Huyện" at position 0
                    selectedDistrict = districtList.get(position - 1);
                    selectedWard = null;
                    loadWards(selectedDistrict.getDistrictId());
                } else {
                    selectedDistrict = null;
                    selectedWard = null;
                    spinnerWard.setEnabled(false);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    /**
     * Setup Ward Spinner with listener
     */
    private void setupWardSpinner() {
        spinnerWard.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) { // Skip "Chọn Phường/Xã" at position 0
                    selectedWard = wardList.get(position - 1);
                } else {
                    selectedWard = null;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    /**
     * Load provinces from API
     */
    private void loadProvinces() {
        addressApiService.getProvinces().enqueue(new Callback<List<Province>>() {
            @Override
            public void onResponse(Call<List<Province>> call, Response<List<Province>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    provinceList = response.body();
                    List<String> provinceNames = new ArrayList<>();
                    provinceNames.add("Chọn Tỉnh/Thành phố");
                    for (Province province : provinceList) {
                        provinceNames.add(province.getProvinceName());
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            AddressPaymentActivity.this,
                            android.R.layout.simple_spinner_item,
                            provinceNames);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerProvince.setAdapter(adapter);
                } else {
                    Log.e(TAG, "Failed to load provinces: " + response.code());
                    Toast.makeText(AddressPaymentActivity.this,
                            "Không thể tải danh sách tỉnh/thành phố",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Province>> call, Throwable t) {
                Log.e(TAG, "Error loading provinces", t);
                Toast.makeText(AddressPaymentActivity.this,
                        "Lỗi kết nối: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Load districts from API based on province
     */
    private void loadDistricts(int provinceId) {
        addressApiService.getProvinceWithDistricts(provinceId, 2).enqueue(new Callback<Province>() {
            @Override
            public void onResponse(Call<Province> call, Response<Province> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getDistricts() != null) {
                    districtList = response.body().getDistricts();
                    List<String> districtNames = new ArrayList<>();
                    districtNames.add("Chọn Quận/Huyện");
                    for (District district : districtList) {
                        districtNames.add(district.getDistrictName());
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            AddressPaymentActivity.this,
                            android.R.layout.simple_spinner_item,
                            districtNames);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerDistrict.setAdapter(adapter);
                    spinnerDistrict.setEnabled(true);
                } else {
                    Log.e(TAG, "Failed to load districts: " + response.code());
                    Toast.makeText(AddressPaymentActivity.this,
                            "Không thể tải danh sách quận/huyện",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Province> call, Throwable t) {
                Log.e(TAG, "Error loading districts", t);
                Toast.makeText(AddressPaymentActivity.this,
                        "Lỗi kết nối: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Load wards from API based on district
     */
    private void loadWards(int districtId) {
        addressApiService.getDistrictWithWards(districtId, 2).enqueue(new Callback<District>() {
            @Override
            public void onResponse(Call<District> call, Response<District> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getWards() != null) {
                    wardList = response.body().getWards();
                    List<String> wardNames = new ArrayList<>();
                    wardNames.add("Chọn Phường/Xã");
                    for (Ward ward : wardList) {
                        wardNames.add(ward.getWardName());
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            AddressPaymentActivity.this,
                            android.R.layout.simple_spinner_item,
                            wardNames);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerWard.setAdapter(adapter);
                    spinnerWard.setEnabled(true);
                } else {
                    Log.e(TAG, "Failed to load wards: " + response.code());
                    Toast.makeText(AddressPaymentActivity.this,
                            "Không thể tải danh sách phường/xã",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<District> call, Throwable t) {
                Log.e(TAG, "Error loading wards", t);
                Toast.makeText(AddressPaymentActivity.this,
                        "Lỗi kết nối: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveAddress(String name, String phone, String address,
            Province province, District district, Ward ward) {
        if (auth.getCurrentUser() == null)
            return;

        Map<String, Object> addressData = new HashMap<>();
        addressData.put("name", name);
        addressData.put("phone", phone);
        addressData.put("address", address);

        // Lưu thông tin địa chỉ 3 cấp
        addressData.put("provinceId", province.getProvinceId());
        addressData.put("provinceName", province.getProvinceName());
        addressData.put("districtId", district.getDistrictId());
        addressData.put("districtName", district.getDistrictName());
        addressData.put("wardCode", ward.getWardCode());
        addressData.put("wardName", ward.getWardName());

        // Tạo city từ provinceName để tương thích với code cũ
        addressData.put("city", province.getProvinceName());
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

    private void showAddressDetailsDialog(Address address) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_address_details, null);

        TextView tvName = dialogView.findViewById(R.id.tvDetailName);
        TextView tvPhone = dialogView.findViewById(R.id.tvDetailPhone);
        TextView tvAddress = dialogView.findViewById(R.id.tvDetailAddress);
        TextView tvWard = dialogView.findViewById(R.id.tvDetailWard);
        TextView tvDistrict = dialogView.findViewById(R.id.tvDetailDistrict);
        TextView tvProvince = dialogView.findViewById(R.id.tvDetailProvince);

        tvName.setText(address.getName());
        tvPhone.setText(address.getPhone());
        tvAddress.setText(address.getAddress());
        tvWard.setText(address.getWardName());
        tvDistrict.setText(address.getDistrictName());
        tvProvince.setText(address.getProvinceName());

        new AlertDialog.Builder(this)
                .setTitle("Chi tiết địa chỉ")
                .setView(dialogView)
                .setPositiveButton("Đóng", null)
                .show();
    }

    private void showEditAddressDialog(Address address) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_address, null);

        TextInputEditText editName = dialogView.findViewById(R.id.etAddressName);
        TextInputEditText editPhone = dialogView.findViewById(R.id.etAddressPhone);
        TextInputEditText editAddress = dialogView.findViewById(R.id.etAddressDetail);
        spinnerProvince = dialogView.findViewById(R.id.spinnerProvince);
        spinnerDistrict = dialogView.findViewById(R.id.spinnerDistrict);
        spinnerWard = dialogView.findViewById(R.id.spinnerWard);

        // Pre-populate fields
        editName.setText(address.getName());
        editPhone.setText(address.getPhone());
        editAddress.setText(address.getAddress());

        setupProvinceSpinner();
        setupDistrictSpinner();
        setupWardSpinner();

        // Load provinces and pre-select
        loadProvincesForEdit(address);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Chỉnh sửa địa chỉ")
                .setView(dialogView)
                .setPositiveButton("Lưu", null)
                .setNegativeButton("Hủy", null)
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            MaterialButton btnSave = (MaterialButton) dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            btnSave.setOnClickListener(v -> {
                String name = editName.getText().toString().trim();
                String phone = editPhone.getText().toString().trim();
                String detailAddress = editAddress.getText().toString().trim();

                if (TextUtils.isEmpty(name)) {
                    editName.setError("Vui lòng nhập tên");
                    return;
                }
                if (TextUtils.isEmpty(phone)) {
                    editPhone.setError("Vui lòng nhập số điện thoại");
                    return;
                }
                if (TextUtils.isEmpty(detailAddress)) {
                    editAddress.setError("Vui lòng nhập địa chỉ chi tiết");
                    return;
                }
                if (selectedProvince == null) {
                    Toast.makeText(this, "Vui lòng chọn Tỉnh/Thành phố", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (selectedDistrict == null) {
                    Toast.makeText(this, "Vui lòng chọn Quận/Huyện", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (selectedWard == null) {
                    Toast.makeText(this, "Vui lòng chọn Phường/Xã", Toast.LENGTH_SHORT).show();
                    return;
                }

                updateAddress(address.getAddressId(), name, phone, detailAddress,
                        selectedProvince, selectedDistrict, selectedWard);
                dialog.dismiss();
            });
        });

        dialog.show();
    }

    private void loadProvincesForEdit(Address address) {
        addressApiService.getProvinces().enqueue(new Callback<List<Province>>() {
            @Override
            public void onResponse(Call<List<Province>> call, Response<List<Province>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    provinceList = response.body();
                    List<String> provinceNames = new ArrayList<>();
                    int selectedPosition = 0;

                    for (int i = 0; i < provinceList.size(); i++) {
                        Province p = provinceList.get(i);
                        provinceNames.add(p.getProvinceName());
                        if (p.getProvinceId() == address.getProvinceId()) {
                            selectedPosition = i;
                        }
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            AddressPaymentActivity.this,
                            android.R.layout.simple_spinner_item,
                            provinceNames);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerProvince.setAdapter(adapter);

                    // Set selected province and load districts
                    final int finalSelectedPosition = selectedPosition;
                    spinnerProvince.post(() -> {
                        spinnerProvince.setSelection(finalSelectedPosition);
                        selectedProvince = provinceList.get(finalSelectedPosition);
                        loadDistrictsForEdit(address);
                    });
                }
            }

            @Override
            public void onFailure(Call<List<Province>> call, Throwable t) {
                Toast.makeText(AddressPaymentActivity.this,
                        "Lỗi tải tỉnh/thành phố", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadDistrictsForEdit(Address address) {
        if (selectedProvince == null)
            return;

        addressApiService.getProvinceWithDistricts(selectedProvince.getProvinceId(), 2)
                .enqueue(new Callback<Province>() {
                    @Override
                    public void onResponse(Call<Province> call, Response<Province> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            districtList = response.body().getDistricts();
                            if (districtList != null && !districtList.isEmpty()) {
                                List<String> districtNames = new ArrayList<>();
                                int selectedPosition = 0;

                                for (int i = 0; i < districtList.size(); i++) {
                                    District d = districtList.get(i);
                                    districtNames.add(d.getDistrictName());
                                    if (d.getDistrictId() == address.getDistrictId()) {
                                        selectedPosition = i;
                                    }
                                }

                                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                        AddressPaymentActivity.this,
                                        android.R.layout.simple_spinner_item,
                                        districtNames);
                                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                spinnerDistrict.setAdapter(adapter);

                                // Set selected district and load wards
                                final int finalSelectedPosition = selectedPosition;
                                spinnerDistrict.post(() -> {
                                    spinnerDistrict.setSelection(finalSelectedPosition);
                                    selectedDistrict = districtList.get(finalSelectedPosition);
                                    loadWardsForEdit(address);
                                });
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<Province> call, Throwable t) {
                        Toast.makeText(AddressPaymentActivity.this,
                                "Lỗi tải quận/huyện", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadWardsForEdit(Address address) {
        if (selectedDistrict == null)
            return;

        addressApiService.getDistrictWithWards(selectedDistrict.getDistrictId(), 2)
                .enqueue(new Callback<District>() {
                    @Override
                    public void onResponse(Call<District> call, Response<District> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            wardList = response.body().getWards();
                            if (wardList != null && !wardList.isEmpty()) {
                                List<String> wardNames = new ArrayList<>();
                                int selectedPosition = 0;

                                for (int i = 0; i < wardList.size(); i++) {
                                    Ward w = wardList.get(i);
                                    wardNames.add(w.getWardName());
                                    if (w.getWardCode() == address.getWardCode()) {
                                        selectedPosition = i;
                                    }
                                }

                                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                        AddressPaymentActivity.this,
                                        android.R.layout.simple_spinner_item,
                                        wardNames);
                                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                spinnerWard.setAdapter(adapter);

                                // Set selected ward
                                final int finalSelectedPosition = selectedPosition;
                                spinnerWard.post(() -> {
                                    spinnerWard.setSelection(finalSelectedPosition);
                                    selectedWard = wardList.get(finalSelectedPosition);
                                });
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<District> call, Throwable t) {
                        Toast.makeText(AddressPaymentActivity.this,
                                "Lỗi tải phường/xã", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateAddress(String addressId, String name, String phone, String address,
            Province province, District district, Ward ward) {
        if (auth.getCurrentUser() == null)
            return;

        Map<String, Object> addressData = new HashMap<>();
        addressData.put("name", name);
        addressData.put("phone", phone);
        addressData.put("address", address);
        addressData.put("provinceId", province.getProvinceId());
        addressData.put("provinceName", province.getProvinceName());
        addressData.put("districtId", district.getDistrictId());
        addressData.put("districtName", district.getDistrictName());
        addressData.put("wardCode", ward.getWardCode());
        addressData.put("wardName", ward.getWardName());
        addressData.put("city", province.getProvinceName());

        db.collection("users")
                .document(auth.getCurrentUser().getUid())
                .collection("addresses")
                .document(addressId)
                .update(addressData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Đã cập nhật địa chỉ", Toast.LENGTH_SHORT).show();
                    loadAddresses();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
