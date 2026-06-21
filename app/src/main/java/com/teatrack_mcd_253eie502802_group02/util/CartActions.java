package com.teatrack_mcd_253eie502802_group02.util;

import android.app.Activity;
import android.widget.Toast;

import com.teatrack_mcd_253eie502802_group02.R;
import com.teatrack_mcd_253eie502802_group02.data.CartManager;
import com.teatrack_mcd_253eie502802_group02.model.CartItem;
import com.teatrack_mcd_253eie502802_group02.model.Product;
import com.teatrack_mcd_253eie502802_group02.shared.ui.CartBadgeHelper;

public final class CartActions {

    private CartActions() {}

    public static void addDefaultProduct(Activity activity, Product product) {
        if (product == null || activity == null) {
            return;
        }
        CartManager.getInstance().addItem(CartItem.fromProduct(activity, product));
        CartBadgeHelper.updateBadge(activity);
        Toast.makeText(activity, R.string.product_detail_added_to_cart, Toast.LENGTH_SHORT).show();
    }

    public static void addItem(Activity activity, CartItem item) {
        if (item == null || activity == null) {
            return;
        }
        CartManager.getInstance().addItem(item);
        CartBadgeHelper.updateBadge(activity);
        Toast.makeText(activity, R.string.product_detail_added_to_cart, Toast.LENGTH_SHORT).show();
    }
}
