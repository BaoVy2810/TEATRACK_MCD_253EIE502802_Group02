package com.teatrack_mcd_253eie502802_group02.client;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.teatrack_mcd_253eie502802_group02.R;
import com.teatrack_mcd_253eie502802_group02.model.User;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.teatrack_mcd_253eie502802_group02.shared.BaseActivity;

public class RegisterActivity extends BaseActivity {

    private com.google.android.material.textfield.TextInputEditText etFullName, etName, etEmail, etPhone, etPassword, etConfirmPassword;
    private TextInputLayout tilFullName, tilName, tilEmail, tilPassword, tilConfirmPassword;
    private android.widget.TextView tvErrorMessage;
    private com.google.android.material.button.MaterialButton btnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();

        btnRegister.setOnClickListener(v -> handleRegister());

        findViewById(R.id.tvSignInLink).setOnClickListener(v -> finish());
    }

    private void initViews() {
        tilFullName = findViewById(R.id.tilFullName);
        tilName = findViewById(R.id.tilName);
        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        tilConfirmPassword = findViewById(R.id.tilConfirmPassword);

        etFullName = findViewById(R.id.etFullName);
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        tvErrorMessage = findViewById(R.id.tvErrorMessage);
        btnRegister = findViewById(R.id.btnRegister);
    }

    private void handleRegister() {
        String fullName = etFullName.getText().toString().trim();
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        resetFieldErrors();

        boolean hasError = false;

        if (fullName.isEmpty()) {
            setFieldError(tilFullName, true);
            hasError = true;
        }
        if (name.isEmpty()) {
            setFieldError(tilName, true);
            hasError = true;
        }
        if (email.isEmpty()) {
            setFieldError(tilEmail, true);
            hasError = true;
        }
        if (password.isEmpty()) {
            setFieldError(tilPassword, true);
            hasError = true;
        }
        if (confirmPassword.isEmpty()) {
            setFieldError(tilConfirmPassword, true);
            hasError = true;
        }

        if (hasError) {
            showError(getString(R.string.register_error_empty));
            return;
        }

        if (!password.equals(confirmPassword)) {
            setFieldError(tilPassword, true);
            setFieldError(tilConfirmPassword, true);
            showError(getString(R.string.register_error_mismatch));
            return;
        }

        tvErrorMessage.setVisibility(android.view.View.GONE);

        String hashedPassword = hashPassword(password);
        String createdAt = new SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault()).format(new Date());
        saveUserToFirebase(name, email, phone, hashedPassword, createdAt);
    }

    private void saveUserToFirebase(String name, String email, String phone, String hashedPassword, String createdAt) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users");
        String userId = usersRef.push().getKey();

        if (userId != null) {
            User newUser = new User(userId, name, email, phone, hashedPassword, createdAt);
            usersRef.child(userId).setValue(newUser)
                    .addOnSuccessListener(aVoid -> {
                        android.content.SharedPreferences sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
                        android.content.SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("userId", userId);
                        editor.apply();

                        Toast.makeText(RegisterActivity.this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> showError("Đăng ký thất bại: " + e.getMessage()));
        }
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

    private void setFieldError(TextInputLayout til, boolean isError) {
        if (til == null) return;
        if (isError) {
            til.setBoxStrokeColor(android.graphics.Color.RED);
            til.setBoxStrokeErrorColor(
                    android.content.res.ColorStateList.valueOf(android.graphics.Color.RED)
            );
            til.setErrorEnabled(true);
            til.setError(" ");
        } else {
            til.setError(null);
            til.setErrorEnabled(false);
        }
    }

    private void resetFieldErrors() {
        setFieldError(tilFullName, false);
        setFieldError(tilName, false);
        setFieldError(tilEmail, false);
        setFieldError(tilPassword, false);
        setFieldError(tilConfirmPassword, false);
    }

    private void showError(String message) {
        tvErrorMessage.setText(message);
        tvErrorMessage.setVisibility(android.view.View.VISIBLE);
    }

}
