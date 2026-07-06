package com.teatrack_mcd_253eie502802_group02.client;

import android.content.Intent;
import androidx.core.content.ContextCompat;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import com.teatrack_mcd_253eie502802_group02.shared.BaseActivity;
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
import com.teatrack_mcd_253eie502802_group02.R;
import com.teatrack_mcd_253eie502802_group02.data.FirebaseProductRepository;
import com.teatrack_mcd_253eie502802_group02.data.PasswordResetManager;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

public class ResetPasswordActivity extends BaseActivity {

    private TextInputLayout tilPassword, tilConfirm;
    private TextInputEditText etPassword, etConfirm;
    private TextView tvErrorMessage, tvBackToSignIn;
    private MaterialButton btnUpdate;
    private String userEmail;
    private PasswordResetManager passwordResetManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_reset_password);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        userEmail = getIntent().getStringExtra("email");
        passwordResetManager = new PasswordResetManager(this);

        initViews();

        btnUpdate.setOnClickListener(v -> handleUpdatePassword());

        tvBackToSignIn.setOnClickListener(v -> {
            Intent intent = new Intent(ResetPasswordActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
    }

    private void initViews() {
        tilPassword = findViewById(R.id.tilPassword);
        tilConfirm = findViewById(R.id.tilConfirm);
        etPassword = findViewById(R.id.etPassword);
        etConfirm = findViewById(R.id.etConfirm);
        tvErrorMessage = findViewById(R.id.tvErrorMessage);
        tvBackToSignIn = findViewById(R.id.tvBackToSignIn);
        btnUpdate = findViewById(R.id.btnUpdate);
    }

    private void handleUpdatePassword() {
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirm.getText().toString().trim();

        // Reset state
        resetErrors();

        boolean hasError = false;

        if (password.isEmpty() || confirmPassword.isEmpty()) {
            showError(getString(R.string.reset_password_error_empty));
            setFieldError(tilPassword, true);
            setFieldError(tilConfirm, true);
            hasError = true;
        } else if (!password.equals(confirmPassword)) {
            showError(getString(R.string.reset_password_error_mismatch));
            setFieldError(tilPassword, true);
            setFieldError(tilConfirm, true);
            hasError = true;
        }

        if (hasError) return;

        if (userEmail == null || userEmail.trim().isEmpty()) {
            showError(getString(R.string.reset_password_error_request_otp_first));
            return;
        }

        btnUpdate.setEnabled(false);
        passwordResetManager.requireVerifiedOtp(userEmail, new PasswordResetManager.Callback() {
            @Override
            public void onSuccess() {
                updatePasswordInFirebase(userEmail, password);
            }

            @Override
            public void onError(String message) {
                btnUpdate.setEnabled(true);
                showError(message);
            }
        });
    }

    private void updatePasswordInFirebase(String email, String newPassword) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance(FirebaseProductRepository.DB_URL).getReference("Users");

        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                DataSnapshot matchedUser = findUserByEmail(snapshot, email);
                if (matchedUser == null) {
                    btnUpdate.setEnabled(true);
                    showError("User not found.");
                    return;
                }

                String hashedPassword = hashPassword(newPassword);
                matchedUser.getRef().child("password").setValue(hashedPassword)
                        .addOnSuccessListener(aVoid -> {
                            passwordResetManager.clearOtp(email);
                            Toast.makeText(ResetPasswordActivity.this, R.string.reset_password_update_success, Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(ResetPasswordActivity.this, SucessfullyChangePasswordActivity.class);
                            startActivity(intent);
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            btnUpdate.setEnabled(true);
                            showError("Update failed: " + e.getMessage());
                        });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                btnUpdate.setEnabled(true);
                showError("Database Error: " + error.getMessage());
            }
        });
    }

    private DataSnapshot findUserByEmail(DataSnapshot usersSnapshot, String email) {
        String target = normalizeEmail(email);
        if (target.isEmpty() || !usersSnapshot.exists()) {
            return null;
        }

        for (DataSnapshot userSnapshot : usersSnapshot.getChildren()) {
            if (isUserEmailMatch(userSnapshot, target)) {
                return userSnapshot;
            }
        }
        return null;
    }

    private boolean isUserEmailMatch(DataSnapshot userSnapshot, String normalizedTarget) {
        String[] emailFields = {"email", "Email", "userEmail", "emailAddress"};
        for (String field : emailFields) {
            if (normalizeEmail(valueAsString(userSnapshot.child(field))).equals(normalizedTarget)) {
                return true;
            }
        }
        return normalizeEmail(userSnapshot.getKey()).equals(normalizedTarget);
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.US);
    }

    private String valueAsString(DataSnapshot snapshot) {
        Object value = snapshot.getValue();
        return value == null ? "" : String.valueOf(value);
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

    private void resetErrors() {
        tvErrorMessage.setVisibility(View.GONE);
        setFieldError(tilPassword, false);
        setFieldError(tilConfirm, false);
    }

    private void showError(String message) {
        tvErrorMessage.setText(message);
        tvErrorMessage.setVisibility(View.VISIBLE);
    }
}
