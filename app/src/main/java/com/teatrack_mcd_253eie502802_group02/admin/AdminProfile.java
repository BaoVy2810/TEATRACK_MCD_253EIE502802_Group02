package com.teatrack_mcd_253eie502802_group02.admin;

import android.os.Bundle;

import com.teatrack_mcd_253eie502802_group02.R;
import com.teatrack_mcd_253eie502802_group02.shared.BaseActivity;
import com.teatrack_mcd_253eie502802_group02.shared.ui.AdminInsetsHelper;
import com.teatrack_mcd_253eie502802_group02.shared.ui.HeaderMenuHelper;

public class AdminProfile extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_profile);
        AdminInsetsHelper.apply(this);
        HeaderMenuHelper.setupProfileMenu(this);
    }
}
