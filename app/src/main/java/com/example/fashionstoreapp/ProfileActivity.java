package com.example.fashionstoreapp;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.bumptech.glide.Glide;
import com.example.fashionstoreapp.models.User;
import com.example.fashionstoreapp.utils.SessionManager;
import com.example.fashionstoreapp.utils.FirestoreManager;
import com.google.android.material.appbar.MaterialToolbar;
import androidx.appcompat.app.AlertDialog;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.UUID;

public class ProfileActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private ImageView profileAvatar;
    private BottomNavigationView bottomNavigation;

    private SessionManager sessionManager;
    private FirestoreManager firestoreManager;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        sessionManager = new SessionManager(this);
        firestoreManager = FirestoreManager.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Check if user is logged in
        if (mAuth.getCurrentUser() == null) {
            // Not logged in, redirect to login
            Toast.makeText(this, "Vui lòng đăng nhập để xem trang cá nhân", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        initViews();
        setupToolbar();
        loadUserData();
        setupBottomNavigation();
    }

    private void initViews() {
        toolbar = findViewById(R.id.profileToolbar);
        profileAvatar = findViewById(R.id.profileAvatar);
        TextView fullNameText = findViewById(R.id.fullNameText);
        TextView emailText = findViewById(R.id.emailText);

        // New section cards
        CardView personalInfoCard = findViewById(R.id.personalInfoCard);
        CardView adminPanelCard = findViewById(R.id.adminPanelCard);
        CardView ordersCard = findViewById(R.id.ordersCard);
        CardView addressPaymentCard = findViewById(R.id.addressPaymentCard);
        CardView offersCard = findViewById(R.id.offersCard);
        CardView settingsCard = findViewById(R.id.settingsCard);
        CardView supportCard = findViewById(R.id.supportCard);
        MaterialButton logoutButton = findViewById(R.id.logoutButton);
        bottomNavigation = findViewById(R.id.bottomNavigation);

        // Check if user is admin and show admin panel
        checkAdminStatus(adminPanelCard);

        // Set up click listeners for new cards
        personalInfoCard.setOnClickListener(v -> {
            startActivity(new Intent(ProfileActivity.this, PersonalInfoActivity.class));
        });

        adminPanelCard.setOnClickListener(v -> {
            startActivity(new Intent(ProfileActivity.this, AdminPanelActivity.class));
        });

        ordersCard.setOnClickListener(v -> {
            startActivity(new Intent(ProfileActivity.this, OrdersActivity.class));
        });

        addressPaymentCard.setOnClickListener(v -> {
            startActivity(new Intent(ProfileActivity.this, AddressPaymentActivity.class));
        });

        offersCard.setOnClickListener(v -> {
            startActivity(new Intent(ProfileActivity.this, OffersActivity.class));
        });

        settingsCard.setOnClickListener(v -> {
            startActivity(new Intent(ProfileActivity.this, SettingsActivity.class));
        });

        supportCard.setOnClickListener(v -> {
            startActivity(new Intent(ProfileActivity.this, SupportActivity.class));
        });

        logoutButton.setOnClickListener(v -> {
            handleLogout();
        });
    }

    private void handleLogout() {
        // Ask for confirmation before signing out
        new AlertDialog.Builder(this)
                .setTitle("Đăng xuất")
                .setMessage("Bạn có chắc chắn muốn đăng xuất?")
                .setPositiveButton("Đăng xuất", (dialog, which) -> {
                    // Sign out from Firebase
                    mAuth.signOut();

                    // Clear session
                    sessionManager.logout();

                    // Navigate to login
                    Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();

                    Toast.makeText(this, "Đã đăng xuất thành công", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Tài khoản của bạn");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void loadUserData() {
        User user = sessionManager.getUser();
        FirebaseUser firebaseUser = mAuth.getCurrentUser();

        TextView fullNameText = findViewById(R.id.fullNameText);
        TextView emailText = findViewById(R.id.emailText);

        if (firebaseUser != null) {
            String userId = firebaseUser.getUid();

            // Display name
            String displayName = firebaseUser.getDisplayName();
            if (displayName != null && !displayName.isEmpty()) {
                fullNameText.setText(displayName);
            } else {
                fullNameText.setText("Người dùng");
            }

            // Display email
            String email = firebaseUser.getEmail();
            if (email != null && !email.isEmpty()) {
                emailText.setText(email);
            }

            // Load profile photo
            Uri photoUrl = firebaseUser.getPhotoUrl();
            if (photoUrl != null) {
                Glide.with(this)
                        .load(photoUrl)
                        .circleCrop()
                        .placeholder(R.drawable.baseline_person_24)
                        .into(profileAvatar);
            }
        }
    }

    private void setupBottomNavigation() {
        bottomNavigation.setSelectedItemId(R.id.nav_account);
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                startActivity(new Intent(this, MainActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (itemId == R.id.nav_categories) {
                startActivity(new Intent(this, CategoriesActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (itemId == R.id.nav_wishlist) {
                startActivity(new Intent(this, FavoritesActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (itemId == R.id.nav_account) {
                return true;
            }
            return false;
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    private void checkAdminStatus(CardView adminPanelCard) {
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser != null) {
            String userId = firebaseUser.getUid();

            // Load user role from Firestore
            firestoreManager.getUserRole(userId, new FirestoreManager.OnUserRoleLoadedListener() {
                @Override
                public void onRoleLoaded(String role) {
                    if ("admin".equalsIgnoreCase(role)) {
                        adminPanelCard.setVisibility(View.VISIBLE);
                    } else {
                        adminPanelCard.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onError(String error) {
                    adminPanelCard.setVisibility(View.GONE);
                }
            });
        } else {
            adminPanelCard.setVisibility(View.GONE);
        }
    }
}
