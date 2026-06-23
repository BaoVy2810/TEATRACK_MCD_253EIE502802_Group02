package com.teatrack_mcd_253eie502802_group02.data;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.teatrack_mcd_253eie502802_group02.model.FirebaseOrder;

import java.util.HashMap;
import java.util.Map;

public class FirebaseOrderRepository {

    public interface SaveCallback {
        void onSuccess(String orderId);

        void onError(String message);
    }

    private final DatabaseReference rootRef;

    public FirebaseOrderRepository() {
        rootRef = FirebaseDatabase.getInstance().getReference();
    }

    public void saveOrder(FirebaseOrder order, SaveCallback callback) {
        if (order == null || order.getOrderId() == null || order.getOrderId().isEmpty()) {
            if (callback != null) {
                callback.onError("Invalid order data");
            }
            return;
        }

        String orderId = order.getOrderId();
        Map<String, Object> updates = new HashMap<>();
        updates.put("orders/" + orderId, order);

        rootRef.updateChildren(updates).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (callback != null) {
                    callback.onSuccess(orderId);
                }
            } else if (callback != null) {
                String message = task.getException() != null
                        ? task.getException().getMessage()
                        : "Failed to save order";
                callback.onError(message);
            }
        });
    }
}
