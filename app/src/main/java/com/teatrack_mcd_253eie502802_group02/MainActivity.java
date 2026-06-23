package com.teatrack_mcd_253eie502802_group02;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.teatrack_mcd_253eie502802_group02.admin.AdminDashboard;
import com.teatrack_mcd_253eie502802_group02.client.Homepage;
import com.teatrack_mcd_253eie502802_group02.client.LoginActivity;
import com.teatrack_mcd_253eie502802_group02.client.Menu;
import com.teatrack_mcd_253eie502802_group02.util.CategoryKeys;

public class MainActivity extends AppCompatActivity {
    public static final String EXTRA_SELECTED_TAB = "extra_selected_tab";
    public static final String EXTRA_MENU_CATEGORY = "extra_menu_category";
    public static final String TAB_HOME = "home";
    public static final String TAB_MENU = "menu";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Check if user is already logged in
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            android.content.SharedPreferences prefs = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
            String userId = prefs.getString("userId", null);
            String role = prefs.getString("role", "Customer");
            if (userId != null) {
                if ("Admin".equalsIgnoreCase(role)) {
                    startActivity(new Intent(this, AdminDashboard.class));
                } else {
                    startActivity(new Intent(this, Homepage.class));
                }
                finish();
                return;
            }
        }

        routeToTarget(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        routeToTarget(intent);
    }

    private void routeToTarget(Intent intent) {
        String selectedTab = intent != null ? intent.getStringExtra(EXTRA_SELECTED_TAB) : null;

        if (TAB_MENU.equals(selectedTab)) {
            navigateToMenu(CategoryKeys.normalize(intent.getStringExtra(EXTRA_MENU_CATEGORY)));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);
        LinearLayout layoutGetStarted = findViewById(R.id.layoutGetStarted);
        if (layoutGetStarted != null) {
            layoutGetStarted.setOnClickListener(v -> {
                Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(loginIntent);
            });
        }
    }

    public void navigateToMenu(String category) {
        Intent menuIntent = new Intent(this, Menu.class);
        if (category != null) {
            menuIntent.putExtra(EXTRA_MENU_CATEGORY, category);
        }
        menuIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(menuIntent);
    }
}
