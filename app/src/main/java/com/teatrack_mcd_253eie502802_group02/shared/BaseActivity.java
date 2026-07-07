package com.teatrack_mcd_253eie502802_group02.shared;

import android.content.Context;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.teatrack_mcd_253eie502802_group02.shared.ui.ChatbotFabHelper;

public class BaseActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getClass().getName().contains(".client.")) {
            com.teatrack_mcd_253eie502802_group02.util.CustomerSessionHelper.activateCustomerSession(this);
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        ChatbotFabHelper.attachIfNeeded(this);
    }
}
