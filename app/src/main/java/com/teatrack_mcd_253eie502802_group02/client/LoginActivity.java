package com.teatrack_mcd_253eie502802_group02.client;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.teatrack_mcd_253eie502802_group02.MainActivity;
import com.teatrack_mcd_253eie502802_group02.R;
import com.teatrack_mcd_253eie502802_group02.model.User;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText edtLoginName, edtPassword;
    private TextInputLayout tilLoginName, tilPassword;
    private MaterialButton btnLogIn;
    private TextView tvErrorMessage, tvSignUp;
    private CheckBox cbRemember;
    private SharedPreferences sharedPreferences;
    private static final String PREF_NAME = "LoginPrefs";
    private static final String KEY_REMEMBER = "remember";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_PASSWORD = "password";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        setupListeners();
        ensureAdminAccount();
    }

    private void ensureAdminAccount() {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users");
        usersRef.orderByChild("name").equalTo("admin").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    String hashedPw = "240be518fabd2724ddb6f0403fd3d3588e6c2c1a0c115bd34d7d1f14e9c06519";
                    String adminId = "admin_fixed";
                    User adminUser = new User(adminId, "admin", "admin@teatrack.com", "0000000000", hashedPw);
                    usersRef.child(adminId).setValue(adminUser);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void initViews() {
        tilLoginName = findViewById(R.id.tilLoginName);
        tilPassword = findViewById(R.id.tilPassword);
        edtLoginName = tilLoginName.findViewById(R.id.edtLoginName);
        edtPassword = tilPassword.findViewById(R.id.edtPassword);
        btnLogIn = findViewById(R.id.btnLogIn);
        tvErrorMessage = findViewById(R.id.tvErrorMessage);
        tvSignUp = findViewById(R.id.tvSignUp);
        cbRemember = findViewById(R.id.cbRemember);

        sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        loadRememberedData();
    }

    private void loadRememberedData() {
        boolean isRemembered = sharedPreferences.getBoolean(KEY_REMEMBER, false);
        if (isRemembered) {
            String username = sharedPreferences.getString(KEY_USERNAME, "");
            String password = sharedPreferences.getString(KEY_PASSWORD, "");
            edtLoginName.setText(username);
            edtPassword.setText(password);
            cbRemember.setChecked(true);
        }
    }

    private void setupListeners() {
        btnLogIn.setOnClickListener(v -> handleLogin());
        tvSignUp.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        TextWatcher clearErrorWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                hideError();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        edtLoginName.addTextChangedListener(clearErrorWatcher);
        edtPassword.addTextChangedListener(clearErrorWatcher);
    }

    private void handleLogin() {
        String loginName = edtLoginName.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        if (TextUtils.isEmpty(loginName) || TextUtils.isEmpty(password)) {
            showError(getString(R.string.login_error_empty));
            return;
        }

        String hashedPassword = hashPassword(password);
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users");

        usersRef.orderByChild("name").equalTo(loginName).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean loginSuccess = false;
                if (snapshot.exists()) {
                    for (DataSnapshot userSnap : snapshot.getChildren()) {
                        User user = userSnap.getValue(User.class);
                        if (user != null && user.getPassword().equals(hashedPassword)) {
                            loginSuccess = true;
                            break;
                        }
                    }
                }

                if (loginSuccess) {
                    saveLoginData(loginName, password);
                    hideError();
                    Intent intent = new Intent(LoginActivity.this, Homepage.class);
                    startActivity(intent);
                    finish();
                } else {
                    showError(getString(R.string.login_error_invalid));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showError("Lỗi hệ thống: " + error.getMessage());
            }
        });
    }

    private void saveLoginData(String username, String password) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (cbRemember.isChecked()) {
            editor.putBoolean(KEY_REMEMBER, true);
            editor.putString(KEY_USERNAME, username);
            editor.putString(KEY_PASSWORD, password);
        } else {
            editor.clear();
        }
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
        tvErrorMessage.setText(message);
        tvErrorMessage.setVisibility(View.VISIBLE);
        tilLoginName.setError(" ");
        tilPassword.setError(" ");
    }

    private void hideError() {
        tvErrorMessage.setVisibility(View.GONE);
        tilLoginName.setError(null);
        tilPassword.setError(null);
    }
}