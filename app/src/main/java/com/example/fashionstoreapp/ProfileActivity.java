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
    private ImageView profileAvatar, editAvatarIcon;
    private TextView greetingText, phoneNumberText;
    private EditText lastNameInput, firstNameInput, birthdayInput, phoneInput;
    private RadioGroup genderGroup;
    private RadioButton maleRadio, femaleRadio;
    private Button updateButton;
    private ProgressBar progressBar;

    // Menu items
    private CardView menuProfile, menuSupport, menuAddress, menuVouchers, menuFavorites, menuPassword;

    private SessionManager sessionManager;
    private FirestoreManager firestoreManager;
    private FirebaseAuth mAuth;
    private Calendar calendar;

    // Image picker
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private Uri selectedImageUri;
    private boolean isImageChanged = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        sessionManager = new SessionManager(this);
        firestoreManager = FirestoreManager.getInstance();
        mAuth = FirebaseAuth.getInstance();
        calendar = Calendar.getInstance();

        // Setup image picker
        setupImagePicker();

        initViews();
        setupToolbar();
        loadUserData();
        setupClickListeners();
    }

    private void setupImagePicker() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        if (selectedImageUri != null) {
                            isImageChanged = true;
                            // Display selected image immediately
                            Glide.with(this)
                                    .load(selectedImageUri)
                                    .circleCrop()
                                    .placeholder(R.drawable.ic_launcher_foreground)
                                    .into(profileAvatar);
                        }
                    }
                });
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private void initViews() {
        toolbar = findViewById(R.id.profileToolbar);
        profileAvatar = findViewById(R.id.profileAvatar);
        editAvatarIcon = findViewById(R.id.editAvatarIcon);
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
        progressBar = findViewById(R.id.uploadProgressBar);

        // Menu items
        menuProfile = findViewById(R.id.menuProfile);
        menuSupport = findViewById(R.id.menuSupport);
        menuAddress = findViewById(R.id.menuAddress);
        menuVouchers = findViewById(R.id.menuVouchers);
        menuFavorites = findViewById(R.id.menuFavorites);
        menuPassword = findViewById(R.id.menuPassword);

        // Hide progress bar initially
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }
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

            // Load profile photo
            Uri photoUrl = firebaseUser.getPhotoUrl();
            if (photoUrl != null) {
                Glide.with(this)
                        .load(photoUrl)
                        .circleCrop()
                        .placeholder(R.drawable.baseline_person_24)
                        .into(profileAvatar);
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
        // Avatar click to change
        profileAvatar.setOnClickListener(v -> openImagePicker());

        if (editAvatarIcon != null) {
            editAvatarIcon.setOnClickListener(v -> openImagePicker());
        }

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
            Intent intent = new Intent(ProfileActivity.this, FavoritesActivity.class);
            startActivity(intent);
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

            progressBar.setVisibility(View.VISIBLE);
            updateButton.setEnabled(false);

            // If image changed, upload it first
            if (isImageChanged && selectedImageUri != null) {
                uploadProfileImage(selectedImageUri, fullName, birthday, gender, phone);
            } else {
                // No image change, just update Firestore
                saveUserProfile(null, fullName, birthday, gender, phone);
            }
        } else {
            Toast.makeText(this, "Lỗi: Không tìm thấy thông tin người dùng", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadProfileImage(Uri imageUri, String fullName, String birthday, String gender, String phone) {
        String userId = mAuth.getCurrentUser().getUid();
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference avatarRef = storageRef.child("profiles/" + userId + "/avatar.jpg");

        avatarRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    avatarRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String photoUrl = uri.toString();
                        // Update Firebase Auth profile first, then Firestore
                        updateFirebaseAuthProfile(photoUrl, fullName, birthday, gender, phone);
                    }).addOnFailureListener(e -> {
                        progressBar.setVisibility(View.GONE);
                        updateButton.setEnabled(true);
                        Toast.makeText(ProfileActivity.this, "Lỗi lấy URL ảnh: " + e.getMessage(), Toast.LENGTH_SHORT)
                                .show();
                    });
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    updateButton.setEnabled(true);
                    Toast.makeText(ProfileActivity.this, "Lỗi tải ảnh lên: " + e.getMessage(), Toast.LENGTH_SHORT)
                            .show();
                });
    }

    private void updateFirebaseAuthProfile(String photoUrl, String fullName, String birthday, String gender,
            String phone) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            progressBar.setVisibility(View.GONE);
            updateButton.setEnabled(true);
            Toast.makeText(this, "Người dùng chưa đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(fullName)
                .setPhotoUri(Uri.parse(photoUrl))
                .build();

        user.updateProfile(profileUpdates)
                .addOnSuccessListener(aVoid -> {
                    // Now update Firestore with photo URL
                    saveUserProfile(photoUrl, fullName, birthday, gender, phone);
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    updateButton.setEnabled(true);
                    Toast.makeText(ProfileActivity.this, "Lỗi cập nhật profile Auth: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void saveUserProfile(String photoUrl, String fullName, String birthday, String gender, String phone) {
        String userId = mAuth.getCurrentUser().getUid();

        // Save to Firestore
        firestoreManager.saveUserProfile(userId, fullName, birthday, gender, phone,
                new FirestoreManager.OnUserProfileSavedListener() {
                    @Override
                    public void onProfileSaved() {
                        // Update photoUrl if provided
                        if (photoUrl != null) {
                            firestoreManager.updateUserPhotoUrl(userId, photoUrl);
                        }

                        // Update session manager
                        User user = sessionManager.getUser();
                        if (user != null) {
                            user.setName(fullName);
                            user.setBirthday(birthday);
                            user.setGender(gender);
                            if (!phone.isEmpty()) {
                                user.setPhone(phone);
                            }
                            if (photoUrl != null) {
                                user.setPhotoUrl(photoUrl);
                            }
                            sessionManager.updateUser(user);
                        }

                        progressBar.setVisibility(View.GONE);
                        updateButton.setEnabled(true);
                        isImageChanged = false;

                        Toast.makeText(ProfileActivity.this, "Cập nhật thông tin thành công!", Toast.LENGTH_SHORT)
                                .show();
                    }

                    @Override
                    public void onError(String error) {
                        progressBar.setVisibility(View.GONE);
                        updateButton.setEnabled(true);
                        Toast.makeText(ProfileActivity.this, "Lỗi cập nhật: " + error, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
