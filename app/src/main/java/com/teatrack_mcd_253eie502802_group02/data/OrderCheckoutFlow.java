package com.teatrack_mcd_253eie502802_group02.data;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.Toast;

import com.teatrack_mcd_253eie502802_group02.R;
import com.teatrack_mcd_253eie502802_group02.client.Checkout;
import com.teatrack_mcd_253eie502802_group02.model.CartItem;
import com.teatrack_mcd_253eie502802_group02.model.FirebaseOrder;

import java.util.List;

public final class OrderCheckoutFlow {

    private OrderCheckoutFlow() {
    }

    public static void placeOrderAndOpenCheckout(Activity activity, int paymentMethod,
                                                 String pickupAddress, String recipientDetails,
                                                 String note, boolean clearTask, View confirmButton) {
        List<CartItem> items = CartManager.getInstance().getItems();
        if (items.isEmpty()) {
            return;
        }

        FirebaseOrder order = OrderPlacementHelper.buildOrder(
                activity,
                items,
                paymentMethod,
                pickupAddress,
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
