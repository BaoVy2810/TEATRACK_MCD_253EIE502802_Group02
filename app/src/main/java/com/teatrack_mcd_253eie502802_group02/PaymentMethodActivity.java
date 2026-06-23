package com.teatrack_mcd_253eie502802_group02;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.widget.RadioButton;

public class PaymentMethodActivity extends AppCompatActivity {

    private static final int CASH_ON_HAND = 0;
    private static final int CASH_IN_BANK = 1;
    private int selected;
    private RadioButton radioCashOnHand, radioCashInBank;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_method);

        selected = getIntent().getIntExtra("selected", CASH_ON_HAND);
        radioCashOnHand = findViewById(R.id.radioCashOnHand);
        radioCashInBank = findViewById(R.id.radioCashInBank);

        updateRadio(selected);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        findViewById(R.id.rowCashOnHand).setOnClickListener(v -> {
            selected = CASH_ON_HAND;
            updateRadio(selected);
            returnResult();
        });

        findViewById(R.id.rowCashInBank).setOnClickListener(v -> {
            selected = CASH_IN_BANK;
            updateRadio(selected);
            returnResult();
        });
    }

    private void updateRadio(int method) {
        radioCashOnHand.setChecked(method == CASH_ON_HAND);
        radioCashInBank.setChecked(method == CASH_IN_BANK);
    }

    private void returnResult() {
        Intent result = new Intent();
        result.putExtra("selected", selected);
        setResult(RESULT_OK, result);
        finish();
    }
}