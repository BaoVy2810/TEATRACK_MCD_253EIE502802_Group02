package com.teatrack_mcd_253eie502802_group02.client;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.credentials.Credential;
import androidx.credentials.CredentialManager;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialException;

import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
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
import com.teatrack_mcd_253eie502802_group02.admin.AdminDashboard;
import com.teatrack_mcd_253eie502802_group02.shared.BaseActivity;
import com.teatrack_mcd_253eie502802_group02.util.UserIdGenerator;
import com.teatrack_mcd_253eie502802_group02.util.UserProfileHelper;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

public class LoginActivity extends BaseActivity {

    private static final String TAG = "LoginActivity_Log";
    private static final String DATABASE_URL = "https://teatrack-htng-default-rtdb.asia-southeast1.firebasedatabase.app";

    private FirebaseAuth mAuth;
    private CredentialManager credentialManager;
    private DatabaseReference databaseReference;

    private TextInputEditText edtLoginName, edtPassword;
    private TextInputLayout tilLoginName, tilPassword;
    private MaterialButton btnLogIn, btnGoogleSignIn;
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
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        credentialManager = CredentialManager.create(this);
        databaseReference = FirebaseDatabase.getInstance(DATABASE_URL).getReference("Users");
        sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        initViews();
        setupListeners();
        loadRememberedData();

        if (mAuth.getCurrentUser() != null) {
            String savedUserId = sharedPreferences.getString(KEY_USER_ID, null);
            if (savedUserId != null) {
                startActivity(new Intent(this, Homepage.class));
                finish();
            }
        }

