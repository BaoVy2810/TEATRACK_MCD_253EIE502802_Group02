package com.teatrack_mcd_253eie502802_group02.shared.ui;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.teatrack_mcd_253eie502802_group02.R;
import com.teatrack_mcd_253eie502802_group02.data.FirebaseProductRepository;
import com.teatrack_mcd_253eie502802_group02.util.UserProfileHelper;

public final class HeaderClientHelper {

    private static final String KEY_AVATAR_BASE64 = "avatarBase64";
    private static final String KEY_AVATAR_USER_ID = "avatarUserId";

    private HeaderClientHelper() {
    }

    public static void bindProfileAvatar(Activity activity) {
        if (activity == null) {
            return;
        }
        ImageView avatarView = activity.findViewById(R.id.imgProfileAvatar);
        if (avatarView == null) {
            return;
        }

        SharedPreferences prefs = activity.getSharedPreferences(UserProfileHelper.PREF_NAME, Context.MODE_PRIVATE);
        String userId = UserProfileHelper.getUserId(activity);
        String cachedUserId = prefs.getString(KEY_AVATAR_USER_ID, "");
        String cachedAvatar = prefs.getString(KEY_AVATAR_BASE64, "");
        if (!userId.isEmpty()
                && userId.equals(cachedUserId)
                && cachedAvatar != null
                && !cachedAvatar.trim().isEmpty()) {
            applyAvatar(avatarView, cachedAvatar);
            return;
        }

        if (userId.isEmpty()) {
            applyDefaultAvatar(avatarView);
            return;
        }

        FirebaseDatabase.getInstance(FirebaseProductRepository.DB_URL)
                .getReference("Users")
                .child(userId)
                .child("avatarBase64")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String base64 = snapshot.getValue(String.class);
                        if (base64 == null || base64.trim().isEmpty()) {
                            applyDefaultAvatar(avatarView);
                            return;
                        }
                        cacheAvatar(activity, userId, base64);
                        applyAvatar(avatarView, base64);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        applyDefaultAvatar(avatarView);
                    }
                });
    }

    public static void cacheAvatar(Context context, String base64Image) {
        cacheAvatar(context, UserProfileHelper.getUserId(context), base64Image);
    }

    public static void cacheAvatar(Context context, String userId, String base64Image) {
        if (context == null || base64Image == null || base64Image.trim().isEmpty()) {
            return;
        }
        context.getSharedPreferences(UserProfileHelper.PREF_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_AVATAR_BASE64, base64Image.trim())
                .putString(KEY_AVATAR_USER_ID, userId != null ? userId : "")
                .apply();
    }

    private static void applyDefaultAvatar(ImageView avatarView) {
        avatarView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        avatarView.setImageResource(R.drawable.user);
        ImageViewCompat.setImageTintList(
                avatarView,
                ColorStateList.valueOf(ContextCompat.getColor(avatarView.getContext(), R.color.brand_blue))
        );
    }

    private static void applyAvatar(ImageView avatarView, String base64Image) {
        try {
            byte[] decoded = Base64.decode(base64Image, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
            if (bitmap != null) {
                avatarView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                ImageViewCompat.setImageTintList(avatarView, null);
                avatarView.setImageBitmap(bitmap);
                return;
            }
        } catch (IllegalArgumentException ignored) {
        }
        applyDefaultAvatar(avatarView);
    }
}
