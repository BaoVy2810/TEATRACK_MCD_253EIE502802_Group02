package com.teatrack_mcd_253eie502802_group02.data;

import android.content.Context;

import com.teatrack_mcd_253eie502802_group02.R;
import com.teatrack_mcd_253eie502802_group02.model.CartItem;
import com.teatrack_mcd_253eie502802_group02.model.FirebaseOrder;
import com.teatrack_mcd_253eie502802_group02.model.FirebaseOrderItem;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.TimeZone;

public final class OrderPlacementHelper {

    private static final String PREF_NAME = "LoginPrefs";
    private static final String KEY_USER_ID = "userId";

    private OrderPlacementHelper() {
    }

    public static String generateOrderId(Context context) {
        int randomNum = new Random().nextInt(90000) + 10000;
        return context.getString(R.string.order_id_format, randomNum);
    }

    public static FirebaseOrder buildOrder(Context context, List<CartItem> items, int paymentMethod,
                                           String pickupAddress, String recipientDetails, String note) {
        String orderId = generateOrderId(context);
        String nowIso = isoNow();
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());

        int subtotal = 0;
        Map<String, FirebaseOrderItem> orderItems = new HashMap<>();
        for (int i = 0; i < items.size(); i++) {
            CartItem item = items.get(i);
            FirebaseOrderItem line = toOrderItem(item);
            orderItems.put(String.valueOf(i), line);
            subtotal += item.getLineTotal();
        }

        int discount = CartManager.getInstance().getVipDiscountTotal();
        int total = subtotal - discount;

        String[] recipient = parseRecipient(recipientDetails);
        String userId = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .getString(KEY_USER_ID, "");

        FirebaseOrder order = new FirebaseOrder();
        order.setId(orderId);
        order.setOrderId(orderId);
        order.setTitle("New Order #" + orderId);
        order.setMeta("just now • " + formatPrice(total));
        order.setDate(nowIso);
        order.setCustomerName(recipient[0]);
        order.setCustomerPhone(recipient[1]);
        order.setCustomerAddress(pickupAddress);
        order.setPaymentMethod(resolvePaymentMethod(context, paymentMethod));
        order.setStatus("pending");
        order.setSubtotal(subtotal);
        order.setShipping(0);
        order.setDiscount(discount);
        order.setTotal(total);
        order.setItems(orderItems);
        android.content.SharedPreferences userPrefs = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String pickupTime = userPrefs.getString("saved_pickup_time", "");
        String customTime = userPrefs.getString("saved_custom_pickup_time", "");
        String pickupDate = userPrefs.getString("saved_pickup_date", "");
        String resolvedTime = customTime != null && !customTime.isEmpty() ? customTime : pickupTime;
        order.setDeliveryTime(resolvedTime == null ? "" : resolvedTime);
        order.setDeliveryDate(pickupDate == null || pickupDate.isEmpty() ? today : pickupDate);
        order.setNote(note == null || note.trim().isEmpty() ? "" : note.trim());
        order.setCouponCode("");
        order.setUserId(userId == null ? "" : userId);
        order.setCreatedAt(nowIso);
        order.setUpdatedAt(nowIso);
        return order;
    }

    private static FirebaseOrderItem toOrderItem(CartItem item) {
        StringBuilder toppings = new StringBuilder();
        for (CartItem.ToppingLine topping : item.getToppings()) {
            if (toppings.length() > 0) {
                toppings.append(", ");
            }
            toppings.append(topping.name);
            if (topping.quantity > 1) {
                toppings.append(" x").append(topping.quantity);
            }
        }
        return new FirebaseOrderItem(
                item.getProductId(),
                item.getProductName(),
                item.getQuantity(),
                item.getLineUnitPrice(),
                item.getLineTotal(),
                item.getSize(),
                item.getSugar(),
                item.getIce(),
                toppings.toString()
        );
    }

    private static String[] parseRecipient(String recipientDetails) {
        if (recipientDetails == null || recipientDetails.trim().isEmpty()) {
            return new String[]{"", ""};
        }
        String[] parts = recipientDetails.split("\\|");
        if (parts.length >= 2) {
            return new String[]{parts[0].trim(), parts[1].trim()};
        }
        return new String[]{recipientDetails.trim(), ""};
    }

    private static String resolvePaymentMethod(Context context, int paymentMethod) {
        switch (paymentMethod) {
            case 1:
                return context.getString(R.string.cart_cash_in_bank);
            case 2:
                return context.getString(R.string.payment_momo);
            case 3:
                return context.getString(R.string.payment_zalopay);
            case 4:
                return context.getString(R.string.payment_ewallet);
            case 0:
            default:
                return context.getString(R.string.cart_cash_on_hand);
        }
    }

    private static String isoNow() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.US);
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        return formatter.format(new Date());
    }

    private static String formatPrice(int price) {
        return String.format(Locale.US, "%,d", price).replace(',', '.') + "đ";
    }
}
