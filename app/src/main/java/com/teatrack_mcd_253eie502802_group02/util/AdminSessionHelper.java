package com.teatrack_mcd_253eie502802_group02.util;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.teatrack_mcd_253eie502802_group02.R;
import com.teatrack_mcd_253eie502802_group02.data.FirebaseProductRepository;
import com.teatrack_mcd_253eie502802_group02.model.User;

public final class AdminSessionHelper {

    private static final String ADMIN_USERNAME = "admin";

    public interface ResolveCallback {
        void onResolved(@NonNull String userId);

        void onFailed();
    }

    private AdminSessionHelper() {
    }

    public static boolean isAdminSession(@Nullable Context context) {
        if (context == null) {
            return false;
        }
        String role = context.getSharedPreferences(UserProfileHelper.PREF_NAME, Context.MODE_PRIVATE)
                .getString(UserProfileHelper.KEY_USER_ROLE, "");
        return "Admin".equalsIgnoreCase(role);
    }

    @Nullable
    public static String getCachedAdminUserId(@Nullable Context context) {
        if (!isAdminSession(context)) {
            return null;
        }
        String userId = UserProfileHelper.getUserId(context);
        return userId.isEmpty() ? null : userId;
    }

    public static void resolveAdminUserId(@NonNull Context context, @NonNull ResolveCallback callback) {
        String cached = getCachedAdminUserId(context);
        if (cached != null) {
            callback.onResolved(cached);
            return;
        }

        FirebaseDatabase.getInstance(FirebaseProductRepository.DB_URL)
                .getReference("Users")
                .orderByChild("name")
                .equalTo(ADMIN_USERNAME)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!snapshot.exists()) {
                            callback.onFailed();
                            return;
                        }
                        DataSnapshot adminSnap = snapshot.getChildren().iterator().next();
                        String userId = adminSnap.getKey();
                        if (userId == null || userId.isEmpty()) {
                            callback.onFailed();
                            return;
                        }
                        cacheAdminSnapshot(context, adminSnap);
                        callback.onResolved(userId);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onFailed();
                    }
                });
    }

    public static void cacheAdminSnapshot(@NonNull Context context, @NonNull DataSnapshot adminSnap) {
        SharedPreferences prefs = context.getSharedPreferences(UserProfileHelper.PREF_NAME, Context.MODE_PRIVATE);
        UserProfileHelper.cacheFromSnapshot(prefs, adminSnap);
        prefs.edit().putString(UserProfileHelper.KEY_USER_ROLE, "Admin").apply();
    }

    public static void bindHeaderAvatar(@NonNull Activity activity) {
        ShapeableImageView avatarView = activity.findViewById(R.id.imgHeaderAvatar);
        if (avatarView == null) {
            return;
        }

        AdminAvatarHelper.ensureListening();
        resolveAdminUserId(activity, new ResolveCallback() {
            @Override
            public void onResolved(@NonNull String userId) {
                FirebaseDatabase.getInstance(FirebaseProductRepository.DB_URL)
                        .getReference("Users")
                        .child(userId)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                String email = null;
                                User user = snapshot.getValue(User.class);
                                if (user != null && user.getEmail() != null) {
                                    email = user.getEmail();
                                }
                                AdminAvatarHelper.bindAvatar(avatarView, userId, email);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                AdminAvatarHelper.bindAvatar(avatarView, userId, null);
                            }
                        });
            }

            @Override
            public void onFailed() {
                AdminAvatarHelper.bindAvatar(avatarView, null, null);
            }
        });
    }
}
