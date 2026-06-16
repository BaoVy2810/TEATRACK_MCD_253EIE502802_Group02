package com.teatrack_mcd_253eie502802_group02.client;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.teatrack_mcd_253eie502802_group02.MainActivity;
import com.teatrack_mcd_253eie502802_group02.R;

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

        // Fake login logic
        if (loginName.equals("admin") && password.equals("admin123")) {
            hideError();
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        } else {
            showError(getString(R.string.login_error_invalid));
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