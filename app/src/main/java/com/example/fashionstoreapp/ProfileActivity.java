package com.example.fashionstoreapp;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.fashionstoreapp.models.User;
import com.example.fashionstoreapp.utils.SessionManager;
import com.example.fashionstoreapp.utils.FirestoreManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class ProfileActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private TextView greetingText, phoneNumberText;
    private EditText lastNameInput, firstNameInput, birthdayInput, phoneInput;
    private RadioGroup genderGroup;
    private RadioButton maleRadio, femaleRadio;
    private Button updateButton;

    // Menu items
    private CardView menuProfile, menuSupport, menuAddress, menuVouchers, menuFavorites, menuPassword;

    private SessionManager sessionManager;
    private FirestoreManager firestoreManager;
    private FirebaseAuth mAuth;
    private Calendar calendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        sessionManager = new SessionManager(this);
        firestoreManager = FirestoreManager.getInstance();
        mAuth = FirebaseAuth.getInstance();
        calendar = Calendar.getInstance();

        initViews();
        setupToolbar();
        loadUserData();
        setupClickListeners();
    }

    private void initViews() {
        toolbar = findViewById(R.id.profileToolbar);
        greetingText = findViewById(R.id.greetingText);
        phoneNumberText = findViewById(R.id.phoneNumberText);

        lastNameInput = findViewById(R.id.lastNameInput);
        firstNameInput = findViewById(R.id.firstNameInput);
        birthdayInput = findViewById(R.id.birthdayInput);
        phoneInput = findViewById(R.id.phoneInput);

        genderGroup = findViewById(R.id.genderGroup);
        maleRadio = findViewById(R.id.maleRadio);
        femaleRadio = findViewById(R.id.femaleRadio);

        updateButton = findViewById(R.id.updateButton);

        // Menu items
        menuProfile = findViewById(R.id.menuProfile);
        menuSupport = findViewById(R.id.menuSupport);
        menuAddress = findViewById(R.id.menuAddress);
        menuVouchers = findViewById(R.id.menuVouchers);
        menuFavorites = findViewById(R.id.menuFavorites);
        menuPassword = findViewById(R.id.menuPassword);
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

        if (firebaseUser != null) {
            String userId = firebaseUser.getUid();

            // Display greeting
            String displayName = firebaseUser.getDisplayName();
            if (displayName != null) {
                greetingText.setText("Hi, " + displayName);
            }

            // Display phone or email
            if (firebaseUser.getPhoneNumber() != null) {
                phoneNumberText.setText("Số điện thoại: " + firebaseUser.getPhoneNumber());
                phoneInput.setText(firebaseUser.getPhoneNumber());
            } else if (firebaseUser.getEmail() != null) {
                phoneNumberText.setText("Email: " + firebaseUser.getEmail());
            }

            // Load profile from Firestore
            firestoreManager.loadUserProfile(userId, new FirestoreManager.OnUserProfileLoadedListener() {
                @Override
                public void onProfileLoaded(String name, String birthday, String gender, String phone) {
                    if (name != null && !name.isEmpty()) {
                        String[] nameParts = name.split(" ", 2);
                        if (nameParts.length > 0) {
                            lastNameInput.setText(nameParts[0]);
                        }
                        if (nameParts.length > 1) {
                            firstNameInput.setText(nameParts[1]);
                        }
                    }

                    if (birthday != null && !birthday.isEmpty()) {
                        birthdayInput.setText(birthday);
                    }

                    if (gender != null && !gender.isEmpty()) {
                        if (gender.equals("Nam")) {
                            maleRadio.setChecked(true);
                        } else if (gender.equals("Nữ")) {
                            femaleRadio.setChecked(true);
                        }
                    }

                    if (phone != null && !phone.isEmpty()) {
                        phoneInput.setText(phone);
                    }
                }

                @Override
                public void onError(String error) {
                    Toast.makeText(ProfileActivity.this, "Lỗi tải dữ liệu: " + error, Toast.LENGTH_SHORT).show();
                }
            });
        } else if (user != null) {
            // Fallback to session manager
            String displayName = user.getDisplayName();
            greetingText.setText("Hi, " + displayName);

            if (user.getPhone() != null) {
                phoneNumberText.setText("Số điện thoại: " + user.getPhone());
                phoneInput.setText(user.getPhone());
            } else if (user.getEmail() != null) {
                phoneNumberText.setText("Email: " + user.getEmail());
            }
        }
    }

    private void setupClickListeners() {
        // Birthday picker
        birthdayInput.setOnClickListener(v -> showDatePicker());

        // Update button
        updateButton.setOnClickListener(v -> updateProfile());

        // Menu items
        menuProfile.setOnClickListener(v -> {
            Toast.makeText(this, "Bạn đang ở trang Thông tin tài khoản", Toast.LENGTH_SHORT).show();
        });

        menuSupport.setOnClickListener(v -> {
            Toast.makeText(this, "Yêu cầu hỗ trợ", Toast.LENGTH_SHORT).show();
            // TODO: Navigate to support activity
        });

        menuAddress.setOnClickListener(v -> {
            Toast.makeText(this, "Sổ địa chỉ", Toast.LENGTH_SHORT).show();
            // TODO: Navigate to address activity
        });

        menuVouchers.setOnClickListener(v -> {
            Toast.makeText(this, "Vouchers", Toast.LENGTH_SHORT).show();
            // TODO: Navigate to vouchers activity
        });

        menuFavorites.setOnClickListener(v -> {
            Toast.makeText(this, "Sản phẩm yêu thích", Toast.LENGTH_SHORT).show();
            // TODO: Navigate to favorites activity
        });

        menuPassword.setOnClickListener(v -> {
            Toast.makeText(this, "Đổi mật khẩu", Toast.LENGTH_SHORT).show();
            // TODO: Navigate to change password activity
        });
    }

    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    updateBirthdayField();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void updateBirthdayField() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        birthdayInput.setText(dateFormat.format(calendar.getTime()));
    }

    private void updateProfile() {
        String lastName = lastNameInput.getText().toString().trim();
        String firstName = firstNameInput.getText().toString().trim();
        final String birthday = birthdayInput.getText().toString().trim();
        final String phone = phoneInput.getText().toString().trim();

        // Validation
        if (lastName.isEmpty()) {
            lastNameInput.setError("Vui lòng nhập họ");
            lastNameInput.requestFocus();
            return;
        }

        if (firstName.isEmpty()) {
            firstNameInput.setError("Vui lòng nhập tên");
            firstNameInput.requestFocus();
            return;
        }

        // Get gender
        int selectedGenderId = genderGroup.getCheckedRadioButtonId();
        final String gender;
        if (selectedGenderId == R.id.maleRadio) {
            gender = "Nam";
        } else if (selectedGenderId == R.id.femaleRadio) {
            gender = "Nữ";
        } else {
            gender = "";
        }

        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser != null) {
            String userId = firebaseUser.getUid();
            final String fullName = lastName + " " + firstName;

            // Save to Firestore
            firestoreManager.saveUserProfile(userId, fullName, birthday, gender, phone,
                    new FirestoreManager.OnUserProfileSavedListener() {
                        @Override
                        public void onProfileSaved() {
                            // Update session manager
                            User user = sessionManager.getUser();
                            if (user != null) {
                                user.setName(fullName);
                                user.setBirthday(birthday);
                                user.setGender(gender);
                                if (!phone.isEmpty()) {
                                    user.setPhone(phone);
                                }
                                sessionManager.updateUser(user);
                            }

                            Toast.makeText(ProfileActivity.this, "Cập nhật thông tin thành công!", Toast.LENGTH_SHORT)
                                    .show();
                        }

                        @Override
                        public void onError(String error) {
                            Toast.makeText(ProfileActivity.this, "Lỗi cập nhật: " + error, Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(this, "Lỗi: Không tìm thấy thông tin người dùng", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
