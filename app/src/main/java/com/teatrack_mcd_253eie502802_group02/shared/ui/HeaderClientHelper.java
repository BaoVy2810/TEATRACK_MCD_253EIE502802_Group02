package com.teatrack_mcd_253eie502802_group02.shared.ui;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.ViewOutlineProvider;
import android.widget.ImageView;

import androidx.annotation.IdRes;
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

import java.util.Arrays;

public final class HeaderClientHelper {

    private static final String KEY_AVATAR_BASE64 = "avatarBase64";
    private static final String KEY_AVATAR_USER_ID = "avatarUserId";

    private HeaderClientHelper() {
    }

    public static void bindProfileAvatar(Activity activity) {
        bindAvatars(activity, R.id.imgProfileAvatar, R.id.imgUserAvatar);
    }

    public static void bindAvatar(Activity activity, @IdRes int imageViewId) {
        bindAvatars(activity, imageViewId);
    }

    public static void bindAvatars(Activity activity, @IdRes int... imageViewIds) {
        if (activity == null || imageViewIds == null || imageViewIds.length == 0) {
            return;
        }

        ImageView[] avatarViews = new ImageView[imageViewIds.length];
        int count = 0;
        for (int imageViewId : imageViewIds) {
            ImageView avatarView = activity.findViewById(imageViewId);
            if (avatarView != null) {
                avatarViews[count++] = avatarView;
            }
        }
        if (count == 0) {
            return;
        }

        ImageView[] targets = count == avatarViews.length
                ? avatarViews
                : Arrays.copyOf(avatarViews, count);

        SharedPreferences prefs = activity.getSharedPreferences(UserProfileHelper.PREF_NAME, Context.MODE_PRIVATE);
        String userId = UserProfileHelper.getUserId(activity);
        String cachedUserId = prefs.getString(KEY_AVATAR_USER_ID, "");
        String cachedAvatar = prefs.getString(KEY_AVATAR_BASE64, "");
        if (!userId.isEmpty()
                && userId.equals(cachedUserId)
                && cachedAvatar != null
                && !cachedAvatar.trim().isEmpty()) {
            applyAvatar(targets, cachedAvatar);
            return;
        }

        if (userId.isEmpty()) {
            applyDefaultAvatar(targets);
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
                            applyDefaultAvatar(targets);
                            return;
                        }
                        cacheAvatar(activity, userId, base64);
                        applyAvatar(targets, base64);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        applyDefaultAvatar(targets);
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

    private static void applyDefaultAvatar(ImageView... avatarViews) {
        for (ImageView avatarView : avatarViews) {
            if (avatarView == null) {
                continue;
            }
            prepareCircularAvatar(avatarView);
            avatarView.setImageResource(R.drawable.user);
            ImageViewCompat.setImageTintList(
                    avatarView,
                    ColorStateList.valueOf(ContextCompat.getColor(avatarView.getContext(), R.color.brand_blue))
            );
        }
    }

    private static void applyAvatar(ImageView[] avatarViews, String base64Image) {
        Bitmap bitmap = decodeAvatar(base64Image);
        if (bitmap == null) {
            applyDefaultAvatar(avatarViews);
            return;
        }
        for (ImageView avatarView : avatarViews) {
            if (avatarView == null) {
                continue;
            }
            prepareCircularAvatar(avatarView);
            ImageViewCompat.setImageTintList(avatarView, null);
            avatarView.setImageBitmap(bitmap);
        }
    }

    private static void prepareCircularAvatar(ImageView avatarView) {
        avatarView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        if (avatarView.getBackground() == null) {
            avatarView.setBackgroundResource(R.drawable.bg_avatar_circle);
        }
        avatarView.setOutlineProvider(ViewOutlineProvider.BACKGROUND);
        avatarView.setClipToOutline(true);
    }

    private static Bitmap decodeAvatar(String base64Image) {
        try {
            byte[] decoded = Base64.decode(base64Image, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }
}
