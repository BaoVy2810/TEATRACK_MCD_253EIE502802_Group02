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
import com.teatrack_mcd_253eie502802_group02.admin.AdminAccount;
import com.teatrack_mcd_253eie502802_group02.admin.AdminAgency;
import com.teatrack_mcd_253eie502802_group02.admin.AdminBlog;
import com.teatrack_mcd_253eie502802_group02.admin.AdminComplaints;
import com.teatrack_mcd_253eie502802_group02.admin.AdminDashboard;
import com.teatrack_mcd_253eie502802_group02.admin.AdminOrders;
import com.teatrack_mcd_253eie502802_group02.admin.AdminProduct;
import com.teatrack_mcd_253eie502802_group02.admin.AdminPromotion;

public class NavBarAdmin extends AppCompatActivity {

    private static final int[] NAV_ITEM_IDS = {
            R.id.nav_dashboard,
            R.id.nav_products,
            R.id.nav_orders,
            R.id.nav_account,
            R.id.nav_forum,
            R.id.nav_branch,
            R.id.nav_feedbacks,
            R.id.nav_promotion
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_nav_bar_admin);
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
            destination = AdminDashboard.class;
        } else if (id == R.id.nav_products) {
            destination = AdminProduct.class;
        } else if (id == R.id.nav_orders) {
            destination = AdminOrders.class;
        } else if (id == R.id.nav_account) {
            destination = AdminAccount.class;
        } else if (id == R.id.nav_forum) {
            destination = AdminBlog.class;
        } else if (id == R.id.nav_branch) {
            destination = AdminAgency.class;
        } else if (id == R.id.nav_feedbacks) {
            destination = AdminComplaints.class;
        } else if (id == R.id.nav_promotion) {
            destination = AdminPromotion.class;
        }

        if (destination != null) {
            startActivity(new Intent(this, destination));
        }
    }
}