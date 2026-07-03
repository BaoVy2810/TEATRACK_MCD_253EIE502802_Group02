package com.teatrack_mcd_253eie502802_group02.util;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.Locale;

/** Generates review IDs: RW-01, RW-02, ... */
public final class ReviewIdGenerator {

    public interface Callback {
        void onGenerated(String reviewId);
        void onError(String message);
    }

    private ReviewIdGenerator() {
    }

    public static void next(DatabaseReference productReviewsRef, Callback callback) {
        productReviewsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int max = 0;
                for (DataSnapshot child : snapshot.getChildren()) {
                    String key = child.getKey();
                    if (key != null && key.matches("RW-\\d+")) {
                        try {
                            int num = Integer.parseInt(key.substring(3));
                            max = Math.max(max, num);
                        } catch (NumberFormatException ignored) {
                            // skip malformed keys
                        }
                    }
                }
                String newId = String.format(Locale.US, "RW-%02d", max + 1);
                callback.onGenerated(newId);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }
}
