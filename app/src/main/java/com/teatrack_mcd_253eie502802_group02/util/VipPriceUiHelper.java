package com.teatrack_mcd_253eie502802_group02.util;

import android.content.Context;
import android.graphics.Paint;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.teatrack_mcd_253eie502802_group02.R;

public final class VipPriceUiHelper {

    private static final float LOCKED_ALPHA = 0.45f;

    private VipPriceUiHelper() {
    }

    public static boolean isVipCustomer(Context context) {
        return UserRoleHelper.isVipCustomer(context);
    }

    public static void applyDetailPrices(
            Context context,
            TextView sizeBadgeM,
            TextView priceM,
            TextView vipBadgeM,
            TextView vipPriceM,
            TextView sizeBadgeL,
            TextView priceL,
            TextView vipBadgeL,
            TextView vipPriceL
    ) {
        applyProductPrices(context, sizeBadgeM, priceM, vipBadgeM, vipPriceM,
                sizeBadgeL, priceL, vipBadgeL, vipPriceL, false);
    }

    public static void applyMenuPrices(
            Context context,
            TextView sizeBadgeM,
            TextView priceM,
            TextView vipBadgeM,
            TextView vipPriceM,
            TextView sizeBadgeL,
            TextView priceL,
            TextView vipBadgeL,
            TextView vipPriceL
    ) {
        applyProductPrices(context, sizeBadgeM, priceM, vipBadgeM, vipPriceM,
                sizeBadgeL, priceL, vipBadgeL, vipPriceL, true);
    }

    public static void applyCardPrices(
            Context context,
            TextView sizeBadgeM,
            TextView priceM,
            TextView vipBadgeM,
            TextView vipPriceM,
            TextView sizeBadgeL,
            TextView priceL,
            TextView vipBadgeL,
            TextView vipPriceL
    ) {
        applyProductPrices(context, sizeBadgeM, priceM, vipBadgeM, vipPriceM,
                sizeBadgeL, priceL, vipBadgeL, vipPriceL, false);
    }

    private static void applyProductPrices(
            Context context,
            TextView sizeBadgeM,
            TextView priceM,
            TextView vipBadgeM,
            TextView vipPriceM,
            TextView sizeBadgeL,
            TextView priceL,
            TextView vipBadgeL,
            TextView vipPriceL,
            boolean menuStyle
    ) {
        boolean isVip = isVipCustomer(context);
        applyStandardRow(context, sizeBadgeM, priceM, isVip, menuStyle);
        applyStandardRow(context, sizeBadgeL, priceL, isVip, menuStyle);
        applyVipRow(context, vipBadgeM, vipPriceM, isVip, menuStyle);
        applyVipRow(context, vipBadgeL, vipPriceL, isVip, menuStyle);
    }

    private static void applyStandardRow(
            Context context,
            TextView badge,
            TextView price,
            boolean isVipAccount,
            boolean menuStyle
    ) {
        applyLockedPresentation(context, badge, price, isVipAccount, menuStyle, false);
    }

    private static void applyVipRow(
            Context context,
            TextView badge,
            TextView price,
            boolean isVipAccount,
            boolean menuStyle
    ) {
        applyLockedPresentation(context, badge, price, !isVipAccount, menuStyle, true);
    }

    private static void applyLockedPresentation(
            Context context,
            TextView badge,
            TextView price,
            boolean locked,
            boolean menuStyle,
            boolean isVipPrice
    ) {
        if (badge != null) {
            badge.setAlpha(locked ? LOCKED_ALPHA : 1f);
        }
        if (price == null) {
            return;
        }

        if (!locked) {
            price.setAlpha(1f);
            price.setPaintFlags(price.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
            if (isVipPrice && menuStyle) {
                price.setTextColor(0xFFC21919);
            } else if (menuStyle) {
                price.setTextColor(0xFF111111);
            } else {
                price.setTextColor(ContextCompat.getColor(context, R.color.black));
            }
            return;
        }

        price.setAlpha(LOCKED_ALPHA);
        price.setPaintFlags(price.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        price.setTextColor(ContextCompat.getColor(context, R.color.nav_inactive));
    }
}
