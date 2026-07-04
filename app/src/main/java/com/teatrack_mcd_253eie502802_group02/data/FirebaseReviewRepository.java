package com.teatrack_mcd_253eie502802_group02.data;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.teatrack_mcd_253eie502802_group02.model.ProductReview;
import com.teatrack_mcd_253eie502802_group02.util.DateTimeHelper;
import com.teatrack_mcd_253eie502802_group02.util.ReviewIdGenerator;

import java.util.ArrayList;
import java.util.List;

public class FirebaseReviewRepository {

    private static final String NODE = "reviews";

    public interface ReviewsCallback {
        void onSuccess(List<ProductReview> reviews);
        void onError(String message);
    }

    public interface SubmitCallback {
        void onSuccess(ProductReview review);
        void onError(String message);
    }

    private final DatabaseReference reviewsRef;

    public FirebaseReviewRepository() {
        reviewsRef = FirebaseDatabase.getInstance(FirebaseProductRepository.DB_URL).getReference(NODE);
    }

    public void getReviewsForProduct(String productId, ReviewsCallback callback) {
        if (productId == null || productId.isEmpty()) {
            if (callback != null) callback.onSuccess(new ArrayList<>());
            return;
        }

        reviewsRef.child(productId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<ProductReview> reviews = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    ProductReview review = child.getValue(ProductReview.class);
                    if (review == null) continue;
                    if (review.getId() == null || review.getId().isEmpty()) {
                        review.setId(child.getKey());
                    }
                    if (review.getProductId() == null || review.getProductId().isEmpty()) {
                        review.setProductId(productId);
                    }
                    review.setCreatedAt(normalizeCreatedAt(child));
                    reviews.add(review);
                }
                reviews.sort((a, b) -> Long.compare(
                        DateTimeHelper.toEpochMillis(b.getCreatedAt()),
                        DateTimeHelper.toEpochMillis(a.getCreatedAt())));
                if (callback != null) callback.onSuccess(reviews);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (callback != null) callback.onError(error.getMessage());
            }
        });
    }

    public void submitReview(ProductReview review, SubmitCallback callback) {
        if (review == null || review.getProductId() == null || review.getProductId().isEmpty()) {
            if (callback != null) callback.onError("Invalid review data");
            return;
        }

        DatabaseReference productReviewsRef = reviewsRef.child(review.getProductId());
        ReviewIdGenerator.next(productReviewsRef, new ReviewIdGenerator.Callback() {
            @Override
            public void onGenerated(String reviewId) {
                review.setId(reviewId);
                if (review.getCreatedAt() == null || review.getCreatedAt().isEmpty()) {
                    review.setCreatedAt(DateTimeHelper.isoNow());
                }

                productReviewsRef.child(reviewId).setValue(review)
                        .addOnSuccessListener(unused -> {
                            if (callback != null) callback.onSuccess(review);
                        })
                        .addOnFailureListener(e -> {
                            if (callback != null) {
                                callback.onError(e.getMessage() != null
                                        ? e.getMessage() : "Failed to save review");
                            }
                        });
            }

            @Override
            public void onError(String message) {
                if (callback != null) {
                    callback.onError(message != null ? message : "Failed to generate review id");
                }
            }
        });
    }

    /** Legacy reviews may store createdAt as number; normalize to ISO string. */
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
