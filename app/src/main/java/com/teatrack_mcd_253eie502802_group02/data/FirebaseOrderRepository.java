package com.teatrack_mcd_253eie502802_group02.data;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.teatrack_mcd_253eie502802_group02.model.FirebaseOrder;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class FirebaseOrderRepository {

    public interface SaveCallback {
        void onSuccess(String orderId);
        void onError(String message);
    }

    public interface OrderStatusListener {
        void onStatusChanged(String status);
        void onError(String errorMessage);
    }

    private interface AgencyCallback {
        void onResolved(String agencyId);
    }

    private final DatabaseReference rootRef;

    public FirebaseOrderRepository() {
        rootRef = FirebaseDatabase.getInstance().getReference();
    }

    public void saveOrder(FirebaseOrder order, SaveCallback callback) {
        if (order == null || order.getOrderId() == null || order.getOrderId().isEmpty()) {
            if (callback != null) callback.onError("Invalid order data");
            return;
        }

        // Nếu đã có agencyId rồi thì save thẳng
        if (order.getAgencyId() != null && !order.getAgencyId().isEmpty()) {
            doSave(order, callback);
            return;
        }

        // Chưa có → tìm agency phù hợp theo địa chỉ khách
        resolveAgencyId(order.getCustomerAddress(), agencyId -> {
            if (agencyId != null) {
                order.setAgencyId(agencyId);
            }
            doSave(order, callback);
        });
    }

    private void doSave(FirebaseOrder order, SaveCallback callback) {
        String orderId = order.getOrderId();
        Map<String, Object> updates = new HashMap<>();
        updates.put("orders/" + orderId, order);

        rootRef.updateChildren(updates).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (callback != null) callback.onSuccess(orderId);
            } else if (callback != null) {
                String message = task.getException() != null
                        ? task.getException().getMessage()
                        : "Failed to save order";
                callback.onError(message);
            }
        });
    }

    private void resolveAgencyId(String customerAddress, AgencyCallback callback) {
        if (customerAddress == null || customerAddress.isEmpty()) {
            callback.onResolved(null);
            return;
        }

        rootRef.child("agencies").get().addOnSuccessListener(snapshot -> {
            String normalizedCustomer = normalizeAddress(customerAddress);
            String bestAgencyId = null;
            int bestScore = 0;

            for (DataSnapshot agency : snapshot.getChildren()) {
                String agencyAddress = agency.child("address").getValue(String.class);
                if (agencyAddress == null) continue;

                int score = countCommonTokens(normalizedCustomer, normalizeAddress(agencyAddress));
                if (score > bestScore) {
                    bestScore = score;
                    bestAgencyId = agency.getKey();
                }
            }

            callback.onResolved(bestScore > 0 ? bestAgencyId : null);

        }).addOnFailureListener(e -> callback.onResolved(null));
    }

    private String normalizeAddress(String s) {
        return s.toLowerCase()
                .replaceAll("[àáạảãăắặẳẵâấậẩẫ]", "a")
                .replaceAll("[èéẹẻẽêếệểễ]", "e")
                .replaceAll("[ìíịỉĩ]", "i")
                .replaceAll("[òóọỏõôốộổỗơớợởỡ]", "o")
                .replaceAll("[ùúụủũưứựửữ]", "u")
                .replaceAll("[ỳýỵỷỹ]", "y")
                .replaceAll("[đ]", "d")
                .replaceAll("[^a-z0-9 ]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private int countCommonTokens(String a, String b) {
        Set<String> setA = new HashSet<>(Arrays.asList(a.split(" ")));
        int count = 0;
        for (String token : b.split(" ")) {
            if (token.length() >= 4 && setA.contains(token)) count++;
        }
        return count;
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