package com.teatrack_mcd_253eie502802_group02;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.teatrack_mcd_253eie502802_group02.admin.AdminAgency;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // --- CODE ĐIỀU HƯỚNG VỀ ADMIN AGENCY ---
        // Khởi tạo Intent chuyển từ MainActivity sang AdminAgency
        Intent intent = new Intent(MainActivity.this, AdminAgency.class);
        startActivity(intent);

        // Đóng hẳn MainActivity để khi ở màn AdminAgency bấm nút Back sẽ thoát App luôn,
        // thay vì bị quay ngược lại một màn hình MainActivity trống.
        finish();
        // --------------------------------------
    }
}