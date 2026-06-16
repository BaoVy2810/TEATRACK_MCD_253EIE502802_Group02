package com.teatrack_mcd_253eie502802_group02.client;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
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
    private MaterialButton btnLogIn;
    private TextView tvErrorMessage;

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
        // Kiểm tra xem đã có tài khoản admin chưa
        usersRef.orderByChild("name").equalTo("admin").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    // admin123 đã băm SHA-256
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
        edtLoginName = findViewById(R.id.tilLoginName).findViewById(R.id.edtLoginName);
        edtPassword = findViewById(R.id.tilPassword).findViewById(R.id.edtPassword);
        btnLogIn = findViewById(R.id.btnLogIn);
        tvErrorMessage = findViewById(R.id.tvErrorMessage);
    }

    private void setupListeners() {
        btnLogIn.setOnClickListener(v -> handleLogin());
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

        // Kiểm tra tài khoản từ Firebase
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
                    hideError();
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
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
    }

    private void hideError() {
        tvErrorMessage.setVisibility(View.GONE);
    }
}