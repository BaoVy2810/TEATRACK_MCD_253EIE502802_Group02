package com.teatrack_mcd_253eie502802_group02.client;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputLayout;
import com.teatrack_mcd_253eie502802_group02.R;

public class RegisterActivity extends AppCompatActivity {

    private com.google.android.material.textfield.TextInputEditText etName, etEmail, etPhone, etPassword, etConfirmPassword;
    private TextInputLayout tilName, tilPassword, tilConfirmPassword; // chỉ validate 3 ô bắt buộc
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
        tilName = findViewById(R.id.tilName);
        tilPassword = findViewById(R.id.tilPassword);
        tilConfirmPassword = findViewById(R.id.tilConfirmPassword);

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);         // optional, chỉ lấy giá trị
        etPhone = findViewById(R.id.etPhone);         // optional, chỉ lấy giá trị
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        tvErrorMessage = findViewById(R.id.tvErrorMessage);
        btnRegister = findViewById(R.id.btnRegister);
    }

    private void handleRegister() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();     // optional
        String phone = etPhone.getText().toString().trim();     // optional
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // Reset trạng thái trước khi validate
        resetFieldErrors();

        boolean hasError = false;

        if (name.isEmpty()) {
            setFieldError(tilName, true);
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

        // Clear error if everything is valid
        tvErrorMessage.setVisibility(android.view.View.GONE);
        // Proceed with registration logic (dùng name, email, phone, password)
    }

    private void setFieldError(TextInputLayout til, boolean isError) {
        if (isError) {
            til.setBoxStrokeColor(android.graphics.Color.RED);
            til.setBoxStrokeErrorColor(
                    android.content.res.ColorStateList.valueOf(android.graphics.Color.RED)
            );
            til.setErrorEnabled(true);
            til.setError(" "); // space để kích hoạt stroke đỏ mà không hiện text lỗi dưới ô
        } else {
            til.setError(null);
            til.setErrorEnabled(false);
        }
    }

    private void resetFieldErrors() {
        setFieldError(tilName, false);
        setFieldError(tilPassword, false);
        setFieldError(tilConfirmPassword, false);
    }

    private void showError(String message) {
        tvErrorMessage.setText(message);
        tvErrorMessage.setVisibility(android.view.View.VISIBLE);
    }
}