package com.teatrack_mcd_253eie502802_group02.client;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.card.MaterialCardView;
import com.teatrack_mcd_253eie502802_group02.R;
import com.teatrack_mcd_253eie502802_group02.data.OrderCheckoutFlow;

public class Payment extends AppCompatActivity {

    private static final int PAYMENT_CASH_IN_BANK = 1;
    private static final int PAYMENT_MOMO = 2;
    private static final int PAYMENT_ZALOPAY = 3;
    private static final int PAYMENT_EWALLET = 4;

    private int method;
    private String pickupAddress;
    private String recipientDetails;
    private String note;
    private TextView tvTimer;
    private int secondsRemaining = 5;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            if (secondsRemaining > 0) {
                if (tvTimer != null) {
                    tvTimer.setText(getString(R.string.payment_waiting_format, secondsRemaining));
                }
                secondsRemaining--;
                handler.postDelayed(this, 1000);
            } else {
                completePayment();
            }
        }
    };

    // drawable mipmap name, label
    private final int[][] bankLogos = {
            {R.mipmap.vib, 0}, {R.mipmap.tech, 0}, {R.mipmap.acb, 0},
            {R.mipmap.scb, 0}, {R.mipmap.vcb, 0}, {R.mipmap.bidv, 0},
            {R.mipmap.agri, 0}, {R.mipmap.mb, 0}, {R.mipmap.no, 0},
            {R.mipmap.lp, 0}
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

        method = getIntent().getIntExtra("method", PAYMENT_MOMO);
        pickupAddress = getIntent().getStringExtra("pickupAddress");
        recipientDetails = getIntent().getStringExtra("recipientDetails");
        note = getIntent().getStringExtra("note");
        findViewById(R.id.btnPaymentBack).setOnClickListener(v -> finish());

        MaterialCardView cardQr = findViewById(R.id.cardQr);
        ImageView ivQrCode = findViewById(R.id.ivQrCode);
        TextView tvInstructions = findViewById(R.id.tvPaymentInstructions);
        TextView tvBankTitle = findViewById(R.id.tvBankSupportTitle);
        GridLayout gridBankSupport = findViewById(R.id.gridBankSupport);

        int bgColor;
        int qrRes;
        String instructions;
        boolean showBankGrid = false;

        switch (method) {
            case PAYMENT_ZALOPAY:
                bgColor = ContextCompat.getColor(this, R.color.payment_zalopay_blue);
                qrRes = R.mipmap.qrzalo;
                instructions = getString(R.string.payment_instructions_zalopay);
                break;
            case PAYMENT_CASH_IN_BANK:
            case PAYMENT_EWALLET:
                bgColor = ContextCompat.getColor(this, R.color.payment_bank_blue);
                qrRes = R.mipmap.qrbanking;
                instructions = getString(R.string.payment_instructions_bank);
                showBankGrid = true;
                break;
            case PAYMENT_MOMO:
            default:
                bgColor = ContextCompat.getColor(this, R.color.payment_momo_pink);
                qrRes = R.mipmap.qrcode;
                instructions = getString(R.string.payment_instructions_momo);
                break;
        }

        cardQr.setCardBackgroundColor(bgColor);
        ivQrCode.setImageResource(qrRes);
        tvInstructions.setText(instructions);

        if (showBankGrid) {
            tvBankTitle.setVisibility(android.view.View.VISIBLE);
            gridBankSupport.setVisibility(android.view.View.VISIBLE);
            populateBankGrid(gridBankSupport);
        } else {
            tvBankTitle.setVisibility(android.view.View.GONE);
            gridBankSupport.setVisibility(android.view.View.GONE);
        }

        handler.post(timerRunnable);
    }

    private void populateBankGrid(GridLayout grid) {
        grid.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);
        for (int[] logo : bankLogos) {
            MaterialCardView item = (MaterialCardView) inflater.inflate(R.layout.item_bank_icon, grid, false);
            ImageView iv = (ImageView) item.getChildAt(0);
            iv.setImageResource(logo[0]);
            grid.addView(item);
        }
    }

    private void completePayment() {
        Toast.makeText(this, "Payment received! Redirecting...", Toast.LENGTH_SHORT).show();
        OrderCheckoutFlow.placeOrderAndOpenCheckout(
                this,
                method,
                pickupAddress,
                recipientDetails,
                note,
                true,
                null
        );
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}