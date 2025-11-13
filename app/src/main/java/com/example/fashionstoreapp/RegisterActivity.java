package com.example.fashionstoreapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fashionstoreapp.models.User;
import com.example.fashionstoreapp.utils.SessionManager;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";

    private EditText phoneInput;
    private Button btnSendOTP;
    private TextView backToLoginText;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        sessionManager = new SessionManager(this);

        initViews();
        setupPhoneAuthCallbacks();
        setupClickListeners();
    }

    private void initViews() {
        phoneInput = findViewById(R.id.phoneInput);
        btnSendOTP = findViewById(R.id.btnSendOTP);
        backToLoginText = findViewById(R.id.backToLoginText);
        progressBar = findViewById(R.id.registerProgressBar);

        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }
    }

    private void setupPhoneAuthCallbacks() {
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                // verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                // detect the incoming verification SMS and perform verification without
                // user action.
                Log.d(TAG, "onVerificationCompleted:" + credential);
                hideLoading();
                signInWithPhoneAuthCredential(credential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                Log.w(TAG, "onVerificationFailed", e);
                hideLoading();
                Toast.makeText(RegisterActivity.this,
                        "Xác thực thất bại: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCodeSent(String verificationId,
                    PhoneAuthProvider.ForceResendingToken token) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                Log.d(TAG, "onCodeSent:" + verificationId);

                hideLoading();

                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId;
                mResendToken = token;

                Toast.makeText(RegisterActivity.this,
                        "Mã OTP đã được gửi đến số điện thoại của bạn", Toast.LENGTH_SHORT).show();

                // Navigate to OTP verification screen
                navigateToOTPVerification();
            }
        };
    }

    private void setupClickListeners() {
        btnSendOTP.setOnClickListener(v -> sendVerificationCode());

        backToLoginText.setOnClickListener(v -> finish());
    }

    private void sendVerificationCode() {
        String phoneNumber = phoneInput.getText().toString().trim();

        // Validate phone number
        if (TextUtils.isEmpty(phoneNumber)) {
            phoneInput.setError("Vui lòng nhập số điện thoại");
            phoneInput.requestFocus();
            return;
        }

        // Format phone number to international format
        if (!phoneNumber.startsWith("+")) {
            // Assume Vietnam phone number (+84)
            if (phoneNumber.startsWith("0")) {
                phoneNumber = "+84" + phoneNumber.substring(1);
            } else {
                phoneNumber = "+84" + phoneNumber;
            }
        }

        // Validate phone number format
        if (phoneNumber.length() < 10) {
            phoneInput.setError("Số điện thoại không hợp lệ");
            phoneInput.requestFocus();
            return;
        }

        showLoading();

        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(mAuth)
                .setPhoneNumber(phoneNumber) // Phone number to verify
                .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                .setActivity(this) // Activity (for callback binding)
                .setCallbacks(mCallbacks) // OnVerificationStateChangedCallbacks
                .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        showLoading();
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithCredential:success");

                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            saveUserToSession(firebaseUser);
                            Toast.makeText(this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
                            navigateToMain();
                        }
                    } else {
                        // Sign in failed, display a message and update the UI
                        Log.w(TAG, "signInWithCredential:failure", task.getException());
                        hideLoading();
                        Toast.makeText(this, "Xác thực thất bại: " +
                                task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void saveUserToSession(FirebaseUser firebaseUser) {
        User user = new User();
        user.setId(firebaseUser.getUid());
        user.setPhone(firebaseUser.getPhoneNumber());
        user.setName("Người dùng"); // Default name
        user.setLastLoginAt(System.currentTimeMillis());
        user.setCreatedAt(System.currentTimeMillis());

        sessionManager.createLoginSession(user);
    }

    private void navigateToOTPVerification() {
        Intent intent = new Intent(this, OTPVerificationActivity.class);
        intent.putExtra("verificationId", mVerificationId);
        intent.putExtra("phoneNumber", phoneInput.getText().toString().trim());
        startActivity(intent);
    }

    private void navigateToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void showLoading() {
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
        btnSendOTP.setEnabled(false);
        btnSendOTP.setText("Đang gửi...");
    }

    private void hideLoading() {
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }
        btnSendOTP.setEnabled(true);
        btnSendOTP.setText("GỬI MÃ XÁC NHẬN");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
