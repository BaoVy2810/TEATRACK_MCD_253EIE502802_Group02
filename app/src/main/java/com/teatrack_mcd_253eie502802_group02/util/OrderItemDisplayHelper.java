package com.teatrack_mcd_253eie502802_group02.util;

import android.content.Context;

import com.teatrack_mcd_253eie502802_group02.R;
import com.teatrack_mcd_253eie502802_group02.model.CartItem;

import java.util.List;

public final class OrderItemDisplayHelper {

    private OrderItemDisplayHelper() {
    }

    public static String formatConfigLine(Context context, String size, String sugar, String ice) {
        return context.getString(
                R.string.cart_config_line,
                safe(size),
                context.getString(R.string.cart_label_sugar),
                safe(sugar),
                context.getString(R.string.cart_label_ice),
                safe(ice)
        );
    }

    public static String formatToppingsBlock(Context context, String toppings) {
        if (toppings == null || toppings.trim().isEmpty()) {
            return "";
        }

        String header = context.getString(R.string.cart_topping_header);
        StringBuilder builder = new StringBuilder(header);
        String[] parts = splitToppingParts(toppings);

        for (String part : parts) {
            String trimmed = part.trim();
            if (trimmed.isEmpty()) {
                continue;
            }

            String name;
            int quantity;
            int quantityIndex = trimmed.lastIndexOf(" x");
            if (quantityIndex > 0) {
                try {
                    quantity = Integer.parseInt(trimmed.substring(quantityIndex + 2).trim());
                    name = trimmed.substring(0, quantityIndex).trim();
                } catch (NumberFormatException ignored) {
                    name = trimmed;
                    quantity = 1;
                }
            } else {
                name = trimmed;
                quantity = 1;
            }

            if (name.isEmpty()) {
                continue;
            }
            builder.append('\n')
                    .append(context.getString(R.string.cart_topping_item, name, quantity));
        }

        return builder.length() > header.length() ? builder.toString() : "";
    }

    public static String encodeToppingsForStorage(List<CartItem.ToppingLine> toppings) {
        if (toppings == null || toppings.isEmpty()) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (CartItem.ToppingLine topping : toppings) {
            if (topping == null || topping.name == null || topping.name.trim().isEmpty()) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append('\n');
            }
            int qty = Math.max(topping.quantity, 1);
            builder.append(topping.name.trim()).append(" x").append(qty);
        }
        return builder.toString();
    }

    private static String[] splitToppingParts(String toppings) {
        if (toppings.contains("\n")) {
            return toppings.split("\n");
        }
        if (toppings.contains(", ")) {
            return toppings.split(", ");
        }
        if (toppings.contains(",")) {
            return toppings.split(",");
        }
        return new String[]{toppings};
    }

    private static String safe(String value) {
        return value != null ? value : "";
    }
}
