package com.teatrack_mcd_253eie502802_group02.shared.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.PopupWindow;

import com.google.firebase.auth.FirebaseAuth;
import com.teatrack_mcd_253eie502802_group02.MainActivity;
import com.teatrack_mcd_253eie502802_group02.R;

public final class HeaderMenuHelper {

    private HeaderMenuHelper() {
    }

    public static void setupProfileMenu(Activity activity) {
        View btnProfile = activity.findViewById(R.id.btn_profile);
        if (btnProfile == null) {
            return;
        }
        btnProfile.setOnClickListener(v -> showSignOutMenu(activity, v));
    }

    private static void showSignOutMenu(Activity activity, View anchor) {
        View popupView = LayoutInflater.from(activity).inflate(R.layout.popup_sign_out, null, false);
        PopupWindow popupWindow = new PopupWindow(
                popupView,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                true
        );
        popupWindow.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        popupWindow.setElevation(12f);
        popupWindow.setOutsideTouchable(true);

        popupView.setOnClickListener(v -> {
            popupWindow.dismiss();
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(activity, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                    | Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            activity.startActivity(intent);
            activity.finish();
        });

        popupView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        int xOff = anchor.getWidth() - popupView.getMeasuredWidth();
        popupWindow.showAsDropDown(anchor, xOff, 8, Gravity.END);
    }
}
