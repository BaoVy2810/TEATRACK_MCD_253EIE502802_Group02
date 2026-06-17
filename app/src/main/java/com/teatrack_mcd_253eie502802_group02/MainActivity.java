package com.teatrack_mcd_253eie502802_group02;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.teatrack_mcd_253eie502802_group02.R;
import com.teatrack_mcd_253eie502802_group02.client.LoginActivity;
import com.teatrack_mcd_253eie502802_group02.client.SucessfullyChangePasswordActivity;
import com.teatrack_mcd_253eie502802_group02.client.Homepage;
import com.teatrack_mcd_253eie502802_group02.client.Menu;
import com.teatrack_mcd_253eie502802_group02.admin.AdminAgency;
import com.teatrack_mcd_253eie502802_group02.admin.AdminDashboard;

public class MainActivity extends AppCompatActivity {
    public static final String EXTRA_SELECTED_TAB = "extra_selected_tab";
    public static final String EXTRA_MENU_CATEGORY = "extra_menu_category";
    public static final String TAB_HOME = "home";
    public static final String TAB_MENU = "menu";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        routeToTarget(getIntent());
        finish();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        routeToTarget(intent);
        finish();
    }

    private void routeToTarget(Intent intent) {
        String selectedTab = intent != null ? intent.getStringExtra(EXTRA_SELECTED_TAB) : null;
        if (TAB_MENU.equals(selectedTab)) {
            navigateToMenu(intent.getStringExtra(EXTRA_MENU_CATEGORY));
            return;
        }
        startActivity(new Intent(this, Homepage.class));
    }

    public void navigateToMenu(String category) {
        Intent menuIntent = new Intent(this, Menu.class);
        if (category != null) {
            menuIntent.putExtra(EXTRA_MENU_CATEGORY, category);
        }
        startActivity(menuIntent);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

// Trong onCreate(), sau dòng setContentView(...)
        LinearLayout layoutGetStarted = findViewById(R.id.layoutGetStarted);
        layoutGetStarted.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
        });
    }
}