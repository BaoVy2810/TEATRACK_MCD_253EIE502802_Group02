package com.teatrack_mcd_253eie502802_group02.shared.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.PopupWindow;

import androidx.activity.ComponentActivity;
import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

import com.google.firebase.auth.FirebaseAuth;
import com.teatrack_mcd_253eie502802_group02.MainActivity;
import com.teatrack_mcd_253eie502802_group02.R;
import com.teatrack_mcd_253eie502802_group02.admin.AdminProfile;
import com.teatrack_mcd_253eie502802_group02.util.AdminSessionHelper;

public final class HeaderMenuHelper {

    private HeaderMenuHelper() {
    }

    public static void setupProfileMenu(Activity activity) {
        View btnProfile = activity.findViewById(R.id.btn_profile);
        if (btnProfile == null) {
            return;
        }
        AdminSessionHelper.bindHeaderAvatar(activity);
        btnProfile.setOnClickListener(v -> showProfileMenu(activity, v));

        if (activity instanceof ComponentActivity) {
            ComponentActivity componentActivity = (ComponentActivity) activity;
            componentActivity.getLifecycle().addObserver(new DefaultLifecycleObserver() {
                @Override
                public void onResume(@NonNull LifecycleOwner owner) {
                    bindHeaderAvatar(activity);
                }
            });
        }
    }

    public static void bindHeaderAvatar(Activity activity) {
        AdminSessionHelper.bindHeaderAvatar(activity);
    }

    private static void showProfileMenu(Activity activity, View anchor) {
        View popupView = LayoutInflater.from(activity).inflate(R.layout.popup_admin_profile_menu, null, false);
        PopupWindow popupWindow = new PopupWindow(
                popupView,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                true
        );
        popupWindow.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        popupWindow.setElevation(12f);
        popupWindow.setOutsideTouchable(true);

        View profileItem = popupView.findViewById(R.id.tvProfile);
        View signOutItem = popupView.findViewById(R.id.tvSignOut);

        if (profileItem != null) {
            profileItem.setOnClickListener(v -> {
                popupWindow.dismiss();
                activity.startActivity(new Intent(activity, AdminProfile.class));
            });
        }

        if (signOutItem != null) {
            signOutItem.setOnClickListener(v -> {
                popupWindow.dismiss();
                FirebaseAuth.getInstance().signOut();
                com.teatrack_mcd_253eie502802_group02.util.UserProfileHelper.clearSession(activity);
                Intent intent = new Intent(activity, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                        | Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                activity.startActivity(intent);
                activity.finish();
            });
        }

        popupView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        int xOff = anchor.getWidth() - popupView.getMeasuredWidth();
        popupWindow.showAsDropDown(anchor, xOff, 8, Gravity.END);
    }
}
