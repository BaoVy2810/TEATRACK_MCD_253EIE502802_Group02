package com.teatrack_mcd_253eie502802_group02.shared.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import com.teatrack_mcd_253eie502802_group02.util.AvatarBitmapHelper;
import android.graphics.drawable.ColorDrawable;
import android.util.Base64;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.ImageView;
import android.widget.PopupWindow;

import androidx.activity.ComponentActivity;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.teatrack_mcd_253eie502802_group02.MainActivity;
import com.teatrack_mcd_253eie502802_group02.R;
import com.teatrack_mcd_253eie502802_group02.data.FirebaseProductRepository;
import com.teatrack_mcd_253eie502802_group02.util.CustomerSessionHelper;
import com.teatrack_mcd_253eie502802_group02.util.UserProfileHelper;

import java.util.Arrays;

public final class HeaderClientHelper {

    private static final String KEY_AVATAR_BASE64 = "avatarBase64";
    private static final String KEY_AVATAR_USER_ID = "avatarUserId";

    private HeaderClientHelper() {
    }

    public static void setupSignOutMenu(Activity activity) {
        View btnProfile = activity.findViewById(R.id.btn_profile);
        if (btnProfile == null) {
            return;
        }
        bindProfileAvatar(activity);
        btnProfile.setOnClickListener(v -> showSignOutMenu(activity, v));

        if (activity instanceof ComponentActivity) {
            ComponentActivity componentActivity = (ComponentActivity) activity;
            componentActivity.getLifecycle().addObserver(new DefaultLifecycleObserver() {
                @Override
                public void onResume(@NonNull LifecycleOwner owner) {
                    bindProfileAvatar(activity);
                }
            });
        }
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

        View signOutItem = popupView.findViewById(R.id.tvSignOut);
        if (signOutItem != null) {
            signOutItem.setOnClickListener(v -> {
                popupWindow.dismiss();
                FirebaseAuth.getInstance().signOut();
                UserProfileHelper.clearSession(activity);
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
        String userId = CustomerSessionHelper.getCustomerUserId(activity);
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
        cacheAvatar(context, CustomerSessionHelper.getCustomerUserId(context), base64Image);
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
        return AvatarBitmapHelper.decodeBase64(base64Image);
    }
}
