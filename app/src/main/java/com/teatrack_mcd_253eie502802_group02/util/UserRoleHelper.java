package com.teatrack_mcd_253eie502802_group02.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.teatrack_mcd_253eie502802_group02.R;

import java.util.Locale;

public final class UserRoleHelper {

    private static final String PREF_NAME = "LoginPrefs";
    private static final String KEY_USER_ROLE = "userRole";
    private static final String KEY_USER_ID = "userId";

    public interface RefreshCallback {
        void onComplete();
    }

    private UserRoleHelper() {
    }

    public static String getUserRole(Context context) {
        if (context == null) {
            return "";
        }
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .getString(KEY_USER_ROLE, context.getString(R.string.role_customer));
    }

    public static boolean isVipCustomer(Context context) {
        return isVipRole(getUserRole(context));
    }

    public static boolean isVipRole(String role) {
        if (role == null) {
            return false;
        }
        String normalized = role.trim().toLowerCase(Locale.ROOT);
        if (normalized.isEmpty() || normalized.equals("customer")) {
            return false;
        }
        return normalized.contains("vip");
    }

    public static String getRoleDisplayLabel(Context context, String role) {
        if (context == null) {
            return "";
        }
        if (role == null || role.trim().isEmpty()) {
            return context.getString(R.string.role_customer);
        }
        String normalized = role.trim();
        if (normalized.equalsIgnoreCase(context.getString(R.string.role_customer_vip))) {
            return context.getString(R.string.loyalty_vip_label);
        }
        if (normalized.equalsIgnoreCase(context.getString(R.string.role_customer))) {
            return context.getString(R.string.loyalty_member_label);
        }
        if (normalized.equalsIgnoreCase(context.getString(R.string.role_admin))) {
            return context.getString(R.string.role_admin);
        }
        return normalized;
    }

    public static void refreshRoleFromFirebase(Context context, RefreshCallback callback) {
        if (context == null) {
            if (callback != null) {
                callback.onComplete();
            }
            return;
        }

        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String userId = prefs.getString(KEY_USER_ID, "");
        if (userId == null || userId.isEmpty()) {
            if (callback != null) {
                callback.onComplete();
            }
            return;
        }

        UserProfileHelper.refreshFromFirebase(context, callback);
    }
}
