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
import com.teatrack_mcd_253eie502802_group02.util.UserIdGenerator;

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
        saveUserToFirebase(name, email.toLowerCase(Locale.US), phone, hashedPassword, createdAt);
    }

    private void saveUserToFirebase(String name, String email, String phone, String hashedPassword, String createdAt) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance(
                com.teatrack_mcd_253eie502802_group02.data.FirebaseProductRepository.DB_URL
        ).getReference("Users");

        // Sinh ID theo quy luật CS01, CS02, CS03...
        UserIdGenerator.next(usersRef, new UserIdGenerator.Callback() {
            @Override
            public void onGenerated(String userId) {
                User newUser = new User(userId, name, email, phone, hashedPassword, createdAt);
                usersRef.child(userId).setValue(newUser)
                        .addOnSuccessListener(aVoid -> {
                            android.content.SharedPreferences sharedPreferences =
                                    getSharedPreferences("LoginPrefs", MODE_PRIVATE);
                            sharedPreferences.edit().putString("userId", userId).apply();

                            Toast.makeText(RegisterActivity.this, getString(R.string.msg_register_success), Toast.LENGTH_SHORT).show();
                            finish();
                        })
                        .addOnFailureListener(e -> showError(getString(R.string.error_register_failed, e.getMessage())));
            }

            @Override
            public void onError(String message) {
                showError(getString(R.string.error_account_creation_failed, message));
            }
        });
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

        til.setErrorEnabled(false); // không chiếm space

        if (isError) {
            til.setBoxStrokeWidth(2);
            til.setBoxStrokeWidthFocused(2);
            // Force màu đỏ bằng cách override toàn bộ state
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
            til.setBoxStrokeWidth(1);
            til.setBoxStrokeWidthFocused(1);
            til.setBoxStrokeColorStateList(
                    androidx.core.content.ContextCompat.getColorStateList(this, R.color.til_stroke_color)
            );
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
