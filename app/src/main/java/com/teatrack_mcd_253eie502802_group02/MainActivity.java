package com.teatrack_mcd_253eie502802_group02;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;
import com.teatrack_mcd_253eie502802_group02.client.LoginActivity;
import com.teatrack_mcd_253eie502802_group02.client.Menu;

public class MainActivity extends AppCompatActivity {
    public static final String EXTRA_SELECTED_TAB = "extra_selected_tab";
    public static final String EXTRA_MENU_CATEGORY = "extra_menu_category";
    public static final String TAB_HOME = "home";
    public static final String TAB_MENU = "menu";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        routeToTarget(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        routeToTarget(intent);
    }

    private void routeToTarget(Intent intent) {
        // Kiểm tra xem có yêu cầu chuyển hướng đến tab cụ thể (ví dụ: Menu) không
        String selectedTab = intent != null ? intent.getStringExtra(EXTRA_SELECTED_TAB) : null;
        
        if (TAB_MENU.equals(selectedTab)) {
            navigateToMenu(intent.getStringExtra(EXTRA_MENU_CATEGORY));
            finish(); // Chỉ đóng MainActivity nếu đã chuyển hướng sang màn hình khác
            return;
        }

        // Nếu không có yêu cầu chuyển hướng, hiển thị màn hình chính (Get Started)
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
        startActivity(menuIntent);
    }
}
