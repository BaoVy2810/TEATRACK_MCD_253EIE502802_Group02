package com.teatrack_mcd_253eie502802_group02.shared.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.teatrack_mcd_253eie502802_group02.R;
import com.teatrack_mcd_253eie502802_group02.client.Homepage;
import com.teatrack_mcd_253eie502802_group02.client.Menu;
import com.teatrack_mcd_253eie502802_group02.client.OrderHistory;
import com.teatrack_mcd_253eie502802_group02.client.UserProfile;

public class NavBarClient extends AppCompatActivity {

    private static final int[] NAV_ITEM_IDS = {
            R.id.nav_dashboard,
            R.id.nav_products,
            R.id.nav_orders,
            R.id.nav_account
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_nav_bar_client);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        NavBarHelper.setupNavBar(this, NAV_ITEM_IDS, R.id.nav_dashboard, this::onNavItemClicked);
    }

    private void onNavItemClicked(View view) {
        for (int itemId : NAV_ITEM_IDS) {
            NavBarHelper.updateItemState(this, findViewById(itemId), itemId == view.getId());
        }

        Class<?> destination = null;
        int id = view.getId();
        if (id == R.id.nav_dashboard) {
            destination = Homepage.class;
        } else if (id == R.id.nav_products) {
            destination = Menu.class;
        } else if (id == R.id.nav_orders) {
            destination = OrderHistory.class;
        } else if (id == R.id.nav_account) {
            destination = UserProfile.class;
        }

        if (destination != null) {
            startActivity(new Intent(this, destination));
        }
    }
}