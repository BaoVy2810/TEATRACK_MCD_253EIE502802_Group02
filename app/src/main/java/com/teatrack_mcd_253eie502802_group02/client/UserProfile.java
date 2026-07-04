package com.teatrack_mcd_253eie502802_group02.client;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.teatrack_mcd_253eie502802_group02.R;
import com.teatrack_mcd_253eie502802_group02.client.ContactWithUs;
import com.teatrack_mcd_253eie502802_group02.shared.BaseActivity;
import com.teatrack_mcd_253eie502802_group02.shared.ui.NavBarHelper;
import com.teatrack_mcd_253eie502802_group02.shared.ui.CartBadgeHelper;

public class UserProfile extends BaseActivity {

    private LinearLayout btnPersonalInfo, btnLanguage, btnPoints, btnReviews, btnPolicies, btnStoreList, btnComplaint;

    private static final int[] NAV_IDS = {
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
        setContentView(R.layout.activity_user_profile);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        setupClickListeners();
        setupBottomNav();
        CartBadgeHelper.setup(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        CartBadgeHelper.updateBadge(this);
    }

    private void initViews() {
        btnPersonalInfo = findViewById(R.id.btnPersonalInfo);
        btnLanguage     = findViewById(R.id.btnLanguage);
        btnPoints       = findViewById(R.id.btnPoints);
        btnReviews      = findViewById(R.id.btnReviews);
        btnPolicies     = findViewById(R.id.btnPolicies);
        btnStoreList    = findViewById(R.id.btnStoreList);
        btnComplaint    = findViewById(R.id.btnComplaint);
    }

    private void setupClickListeners() {
        btnPersonalInfo.setOnClickListener(v ->
                startActivity(new Intent(this, PersonalInformationActivity.class)));
        btnLanguage.setOnClickListener(v ->
                startActivity(new Intent(this, ChangeLanguageActivity.class)));
        btnPoints.setOnClickListener(v ->
                startActivity(new Intent(this, EarnedPointHistoryActivity.class)));
        btnReviews.setOnClickListener(v ->
                startActivity(new Intent(this, MyReviewsActivity.class)));
        btnPolicies.setOnClickListener(v ->
                startActivity(new Intent(this, PolicyandTermActivity.class)));
        if (btnComplaint != null) {
            btnComplaint.setOnClickListener(v ->
                    startActivity(new Intent(this, ContactWithUs.class)));
        }
        if (btnStoreList != null) {
            btnStoreList.setOnClickListener(v ->
                    startActivity(new Intent(this, Agency.class)));
        }
    }

    private void setupBottomNav() {
        NavBarHelper.setupNavBar(this, NAV_IDS, R.id.nav_profile, v -> {
            int id = v.getId();
            if (id == R.id.nav_profile) return; // đang ở đây rồi

            Intent intent = null;
            if (id == R.id.nav_home)      intent = new Intent(this, Homepage.class);
            else if (id == R.id.nav_menu) intent = new Intent(this, Menu.class);
            else if (id == R.id.nav_orders)    intent = new Intent(this, OrderHistory.class);
            else if (id == R.id.nav_promotion) intent = new Intent(this, PromotionClient.class);

            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                overridePendingTransition(0, 0);
            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, Homepage.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
        overridePendingTransition(0, 0);
    }
}
