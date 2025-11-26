package com.example.fashionstoreapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

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
    private Handler timeoutHandler;
    private Runnable timeoutRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        firestoreManager = FirestoreManager.getInstance();
        sessionManager = new SessionManager(this);
        timeoutHandler = new Handler(Looper.getMainLooper());

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

        // Timeout handler - 15 giây
        timeoutRunnable = () -> {
            hideLoading();
            Toast.makeText(RegisterActivity.this,
                    "Đăng ký mất quá nhiều thời gian. Vui lòng kiểm tra kết nối mạng và thử lại.",
                    Toast.LENGTH_LONG).show();
        };
        timeoutHandler.postDelayed(timeoutRunnable, 15000); // 15 seconds

        // Hiển thị tiến trình
        runOnUiThread(() -> {
            if (progressBar != null) {
                // Có thể thêm message "Đang tạo tài khoản..."
            }
        });

        // Create user with email and password
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    // Hủy timeout
                    timeoutHandler.removeCallbacks(timeoutRunnable);

                    if (task.isSuccessful()) {
                        FirebaseUser user = null;
                        if (task.getResult() != null) {
                            user = task.getResult().getUser();
                        }
                        if (user == null) {
                            // Fallback to currentUser (rare cases)
                            user = mAuth.getCurrentUser();
                        }

                        if (user != null) {
                            // Thực hiện song song updateProfile và saveToFirestore để nhanh hơn
                            String userId = user.getUid();
                            String passwordHash = hashPassword(password);

                            // Update display name (không chờ kết quả)
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(fullName)
                                    .build();
                            user.updateProfile(profileUpdates);

                            // Use an AuthStateListener to ensure FirebaseAuth state and token are
                            // fully established before attempting a Firestore write. This is
                            // more robust than a single getIdToken() call immediately after
                            // createUserWithEmailAndPassword which can still see an unauthenticated
                            // state and produce PERMISSION_DENIED.
                            final FirebaseAuth.AuthStateListener[] listenerHolder = new FirebaseAuth.AuthStateListener[1];

                            FirebaseAuth.AuthStateListener authListener = new FirebaseAuth.AuthStateListener() {
                                @Override
                                public void onAuthStateChanged(FirebaseAuth firebaseAuth) {
                                    FirebaseUser current = firebaseAuth.getCurrentUser();
                                    if (current != null && current.getUid().equals(userId)) {
                                        // Try to get a fresh token and then write
                                        current.getIdToken(true).addOnCompleteListener(tt -> {
                                            if (tt.isSuccessful()) {
                                                boolean tokenPresent = tt.getResult() != null
                                                        && tt.getResult().getToken() != null
                                                        && !tt.getResult().getToken().isEmpty();
                                                Log.d(TAG, "AuthStateListener: current UID=" + userId
                                                        + ", tokenPresent=" + tokenPresent);

                                                firestoreManager.saveUserProfileWithEmail(userId, fullName, email,
                                                        passwordHash, "", "", "",
                                                        new FirestoreManager.OnUserProfileSavedListener() {
                                                            @Override
                                                            public void onProfileSaved() {
                                                                // cleanup listener
                                                                if (listenerHolder[0] != null)
                                                                    mAuth.removeAuthStateListener(listenerHolder[0]);
                                                                hideLoading();
                                                                showSuccessDialogWithoutVerification(email);
                                                            }

                                                            @Override
                                                            public void onError(String error) {
                                                                if (listenerHolder[0] != null)
                                                                    mAuth.removeAuthStateListener(listenerHolder[0]);
                                                                hideLoading();
                                                                Toast.makeText(RegisterActivity.this,
                                                                        "Lỗi lưu thông tin: " + error,
                                                                        Toast.LENGTH_SHORT).show();
                                                            }
                                                        });
                                            }
                                        });
                                    }
                                }
                            };

                            listenerHolder[0] = authListener;
                            mAuth.addAuthStateListener(authListener);

                            // Safety timeout: if auth state / token not ready in 10s, remove listener
                            // and show an error to the user.
                            timeoutHandler.postDelayed(() -> {
                                if (listenerHolder[0] != null) {
                                    mAuth.removeAuthStateListener(listenerHolder[0]);
                                    hideLoading();
                                    Toast.makeText(RegisterActivity.this,
                                            "Đăng ký thất bại: không thể xác thực người dùng. Vui lòng thử lại.",
                                            Toast.LENGTH_LONG).show();
                                }
                            }, 10000);
                        } else {
                            // Very unlikely: user still null even after success; wait briefly then try to
                            // fetch currentUser
                            new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                                FirebaseUser fallback = mAuth.getCurrentUser();
                                if (fallback != null) {
                                    String userId = fallback.getUid();
                                    String passwordHash = hashPassword(password);

                                    // Ensure token is ready before writing
                                    fallback.getIdToken(true).addOnCompleteListener(tt -> {
                                        if (tt.isSuccessful()) {
                                            firestoreManager.saveUserProfileWithEmail(userId, fullName, email,
                                                    passwordHash, "",
                                                    "", "",
                                                    new FirestoreManager.OnUserProfileSavedListener() {
                                                        @Override
                                                        public void onProfileSaved() {
                                                            hideLoading();
                                                            showSuccessDialogWithoutVerification(email);
                                                        }

                                                        @Override
                                                        public void onError(String error) {
                                                            hideLoading();
                                                            Toast.makeText(RegisterActivity.this,
                                                                    "Lỗi lưu thông tin: " + error, Toast.LENGTH_SHORT)
                                                                    .show();
                                                        }
                                                    });
                                        } else {
                                            hideLoading();
                                            Toast.makeText(RegisterActivity.this,
                                                    "Tạo tài khoản thất bại: không thể xác thực người dùng để lưu dữ liệu.",
                                                    Toast.LENGTH_LONG).show();
                                        }
                                    });
                                } else {
                                    hideLoading();
                                    Toast.makeText(RegisterActivity.this,
                                            "Tạo tài khoản thất bại: không thể xác thực người dùng để lưu dữ liệu.",
                                            Toast.LENGTH_LONG).show();
                                }
                            }, 1000);
                        }
                    } else {
                        hideLoading();
                        String errorMessage = "Đăng ký thất bại";
                        if (task.getException() != null) {
                            String exceptionMessage = task.getException().getMessage();
                            if (exceptionMessage != null) {
                                if (exceptionMessage.contains("email address is already in use")) {
                                    errorMessage = "Email này đã được đăng ký";
                                } else if (exceptionMessage.contains("network") ||
                                        exceptionMessage.contains("timeout") ||
                                        exceptionMessage.contains("connection")) {
                                    errorMessage = "Lỗi kết nối mạng. Vui lòng kiểm tra Internet và thử lại.";
                                } else if (exceptionMessage.contains("weak password")) {
                                    errorMessage = "Mật khẩu quá yếu. Vui lòng nhập ít nhất 6 ký tự.";
                                } else {
                                    errorMessage = "Đăng ký thất bại: " + exceptionMessage;
                                }
                            }
                        }
                        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
    }

    /**
     * Hash password using SHA-256
     */
    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1)
                    hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "Error hashing password", e);
            return "";
        }
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

    private void showSuccessDialogWithoutVerification(String email) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("✅ Đăng ký thành công!");
        builder.setMessage(
                "Tài khoản của bạn đã được tạo thành công!\n\n" +
                        "Email: " + email + "\n\n" +
                        "Bạn có thể đăng nhập ngay bây giờ.");
        builder.setPositiveButton("Đăng nhập ngay", (dialog, which) -> {
            dialog.dismiss();
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
