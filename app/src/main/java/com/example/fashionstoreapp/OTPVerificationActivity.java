package com.example.fashionstoreapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

public class OTPVerificationActivity extends AppCompatActivity {

    private static final String TAG = "OTPVerification";

    private EditText otp1, otp2, otp3, otp4, otp5, otp6;
    private Button btnVerifyOTP;
    private TextView phoneNumberText, resendOTPText, backToRegisterText;
    private ProgressBar progressBar;

    private String verificationId;
    private String phoneNumber;

    private FirebaseAuth mAuth;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_verification);

        mAuth = FirebaseAuth.getInstance();
        sessionManager = new SessionManager(this);

        // Get data from intent
        verificationId = getIntent().getStringExtra("verificationId");
        phoneNumber = getIntent().getStringExtra("phoneNumber");

        initViews();
        setupOTPInputs();
        setupClickListeners();

        // Display phone number
        if (phoneNumberText != null && phoneNumber != null) {
            phoneNumberText.setText("Mã OTP đã được gửi đến " + maskPhoneNumber(phoneNumber));
        }
    }

    private void initViews() {
        otp1 = findViewById(R.id.otp1);
        otp2 = findViewById(R.id.otp2);
        otp3 = findViewById(R.id.otp3);
        otp4 = findViewById(R.id.otp4);
        otp5 = findViewById(R.id.otp5);
        otp6 = findViewById(R.id.otp6);

        btnVerifyOTP = findViewById(R.id.btnVerifyOTP);
        phoneNumberText = findViewById(R.id.phoneNumberText);
        resendOTPText = findViewById(R.id.resendOTPText);
        backToRegisterText = findViewById(R.id.backToRegisterText);
        progressBar = findViewById(R.id.otpProgressBar);

        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }
    }

    private void setupOTPInputs() {
        // Auto move to next input
        otp1.addTextChangedListener(new OTPTextWatcher(otp1, otp2));
        otp2.addTextChangedListener(new OTPTextWatcher(otp2, otp3));
        otp3.addTextChangedListener(new OTPTextWatcher(otp3, otp4));
        otp4.addTextChangedListener(new OTPTextWatcher(otp4, otp5));
        otp5.addTextChangedListener(new OTPTextWatcher(otp5, otp6));
        otp6.addTextChangedListener(new OTPTextWatcher(otp6, null));

        // Request focus on first input
        otp1.requestFocus();
    }

    private void setupClickListeners() {
        btnVerifyOTP.setOnClickListener(v -> verifyOTP());

        resendOTPText.setOnClickListener(v -> {
            Toast.makeText(this, "Đang gửi lại mã OTP...", Toast.LENGTH_SHORT).show();
            // Go back to register to resend
            finish();
        });

        backToRegisterText.setOnClickListener(v -> finish());
    }

    private void verifyOTP() {
        String code = otp1.getText().toString() +
                otp2.getText().toString() +
                otp3.getText().toString() +
                otp4.getText().toString() +
                otp5.getText().toString() +
                otp6.getText().toString();

        if (TextUtils.isEmpty(code) || code.length() < 6) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ mã OTP", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(verificationId)) {
            Toast.makeText(this, "Lỗi: Không tìm thấy mã xác thực", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        showLoading();

        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success
                        Log.d(TAG, "signInWithCredential:success");

                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            saveUserToSession(firebaseUser);
                            Toast.makeText(this, "Xác thực thành công!", Toast.LENGTH_SHORT).show();
                            navigateToMain();
                        }
                    } else {
                        // Sign in failed
                        Log.w(TAG, "signInWithCredential:failure", task.getException());
                        hideLoading();
                        Toast.makeText(this, "Mã OTP không đúng. Vui lòng thử lại.",
                                Toast.LENGTH_LONG).show();
                        clearOTP();
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

    private void navigateToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void clearOTP() {
        otp1.setText("");
        otp2.setText("");
        otp3.setText("");
        otp4.setText("");
        otp5.setText("");
        otp6.setText("");
        otp1.requestFocus();
    }

    private String maskPhoneNumber(String phone) {
        if (phone == null || phone.length() < 4)
            return phone;
        int length = phone.length();
        return phone.substring(0, length - 4) + "****";
    }

    private void showLoading() {
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
        btnVerifyOTP.setEnabled(false);
        btnVerifyOTP.setText("Đang xác thực...");
    }

    private void hideLoading() {
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }
        btnVerifyOTP.setEnabled(true);
        btnVerifyOTP.setText("XÁC NHẬN");
    }

    // TextWatcher for OTP inputs
    private class OTPTextWatcher implements TextWatcher {
        private View currentView;
        private View nextView;

        public OTPTextWatcher(View currentView, View nextView) {
            this.currentView = currentView;
            this.nextView = nextView;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (s.length() == 1 && nextView != null) {
                nextView.requestFocus();
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
