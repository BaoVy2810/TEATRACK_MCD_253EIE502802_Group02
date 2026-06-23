package com.teatrack_mcd_253eie502802_group02.client;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.teatrack_mcd_253eie502802_group02.R;

public class ForgotPasswordActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        
        TextView tvSignIn = findViewById(R.id.tvSignIn);
        if (tvSignIn != null) {
            tvSignIn.setOnClickListener(v -> finish());
        }
    }
}