        ensureAdminAccount();
    }

    private void ensureAdminAccount() {
        databaseReference.orderByChild("name").equalTo("admin").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    String adminId = databaseReference.push().getKey();
                    Map<String, Object> adminData = new HashMap<>();
                    adminData.put("id", adminId);
                    adminData.put("name", "admin");
                    adminData.put("fullName", "System Administrator");
                    adminData.put("email", "admin@teatrack.com");
                    adminData.put("role", "Admin");
                    adminData.put("status", "Active");
                    adminData.put("password", hashPassword("admin123"));
                    adminData.put("createdAt", String.valueOf(System.currentTimeMillis()));

                    if (adminId != null) {
                        databaseReference.child(adminId).setValue(adminData);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Admin check cancelled: " + error.getMessage());
            }
        });
    }

    private void initViews() {
        edtLoginName = findViewById(R.id.edtLoginName);
        edtPassword = findViewById(R.id.edtPassword);
        tilLoginName = findViewById(R.id.tilLoginName);
        tilPassword = findViewById(R.id.tilPassword);
        btnLogIn = findViewById(R.id.btnLogIn);
        btnGoogleSignIn = findViewById(R.id.btnGoogle);
        tvErrorMessage = findViewById(R.id.tvErrorMessage);
        tvSignUp = findViewById(R.id.tvSignUp);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        cbRemember = findViewById(R.id.cbRemember);
    }

    private void setupListeners() {
        if (btnLogIn != null) btnLogIn.setOnClickListener(v -> handleLogin());
        if (btnGoogleSignIn != null) btnGoogleSignIn.setOnClickListener(v -> signInWithGoogle());

        if (tvForgotPassword != null) {
            tvForgotPassword.setOnClickListener(v -> {
                startActivity(new Intent(this, ForgotPasswordActivity.class));
            });
        }

        if (tvSignUp != null) {
            tvSignUp.setOnClickListener(v -> {
                startActivity(new Intent(this, RegisterActivity.class));
            });
        }

        TextWatcher clearErrorWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { hideError(); }
            @Override
            public void afterTextChanged(Editable s) {}
        };

        if (edtLoginName != null) edtLoginName.addTextChangedListener(clearErrorWatcher);
        if (edtPassword != null) edtPassword.addTextChangedListener(clearErrorWatcher);
    }

    private void setFieldError(TextInputLayout til, boolean isError) {
        if (til == null) return;
        til.setErrorEnabled(false);
        if (isError) {
            int red = android.graphics.Color.RED;
            android.content.res.ColorStateList redList = new android.content.res.ColorStateList(
                    new int[][] {
                            new int[] { android.R.attr.state_focused },
                            new int[] { -android.R.attr.state_focused },
                            new int[] {}
                    },
                    new int[] { red, red, red }
            );
            til.setBoxStrokeColorStateList(redList);
        } else {
            til.setBoxStrokeColorStateList(
                    ContextCompat.getColorStateList(this, R.color.til_stroke_color)
            );
        }
    }

    private void signInWithGoogle() {
        GetGoogleIdOption googleIdOption = new GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setAutoSelectEnabled(false)
                .setServerClientId(getString(R.string.default_web_client_id))
                .build();

        GetCredentialRequest request = new GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build();

        Executor mainExecutor = ContextCompat.getMainExecutor(this);

        credentialManager.getCredentialAsync(
                this,
                request,
                null,
                mainExecutor,
                new androidx.credentials.CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
                    @Override
                    public void onResult(GetCredentialResponse result) {
                        handleSignIn(result.getCredential());
                    }

                    @Override
                    public void onError(GetCredentialException e) {
                        Toast.makeText(LoginActivity.this, "Google Sign-In failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void handleSignIn(Credential credential) {
        if (credential instanceof GoogleIdTokenCredential) {
            GoogleIdTokenCredential googleIdTokenCredential = (GoogleIdTokenCredential) credential;
            firebaseAuthWithGoogle(googleIdTokenCredential.getIdToken());
        } else {
            Log.e(TAG, "Unexpected credential type");
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
                        Toast.makeText(this, "Authentication Failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveUserToDatabase(FirebaseUser user) {
        if (user == null) return;
        String uid = user.getUid();

        databaseReference.orderByChild("firebaseUid").equalTo(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String existingRole = "Customer";
                            String existingUserId = null;
                            String existingFullName = null;
                            String existingPhone = null;
                            for (DataSnapshot child : snapshot.getChildren()) {
                                existingUserId = child.getKey();
                                String r = child.child("role").getValue(String.class);
                                if (r != null) {
                                    existingRole = r;
                                }
                                existingFullName = child.child("fullName").getValue(String.class);
                                if (existingFullName == null || existingFullName.trim().isEmpty()) {
                                    existingFullName = child.child("name").getValue(String.class);
                                }
                                existingPhone = child.child("phoneNumber").getValue(String.class);
                                if (existingPhone == null || existingPhone.trim().isEmpty()) {
                                    existingPhone = child.child("phone").getValue(String.class);
                                }
                                break;
                            }
                            UserProfileHelper.cacheProfile(
                                    LoginActivity.this,
                                    existingUserId,
                                    existingRole,
                                    existingFullName,
                                    existingPhone
                            );
                            startActivity(new Intent(LoginActivity.this, Homepage.class));
                            finish();
                            return;
                        }
                        UserIdGenerator.next(databaseReference, new UserIdGenerator.Callback() {
                            @Override
                            public void onGenerated(String csId) {
                                Map<String, Object> userMap = new HashMap<>();
                                userMap.put("id", csId);
                                userMap.put("firebaseUid", uid);
                                userMap.put("name", user.getDisplayName());
                                userMap.put("fullName", user.getDisplayName());
                                userMap.put("email", user.getEmail());
                                userMap.put("role", "Customer");
                                userMap.put("status", "Active");
                                userMap.put("provider", "google");
                                databaseReference.child(csId).setValue(userMap);

                                sharedPreferences.edit()
                                        .putString(KEY_USER_ID, csId)
                                        .putString("userRole", "Customer")
                                        .apply();
                                UserProfileHelper.cacheProfile(
                                        LoginActivity.this,
                                        csId,
                                        "Customer",
                                        user.getDisplayName(),
                                        null
                                );

                                startActivity(new Intent(LoginActivity.this, Homepage.class));
                                finish();
                            }

                            @Override
                            public void onError(String message) {
                                startActivity(new Intent(LoginActivity.this, Homepage.class));
                                finish();
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        startActivity(new Intent(LoginActivity.this, Homepage.class));
                        finish();
                    }
                });
    }

    private void handleLogin() {
        if (edtLoginName == null || edtPassword == null) return;

        String loginName = edtLoginName.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        hideError();

        if (TextUtils.isEmpty(loginName) || TextUtils.isEmpty(password)) {
            showError("Vui lòng điền đầy đủ tên đăng nhập và mật khẩu!");
            if (TextUtils.isEmpty(loginName)) setFieldError(tilLoginName, true);
            if (TextUtils.isEmpty(password)) setFieldError(tilPassword, true);
            return;
        }

        btnLogIn.setEnabled(false);
        Handler timeoutHandler = new Handler(Looper.getMainLooper());
        Runnable timeoutRunnable = () -> {
            if (!isFinishing()) {
                btnLogIn.setEnabled(true);
                showError("Kết nối quá lâu, vui lòng kiểm tra mạng!");
                Log.e(TAG, "Login timeout reached");
            }
        };
        timeoutHandler.postDelayed(timeoutRunnable, 10000);

        if (loginName.equalsIgnoreCase("admin") && password.equals("admin123")) {
            timeoutHandler.removeCallbacks(timeoutRunnable);
            hideError();
            Toast.makeText(this, "Chào mừng Admin!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, AdminDashboard.class));
            finish();
            return;
        }

        try {
            FirebaseDatabase db = FirebaseDatabase.getInstance(DATABASE_URL);
            DatabaseReference usersRef = db.getReference("Users");

            usersRef.orderByChild("name").equalTo(loginName).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    timeoutHandler.removeCallbacks(timeoutRunnable);
                    btnLogIn.setEnabled(true);

                    if (!snapshot.exists()) {
                        showError("Tên đăng nhập hoặc mật khẩu không đúng!");
                        setFieldError(tilLoginName, true);
                        setFieldError(tilPassword, true);
                        return;
                    }

                    String userId = null;
                    boolean passwordMatched = false;
                    String hashedInput = hashPassword(password);

                    for (DataSnapshot userSnap : snapshot.getChildren()) {
                        String storedPassword = userSnap.child("password").getValue(String.class);
                        if (hashedInput.equals(storedPassword)) {
                            passwordMatched = true;
                            userId = userSnap.getKey();
                            String role = userSnap.child("role").getValue(String.class);
                            String fullName = userSnap.child("fullName").getValue(String.class);
                            if (fullName == null || fullName.trim().isEmpty()) {
                                fullName = userSnap.child("name").getValue(String.class);
                            }
                            String phone = userSnap.child("phoneNumber").getValue(String.class);
                            if (phone == null || phone.trim().isEmpty()) {
                                phone = userSnap.child("phone").getValue(String.class);
                            }

                            sharedPreferences.edit()
                                    .putString(KEY_USER_ID, userId)
                                    .putString("userRole", role != null ? role : "Customer")
                                    .apply();
                            UserProfileHelper.cacheProfile(
                                    LoginActivity.this,
                                    userId,
                                    role != null ? role : "Customer",
                                    fullName,
                                    phone
                            );

                            if (cbRemember.isChecked()) {
                                saveLoginData(loginName, password, userId);
                            }

                            if ("Admin".equalsIgnoreCase(role)) {
                                startActivity(new Intent(LoginActivity.this, AdminDashboard.class));
                            } else {
                                startActivity(new Intent(LoginActivity.this, Homepage.class));
                            }
                            finish();
                            break;
                        }
                    }

                    if (!passwordMatched) {
                        showError("Tên đăng nhập hoặc mật khẩu không đúng!");
                        setFieldError(tilPassword, true);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    timeoutHandler.removeCallbacks(timeoutRunnable);
                    btnLogIn.setEnabled(true);
                    showError("Lỗi hệ thống: " + error.getMessage());
                }
            });
        } catch (Exception e) {
            timeoutHandler.removeCallbacks(timeoutRunnable);
            btnLogIn.setEnabled(true);
            showError("Lỗi kết nối Firebase!");
        }
    }

    private void loadRememberedData() {
        boolean remember = sharedPreferences.getBoolean(KEY_REMEMBER, false);
        if (remember) {
            cbRemember.setChecked(true);
            edtLoginName.setText(sharedPreferences.getString(KEY_USERNAME, ""));
            edtPassword.setText(sharedPreferences.getString(KEY_PASSWORD, ""));
        }
    }

    private void saveLoginData(String username, String password, String userId) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_REMEMBER, true);
        editor.putString(KEY_USERNAME, username);
        editor.putString(KEY_PASSWORD, password);
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
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return password;
        }
    }

    private void showError(String message) {
        if (tvErrorMessage != null) {
            tvErrorMessage.setText(message);
            tvErrorMessage.setVisibility(View.VISIBLE);
        } else {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }
    }

    private void hideError() {
        if (tvErrorMessage != null) {
            tvErrorMessage.setVisibility(View.GONE);
        }
        setFieldError(tilLoginName, false);
        setFieldError(tilPassword, false);
    }
}