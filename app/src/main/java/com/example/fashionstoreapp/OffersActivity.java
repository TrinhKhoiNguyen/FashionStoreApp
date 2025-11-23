package com.example.fashionstoreapp;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fashionstoreapp.adapters.VoucherAdapter;
import com.example.fashionstoreapp.model.Voucher;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class OffersActivity extends AppCompatActivity implements VoucherAdapter.OnVoucherClickListener {

    private MaterialToolbar toolbar;
    private RecyclerView vouchersRecyclerView;
    private ProgressBar loadingProgress;
    private TextView emptyText, tvPoints;

    private VoucherAdapter adapter;
    private List<Voucher> voucherList;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offers);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        initViews();
        setupToolbar();
        setupRecyclerView();
        loadVouchers();
        loadUserPoints();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        vouchersRecyclerView = findViewById(R.id.vouchersRecyclerView);
        loadingProgress = findViewById(R.id.loadingProgress);
        emptyText = findViewById(R.id.emptyText);
        tvPoints = findViewById(R.id.tvPoints);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Ưu đãi");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        voucherList = new ArrayList<>();
        adapter = new VoucherAdapter(this, voucherList, this);
        vouchersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        vouchersRecyclerView.setAdapter(adapter);
    }

    private void loadVouchers() {
        loadingProgress.setVisibility(View.VISIBLE);
        vouchersRecyclerView.setVisibility(View.GONE);
        emptyText.setVisibility(View.GONE);

        db.collection("vouchers")
                .whereEqualTo("isActive", true)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    voucherList.clear();
                    Date now = new Date();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Voucher voucher = doc.toObject(Voucher.class);
                        // Only show vouchers that haven't expired
                        if (voucher.getExpiryDate() != null && voucher.getExpiryDate().after(now)) {
                            voucherList.add(voucher);
                        }
                    }

                    loadingProgress.setVisibility(View.GONE);
                    if (voucherList.isEmpty()) {
                        emptyText.setVisibility(View.VISIBLE);
                        vouchersRecyclerView.setVisibility(View.GONE);
                    } else {
                        emptyText.setVisibility(View.GONE);
                        vouchersRecyclerView.setVisibility(View.VISIBLE);
                        adapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(e -> {
                    loadingProgress.setVisibility(View.GONE);
                    emptyText.setVisibility(View.VISIBLE);
                    Toast.makeText(this, "Lỗi tải voucher: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void loadUserPoints() {
        if (auth.getCurrentUser() == null)
            return;

        db.collection("users")
                .document(auth.getCurrentUser().getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Long points = documentSnapshot.getLong("points");
                        tvPoints.setText(String.valueOf(points != null ? points : 0));
                    }
                });
    }

    @Override
    public void onCopyCodeClick(Voucher voucher) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Voucher Code", voucher.getCode());
        clipboard.setPrimaryClip(clip);
        Toast.makeText(this, "Đã sao chép mã: " + voucher.getCode(), Toast.LENGTH_SHORT).show();
    }
}
