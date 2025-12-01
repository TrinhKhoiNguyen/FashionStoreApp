package com.example.fashionstoreapp.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fashionstoreapp.AdminVoucherFormActivity;
import com.example.fashionstoreapp.R;
import com.example.fashionstoreapp.adapters.AdminVoucherAdapter;
import com.example.fashionstoreapp.models.Voucher;
import com.example.fashionstoreapp.utils.FirestoreManager;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

public class AdminVouchersFragment extends Fragment {

    private RecyclerView recyclerViewVouchers;
    private AdminVoucherAdapter adapter;
    private ProgressBar progressBar;
    private View layoutEmptyState;
    private FloatingActionButton fabAddVoucher;
    private TextInputEditText etSearchVoucher;
    private ChipGroup chipGroupFilter;

    private FirestoreManager firestoreManager;
    private String currentFilter = "all";
    private String currentSearchQuery = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_vouchers, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupRecyclerView();
        setupSearchAndFilter();
        setupFab();
        loadVouchers();
    }

    private void initViews(View view) {
        recyclerViewVouchers = view.findViewById(R.id.recyclerViewVouchers);
        progressBar = view.findViewById(R.id.progressBar);
        layoutEmptyState = view.findViewById(R.id.layoutEmptyState);
        fabAddVoucher = view.findViewById(R.id.fabAddVoucher);
        etSearchVoucher = view.findViewById(R.id.etSearchVoucher);
        chipGroupFilter = view.findViewById(R.id.chipGroupFilter);

        firestoreManager = FirestoreManager.getInstance();
    }

    private void setupRecyclerView() {
        adapter = new AdminVoucherAdapter(new AdminVoucherAdapter.OnVoucherActionListener() {
            @Override
            public void onEdit(Voucher voucher) {
                openVoucherForm(voucher);
            }

            @Override
            public void onDelete(Voucher voucher) {
                showDeleteConfirmDialog(voucher);
            }

            @Override
            public void onToggleStatus(Voucher voucher) {
                toggleVoucherStatus(voucher);
            }
        });

        recyclerViewVouchers.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewVouchers.setAdapter(adapter);
    }

    private void setupSearchAndFilter() {
        // Search
        etSearchVoucher.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchQuery = s.toString();
                adapter.filter(currentSearchQuery, currentFilter);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // Filter Chips
        chipGroupFilter.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty())
                return;

            int chipId = checkedIds.get(0);
            if (chipId == R.id.chipAll) {
                currentFilter = "all";
            } else if (chipId == R.id.chipActive) {
                currentFilter = "active";
            } else if (chipId == R.id.chipInactive) {
                currentFilter = "inactive";
            } else if (chipId == R.id.chipExpired) {
                currentFilter = "expired";
            } else if (chipId == R.id.chipPercent) {
                currentFilter = "percent";
            } else if (chipId == R.id.chipFixed) {
                currentFilter = "fixed";
            }

            adapter.filter(currentSearchQuery, currentFilter);
        });
    }

    private void setupFab() {
        fabAddVoucher.setOnClickListener(v -> openVoucherForm(null));
    }

    private void loadVouchers() {
        showLoading(true);

        firestoreManager.getAllVouchers(new FirestoreManager.OnVouchersLoadedListener() {
            @Override
            public void onVouchersLoaded(List<Voucher> vouchers) {
                showLoading(false);
                adapter.setVouchers(vouchers);
                updateEmptyState(vouchers.isEmpty());
            }

            @Override
            public void onError(String error) {
                showLoading(false);
                Toast.makeText(getContext(), "Lỗi: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openVoucherForm(Voucher voucher) {
        Intent intent = new Intent(getContext(), AdminVoucherFormActivity.class);
        if (voucher != null) {
            intent.putExtra("VOUCHER_ID", voucher.getId());
        }
        startActivity(intent);
    }

    private void showDeleteConfirmDialog(Voucher voucher) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Xóa voucher")
                .setMessage("Bạn có chắc muốn xóa voucher \"" + voucher.getCode() + "\"?")
                .setPositiveButton("Xóa", (dialog, which) -> deleteVoucher(voucher))
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void deleteVoucher(Voucher voucher) {
        showLoading(true);

        firestoreManager.deleteVoucher(voucher.getId(), new FirestoreManager.OnVoucherDeletedListener() {
            @Override
            public void onVoucherDeleted() {
                showLoading(false);
                Toast.makeText(getContext(), "Đã xóa voucher", Toast.LENGTH_SHORT).show();
                loadVouchers();
            }

            @Override
            public void onError(String error) {
                showLoading(false);
                Toast.makeText(getContext(), "Lỗi: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void toggleVoucherStatus(Voucher voucher) {
        boolean newStatus = !voucher.isActive();

        firestoreManager.toggleVoucherStatus(voucher.getId(), newStatus,
                new FirestoreManager.OnVoucherSavedListener() {
                    @Override
                    public void onVoucherSaved(String voucherId) {
                        String message = newStatus ? "Đã kích hoạt voucher" : "Đã vô hiệu hóa voucher";
                        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                        loadVouchers();
                    }

                    @Override
                    public void onError(String error) {
                        Toast.makeText(getContext(), "Lỗi: " + error, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void updateEmptyState(boolean isEmpty) {
        layoutEmptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        recyclerViewVouchers.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadVouchers();
    }
}
