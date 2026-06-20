package com.teatrack_mcd_253eie502802_group02.client;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.teatrack_mcd_253eie502802_group02.R;
import com.teatrack_mcd_253eie502802_group02.model.User;
import com.teatrack_mcd_253eie502802_group02.shared.BaseActivity;

// Các thư viện Credential Manager mới
import androidx.credentials.Credential;
import androidx.credentials.CredentialManager;
import androidx.credentials.CustomCredential;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialException;
import androidx.credentials.CredentialManagerCallback;
import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends BaseActivity {

    private static final String TAG = "LoginActivity_Log";
    private static final String DATABASE_URL = "https://teatrack-htng-default-rtdb.asia-southeast1.firebasedatabase.app";

    // Khai báo thêm các biến phục vụ đăng nhập Google
    private FirebaseAuth mAuth;
    private CredentialManager credentialManager;
    private DatabaseReference databaseReference;

    private TextInputEditText edtLoginName, edtPassword;
    private TextInputLayout tilLoginName, tilPassword;
    private MaterialButton btnLogIn, btnGoogleSignIn; // Thêm biến cho nút Google
    private TextView tvErrorMessage, tvSignUp, tvForgotPassword;
    private CheckBox cbRemember;
    private SharedPreferences sharedPreferences;

    private static final String PREF_NAME = "LoginPrefs";
    private static final String KEY_REMEMBER = "remember";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_USER_ID = "userId";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: Activity started");
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        // Khởi tạo Firebase Auth và Credential Manager
        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance(DATABASE_URL).getReference("Users");
        credentialManager = CredentialManager.create(this);

        View mainView = findViewById(R.id.main);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        initViews();
        setupListeners();

        if (btnLogIn != null) {
            btnLogIn.post(() -> {
                btnLogIn.setOnClickListener(v -> {
                    Log.d(TAG, "Log In Button Clicked (Post)!");
                    handleLogin();
                });
            });
        }
    }

    private void initViews() {
        Log.d(TAG, "initViews: Mapping views");
        tilLoginName = findViewById(R.id.tilLoginName);
        tilPassword = findViewById(R.id.tilPassword);
        edtLoginName = findViewById(R.id.edtLoginName);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogIn = findViewById(R.id.btnLogIn);
        btnGoogleSignIn = findViewById(R.id.btnGoogle); // Ánh xạ nút Google từ XML
        tvErrorMessage = findViewById(R.id.tvErrorMessage);
        tvSignUp = findViewById(R.id.tvSignUp);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        cbRemember = findViewById(R.id.cbRemember);

        sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        loadRememberedData();
    }

    private void setupListeners() {
        if (btnLogIn != null) {
            btnLogIn.setOnClickListener(v -> {
                Log.d(TAG, "Log In Button Clicked!");
                handleLogin();
            });
        } else {
            Log.e(TAG, "btnLogIn is NULL. Check activity_login.xml");
        }

        // Đăng ký sự kiện click cho nút Google Sign-In
        if (btnGoogleSignIn != null) {
            btnGoogleSignIn.setOnClickListener(v -> {
                Log.d(TAG, "Google Sign In Clicked");
                signInWithGoogle();
            });
        }

        if (tvSignUp != null) {
            tvSignUp.setOnClickListener(v -> {
                Log.d(TAG, "Sign Up Clicked");
                startActivity(new Intent(this, RegisterActivity.class));
            });
        }

        TextWatcher clearErrorWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { hideError(); }
            @Override public void afterTextChanged(Editable s) {}
        };

        if (edtLoginName != null) edtLoginName.addTextChangedListener(clearErrorWatcher);
        if (edtPassword != null) edtPassword.addTextChangedListener(clearErrorWatcher);
    }

    // --- LOGIC ĐĂNG NHẬP GOOGLE CỦA BẠN ĐÃ ĐƯỢC TÍCH HỢP ---

    private void signInWithGoogle() {
        GetGoogleIdOption googleIdOption = new GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setAutoSelectEnabled(false)
                .setServerClientId(getString(R.string.default_web_client_id))
                .build();

        GetCredentialRequest request = new GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .setPreferImmediatelyAvailableCredentials(false)
                .build();

        credentialManager.getCredentialAsync(
                this,
                request,
                new CancellationSignal(),
                ContextCompat.getMainExecutor(this),
                new CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
                    @Override
                    public void onResult(GetCredentialResponse result) {
                        handleSignIn(result.getCredential());
                    }

                    @Override
                    public void onError(GetCredentialException e) {
                        Log.e(TAG, "Lỗi đăng nhập Google", e);
                        Toast.makeText(LoginActivity.this,
                                "Đăng nhập Google không thành công", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void handleSignIn(Credential credential) {
        if (credential instanceof CustomCredential
                && credential.getType().equals(GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL)) {
            Bundle credentialData = ((CustomCredential) credential).getData();
            GoogleIdTokenCredential googleIdTokenCredential =
                    GoogleIdTokenCredential.createFrom(credentialData);
            firebaseAuthWithGoogle(googleIdTokenCredential.getIdToken());
        } else {
            Log.w(TAG, "Credential không phải Google ID Token");
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        saveUserToDatabase(user);
                    } else {
                        Log.w(TAG, "signInWithCredential:failure", task.getException());
                        Toast.makeText(this, "Đăng nhập thất bại", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveUserToDatabase(FirebaseUser user) {
        if (user == null) return;
        String uid = user.getUid();

        databaseReference.child(uid).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && !task.getResult().exists()) {
                Map<String, Object> userMap = new HashMap<>();
                userMap.put("id", uid); // Nên lưu thêm ID của user luôn nhé
                userMap.put("name", user.getDisplayName());
                userMap.put("email", user.getEmail());
                userMap.put("provider", "google");
                databaseReference.child(uid).setValue(userMap);
            }
            // Điều hướng về Homepage giống luồng đăng nhập thường của bạn
            startActivity(new Intent(LoginActivity.this, Homepage.class));
            finish();
        });
    }

    // --- GIỮ NGUYÊN LOGIC ĐĂNG NHẬP THƯỜNG Ở DƯỚI ---

    private void handleLogin() {
        if (edtLoginName == null || edtPassword == null) {
            Log.e(TAG, "EditTexts are null");
            return;
        }

        String loginName = edtLoginName.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        hideError();

        if (TextUtils.isEmpty(loginName) || TextUtils.isEmpty(password)) {
            Log.w(TAG, "Empty fields");
            if (TextUtils.isEmpty(loginName)) {
                tilLoginName.setErrorEnabled(true);
                tilLoginName.setError("!");
            }
            if (TextUtils.isEmpty(password)) {
                tilPassword.setErrorEnabled(true);
                tilPassword.setError("!");
            }
            showError(getString(R.string.login_error_empty));
            return;
        }

        Log.d(TAG, "Attempting login for: " + loginName);
        btnLogIn.setEnabled(false);

        Handler timeoutHandler = new Handler(Looper.getMainLooper());
        Runnable timeoutRunnable = () -> {
            if (!btnLogIn.isEnabled()) {
                btnLogIn.setEnabled(true);
                showError("Kết nối quá lâu, vui lòng kiểm tra mạng!");
                Log.e(TAG, "Login timeout reached");
            }
        };
        timeoutHandler.postDelayed(timeoutRunnable, 10000);

        String hashedPassword = hashPassword(password);

        try {
            FirebaseDatabase db = FirebaseDatabase.getInstance(DATABASE_URL);
            DatabaseReference usersRef = db.getReference("Users");

            usersRef.orderByChild("name").equalTo(loginName).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    timeoutHandler.removeCallbacks(timeoutRunnable);
                    btnLogIn.setEnabled(true);

                    if (!snapshot.exists()) {
                        showError(getString(R.string.login_error_invalid));
                        tilLoginName.setError("!");
                        tilPassword.setError("!");
                        return;
                    }

                    boolean loginSuccess = false;
                    String userId = null;

                    for (DataSnapshot userSnap : snapshot.getChildren()) {
                        User user = userSnap.getValue(User.class);
                        if (user != null && user.getPassword() != null) {
                            if (user.getPassword().equals(hashedPassword)) {
                                loginSuccess = true;
                                userId = user.getId();
                                break;
                            }
                        }
                    }

                    if (loginSuccess) {
                        saveLoginData(loginName, password, userId);
                        startActivity(new Intent(LoginActivity.this, Homepage.class));
                        finish();
                    } else {
                        showError(getString(R.string.login_error_invalid));
                        tilLoginName.setErrorEnabled(true);
                        tilLoginName.setError("!");
                        tilPassword.setErrorEnabled(true);
                        tilPassword.setError("!");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    timeoutHandler.removeCallbacks(timeoutRunnable);
                    btnLogIn.setEnabled(true);
                    showError("Lỗi kết nối Firebase: " + error.getMessage());
                }
            });
        } catch (Exception e) {
            btnLogIn.setEnabled(true);
            showError("Lỗi khởi tạo hệ thống.");
        }
    }

    private void loadRememberedData() {
        if (sharedPreferences.getBoolean(KEY_REMEMBER, false)) {
            if (edtLoginName != null) edtLoginName.setText(sharedPreferences.getString(KEY_USERNAME, ""));
            if (edtPassword != null) edtPassword.setText(sharedPreferences.getString(KEY_PASSWORD, ""));
            if (cbRemember != null) cbRemember.setChecked(true);
        }
    }

    private void saveLoginData(String username, String password, String userId) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (cbRemember != null && cbRemember.isChecked()) {
            editor.putBoolean(KEY_REMEMBER, true);
            editor.putString(KEY_USERNAME, username);
            editor.putString(KEY_PASSWORD, password);
        } else {
            editor.putBoolean(KEY_REMEMBER, false);
            editor.remove(KEY_USERNAME);
            editor.remove(KEY_PASSWORD);
        }
        editor.putString(KEY_USER_ID, userId);
        editor.apply();
    }

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) { return password; }
    }

    private void showError(String message) {
        if (tvErrorMessage != null) {
            tvErrorMessage.setText(message);
            tvErrorMessage.setVisibility(View.VISIBLE);
            tvErrorMessage.bringToFront();
        }
    }

    private void hideError() {
        if (tvErrorMessage != null) tvErrorMessage.setVisibility(View.GONE);
        if (tilLoginName != null) {
            tilLoginName.setError(null);
            tilLoginName.setErrorEnabled(false);
        }
        if (tilPassword != null) {
            tilPassword.setError(null);
            tilPassword.setErrorEnabled(false);
        }
    }
}