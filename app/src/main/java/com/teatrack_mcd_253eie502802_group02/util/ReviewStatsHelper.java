package com.teatrack_mcd_253eie502802_group02.util;

import com.teatrack_mcd_253eie502802_group02.model.Product;
import com.teatrack_mcd_253eie502802_group02.model.ProductReview;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class ReviewStatsHelper {

    public static final class Stats {
        public final float average;
        public final int count;

        public Stats(float average, int count) {
            this.average = average;
            this.count = count;
        }
    }

    private ReviewStatsHelper() {
    }

    public static Map<String, Stats> buildStatsMap(Map<String, List<ProductReview>> reviewsByProduct) {
        Map<String, Stats> statsMap = new HashMap<>();
        if (reviewsByProduct == null) {
            return statsMap;
        }
        for (Map.Entry<String, List<ProductReview>> entry : reviewsByProduct.entrySet()) {
            List<ProductReview> reviews = entry.getValue();
            if (reviews == null || reviews.isEmpty()) {
                continue;
            }
            float sum = 0f;
            for (ProductReview review : reviews) {
                sum += review.getRating();
            }
            statsMap.put(entry.getKey(), new Stats(sum / reviews.size(), reviews.size()));
        }
        return statsMap;
    }

    public static void applyStatsToProducts(List<Product> products, Map<String, Stats> statsMap) {
        if (products == null || statsMap == null) {
            return;
        }
        for (Product product : products) {
            if (product == null || product.getId() == null) {
                continue;
            }
            Stats stats = statsMap.get(product.getId());
            if (stats != null && stats.count > 0) {
                product.setRating(stats.average);
                product.setReviewCount(formatCount(stats.count));
            }
        }
    }

    public static String formatCount(int count) {
        if (count >= 1000) {
            if (count % 1000 == 0) {
                return (count / 1000) + "k";
            }
            return String.format(Locale.getDefault(), "%.1fk", count / 1000f);
        }
        return String.valueOf(count);
    }

    public static String formatRating(float rating) {
        return String.format(Locale.getDefault(), "%.1f", rating);
    }
}
