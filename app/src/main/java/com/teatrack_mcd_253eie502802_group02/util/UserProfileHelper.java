package com.teatrack_mcd_253eie502802_group02.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.teatrack_mcd_253eie502802_group02.data.FirebaseProductRepository;
import com.teatrack_mcd_253eie502802_group02.model.User;

public final class UserProfileHelper {

    public static final String PREF_NAME = "LoginPrefs";
    public static final String KEY_USER_ID = "userId";
    public static final String KEY_USER_ROLE = "userRole";
    public static final String KEY_FULL_NAME = "userFullName";
    public static final String KEY_PHONE = "userPhone";
    public static final String KEY_USERNAME = "username";

    private UserProfileHelper() {
    }

    public static String resolveDisplayName(User user) {
        if (user == null) {
            return "";
        }
        String fullName = user.getFullName();
        if (fullName != null && !fullName.trim().isEmpty()) {
            return fullName.trim();
        }
        String username = user.getUsername();
        return username != null ? username.trim() : "";
    }

    public static String getDisplayFullName(Context context) {
        if (context == null) {
            return "";
        }
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String fullName = prefs.getString(KEY_FULL_NAME, "");
        if (fullName != null && !fullName.trim().isEmpty()) {
            return fullName.trim();
        }
        String username = prefs.getString("username", "");
        return username != null ? username.trim() : "";
    }

    public static String getUserId(Context context) {
        if (context == null) {
            return "";
        }
        String userId = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .getString(KEY_USER_ID, "");
        return userId != null ? userId : "";
    }

    public static void cacheFromSnapshot(SharedPreferences prefs, DataSnapshot snapshot) {
        if (prefs == null || snapshot == null || !snapshot.exists()) {
            return;
        }

        User user = snapshot.getValue(User.class);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_USER_ID, snapshot.getKey());

        if (user != null) {
            String role = user.getRole();
            if (role != null && !role.trim().isEmpty()) {
                editor.putString(KEY_USER_ROLE, role.trim());
            }
            String displayName = resolveDisplayName(user);
            if (!displayName.isEmpty()) {
                editor.putString(KEY_FULL_NAME, displayName);
            }
            String username = user.getUsername();
            if (username != null && !username.trim().isEmpty()) {
                editor.putString(KEY_USERNAME, username.trim());
            }
            String phone = user.getPhoneNumber();
            if (phone != null && !phone.trim().isEmpty()) {
                editor.putString(KEY_PHONE, phone.trim());
            }
            String avatar = user.getAvatarBase64();
            if (avatar != null && !avatar.trim().isEmpty()) {
                editor.putString("avatarBase64", avatar.trim());
                editor.putString("avatarUserId", snapshot.getKey());
            }
        }
        editor.apply();
    }

    public static void cacheProfile(
            Context context,
            String userId,
            String role,
            String fullName,
            String phone
    ) {
        if (context == null) {
            return;
        }
        SharedPreferences.Editor editor = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit();
        if (userId != null && !userId.isEmpty()) {
            editor.putString(KEY_USER_ID, userId);
        }
        if (role != null && !role.trim().isEmpty()) {
            editor.putString(KEY_USER_ROLE, role.trim());
        }
        if (fullName != null && !fullName.trim().isEmpty()) {
            editor.putString(KEY_FULL_NAME, fullName.trim());
        }
        if (phone != null && !phone.trim().isEmpty()) {
            editor.putString(KEY_PHONE, phone.trim());
        }
        editor.apply();
    }

    public static String getDisplayPhone(Context context) {
        if (context == null) {
            return "";
        }
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String phone = prefs.getString(KEY_PHONE, "");
        return phone != null ? phone.trim() : "";
    }

    public static void refreshFromFirebase(Context context, UserRoleHelper.RefreshCallback callback) {
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

        FirebaseDatabase.getInstance(FirebaseProductRepository.DB_URL)
                .getReference("Users")
                .child(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        cacheFromSnapshot(prefs, snapshot);
                        if (callback != null) {
                            callback.onComplete();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        if (callback != null) {
                            callback.onComplete();
                        }
                    }
                });
    }
}
