package com.teatrack_mcd_253eie502802_group02.client;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.teatrack_mcd_253eie502802_group02.R;
import com.teatrack_mcd_253eie502802_group02.shared.BaseActivity;

import java.util.Locale;

public class Checkout extends BaseActivity {

    public static final String EXTRA_ORDER_ID = "orderId";
    public static final String EXTRA_PICKUP_ADDRESS = "pickupAddress";
    public static final String EXTRA_ORDER_TOTAL = "orderTotal";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_checkout);
        
        View root = findViewById(R.id.main);
        if (root != null) {
            ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        String orderId = getIntent().getStringExtra(EXTRA_ORDER_ID);
        TextView tvOrderId = findViewById(R.id.tvOrderId);
        if (tvOrderId != null && orderId != null && !orderId.isEmpty()) {
            tvOrderId.setText(Html.fromHtml(
                    getString(R.string.order_success_order_line, orderId),
                    Html.FROM_HTML_MODE_LEGACY
            ));
        }

        String pickupAddress = getIntent().getStringExtra(EXTRA_PICKUP_ADDRESS);
        TextView tvPickupAddress = findViewById(R.id.tvPickupAddress);
        if (tvPickupAddress != null && pickupAddress != null) {
            tvPickupAddress.setText(pickupAddress);
        }

        TextView tvPickupTime = findViewById(R.id.tvPickupTime);
        if (tvPickupTime != null) {
            tvPickupTime.setText(R.string.order_pickup_time_value);
        }

        int orderTotal = getIntent().getIntExtra(EXTRA_ORDER_TOTAL, 0);
        TextView tvOrderTotal = findViewById(R.id.tvOrderTotal);
        if (tvOrderTotal != null) {
            tvOrderTotal.setText(formatPrice(orderTotal));
        }

        findViewById(R.id.btnBackToMenu).setOnClickListener(v -> navigateToMenu());
        findViewById(R.id.btnTrackOrder).setOnClickListener(v -> {
            Intent intent = new Intent(this, OrderHistory.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private String formatPrice(int price) {
        return String.format(Locale.US, "%,d", price).replace(',', '.') + "đ";
    }

    private void navigateToMenu() {
        Intent intent = new Intent(this, Menu.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
