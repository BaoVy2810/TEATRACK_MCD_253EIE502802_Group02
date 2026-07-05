package com.teatrack_mcd_253eie502802_group02.util;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.teatrack_mcd_253eie502802_group02.R;

import java.util.Locale;

public final class OrderStatusBadgeHelper {

    private OrderStatusBadgeHelper() {
    }

    public static void apply(@NonNull TextView badge, @NonNull String status) {
        Context context = badge.getContext();
        String normalized = status == null ? "" : status.trim().toLowerCase(Locale.US);

        String label;
        int bgRes;
        int textColor;

        switch (normalized) {
            case "pending":
                label = context.getString(R.string.str_status_pending);
                bgRes = R.drawable.bg_status_pending;
                textColor = 0xFFEA580C;
                break;
            case "processing":
                label = context.getString(R.string.str_status_processing);
                bgRes = R.drawable.bg_status_processing;
                textColor = 0xFF2563EB;
                break;
            case "ready":
                label = context.getString(R.string.str_status_ready);
                bgRes = R.drawable.bg_status_ready;
                textColor = 0xFF7C3AED;
                break;
            case "shipping":
                label = context.getString(R.string.str_status_shipping);
                bgRes = R.drawable.bg_status_shipping;
                textColor = 0xFF059669;
                break;
            case "delivered":
            case "completed":
                label = context.getString(R.string.str_status_delivered);
                bgRes = R.drawable.bg_status_delivered;
                textColor = 0xFF16A34A;
                break;
            case "cancelled":
                label = context.getString(R.string.str_status_cancelled);
                bgRes = R.drawable.bg_status_cancelled;
                textColor = ContextCompat.getColor(context, R.color.Red);
                break;
            default:
                label = normalized.isEmpty() ? "—" : status;
                bgRes = R.drawable.bg_status_pending;
                textColor = 0xFFEA580C;
                break;
        }

        badge.setText(label);
        badge.setTypeface(badge.getTypeface(), Typeface.BOLD);
        badge.setTextColor(textColor);
        badge.setBackgroundResource(bgRes);
        applyDot(badge, textColor);
    }

    private static void applyDot(TextView badge, int color) {
        float density = badge.getResources().getDisplayMetrics().density;
        int size = (int) (6 * density);

        GradientDrawable dot = new GradientDrawable();
        dot.setShape(GradientDrawable.OVAL);
        dot.setColor(color);
        dot.setSize(size, size);

        int padding = (int) (5 * density);
        dot.setBounds(0, 0, size, size);
        badge.setCompoundDrawables(dot, null, null, null);
        badge.setCompoundDrawablePadding(padding);
    }
}
