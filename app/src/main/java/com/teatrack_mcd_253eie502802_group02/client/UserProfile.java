package com.teatrack_mcd_253eie502802_group02.client;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.teatrack_mcd_253eie502802_group02.R;
import com.teatrack_mcd_253eie502802_group02.shared.ui.CartBadgeHelper;
import com.teatrack_mcd_253eie502802_group02.shared.ui.NavBarHelper;

public class UserProfile extends AppCompatActivity {

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

        setupBottomNav();
        CartBadgeHelper.setup(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        CartBadgeHelper.updateBadge(this);
    }

    private void setupBottomNav() {
        NavBarHelper.setupNavBar(this, NAV_IDS, R.id.nav_profile, this::onNavItemClicked);
    }

    private void onNavItemClicked(View view) {
        int id = view.getId();
        if (id == R.id.nav_profile) {
            return;
        }
        if (id == R.id.nav_home) {
            startActivity(new Intent(this, Homepage.class));
        } else if (id == R.id.nav_menu) {
            startActivity(new Intent(this, Menu.class));
        } else if (id == R.id.nav_orders) {
            startActivity(new Intent(this, OrderHistory.class));
        } else if (id == R.id.nav_promotion) {
            startActivity(new Intent(this, BlogGeneral.class));
        }
        finish();
    }
}
