package com.example.fashionstoreapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.fashionstoreapp.models.User;
import com.example.fashionstoreapp.utils.FirestoreManager;
import com.example.fashionstoreapp.utils.SessionManager;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.Arrays;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    private ImageView backButton;
    private TextInputEditText emailInput, passwordInput;
    private Button btnLogin, btnLoginGoogle, btnLoginFacebook;
    private TextView forgotPasswordText, registerText;
    private ProgressBar progressBar;

    private SessionManager sessionManager;

    // Firebase
    private FirebaseAuth mAuth;

    // Google Sign In
    private GoogleSignInClient mGoogleSignInClient;
    private ActivityResultLauncher<Intent> googleSignInLauncher;

    // Facebook Sign In
    private CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        sessionManager = new SessionManager(this);

        // Check if already logged in
        if (sessionManager.isLoggedIn() || mAuth.getCurrentUser() != null) {
            navigateToMain();
            return;
        }

        // Configure Google Sign In
        configureGoogleSignIn();

        // Setup Activity Result Launcher for Google Sign In
        setupGoogleSignInLauncher();

        initViews();
        setupClickListeners();
    }

    private void configureGoogleSignIn() {
        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void configureFacebookSignIn() {
        // Initialize Facebook SDK callback manager
        callbackManager = CallbackManager.Factory.create();

        // Register Facebook callback
        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        Log.d(TAG, "facebook:onSuccess:" + loginResult);
                        handleFacebookAccessToken(loginResult.getAccessToken());
                    }

                    @Override
                    public void onCancel() {
                        Log.d(TAG, "facebook:onCancel");
                        hideLoading();
                        Toast.makeText(LoginActivity.this, "Đăng nhập Facebook đã bị hủy",
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(FacebookException error) {
                        Log.d(TAG, "facebook:onError", error);
                        hideLoading();
                        Toast.makeText(LoginActivity.this, "Lỗi đăng nhập Facebook: " + error.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setupGoogleSignInLauncher() {
        googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                        try {
                            // Google Sign In was successful, authenticate with Firebase
                            GoogleSignInAccount account = task.getResult(ApiException.class);
                            Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());
                            firebaseAuthWithGoogle(account.getIdToken());
                        } catch (ApiException e) {
                            // Google Sign In failed
                            Log.w(TAG, "Google sign in failed", e);
                            hideLoading();
                            Toast.makeText(this, "Đăng nhập Google thất bại: " + e.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        hideLoading();
                    }
                });
    }

    private void initViews() {
        backButton = findViewById(R.id.backButton);
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        btnLogin = findViewById(R.id.btnLogin);
        btnLoginGoogle = findViewById(R.id.btnLoginGoogle);
        btnLoginFacebook = findViewById(R.id.btnLoginFacebook);
        forgotPasswordText = findViewById(R.id.forgotPasswordText);
        registerText = findViewById(R.id.registerText);

        // Add ProgressBar if not in layout
        progressBar = findViewById(R.id.loginProgressBar);
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }
    }

    private void setupClickListeners() {
        backButton.setOnClickListener(v -> finish());

        btnLogin.setOnClickListener(v -> performEmailPasswordLogin());

        btnLoginGoogle.setOnClickListener(v -> signInWithGoogle());

        btnLoginFacebook.setOnClickListener(v -> {
            signInWithFacebook();
        });

        forgotPasswordText.setOnClickListener(v -> {
            // Navigate to forgot password activity
            Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
            startActivity(intent);
        });

        registerText.setOnClickListener(v -> {
            // Navigate to register activity
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void signInWithGoogle() {
        showLoading();
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        googleSignInLauncher.launch(signInIntent);
    }

    private void signInWithFacebook() {
        // Lazy initialize Facebook SDK only when needed
        if (callbackManager == null) {
            FacebookSdk.sdkInitialize(getApplicationContext());
            configureFacebookSignIn();
        }
        
        showLoading();
        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("email", "public_profile"));
    }

    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);
        showLoading();

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success
                        Log.d(TAG, "signInWithCredential:success");
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            saveUserToSession(firebaseUser);
                            Toast.makeText(this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                            navigateToMain();
                        }
                    } else {
                        // Sign in fails
                        Log.w(TAG, "signInWithCredential:failure", task.getException());
                        hideLoading();
                        Toast.makeText(this, "Xác thực thất bại: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void firebaseAuthWithGoogle(String idToken) {
        showLoading();
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success
                        Log.d(TAG, "signInWithCredential:success");
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            saveUserToSession(firebaseUser);
                            Toast.makeText(this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                            navigateToMain();
                        }
                    } else {
                        // Sign in fails
                        Log.w(TAG, "signInWithCredential:failure", task.getException());
                        hideLoading();
                        Toast.makeText(this, "Xác thực thất bại: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveUserToSession(FirebaseUser firebaseUser) {
        // Create user object from Firebase user
        User user = new User();
        user.setId(firebaseUser.getUid());
        user.setEmail(firebaseUser.getEmail());
        user.setName(firebaseUser.getDisplayName());
        user.setPhone(firebaseUser.getPhoneNumber());
        if (firebaseUser.getPhotoUrl() != null) {
            user.setProfileImageUrl(firebaseUser.getPhotoUrl().toString());
        }
        user.setLastLoginAt(System.currentTimeMillis());

        // Save to session
        sessionManager.createLoginSession(user);
    }

    private void performEmailPasswordLogin() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        // Validate inputs
        if (TextUtils.isEmpty(email)) {
            emailInput.setError("Vui lòng nhập email");
            emailInput.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            passwordInput.setError("Vui lòng nhập mật khẩu");
            passwordInput.requestFocus();
            return;
        }

        if (password.length() < 6) {
            passwordInput.setError("Mật khẩu phải có ít nhất 6 ký tự");
            passwordInput.requestFocus();
            return;
        }

        // Show loading
        showLoading();

        // Sign in with Firebase
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success
                        Log.d(TAG, "signInWithEmail:success");
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            saveUserToSession(firebaseUser);
                            Toast.makeText(this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                            navigateToMain();
                        }
                    } else {
                        // Sign in fails
                        Log.w(TAG, "signInWithEmail:failure", task.getException());
                        hideLoading();
                        String errorMessage = "Đăng nhập thất bại";
                        if (task.getException() != null) {
                            errorMessage = task.getException().getMessage();
                        }
                        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Pass the activity result back to the Facebook SDK
        if (callbackManager != null) {
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void showLoading() {
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
        btnLogin.setEnabled(false);
        btnLoginGoogle.setEnabled(false);
        btnLoginFacebook.setEnabled(false);
        btnLogin.setText("Đang đăng nhập...");
    }

    private void hideLoading() {
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }
        btnLogin.setEnabled(true);
        btnLoginGoogle.setEnabled(true);
        btnLoginFacebook.setEnabled(true);
        btnLogin.setText("ĐĂNG NHẬP");
    }

    private void navigateToMain() {
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser != null) {
            // Check user role and navigate accordingly with timeout
            final boolean[] roleLoaded = {false};
            
            // Set timeout - if role check takes too long, default to customer
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (!roleLoaded[0]) {
                    // Timeout - default to customer
                    Log.d(TAG, "Role check timeout, defaulting to customer");
                    navigateToActivity(MainActivity.class);
                }
            }, 2000); // 2 second timeout
            
            FirestoreManager.getInstance().loadUserRole(firebaseUser.getUid(), role -> {
                if (!roleLoaded[0]) {
                    roleLoaded[0] = true;
                    Intent intent;
                    if (role != null && role.toLowerCase().contains("admin")) {
                        // User is admin, go to Admin Dashboard
                        intent = new Intent(LoginActivity.this, com.example.fashionstoreapp.admin.AdminActivity.class);
                    } else {
                        // User is customer, go to Main Activity
                        intent = new Intent(LoginActivity.this, MainActivity.class);
                    }
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }
            });
        } else {
            // Fallback to MainActivity if no user
            navigateToActivity(MainActivity.class);
        }
    }
    
    private void navigateToActivity(Class<?> activityClass) {
        Intent intent = new Intent(LoginActivity.this, activityClass);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
