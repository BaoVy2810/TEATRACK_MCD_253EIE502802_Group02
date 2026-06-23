package com.teatrack_mcd_253eie502802_group02.util;

import java.util.Locale;

public final class CategoryKeys {

    public static final String PURE_TEA = "Pure Tea";
    public static final String TEA_LATTE = "Tea Latte";
    public static final String MILK_TEA = "Milk Tea";
    public static final String NEW_ARRIVALS = "New Arrivals";
    public static final String BEST_SELLERS = "Best Sellers";
    public static final String FRUIT_TEA = "Fruit Tea";

    private CategoryKeys() {}

    public static String normalize(String category) {
        if (category == null) {
            return null;
        }
        String trimmed = category.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        String lower = trimmed.toLowerCase(Locale.ROOT);
        if (matches(lower, PURE_TEA, "loại thuần trà", "thuần trà")) {
            return PURE_TEA;
        }
        if (matches(lower, TEA_LATTE, "loại trà latte", "trà latte")) {
            return TEA_LATTE;
        }
        if (matches(lower, MILK_TEA, "loại trà sữa", "trà sữa")) {
            return MILK_TEA;
        }
        if (matches(lower, NEW_ARRIVALS, "new drinks", "thức uống mới", "new arrivals")) {
            return NEW_ARRIVALS;
        }
        if (matches(lower, BEST_SELLERS, "hot drinks", "thức uống hot", "best sellers")) {
            return BEST_SELLERS;
        }
        if (matches(lower, FRUIT_TEA, "loại trà trái cây", "trà trái cây", "fruit tea")) {
            return FRUIT_TEA;
        }
        return trimmed;
    }

    private static boolean matches(String lower, String canonical, String... aliases) {
        if (lower.equals(canonical.toLowerCase(Locale.ROOT))) {
            return true;
        }
        for (String alias : aliases) {
            if (lower.equals(alias)) {
                return true;
            }
        }
        return false;
    }
}
