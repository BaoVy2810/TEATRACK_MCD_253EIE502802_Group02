package com.teatrack_mcd_253eie502802_group02.util;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public final class CustomerSessionHelper {

    private CustomerSessionHelper() {
    }

    public static void activateCustomerSession(@Nullable Context context) {
        if (context == null) {
            return;
        }
        SharedPreferences prefs = context.getSharedPreferences(UserProfileHelper.PREF_NAME, Context.MODE_PRIVATE);
        String customerId = getCustomerUserId(context);
        if (customerId.isEmpty()) {
            return;
        }

        SharedPreferences.Editor editor = prefs.edit()
                .putString(UserProfileHelper.KEY_USER_ID, customerId)
                .putString(UserProfileHelper.KEY_USER_ROLE,
                        prefs.getString(UserProfileHelper.KEY_CUSTOMER_ROLE, "Customer"));

        String fullName = prefs.getString(UserProfileHelper.KEY_CUSTOMER_FULL_NAME, "");
        if (!fullName.isEmpty()) {
            editor.putString(UserProfileHelper.KEY_FULL_NAME, fullName);
        }
        String phone = prefs.getString(UserProfileHelper.KEY_CUSTOMER_PHONE, "");
        if (!phone.isEmpty()) {
            editor.putString(UserProfileHelper.KEY_PHONE, phone);
        }
        editor.apply();
    }

    @NonNull
    public static String getCustomerUserId(@Nullable Context context) {
        if (context == null) {
            return "";
        }
        SharedPreferences prefs = context.getSharedPreferences(UserProfileHelper.PREF_NAME, Context.MODE_PRIVATE);
        String customerId = prefs.getString(UserProfileHelper.KEY_CUSTOMER_USER_ID, "");
        if (customerId != null && !customerId.isEmpty()) {
            return customerId;
        }

        String role = prefs.getString(UserProfileHelper.KEY_USER_ROLE, "");
        if (!UserProfileHelper.isAdminRole(role)) {
            return UserProfileHelper.getUserId(context);
        }
        return "";
    }

    public static boolean hasCustomerSession(@Nullable Context context) {
        return !getCustomerUserId(context).isEmpty();
    }
}
