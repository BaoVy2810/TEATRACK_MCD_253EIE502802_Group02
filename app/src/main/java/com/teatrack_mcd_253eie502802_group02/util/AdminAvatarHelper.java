package com.teatrack_mcd_253eie502802_group02.util;

import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.ImageViewCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.android.material.imageview.ShapeableImageView;
import com.teatrack_mcd_253eie502802_group02.data.FirebaseProductRepository;
import com.teatrack_mcd_253eie502802_group02.model.User;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public final class AdminAvatarHelper {

    private static final Map<String, String> AVATAR_BY_EMAIL = new HashMap<>();
    private static final Map<String, String> AVATAR_BY_USER_ID = new HashMap<>();
    private static boolean listening;

    private AdminAvatarHelper() {
    }

    public static void ensureListening() {
        if (listening) {
            return;
        }
        listening = true;
        FirebaseDatabase.getInstance(FirebaseProductRepository.DB_URL)
                .getReference("Users")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        AVATAR_BY_EMAIL.clear();
                        AVATAR_BY_USER_ID.clear();
                        for (DataSnapshot child : snapshot.getChildren()) {
                            User user = child.getValue(User.class);
                            if (user == null) {
                                continue;
                            }
                            String avatar = user.getAvatarBase64();
                            if (TextUtils.isEmpty(avatar)) {
                                continue;
                            }
                            String userId = !TextUtils.isEmpty(user.getId()) ? user.getId() : child.getKey();
                            if (!TextUtils.isEmpty(userId)) {
                                AVATAR_BY_USER_ID.put(userId, avatar);
                            }
                            if (!TextUtils.isEmpty(user.getEmail())) {
                                AVATAR_BY_EMAIL.put(user.getEmail().trim().toLowerCase(Locale.ROOT), avatar);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
    }

    public static void bindAvatar(
            @Nullable ImageView avatarView,
            @Nullable TextView initialView,
            @Nullable String userId,
            @Nullable String email,
            @Nullable String displayName) {
        if (avatarView == null && initialView == null) {
            return;
        }

        String base64 = resolveAvatar(userId, email);
        Bitmap bitmap = decodeAvatar(base64);
        if (bitmap != null) {
            if (avatarView != null) {
                avatarView.setVisibility(View.VISIBLE);
                avatarView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                ImageViewCompat.setImageTintList(avatarView, null);
                avatarView.setImageBitmap(bitmap);
            }
            if (initialView != null) {
                initialView.setVisibility(View.GONE);
            }
            return;
        }

        if (avatarView != null) {
            avatarView.setVisibility(View.VISIBLE);
            avatarView.setImageDrawable(null);
        }
        if (initialView != null) {
            initialView.setVisibility(View.VISIBLE);
            initialView.setText(getInitial(displayName, email));
        }
    }

    public static void bindAvatar(
            @Nullable ShapeableImageView avatarView,
            @Nullable String userId,
            @Nullable String email) {
        if (avatarView == null) {
            return;
        }

        String base64 = resolveAvatar(userId, email);
        Bitmap bitmap = decodeAvatar(base64);
        if (bitmap != null) {
            ImageViewCompat.setImageTintList(avatarView, null);
            avatarView.setImageBitmap(bitmap);
            return;
        }

        avatarView.setImageResource(com.teatrack_mcd_253eie502802_group02.R.drawable.user);
        ImageViewCompat.setImageTintList(
                avatarView,
                android.content.res.ColorStateList.valueOf(
                        avatarView.getContext().getColor(com.teatrack_mcd_253eie502802_group02.R.color.brand_blue)
                )
        );

        if (!TextUtils.isEmpty(email)) {
            fetchAvatarByEmail(email, avatarView);
        }
    }

    private static void fetchAvatarByEmail(@NonNull String email, @NonNull ShapeableImageView avatarView) {
        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
        FirebaseDatabase.getInstance(FirebaseProductRepository.DB_URL)
                .getReference("Users")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot child : snapshot.getChildren()) {
                            User user = child.getValue(User.class);
                            if (user == null || TextUtils.isEmpty(user.getEmail())) {
                                continue;
                            }
                            if (!normalizedEmail.equals(user.getEmail().trim().toLowerCase(Locale.ROOT))) {
                                continue;
                            }
                            String avatar = user.getAvatarBase64();
                            if (TextUtils.isEmpty(avatar)) {
                                return;
                            }
                            AVATAR_BY_EMAIL.put(normalizedEmail, avatar);
                            Bitmap bitmap = decodeAvatar(avatar);
                            if (bitmap != null) {
                                ImageViewCompat.setImageTintList(avatarView, null);
                                avatarView.setImageBitmap(bitmap);
                            }
                            return;
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
    }

    public static void bindUserAvatar(
            @Nullable ImageView avatarView,
            @Nullable TextView initialView,
            @NonNull User user) {
        String displayName = !TextUtils.isEmpty(user.getFullName()) ? user.getFullName() : user.getUsername();
        if (!TextUtils.isEmpty(user.getAvatarBase64())) {
            Bitmap bitmap = decodeAvatar(user.getAvatarBase64());
            if (bitmap != null) {
                if (avatarView != null) {
                    avatarView.setVisibility(View.VISIBLE);
                    avatarView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    ImageViewCompat.setImageTintList(avatarView, null);
                    avatarView.setImageBitmap(bitmap);
                }
                if (initialView != null) {
                    initialView.setVisibility(View.GONE);
                }
                return;
            }
        }
        bindAvatar(avatarView, initialView, user.getId(), user.getEmail(), displayName);
    }

    @Nullable
    private static String resolveAvatar(@Nullable String userId, @Nullable String email) {
        if (!TextUtils.isEmpty(userId) && AVATAR_BY_USER_ID.containsKey(userId)) {
            return AVATAR_BY_USER_ID.get(userId);
        }
        if (!TextUtils.isEmpty(email)) {
            return AVATAR_BY_EMAIL.get(email.trim().toLowerCase(Locale.ROOT));
        }
        return null;
    }

    @Nullable
    private static Bitmap decodeAvatar(@Nullable String base64Image) {
        return AvatarBitmapHelper.decodeBase64(base64Image);
    }

    @NonNull
    private static String getInitial(@Nullable String displayName, @Nullable String email) {
        String source = !TextUtils.isEmpty(displayName) ? displayName : email;
        if (TextUtils.isEmpty(source)) {
            return "?";
        }
        source = source.trim();
        return source.isEmpty() ? "?" : source.substring(0, 1).toUpperCase(Locale.getDefault());
    }
}
