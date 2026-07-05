package com.teatrack_mcd_253eie502802_group02.client;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.teatrack_mcd_253eie502802_group02.R;
import com.teatrack_mcd_253eie502802_group02.data.PasswordResetManager;

public class ForgotPassword extends AppCompatActivity {

    private TextView tvSignIn, tvErrorMessage;
    private TextInputLayout tilEmail;
    private TextInputEditText etEmail;
    private Button btnSendOTP;
    private PasswordResetManager passwordResetManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forgot_password);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        passwordResetManager = new PasswordResetManager(this);

        tvSignIn.setOnClickListener(v -> {
            Intent intent = new Intent(ForgotPassword.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

        btnSendOTP.setOnClickListener(v -> handleSendOTP());
    }

    private void initViews() {
        tvSignIn = findViewById(R.id.tvSignIn);
        tvErrorMessage = findViewById(R.id.tvErrorMessage);
        tilEmail = findViewById(R.id.tilEmail);
        etEmail = findViewById(R.id.etEmail);
        btnSendOTP = findViewById(R.id.btnSendOTP);
    }

    private void handleSendOTP() {
        String email = etEmail.getText().toString().trim();

        // Reset state
        setFieldError(false);
        tvErrorMessage.setVisibility(View.GONE);

        if (email.isEmpty()) {
            showError(getString(R.string.forgot_password_error_empty));
            setFieldError(true);
            return;
        }

        setLoading(true);
        passwordResetManager.sendOtp(email, new PasswordResetManager.Callback() {
            @Override
            public void onSuccess() {
                setLoading(false);
                Toast.makeText(ForgotPassword.this, R.string.forgot_password_otp_sent, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(ForgotPassword.this, FilledOtpActivity.class);
                intent.putExtra(FilledOtpActivity.EXTRA_EMAIL, email);
                startActivity(intent);
            }

            @Override
            public void onError(String message) {
                setLoading(false);
                showError(message);
                setFieldError(true);
            }
        });
    }

    private void setLoading(boolean loading) {
        btnSendOTP.setEnabled(!loading);
        btnSendOTP.setAlpha(loading ? 0.65f : 1f);
    }

    private void setFieldError(boolean isError) {
        if (isError) {
            tilEmail.setBoxStrokeColor(Color.RED);
            tilEmail.setBoxStrokeErrorColor(ColorStateList.valueOf(Color.RED));
            tilEmail.setErrorEnabled(true);
            tilEmail.setError(" "); // Space to trigger red stroke without text below
        } else {
            tilEmail.setError(null);
            tilEmail.setErrorEnabled(false);
        }
    }

    private void showError(String message) {
        tvErrorMessage.setText(message);
        tvErrorMessage.setVisibility(View.VISIBLE);
    }
}
