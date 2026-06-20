package com.teatrack_mcd_253eie502802_group02.shared.ui;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import com.teatrack_mcd_253eie502802_group02.R;
import com.teatrack_mcd_253eie502802_group02.client.Cart;
import com.teatrack_mcd_253eie502802_group02.data.CartManager;

public final class CartBadgeHelper {

    private CartBadgeHelper() {}

    public static void setup(Activity activity) {
        View btnCart = activity.findViewById(R.id.btn_cart);
        if (btnCart != null) {
            btnCart.setOnClickListener(v ->
                    activity.startActivity(new Intent(activity, Cart.class)));
        }
        updateBadge(activity);
    }

    public static void updateBadge(Activity activity) {
        TextView badge = activity.findViewById(R.id.tvCartBadge);
        if (badge == null) {
            return;
        }
        int count = CartManager.getInstance().getTotalQuantity();
        if (count > 0) {
            badge.setVisibility(View.VISIBLE);
            badge.setText(count > 99 ? "99+" : String.valueOf(count));
        } else {
            badge.setVisibility(View.GONE);
        }
    }
}
