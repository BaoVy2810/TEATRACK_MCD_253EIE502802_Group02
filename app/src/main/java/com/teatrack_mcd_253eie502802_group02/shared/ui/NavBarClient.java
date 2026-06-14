package com.teatrack_mcd_253eie502802_group02.shared.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.teatrack_mcd_253eie502802_group02.R;
import com.teatrack_mcd_253eie502802_group02.client.BlogGeneral;
import com.teatrack_mcd_253eie502802_group02.client.Homepage;
import com.teatrack_mcd_253eie502802_group02.client.Menu;
import com.teatrack_mcd_253eie502802_group02.client.OrderHistory;
import com.teatrack_mcd_253eie502802_group02.client.UserProfile;

public class NavBarClient extends AppCompatActivity {

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
        setContentView(R.layout.activity_nav_bar_client);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        NavBarHelper.setupNavBar(this, NAV_ITEM_IDS, R.id.nav_home, this::onNavItemClicked);
    }

    private void onNavItemClicked(View view) {
        for (int itemId : NAV_ITEM_IDS) {
            NavBarHelper.updateItemState(this, findViewById(itemId), itemId == view.getId());
        }

        Class<?> destination = null;
        int id = view.getId();
        if (id == R.id.nav_home) {
            destination = Homepage.class;
        } else if (id == R.id.nav_menu) {
            destination = Menu.class;
        } else if (id == R.id.nav_orders) {
            destination = OrderHistory.class;
        } else if (id == R.id.nav_promotion) {
            destination = BlogGeneral.class;
        } else if (id == R.id.nav_profile) {
            destination = UserProfile.class;
        }

        if (destination != null) {
            startActivity(new Intent(this, destination));
        }
    }
}
