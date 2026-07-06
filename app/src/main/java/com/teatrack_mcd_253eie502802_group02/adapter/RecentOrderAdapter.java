package com.teatrack_mcd_253eie502802_group02.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.teatrack_mcd_253eie502802_group02.R;
import com.teatrack_mcd_253eie502802_group02.model.FirebaseOrder;
import com.teatrack_mcd_253eie502802_group02.model.FirebaseOrderItem;
import com.teatrack_mcd_253eie502802_group02.util.DateTimeHelper;

import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Locale;

public class RecentOrderAdapter extends RecyclerView.Adapter<RecentOrderAdapter.ViewHolder> {

    public interface OnOrderClickListener {
        void onOrderClick(FirebaseOrder order);
    }

    private final List<FirebaseOrder> orders;
    private final OnOrderClickListener listener;

    public RecentOrderAdapter(List<FirebaseOrder> orders) {
        this(orders, null);
    }

    public RecentOrderAdapter(List<FirebaseOrder> orders, OnOrderClickListener listener) {
        this.orders = orders;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recent_order, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FirebaseOrder order = orders.get(position);
        holder.tvOrderTitle.setText(buildTitle(holder, order));
        holder.tvOrderMeta.setText(buildMeta(order));
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onOrderClick(order);
        });
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderTitle, tvOrderMeta;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderTitle = itemView.findViewById(R.id.tvOrderTitle);
            tvOrderMeta = itemView.findViewById(R.id.tvOrderMeta);
        }
    }

    private SpannableString buildTitle(ViewHolder holder, FirebaseOrder order) {
        String orderId = order.getOrderId();
        if (orderId == null || orderId.isEmpty()) orderId = order.getId();
        if (orderId == null || orderId.isEmpty()) orderId = "";

        String title = "New Order #" + orderId;
        SpannableString spannable = new SpannableString(title);
        int start = title.indexOf("#");
        if (start >= 0) {
            spannable.setSpan(
                    new ForegroundColorSpan(ContextCompat.getColor(holder.itemView.getContext(), R.color.primary)),
                    start,
                    title.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            );
        }
        return spannable;
    }

    private String buildMeta(FirebaseOrder order) {
        return formatRelativeTime(order) + " • " + formatVnd(getOrderRevenue(order));
    }

    private String formatRelativeTime(FirebaseOrder order) {
        long orderTime = getOrderTime(order);
        if (orderTime <= 0) return "Just now";

        long diffMillis = Math.max(0, System.currentTimeMillis() - orderTime);
        long minutes = diffMillis / 60000L;
        if (minutes < 1) return "Just now";
        if (minutes < 60) return String.format(Locale.US, "%d %s ago", minutes, minutes == 1 ? "minute" : "minutes");

        long hours = minutes / 60L;
        if (hours < 24) return String.format(Locale.US, "%d %s ago", hours, hours == 1 ? "hour" : "hours");

        long days = hours / 24L;
        if (days < 30) return String.format(Locale.US, "%d %s ago", days, days == 1 ? "day" : "days");

        long months = days / 30L;
        if (months < 12) return String.format(Locale.US, "%d %s ago", months, months == 1 ? "month" : "months");

        long years = days / 365L;
        return String.format(Locale.US, "%d %s ago", years, years == 1 ? "year" : "years");
    }

    private long getOrderTime(FirebaseOrder order) {
        if (order == null) return 0L;
        String createdAt = order.getCreatedAt();
        if (createdAt == null || createdAt.isEmpty()) createdAt = order.getDate();
        return DateTimeHelper.toEpochMillis(createdAt);
    }

    private double getOrderRevenue(FirebaseOrder order) {
        if (order == null) return 0;

        double total = Math.max(0, order.getTotal());
        if (total > 0) return total;

        double subtotal = Math.max(0, order.getSubtotal());
        if (subtotal > 0 || order.getShipping() > 0 || order.getDiscount() > 0) {
            return Math.max(0, subtotal + order.getShipping() - order.getDiscount());
        }

        if (order.getItems() != null && !order.getItems().isEmpty()) {
            double sum = 0;
            for (FirebaseOrderItem item : order.getItems()) {
                sum += item.getLineTotal();
            }
            return Math.max(0, sum + order.getShipping() - order.getDiscount());
        }

        return 0;
    }

    private String formatVnd(double value) {
        return new DecimalFormat("#,###").format(value).replace(",", ".") + "đ";
    }
}
