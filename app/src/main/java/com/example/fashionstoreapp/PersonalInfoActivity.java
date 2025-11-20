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

import com.bumptech.glide.Glide;
import com.example.fashionstoreapp.utils.FirestoreManager;
import com.example.fashionstoreapp.utils.SessionManager;
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

public class PersonalInfoActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private ImageView profileAvatar, editAvatarIcon;
    private EditText lastNameInput, firstNameInput, birthdayInput, phoneInput;
    private RadioGroup genderGroup;
    private RadioButton maleRadio, femaleRadio;
    private Button updateButton;
    private ProgressBar progressBar;

    private SessionManager sessionManager;
    private FirestoreManager firestoreManager;
    private FirebaseAuth mAuth;
    private Calendar calendar;

    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private Uri selectedImageUri;
    private boolean isImageChanged = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_info);

        sessionManager = new SessionManager(this);
        firestoreManager = FirestoreManager.getInstance();
        mAuth = FirebaseAuth.getInstance();
        calendar = Calendar.getInstance();

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
                            Glide.with(this)
                                    .load(selectedImageUri)
                                    .circleCrop()
                                    .placeholder(R.drawable.baseline_person_24)
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
        toolbar = findViewById(R.id.toolbar);
        profileAvatar = findViewById(R.id.profileAvatar);
        editAvatarIcon = findViewById(R.id.editAvatarIcon);

        lastNameInput = findViewById(R.id.lastNameInput);
        firstNameInput = findViewById(R.id.firstNameInput);
        birthdayInput = findViewById(R.id.birthdayInput);
        phoneInput = findViewById(R.id.phoneInput);

        genderGroup = findViewById(R.id.genderGroup);
        maleRadio = findViewById(R.id.maleRadio);
        femaleRadio = findViewById(R.id.femaleRadio);

        updateButton = findViewById(R.id.updateButton);
        progressBar = findViewById(R.id.progressBar);

        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Thông tin cá nhân");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void loadUserData() {
        FirebaseUser firebaseUser = mAuth.getCurrentUser();

        if (firebaseUser != null) {
            String userId = firebaseUser.getUid();

            // Load profile photo
            Uri photoUrl = firebaseUser.getPhotoUrl();
            if (photoUrl != null) {
                Glide.with(this)
                        .load(photoUrl)
                        .circleCrop()
                        .placeholder(R.drawable.baseline_person_24)
                        .into(profileAvatar);
            }

            // Load from Firestore
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

                    if (gender != null) {
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
                    Toast.makeText(PersonalInfoActivity.this, "Lỗi tải dữ liệu: " + error, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void setupClickListeners() {
        profileAvatar.setOnClickListener(v -> openImagePicker());
        editAvatarIcon.setOnClickListener(v -> openImagePicker());

        birthdayInput.setOnClickListener(v -> showDatePicker());

        updateButton.setOnClickListener(v -> updateProfile());
    }

    private void showDatePicker() {
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    calendar.set(selectedYear, selectedMonth, selectedDay);
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    birthdayInput.setText(sdf.format(calendar.getTime()));
                },
                year, month, day);
        datePickerDialog.show();
    }

    private void updateProfile() {
        String lastName = lastNameInput.getText().toString().trim();
        String firstName = firstNameInput.getText().toString().trim();
        String birthday = birthdayInput.getText().toString().trim();
        String phone = phoneInput.getText().toString().trim();

        if (lastName.isEmpty() || firstName.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ họ tên", Toast.LENGTH_SHORT).show();
            return;
        }

        String fullName = lastName + " " + firstName;
        int selectedGenderId = genderGroup.getCheckedRadioButtonId();
        String gender = "";
        if (selectedGenderId == R.id.maleRadio) {
            gender = "Nam";
        } else if (selectedGenderId == R.id.femaleRadio) {
            gender = "Nữ";
        }

        progressBar.setVisibility(View.VISIBLE);
        updateButton.setEnabled(false);

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            if (isImageChanged && selectedImageUri != null) {
                uploadImageAndUpdateProfile(currentUser, fullName, birthday, gender, phone);
            } else {
                updateFirebaseProfile(currentUser, fullName, birthday, gender, phone, null);
            }
        }
    }

    private void uploadImageAndUpdateProfile(FirebaseUser user, String fullName, String birthday, String gender,
            String phone) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        String fileName = "profile_images/" + UUID.randomUUID().toString() + ".jpg";
        StorageReference imageRef = storageRef.child(fileName);

        imageRef.putFile(selectedImageUri)
                .addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl()
                        .addOnSuccessListener(uri -> {
                            updateFirebaseProfile(user, fullName, birthday, gender, phone, uri.toString());
                        }))
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    updateButton.setEnabled(true);
                    Toast.makeText(this, "Lỗi tải ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void updateFirebaseProfile(FirebaseUser user, String fullName, String birthday, String gender, String phone,
            String photoUrl) {
        UserProfileChangeRequest.Builder profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(fullName);

        if (photoUrl != null) {
            profileUpdates.setPhotoUri(Uri.parse(photoUrl));
        }

        user.updateProfile(profileUpdates.build())
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        saveToFirestore(user.getUid(), fullName, birthday, gender, phone);
                    } else {
                        progressBar.setVisibility(View.GONE);
                        updateButton.setEnabled(true);
                        Toast.makeText(this, "Cập nhật thất bại", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveToFirestore(String userId, String name, String birthday, String gender, String phone) {
        firestoreManager.saveUserProfile(userId, name, birthday, gender, phone,
                new FirestoreManager.OnUserProfileSavedListener() {
                    @Override
                    public void onProfileSaved() {
                        progressBar.setVisibility(View.GONE);
                        updateButton.setEnabled(true);
                        isImageChanged = false;
                        Toast.makeText(PersonalInfoActivity.this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                        finish();
                    }

                    @Override
                    public void onError(String error) {
                        progressBar.setVisibility(View.GONE);
                        updateButton.setEnabled(true);
                        Toast.makeText(PersonalInfoActivity.this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
