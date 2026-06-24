package com.teatrack_mcd_253eie502802_group02.data;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.teatrack_mcd_253eie502802_group02.model.FirebaseOrder;

import java.util.HashMap;
import java.util.Map;

public class FirebaseOrderRepository {

    public interface SaveCallback {
        void onSuccess(String orderId);
        void onError(String message);
    }

    public interface OrderStatusListener {
        void onStatusChanged(String status);
        void onError(String errorMessage);
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

    public DatabaseReference listenToOrder(String orderId, OrderStatusListener listener) {
        DatabaseReference orderRef = rootRef.child("orders").child(orderId);
        orderRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String status = snapshot.child("status").getValue(String.class);
                    if (listener != null) {
                        listener.onStatusChanged(status);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (listener != null) {
                    listener.onError(error.getMessage());
                }
            }
        });
        return orderRef;
    }

    public void updateOrderStatus(String orderId, String newStatus, OnCompleteListener<Void> callback) {
        rootRef.child("orders").child(orderId).child("status").setValue(newStatus)
                .addOnCompleteListener(callback);
    }
}
