package com.teatrack_mcd_253eie502802_group02.util;

import java.util.Locale;

public final class PriceFormatHelper {

    private PriceFormatHelper() {
    }

    public static String formatVnd(int amount) {
        return String.format(Locale.US, "%,d", amount).replace(',', '.') + "đ";
    }

    public static String formatVndSigned(int amount) {
        if (amount < 0) {
            return "-" + formatVnd(Math.abs(amount));
        }
        return formatVnd(amount);
    }
}
