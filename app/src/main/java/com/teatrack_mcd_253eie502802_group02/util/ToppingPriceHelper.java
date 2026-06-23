package com.teatrack_mcd_253eie502802_group02.util;

import java.util.LinkedHashMap;
import java.util.Map;

public final class ToppingPriceHelper {

    private static final Map<String, Integer> PRICES = new LinkedHashMap<>();

    static {
        PRICES.put("Sương sáo", 3000);
        PRICES.put("Thạch dừa nguyên vị", 3000);
        PRICES.put("Hạt é", 3000);
        PRICES.put("Thạch dứa hương đào", 5000);
        PRICES.put("Thạch aiyu", 5000);
        PRICES.put("Thạch sợi lá dứa", 5000);
        PRICES.put("Thạch sương sáo viên (8)", 5000);
        PRICES.put("Trân châu hoàng kim", 5000);
        PRICES.put("Trân châu đường đen", 5000);
        PRICES.put("Trân châu 3Q trắng/đen", 5000);
        PRICES.put("Trân châu khoai môn", 5000);
        PRICES.put("Hạt thủy tinh củ năng", 5000);
        PRICES.put("Hạt thủy tinh lúa mạch", 5000);
        PRICES.put("Đào miếng", 5000);
        PRICES.put("Khoai môn nghiền", 5000);
        PRICES.put("Hạt sen", 7000);
        PRICES.put("Kem tươi vani", 7000);
        PRICES.put("Kem cheese", 7000);
        PRICES.put("Pudding trứng", 7000);
        PRICES.put("Thạch sữa viên (8)", 7000);
    }

    private ToppingPriceHelper() {}

    public static int getPrice(String toppingName) {
        Integer price = PRICES.get(toppingName);
        return price != null ? price : 0;
    }
}
