package com.teatrack_mcd_253eie502802_group02.model;

import android.content.Context;

import com.teatrack_mcd_253eie502802_group02.R;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class CartItem {

    public static class ToppingLine {
        public final String name;
        public final int price;
        public final int quantity;

        public ToppingLine(String name, int price, int quantity) {
            this.name = name;
            this.price = price;
            this.quantity = quantity;
        }
    }

    private String productId;
    private String productName;
    private String category;
    private String image;
    private int imageRes;
    private String size;
    private String sugar;
    private String ice;
    private final List<ToppingLine> toppings = new ArrayList<>();
    private int quantity = 1;
    private int unitPrice;
    private int vipUnitPrice;

    public static CartItem fromProduct(Context context, Product product) {
        CartItem item = new CartItem();
        if (product == null) {
            return item;
        }
        item.productId = product.getId();
        item.productName = product.getName();
        item.category = product.getCategory();
        item.image = product.getImage();
        item.imageRes = product.getImageRes(context);
        item.size = "M";
        item.sugar = context.getString(R.string.product_detail_level_medium);
        item.ice = context.getString(R.string.product_detail_level_medium);
        item.unitPrice = product.getPriceM();
        item.vipUnitPrice = product.getVipPriceM();
        item.quantity = 1;
        return item;
    }

    public void addTopping(String name, int price, int quantity) {
        if (quantity <= 0) {
            return;
        }
        toppings.add(new ToppingLine(name, price, quantity));
    }

    public int getToppingUnitTotal() {
        int total = 0;
        for (ToppingLine topping : toppings) {
            total += topping.price * topping.quantity;
        }
        return total;
    }

    public int getLineUnitPrice() {
        return unitPrice + getToppingUnitTotal();
    }

    public int getLineTotal() {
        return getLineUnitPrice() * quantity;
    }

    public int getVipDiscountTotal() {
        if (vipUnitPrice <= 0 || vipUnitPrice >= unitPrice) {
            return 0;
        }
        return (unitPrice - vipUnitPrice) * quantity;
    }

    public String getOptionsSummary(Context context) {
        StringBuilder builder = new StringBuilder();
        builder.append(context.getString(R.string.cart_option_size, size));
        builder.append(", ").append(context.getString(R.string.cart_option_sugar, sugar));
        builder.append(", ").append(context.getString(R.string.cart_option_ice, ice));
        for (ToppingLine topping : toppings) {
            builder.append(", ").append(topping.name);
            if (topping.quantity > 1) {
                builder.append(" x").append(topping.quantity);
            }
        }
        return builder.toString();
    }

    public boolean matchesConfiguration(CartItem other) {
        if (other == null) {
            return false;
        }
        if (!Objects.equals(productName, other.productName)
                || !Objects.equals(size, other.size)
                || !Objects.equals(sugar, other.sugar)
                || !Objects.equals(ice, other.ice)) {
            return false;
        }
        if (toppings.size() != other.toppings.size()) {
            return false;
        }
        Map<String, Integer> thisMap = toToppingMap();
        Map<String, Integer> otherMap = other.toToppingMap();
        return thisMap.equals(otherMap);
    }

    private Map<String, Integer> toToppingMap() {
        Map<String, Integer> map = new LinkedHashMap<>();
        for (ToppingLine topping : toppings) {
            map.put(topping.name, topping.quantity);
        }
        return map;
    }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    public int getImageRes() { return imageRes; }
    public void setImageRes(int imageRes) { this.imageRes = imageRes; }

    public String getSize() { return size; }
    public void setSize(String size) { this.size = size; }

    public String getSugar() { return sugar; }
    public void setSugar(String sugar) { this.sugar = sugar; }

    public String getIce() { return ice; }
    public void setIce(String ice) { this.ice = ice; }

    public List<ToppingLine> getToppings() { return toppings; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = Math.max(1, quantity); }

    public int getUnitPrice() { return unitPrice; }
    public void setUnitPrice(int unitPrice) { this.unitPrice = unitPrice; }

    public int getVipUnitPrice() { return vipUnitPrice; }
    public void setVipUnitPrice(int vipUnitPrice) { this.vipUnitPrice = vipUnitPrice; }
}
