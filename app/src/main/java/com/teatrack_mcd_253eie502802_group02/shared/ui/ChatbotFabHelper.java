package com.teatrack_mcd_253eie502802_group02.shared.ui;

import android.app.Activity;
import android.content.Intent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.teatrack_mcd_253eie502802_group02.R;
import com.teatrack_mcd_253eie502802_group02.client.ChatbotBubble;
import com.teatrack_mcd_253eie502802_group02.client.FilledOTP;
import com.teatrack_mcd_253eie502802_group02.client.FilledOtpActivity;
import com.teatrack_mcd_253eie502802_group02.client.ForgotPassword;
import com.teatrack_mcd_253eie502802_group02.client.ForgotPasswordActivity;
import com.teatrack_mcd_253eie502802_group02.client.LoginActivity;
import com.teatrack_mcd_253eie502802_group02.client.PageNotFound;
import com.teatrack_mcd_253eie502802_group02.client.RegisterActivity;
import com.teatrack_mcd_253eie502802_group02.client.ResetPasswordActivity;
import com.teatrack_mcd_253eie502802_group02.client.SucessfullyChangePasswordActivity;

public final class ChatbotFabHelper {

    private ChatbotFabHelper() {
    }

    public static void attachIfNeeded(Activity activity) {
        if (!shouldShow(activity) || activity.findViewById(R.id.fabChatbot) != null) {
            return;
        }

        ViewGroup content = activity.findViewById(android.R.id.content);
        if (content == null) {
            return;
        }

        View overlay = LayoutInflater.from(activity).inflate(R.layout.layout_chatbot_fab, content, false);
        FloatingActionButton fab = overlay.findViewById(R.id.fabChatbot);
        if (fab == null) {
            return;
        }

        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) fab.getLayoutParams();
        params.gravity = Gravity.BOTTOM | Gravity.END;
        params.setMarginEnd(activity.getResources().getDimensionPixelSize(R.dimen.fab_margin_end));
        params.bottomMargin = activity.getResources().getDimensionPixelSize(
                hasClientNavBar(activity) ? R.dimen.fab_margin_bottom_above_nav : R.dimen.fab_margin_bottom);
        fab.setLayoutParams(params);
        fab.setOnClickListener(v ->
                activity.startActivity(new Intent(activity, ChatbotBubble.class)));

        content.addView(overlay);
    }

    private static boolean shouldShow(Activity activity) {
        String packageName = activity.getClass().getPackageName();
        if (!packageName.endsWith(".client")) {
            return false;
        }

        return !(activity instanceof LoginActivity)
                && !(activity instanceof RegisterActivity)
                && !(activity instanceof ForgotPassword)
                && !(activity instanceof ForgotPasswordActivity)
                && !(activity instanceof FilledOTP)
                && !(activity instanceof FilledOtpActivity)
                && !(activity instanceof ResetPasswordActivity)
                && !(activity instanceof SucessfullyChangePasswordActivity)
                && !(activity instanceof PageNotFound)
                && !(activity instanceof ChatbotBubble);
    }

    private static boolean hasClientNavBar(Activity activity) {
        return activity.findViewById(R.id.navBarClient) != null
                || activity.findViewById(R.id.layout_nav_bar) != null;
    }
}
