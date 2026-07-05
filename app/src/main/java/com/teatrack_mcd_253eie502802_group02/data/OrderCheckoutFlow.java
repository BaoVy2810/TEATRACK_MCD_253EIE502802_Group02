package com.teatrack_mcd_253eie502802_group02.data;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.teatrack_mcd_253eie502802_group02.R;
import com.teatrack_mcd_253eie502802_group02.client.Checkout;
import com.teatrack_mcd_253eie502802_group02.model.CartItem;
import com.teatrack_mcd_253eie502802_group02.model.FirebaseOrder;
import com.teatrack_mcd_253eie502802_group02.model.Order;

import java.util.List;

public final class OrderCheckoutFlow {

    private OrderCheckoutFlow() {
    }
    public static void handleCashPayment(Context context, Order order) {
        if (context instanceof Activity && ((Activity) context).isFinishing()) return;

        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Đang xác nhận đơn hàng...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        DatabaseReference db = FirebaseDatabase.getInstance().getReference("Orders");
        String orderId = db.push().getKey();

        if (orderId != null) {
            order.setId(orderId);
        }

        db.child(orderId).setValue(order).addOnCompleteListener(task -> {
            if (context instanceof Activity && !((Activity) context).isFinishing()) {
                if (progressDialog.isShowing()) progressDialog.dismiss();
            }

            if (task.isSuccessful()) {
                Intent intent = new Intent(context, Checkout.class);
                intent.putExtra("ORDER_ID", orderId);

                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

                context.startActivity(intent);

                if (context instanceof Activity) {
                    ((Activity) context).finish();
                }
            } else {
                Toast.makeText(context, "Lỗi: không thể lưu đơn hàng", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            if (context instanceof Activity && !((Activity) context).isFinishing()) {
                if (progressDialog.isShowing()) progressDialog.dismiss();
            }
            Toast.makeText(context, "Lỗi kết nối: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }    public static void placeOrderAndOpenCheckout(Activity activity, int paymentMethod,
                                                 String pickupAddress, String agencyId,
                                                 String recipientDetails, String note,
                                                 boolean clearTask, View confirmButton) {
        List<CartItem> items = CartManager.getInstance().getItems();
        if (activity == null || activity.isFinishing() || activity.isDestroyed()) {
            return;
        }

        FirebaseOrder order = OrderPlacementHelper.buildOrder(
                activity,
                items,
                paymentMethod,
                pickupAddress,
                agencyId,
                recipientDetails,
                note
        );

        new FirebaseOrderRepository().saveOrder(order, new FirebaseOrderRepository.SaveCallback() {
            @Override
            public void onSuccess(String orderId) {
                int orderTotal = order.getTotal();
                CartManager.getInstance().clear();

                Intent intent = new Intent(activity, Checkout.class);
                intent.putExtra(Checkout.EXTRA_ORDER_ID, orderId);
                intent.putExtra(Checkout.EXTRA_PICKUP_ADDRESS, pickupAddress);
                intent.putExtra(Checkout.EXTRA_ORDER_TOTAL, orderTotal);
                if (clearTask) {
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                }
                activity.startActivity(intent);
                activity.finish();
            }

            @Override
            public void onError(String message) {
                if (confirmButton != null) {
                    confirmButton.setEnabled(true);
                }
                Toast.makeText(activity, R.string.order_save_error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
