package com.teatrack_mcd_253eie502802_group02.util;

import android.content.Context;
import android.graphics.Typeface;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.teatrack_mcd_253eie502802_group02.R;

import java.util.Locale;

public final class PaymentMethodBadgeHelper {

    private static final int TYPE_CASH_ON_HAND = 0;
    private static final int TYPE_CASH_IN_BANK = 1;
    private static final int TYPE_MOMO = 2;
    private static final int TYPE_ZALOPAY = 3;
    private static final int TYPE_EWALLET = 4;
    private static final int TYPE_UNKNOWN = 5;

    private PaymentMethodBadgeHelper() {
    }

    public static void apply(@NonNull TextView badge, @Nullable String paymentMethod) {
        Context context = badge.getContext();
        int type = resolveType(context, paymentMethod);

        String label;
        int bgRes;
        int textColor;

        switch (type) {
            case TYPE_MOMO:
                label = context.getString(R.string.payment_momo);
                bgRes = R.drawable.bg_payment_momo;
                textColor = ContextCompat.getColor(context, R.color.payment_momo_pink);
                break;
            case TYPE_ZALOPAY:
                label = context.getString(R.string.payment_zalopay);
                bgRes = R.drawable.bg_payment_zalopay;
                textColor = ContextCompat.getColor(context, R.color.payment_zalopay_blue);
                break;
            case TYPE_EWALLET:
                label = context.getString(R.string.payment_ewallet);
                bgRes = R.drawable.bg_payment_ewallet;
                textColor = ContextCompat.getColor(context, R.color.payment_ewallet_purple);
                break;
            case TYPE_CASH_IN_BANK:
                label = context.getString(R.string.cart_cash_in_bank);
                bgRes = R.drawable.bg_payment_cash_in_bank;
                textColor = 0xFF1E40AF;
                break;
            case TYPE_UNKNOWN:
                label = paymentMethod == null || paymentMethod.trim().isEmpty()
                        ? "—"
                        : paymentMethod.trim();
                bgRes = R.drawable.bg_payment_chip;
                textColor = 0xFF1E40AF;
                break;
            case TYPE_CASH_ON_HAND:
            default:
                label = context.getString(R.string.cart_cash_on_hand);
                bgRes = R.drawable.bg_payment_cash_on_hand;
                textColor = 0xFF065F46;
                break;
        }

        badge.setText(label);
        badge.setTypeface(badge.getTypeface(), Typeface.BOLD);
        badge.setTextColor(textColor);
        badge.setBackgroundResource(bgRes);
    }

    /** Plain secondary text — no badge background (e.g. admin order card rows). */
    public static void applyPlain(@NonNull TextView textView, @Nullable String paymentMethod) {
        Context context = textView.getContext();
        textView.setText(resolveLabel(context, paymentMethod));
        textView.setTypeface(Typeface.create(textView.getTypeface(), Typeface.NORMAL));
        textView.setBackground(null);
        textView.setTextColor(ContextCompat.getColor(context, R.color.text_secondary));
    }

    @NonNull
    public static String resolveLabel(@NonNull Context context, @Nullable String paymentMethod) {
        int type = resolveType(context, paymentMethod);
        switch (type) {
            case TYPE_MOMO:
                return context.getString(R.string.payment_momo);
            case TYPE_ZALOPAY:
                return context.getString(R.string.payment_zalopay);
            case TYPE_EWALLET:
                return context.getString(R.string.payment_ewallet);
            case TYPE_CASH_IN_BANK:
                return context.getString(R.string.cart_cash_in_bank);
            case TYPE_UNKNOWN:
                if (paymentMethod == null || paymentMethod.trim().isEmpty()) {
                    return "—";
                }
                return paymentMethod.trim();
            case TYPE_CASH_ON_HAND:
            default:
                return context.getString(R.string.cart_cash_on_hand);
        }
    }

    private static int resolveType(Context context, @Nullable String paymentMethod) {
        if (paymentMethod == null || paymentMethod.trim().isEmpty()) {
            return TYPE_UNKNOWN;
        }

        String raw = paymentMethod.trim();
        String lower = raw.toLowerCase(Locale.US);

        if (matches(context, lower, R.string.payment_momo) || lower.contains("momo")) {
            return TYPE_MOMO;
        }
        if (matches(context, lower, R.string.payment_zalopay)
                || lower.contains("zalopay") || lower.contains("zalo pay")) {
            return TYPE_ZALOPAY;
        }
        if (matches(context, lower, R.string.payment_ewallet)
                || lower.contains("e-wallet") || lower.contains("ewallet") || lower.contains("e wallet")) {
            return TYPE_EWALLET;
        }
        if (matches(context, lower, R.string.cart_cash_in_bank) || lower.contains("cash in bank")) {
            return TYPE_CASH_IN_BANK;
        }
        if (matches(context, lower, R.string.cart_cash_on_hand) || lower.contains("cash on hand")) {
            return TYPE_CASH_ON_HAND;
        }

        return TYPE_UNKNOWN;
    }

    private static boolean matches(Context context, String lower, int stringRes) {
        return lower.equals(context.getString(stringRes).toLowerCase(Locale.US));
    }
}
