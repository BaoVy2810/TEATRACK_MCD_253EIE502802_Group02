package com.teatrack_mcd_253eie502802_group02.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Base64;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public final class AvatarBitmapHelper {

    private static final int AVATAR_SIZE_PX = 200;
    private static final int JPEG_QUALITY = 70;

    private AvatarBitmapHelper() {
    }

    @Nullable
    public static Bitmap prepareFromUri(@NonNull Context context, @NonNull Uri imageUri) throws IOException {
        Bitmap decoded = decodeBitmapFromUri(context, imageUri);
        if (decoded == null) {
            return null;
        }
        int orientation = readExifOrientation(context, imageUri);
        Bitmap oriented = applyExifOrientation(decoded, orientation);
        Bitmap squared = centerCropSquare(oriented);
        if (squared != oriented && oriented != decoded) {
            oriented.recycle();
        }
        if (decoded != oriented && decoded != squared) {
            decoded.recycle();
        }
        Bitmap resized = Bitmap.createScaledBitmap(squared, AVATAR_SIZE_PX, AVATAR_SIZE_PX, true);
        if (resized != squared) {
            squared.recycle();
        }
        return resized;
    }

    @Nullable
    public static String toBase64Jpeg(@Nullable Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, baos);
        return Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
    }

    @Nullable
    public static Bitmap decodeBase64(@Nullable String base64Image) {
        if (TextUtils.isEmpty(base64Image)) {
            return null;
        }
        try {
            byte[] decoded = Base64.decode(base64Image, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    @Nullable
    private static Bitmap decodeBitmapFromUri(@NonNull Context context, @NonNull Uri imageUri) throws IOException {
        try (InputStream inputStream = context.getContentResolver().openInputStream(imageUri)) {
            if (inputStream == null) {
                return null;
            }
            return BitmapFactory.decodeStream(inputStream);
        }
    }

    private static int readExifOrientation(@NonNull Context context, @NonNull Uri imageUri) {
        try (InputStream inputStream = context.getContentResolver().openInputStream(imageUri)) {
            if (inputStream == null) {
                return ExifInterface.ORIENTATION_NORMAL;
            }
            ExifInterface exif = new ExifInterface(inputStream);
            return exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        } catch (IOException ignored) {
            return ExifInterface.ORIENTATION_NORMAL;
        }
    }

    @NonNull
    static Bitmap applyExifOrientation(@NonNull Bitmap bitmap, int orientation) {
        Matrix matrix = new Matrix();
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.postRotate(90f);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.postRotate(180f);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.postRotate(270f);
                break;
            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                matrix.postScale(-1f, 1f);
                break;
            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                matrix.postScale(1f, -1f);
                break;
            case ExifInterface.ORIENTATION_TRANSPOSE:
                matrix.postRotate(90f);
                matrix.postScale(-1f, 1f);
                break;
            case ExifInterface.ORIENTATION_TRANSVERSE:
                matrix.postRotate(270f);
                matrix.postScale(-1f, 1f);
                break;
            case ExifInterface.ORIENTATION_NORMAL:
            case ExifInterface.ORIENTATION_UNDEFINED:
            default:
                return bitmap;
        }
        Bitmap rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        return rotated != null ? rotated : bitmap;
    }

    @NonNull
    static Bitmap centerCropSquare(@NonNull Bitmap source) {
        int width = source.getWidth();
        int height = source.getHeight();
        if (width <= 0 || height <= 0) {
            return source;
        }
        int size = Math.min(width, height);
        int x = (width - size) / 2;
        int y = (height - size) / 2;
        return Bitmap.createBitmap(source, x, y, size, size);
    }
}
