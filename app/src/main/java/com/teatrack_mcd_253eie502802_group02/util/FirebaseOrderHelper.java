package com.teatrack_mcd_253eie502802_group02.util;

import androidx.annotation.Nullable;

import com.teatrack_mcd_253eie502802_group02.model.FirebaseOrder;

public final class FirebaseOrderHelper {

    private FirebaseOrderHelper() {
    }

    /** Firebase node key used when reading/writing orders/{key}. */
    @Nullable
    public static String resolveFirebaseKey(@Nullable FirebaseOrder order) {
        if (order == null) return null;
        String id = order.getId();
        if (id != null && !id.isEmpty()) return id;
        String orderId = order.getOrderId();
        if (orderId != null && !orderId.isEmpty()) return orderId;
        return null;
    }
}
