package com.teatrack_mcd_253eie502802_group02.data;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.teatrack_mcd_253eie502802_group02.model.Product;
import com.teatrack_mcd_253eie502802_group02.model.ProductReview;
import com.teatrack_mcd_253eie502802_group02.util.DateTimeHelper;
import com.teatrack_mcd_253eie502802_group02.util.ReviewStatsHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Seeds mock reviews (3 per product) when empty, then returns aggregated stats for product cards.
 */
public class ReviewCatalogSync {

    public interface Callback {
        void onReady(Map<String, ReviewStatsHelper.Stats> statsByProductId);
        void onError(String message);
    }

    private static final String NODE = "reviews";

    private static final String[] MOCK_NAMES = {
            "Nguyễn Minh An",
            "Trần Thảo Vy",
            "Lê Hoàng Nam",
            "Phạm Bảo Ngọc",
            "Hoàng Quốc Bảo",
            "Võ Lan Chi"
    };

    private static final String[] MOCK_TITLES = {
            "Rất ngon, recommend!",
            "Chất lượng ổn áp",
            "Sẽ quay lại mua tiếp",
            "Đúng vị yêu thích",
            "Phục vụ nhiệt tình"
    };

    private static final String[] MOCK_COMMENTS = {
            "Nước thơm, vị cân bằng, uống rất đã.",
            "Ngọt vừa phải, topping tươi, rất hài lòng.",
            "Món này ngon thật, mình sẽ giới thiệu bạn bè.",
            "Đóng gói cẩn thận, hương vị chuẩn Ngô Gia.",
            "Nhân viên thân thiện, đồ uống chất lượng."
    };

    private final DatabaseReference reviewsRef;
    private final DatabaseReference rootRef;

    public ReviewCatalogSync() {
        rootRef = FirebaseDatabase.getInstance(FirebaseProductRepository.DB_URL).getReference();
        reviewsRef = rootRef.child(NODE);
    }

    public void syncProducts(List<Product> products, Callback callback) {
        if (products == null || products.isEmpty()) {
            if (callback != null) {
                callback.onReady(new HashMap<>());
            }
            return;
        }

        reviewsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Map<String, List<ProductReview>> reviewsByProduct = parseSnapshot(snapshot);
                Map<String, Object> seedUpdates = buildSeedUpdates(products, reviewsByProduct);
                if (seedUpdates.isEmpty()) {
                    deliverStats(reviewsByProduct, callback);
                    return;
                }
                rootRef.updateChildren(seedUpdates).addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        if (callback != null) {
                            String message = task.getException() != null
                                    ? task.getException().getMessage()
                                    : "Failed to seed reviews";
                            callback.onError(message);
                        }
                        return;
                    }
                    mergeSeededReviews(reviewsByProduct, seedUpdates);
                    deliverStats(reviewsByProduct, callback);
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (callback != null) {
                    callback.onError(error.getMessage());
                }
            }
        });
    }

    private Map<String, List<ProductReview>> parseSnapshot(DataSnapshot snapshot) {
        Map<String, List<ProductReview>> map = new HashMap<>();
        for (DataSnapshot productNode : snapshot.getChildren()) {
            String productId = productNode.getKey();
            if (productId == null) {
                continue;
            }
            List<ProductReview> reviews = new ArrayList<>();
            for (DataSnapshot child : productNode.getChildren()) {
                ProductReview review = child.getValue(ProductReview.class);
                if (review == null) {
                    continue;
                }
                if (review.getId() == null || review.getId().isEmpty()) {
                    review.setId(child.getKey());
                }
                review.setProductId(productId);
                review.setCreatedAt(normalizeCreatedAt(child));
                reviews.add(review);
            }
            map.put(productId, reviews);
        }
        return map;
    }

    private Map<String, Object> buildSeedUpdates(List<Product> products,
                                                 Map<String, List<ProductReview>> existing) {
        Map<String, Object> updates = new HashMap<>();
        int productIndex = 0;
        for (Product product : products) {
            if (product == null || product.getId() == null || product.getId().isEmpty()) {
                productIndex++;
                continue;
            }
            List<ProductReview> current = existing.get(product.getId());
            if (current != null && !current.isEmpty()) {
                productIndex++;
                continue;
            }

            float[] ratings = ratingsForProduct(productIndex);
            List<ProductReview> seeded = new ArrayList<>();
            for (int i = 0; i < 3; i++) {
                String reviewId = String.format(Locale.US, "RW-%02d", i + 1);
                ProductReview review = buildMockReview(product, reviewId, ratings[i], productIndex + i);
                String path = NODE + "/" + product.getId() + "/" + reviewId;
                updates.put(path, review);
                seeded.add(review);
            }
            existing.put(product.getId(), seeded);
            productIndex++;
        }
        return updates;
    }

    @SuppressWarnings("unchecked")
    private void mergeSeededReviews(Map<String, List<ProductReview>> reviewsByProduct,
                                    Map<String, Object> seedUpdates) {
        for (Map.Entry<String, Object> entry : seedUpdates.entrySet()) {
            if (!(entry.getValue() instanceof ProductReview)) {
                continue;
            }
            ProductReview review = (ProductReview) entry.getValue();
            String productId = review.getProductId();
            if (productId == null) {
                continue;
            }
            List<ProductReview> list = reviewsByProduct.computeIfAbsent(productId, key -> new ArrayList<>());
            list.add(review);
        }
    }

    private void deliverStats(Map<String, List<ProductReview>> reviewsByProduct, Callback callback) {
        if (callback != null) {
            callback.onReady(ReviewStatsHelper.buildStatsMap(reviewsByProduct));
        }
    }

    private float[] ratingsForProduct(int productIndex) {
        int bucket = Math.floorMod(productIndex, 3);
        if (bucket == 0) {
            return new float[]{5.0f, 5.0f, 5.0f};
        }
        if (bucket == 1) {
            return new float[]{5.0f, 5.0f, 4.8f};
        }
        return new float[]{5.0f, 4.8f, 4.6f};
    }

    private ProductReview buildMockReview(Product product, String reviewId, float rating, int seedIndex) {
        long createdAtMillis = System.currentTimeMillis()
                - TimeUnit.DAYS.toMillis(10L - (seedIndex % 3))
                - TimeUnit.HOURS.toMillis(seedIndex * 5L);

        ProductReview review = new ProductReview();
        review.setId(reviewId);
        review.setProductId(product.getId());
        review.setUserId("");
        review.setUserName(MOCK_NAMES[seedIndex % MOCK_NAMES.length]);
        review.setRating(rating);
        review.setTitle(MOCK_TITLES[seedIndex % MOCK_TITLES.length]);
        review.setComment(MOCK_COMMENTS[seedIndex % MOCK_COMMENTS.length]);
        review.setCreatedAt(DateTimeHelper.isoFromMillis(createdAtMillis));
        return review;
    }

    private String normalizeCreatedAt(DataSnapshot child) {
        Object raw = child.child("createdAt").getValue();
        if (raw instanceof Long) {
            return DateTimeHelper.isoFromMillis((Long) raw);
        }
        if (raw instanceof Double) {
            return DateTimeHelper.isoFromMillis(((Double) raw).longValue());
        }
        if (raw instanceof String) {
            String value = ((String) raw).trim();
            if (value.matches("\\d+")) {
                return DateTimeHelper.isoFromMillis(Long.parseLong(value));
            }
            return value;
        }
        return DateTimeHelper.isoNow();
    }
}
