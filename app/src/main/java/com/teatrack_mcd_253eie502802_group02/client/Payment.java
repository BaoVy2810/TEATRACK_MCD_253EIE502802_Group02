package com.teatrack_mcd_253eie502802_group02.client;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.teatrack_mcd_253eie502802_group02.R;
import com.teatrack_mcd_253eie502802_group02.data.CartManager;

public class Payment extends AppCompatActivity {

    private int method;
    private String pickupAddress;
    private TextView tvTimer;
    private int secondsRemaining = 5;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            if (secondsRemaining > 0) {
                tvTimer.setText(getString(R.string.payment_waiting_format, secondsRemaining));
                secondsRemaining--;
                handler.postDelayed(this, 1000);
            } else {
                completePayment();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_payment);
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        method = getIntent().getIntExtra("method", 2);
        pickupAddress = getIntent().getStringExtra("pickupAddress");
        tvTimer = findViewById(R.id.tvTimer);
        TextView tvMethodName = findViewById(R.id.tvPaymentMethodName);
        findViewById(R.id.btnPaymentBack).setOnClickListener(v -> finish());

        String name = getString(R.string.payment_momo);
        if (method == 3) name = getString(R.string.payment_zalopay);
        if (method == 4) name = getString(R.string.payment_ewallet);
        tvMethodName.setText(name);

        handler.post(timerRunnable);
    }

    private void completePayment() {
        Toast.makeText(this, "Payment received! Redirecting...", Toast.LENGTH_SHORT).show();
        int orderTotal = CartManager.getInstance().getSubtotal();
        CartManager.getInstance().clear();
        Intent intent = new Intent(this, Checkout.class);
        intent.putExtra("pickupAddress", pickupAddress);
        intent.putExtra("orderTotal", orderTotal);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacks(timerRunnable);
        super.onDestroy();
    }
}