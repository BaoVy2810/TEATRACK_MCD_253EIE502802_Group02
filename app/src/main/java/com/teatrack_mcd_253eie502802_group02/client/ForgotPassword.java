package com.teatrack_mcd_253eie502802_group02.client;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import com.teatrack_mcd_253eie502802_group02.shared.BaseActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.teatrack_mcd_253eie502802_group02.R;

public class ForgotPassword extends BaseActivity {

    private TextView tvSignIn, tvErrorMessage;
    private TextInputLayout tilEmail;
    private TextInputEditText etEmail;
    private Button btnSendOTP;

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

        checkEmailInFirebase(email);
    }

    private void checkEmailInFirebase(String email) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users");
        Query query = usersRef.orderByChild("email").equalTo(email);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Email found, proceed to next screen (ResetPassword or OTP)
                    // For now, let's assume it goes to ResetPasswordActivity
                    Intent intent = new Intent(ForgotPassword.this, ResetPasswordActivity.class);
                    intent.putExtra("email", email);
                    startActivity(intent);
                } else {
                    // Email not found
                    showError(getString(R.string.forgot_password_error_not_found));
                    setFieldError(true);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showError("Database Error: " + error.getMessage());
            }
        });
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
