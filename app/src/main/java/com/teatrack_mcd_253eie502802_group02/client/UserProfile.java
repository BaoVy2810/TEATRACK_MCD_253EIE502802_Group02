package com.teatrack_mcd_253eie502802_group02.client;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.teatrack_mcd_253eie502802_group02.R;
import com.teatrack_mcd_253eie502802_group02.shared.BaseActivity;
import com.teatrack_mcd_253eie502802_group02.shared.ui.NavBarHelper;

public class UserProfile extends BaseActivity {

    private LinearLayout btnPersonalInfo, btnLanguage, btnPoints, btnReviews, btnPolicies;
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
        setContentView(R.layout.activity_user_profile);
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
        btnPersonalInfo = findViewById(R.id.btnPersonalInfo);
        btnLanguage = findViewById(R.id.btnLanguage);
        btnPoints = findViewById(R.id.btnPoints);
        btnReviews = findViewById(R.id.btnReviews);
        btnPolicies = findViewById(R.id.btnPolicies);
    }

    private void setupClickListeners() {
        btnPersonalInfo.setOnClickListener(v -> {
            startActivity(new Intent(this, PersonalInformationActivity.class));
        });

        btnLanguage.setOnClickListener(v -> {
            startActivity(new Intent(this, ChangeLanguageActivity.class));
        });

        btnPoints.setOnClickListener(v -> {
            startActivity(new Intent(this, EarnedPointHistoryActivity.class));
        });

        btnReviews.setOnClickListener(v -> {
            startActivity(new Intent(this, MyReviewsActivity.class));
        });

        btnPolicies.setOnClickListener(v -> {
            startActivity(new Intent(this, PolicyandTermActivity.class));
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
                // Đang ở UserProfile, không cần chuyển trang
            }
        });
    }
}