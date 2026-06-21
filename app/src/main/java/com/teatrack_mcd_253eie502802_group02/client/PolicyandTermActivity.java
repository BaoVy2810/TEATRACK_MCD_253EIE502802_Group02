package com.teatrack_mcd_253eie502802_group02.client;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.teatrack_mcd_253eie502802_group02.R;
import com.teatrack_mcd_253eie502802_group02.shared.BaseActivity;
import com.teatrack_mcd_253eie502802_group02.shared.ui.ProfileBackHelper;

public class PolicyandTermActivity extends BaseActivity {

    private CardView cardTerms, cardPrivacy, cardMembership, cardRefund;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_policyand_term);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        setupClickListeners();
        ProfileBackHelper.setupBackToProfile(this);
    }

    private void initViews() {
        cardTerms = findViewById(R.id.cardTerms);
        cardPrivacy = findViewById(R.id.cardPrivacy);
        cardMembership = findViewById(R.id.cardMembership);
        cardRefund = findViewById(R.id.cardRefund);
    }

    private void setupClickListeners() {
        cardTerms.setOnClickListener(v -> {
            Intent intent = new Intent(PolicyandTermActivity.this, TermAndConditionActivity.class);
            startActivity(intent);
        });

        cardPrivacy.setOnClickListener(v -> {
            Intent intent = new Intent(PolicyandTermActivity.this, PrivacyPolicyActivity.class);
            startActivity(intent);
        });

        cardMembership.setOnClickListener(v -> {
            Intent intent = new Intent(PolicyandTermActivity.this, MembershipPolicyActivity.class);
            startActivity(intent);
        });

        cardRefund.setOnClickListener(v -> {
            Intent intent = new Intent(PolicyandTermActivity.this, RefundPolicyActivity.class);
            startActivity(intent);
        });
    }
}