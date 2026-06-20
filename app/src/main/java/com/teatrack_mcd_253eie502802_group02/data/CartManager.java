package com.teatrack_mcd_253eie502802_group02.data;

import com.teatrack_mcd_253eie502802_group02.model.CartItem;

import java.util.ArrayList;
import java.util.List;

public class CartManager {

    public interface CartChangeListener {
        void onCartChanged();
    }

    private static CartManager instance;

    private final List<CartItem> items = new ArrayList<>();
    private final List<CartChangeListener> listeners = new ArrayList<>();

    private CartManager() {}

    public static synchronized CartManager getInstance() {
        if (instance == null) {
            instance = new CartManager();
        }
        return instance;
    }

    public void addListener(CartChangeListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeListener(CartChangeListener listener) {
        listeners.remove(listener);
    }

    public List<CartItem> getItems() {
        return new ArrayList<>(items);
    }

    public void addItem(CartItem newItem) {
        if (newItem == null) {
            return;
        }
        for (CartItem existing : items) {
            if (existing.matchesConfiguration(newItem)) {
                existing.setQuantity(existing.getQuantity() + newItem.getQuantity());
                notifyChanged();
                return;
            }
        }
        items.add(newItem);
        notifyChanged();
    }

    public void removeItem(int position) {
        if (position >= 0 && position < items.size()) {
            items.remove(position);
            notifyChanged();
        }
    }

    public void updateQuantity(int position, int quantity) {
        if (position < 0 || position >= items.size()) {
            return;
        }
        if (quantity <= 0) {
            items.remove(position);
        } else {
            items.get(position).setQuantity(quantity);
        }
        notifyChanged();
    }

    public int getTotalQuantity() {
        int total = 0;
        for (CartItem item : items) {
            total += item.getQuantity();
        }
        return total;
    }

    public int getSubtotal() {
        int total = 0;
        for (CartItem item : items) {
            total += item.getLineTotal();
        }
        return total;
    }

    public int getVipDiscountTotal() {
        int total = 0;
        for (CartItem item : items) {
            total += item.getVipDiscountTotal();
        }
        return total;
    }

    public void clear() {
        items.clear();
        notifyChanged();
    }

    private void notifyChanged() {
        for (CartChangeListener listener : new ArrayList<>(listeners)) {
            listener.onCartChanged();
        }
    }
}
