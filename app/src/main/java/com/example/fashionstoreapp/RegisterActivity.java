package com.example.fashionstoreapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.fashionstoreapp.models.User;
import com.example.fashionstoreapp.utils.FirestoreManager;
import com.example.fashionstoreapp.utils.SessionManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";

    private EditText etEmail, etPassword, etConfirmPassword, etFullName;
    private CheckBox cbTerms;
    private Button btnRegister;
    private TextView tvLogin;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;
    private FirestoreManager firestoreManager;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        firestoreManager = FirestoreManager.getInstance();
        sessionManager = new SessionManager(this);

        initViews();
        setupClickListeners();
    }

    private void initViews() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        etFullName = findViewById(R.id.etFullName);
        cbTerms = findViewById(R.id.cbTerms);
        btnRegister = findViewById(R.id.btnRegister);
        tvLogin = findViewById(R.id.tvLogin);
        progressBar = findViewById(R.id.progressBar);

        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }
    }

    private void setupClickListeners() {
        btnRegister.setOnClickListener(v -> registerWithEmail());
        tvLogin.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    private void registerWithEmail() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString();
        String confirmPassword = etConfirmPassword.getText().toString();
        String fullName = etFullName.getText().toString().trim();

        // Validation
        if (!validateInput(email, password, confirmPassword, fullName)) {
            return;
        }

        showLoading();

        // Create user with email and password
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // Update display name
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(fullName)
                                    .build();

                            user.updateProfile(profileUpdates)
                                    .addOnCompleteListener(updateTask -> {
                                        if (updateTask.isSuccessful()) {
                                            // Save to Firestore then send verification email
                                            saveUserToFirestore(user, fullName, email);
                                        } else {
                                            hideLoading();
                                            Toast.makeText(this, "Cập nhật thông tin thất bại",
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    } else {
                        hideLoading();
                        String errorMessage = "Đăng ký thất bại";
                        if (task.getException() != null) {
                            String exceptionMessage = task.getException().getMessage();
                            if (exceptionMessage != null) {
                                if (exceptionMessage.contains("email address is already in use")) {
                                    errorMessage = "Email này đã được đăng ký";
                                } else if (exceptionMessage.contains("network error")) {
                                    errorMessage = "Lỗi kết nối mạng";
                                } else {
                                    errorMessage = "Đăng ký thất bại: " + exceptionMessage;
                                }
                            }
                        }
                        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void saveUserToFirestore(FirebaseUser firebaseUser, String fullName, String email) {
        String userId = firebaseUser.getUid();

        firestoreManager.saveUserProfile(userId, fullName, "", "", "",
                new FirestoreManager.OnUserProfileSavedListener() {
                    @Override
                    public void onProfileSaved() {
                        // Now send verification email
                        sendVerificationEmail(firebaseUser, email);
                    }

                    @Override
                    public void onError(String error) {
                        hideLoading();
                        Toast.makeText(RegisterActivity.this,
                                "Lỗi lưu thông tin: " + error, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void sendVerificationEmail(FirebaseUser user, String email) {
        user.sendEmailVerification()
                .addOnCompleteListener(task -> {
                    hideLoading();
                    if (task.isSuccessful()) {
                        showSuccessDialog(email);
                    } else {
                        Toast.makeText(this,
                                "Không thể gửi email xác thực. Vui lòng kiểm tra lại.",
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void showSuccessDialog(String email) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("✅ Đăng ký thành công!");
        builder.setMessage(
                "Một email xác thực đã được gửi đến:\n\n" + email + "\n\n" +
                        "Vui lòng kiểm tra hộp thư và click vào link xác thực để kích hoạt tài khoản.\n\n" +
                        "Lưu ý: Kiểm tra cả thư mục Spam nếu không thấy email.");
        builder.setPositiveButton("Đã hiểu", (dialog, which) -> {
            dialog.dismiss();
            mAuth.signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
        builder.setCancelable(false);
        builder.show();
    }

    private boolean validateInput(String email, String password, String confirmPassword, String fullName) {
        // Check empty fields
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Vui lòng nhập email");
            etEmail.requestFocus();
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Email không hợp lệ");
            etEmail.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(fullName)) {
            etFullName.setError("Vui lòng nhập họ tên");
            etFullName.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Vui lòng nhập mật khẩu");
            etPassword.requestFocus();
            return false;
        }

        if (password.length() < 6) {
            etPassword.setError("Mật khẩu phải có ít nhất 6 ký tự");
            etPassword.requestFocus();
            return false;
        }

        // Check password strength
        if (!isPasswordStrong(password)) {
            etPassword.setError("Mật khẩu phải chứa chữ hoa, chữ thường và số");
            etPassword.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            etConfirmPassword.setError("Vui lòng xác nhận mật khẩu");
            etConfirmPassword.requestFocus();
            return false;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Mật khẩu không khớp");
            etConfirmPassword.requestFocus();
            return false;
        }

        if (!cbTerms.isChecked()) {
            Toast.makeText(this, "Vui lòng đồng ý với điều khoản sử dụng",
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private boolean isPasswordStrong(String password) {
        boolean hasUpperCase = false;
        boolean hasLowerCase = false;
        boolean hasDigit = false;

        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c))
                hasUpperCase = true;
            if (Character.isLowerCase(c))
                hasLowerCase = true;
            if (Character.isDigit(c))
                hasDigit = true;
        }

        return hasUpperCase && hasLowerCase && hasDigit;
    }

    private void showLoading() {
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
        btnRegister.setEnabled(false);
    }

    private void hideLoading() {
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }
        btnRegister.setEnabled(true);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Check if user is already signed in and email verified
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null && currentUser.isEmailVerified()) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
