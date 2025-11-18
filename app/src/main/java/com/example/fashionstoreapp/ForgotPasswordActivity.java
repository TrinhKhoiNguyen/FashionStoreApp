package com.example.fashionstoreapp;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {

    private static final String TAG = "ForgotPassword";

    private ImageView backButton;
    private TextInputEditText emailInput;
    private Button btnResetPassword;
    private TextView backToLoginText;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        initViews();
        setupClickListeners();

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Handle the back button event
                finish();
            }
        });
    }

    private void initViews() {
        backButton = findViewById(R.id.backButton);
        emailInput = findViewById(R.id.emailInput);
        btnResetPassword = findViewById(R.id.btnResetPassword);
        backToLoginText = findViewById(R.id.backToLoginText);
        progressBar = findViewById(R.id.progressBar);

        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }
    }

    private void setupClickListeners() {
        backButton.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        backToLoginText.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        btnResetPassword.setOnClickListener(v -> sendPasswordResetEmail());
    }

    private void sendPasswordResetEmail() {
        String email = emailInput.getText().toString().trim();

        // Validate email
        if (TextUtils.isEmpty(email)) {
            emailInput.setError("Vui lòng nhập email");
            emailInput.requestFocus();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.setError("Email không hợp lệ");
            emailInput.requestFocus();
            return;
        }

        // Show loading
        showLoading();

        // Send password reset email
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    hideLoading();

                    if (task.isSuccessful()) {
                        // Email sent successfully
                        android.util.Log.d(TAG, "Password reset email sent to: " + email);

                        // Show success dialog
                        showSuccessDialog(email);
                    } else {
                        // Failed to send email
                        android.util.Log.w(TAG, "Failed to send reset email", task.getException());

                        String errorMessage = "Không thể gửi email đặt lại mật khẩu";
                        if (task.getException() != null) {
                            String exceptionMessage = task.getException().getMessage();

                            // Handle specific error cases
                            if (exceptionMessage.contains("no user record")) {
                                errorMessage = "Email này chưa được đăng ký";
                            } else if (exceptionMessage.contains("invalid email")) {
                                errorMessage = "Email không hợp lệ";
                            } else if (exceptionMessage.contains("network")) {
                                errorMessage = "Lỗi kết nối. Vui lòng kiểm tra internet";
                            } else {
                                errorMessage = exceptionMessage;
                            }
                        }

                        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void showSuccessDialog(String email) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Gửi email thành công!");
        builder.setMessage("Chúng tôi đã gửi link đặt lại mật khẩu đến:\n\n" + email +
                "\n\nVui lòng kiểm tra email (kể cả thư mục spam) và làm theo hướng dẫn để đặt lại mật khẩu.");
        builder.setPositiveButton("Đã hiểu", (dialog, which) -> {
            dialog.dismiss();
            finish(); // Return to login screen
        });
        builder.setCancelable(false);

        androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showLoading() {
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
        btnResetPassword.setEnabled(false);
        btnResetPassword.setText("Đang gửi...");
    }

    private void hideLoading() {
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }
        btnResetPassword.setEnabled(true);
        btnResetPassword.setText("GỬI EMAIL ĐẶT LẠI MẬT KHẨU");
    }
}
