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
        View.OnClickListener openCart = v ->
                activity.startActivity(new Intent(activity, Cart.class));

        View btnCart = activity.findViewById(R.id.btn_cart);
        if (btnCart != null) {
            btnCart.setOnClickListener(openCart);
        }
        View btnCartHeader = activity.findViewById(R.id.btnCartHeaderIcon);
        if (btnCartHeader != null) {
            btnCartHeader.setOnClickListener(openCart);
        }
        HeaderMenuHelper.setupProfileMenu(activity);
        updateBadge(activity);
    }

    public static void updateBadge(Activity activity) {
        int count = CartManager.getInstance().getTotalQuantity();
        String label = count > 99 ? "99+" : String.valueOf(count);

        TextView badge = activity.findViewById(R.id.tvCartBadge);
        if (badge != null) {
            if (count > 0) {
                badge.setVisibility(View.VISIBLE);
                badge.setText(label);
            } else {
                badge.setVisibility(View.GONE);
            }
        }

        TextView headerBadge = activity.findViewById(R.id.tvCartHeaderBadge);
        if (headerBadge != null) {
            if (count > 0) {
                headerBadge.setVisibility(View.VISIBLE);
                headerBadge.setText(label);
            } else {
                headerBadge.setVisibility(View.GONE);
            }
        }
    }
}
