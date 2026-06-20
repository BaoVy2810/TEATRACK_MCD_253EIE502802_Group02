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
import com.teatrack_mcd_253eie502802_group02.shared.ui.NavBarHelper;

public class PolicyandTermActivity extends BaseActivity {

    private CardView cardTerms, cardPrivacy, cardMembership, cardRefund;

    private static final int[] NAV_ITEM_IDS = {
            R.id.nav_home,
            R.id.nav_menu,
            R.id.nav_orders,
            R.id.nav_promotion,
            R.id.nav_profile
    };

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
        setupNavBar();
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

    private void setupNavBar() {
        NavBarHelper.setupNavBar(this, NAV_ITEM_IDS, R.id.nav_profile, v -> {
            int id = v.getId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(this, Homepage.class));
            } else if (id == R.id.nav_menu) {
                startActivity(new Intent(this, Menu.class));
            } else if (id == R.id.nav_orders) {
                startActivity(new Intent(this, OrderHistory.class));
            } else if (id == R.id.nav_promotion) {
                // TODO: Chờ màn hình Promotion
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, UserProfile.class));
            }
        });
    }
}