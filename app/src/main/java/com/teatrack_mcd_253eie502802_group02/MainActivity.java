package com.teatrack_mcd_253eie502802_group02;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.teatrack_mcd_253eie502802_group02.admin.AdminDashboard;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        startActivity(new Intent(this, AdminDashboard.class));
        finish();
    }
}