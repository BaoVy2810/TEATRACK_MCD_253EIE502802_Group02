package com.teatrack_mcd_253eie502802_group02.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.teatrack_mcd_253eie502802_group02.R;
import com.teatrack_mcd_253eie502802_group02.model.FirebaseOrder;

import java.util.List;

public class RecentOrderAdapter extends RecyclerView.Adapter<RecentOrderAdapter.ViewHolder> {

    private final List<FirebaseOrder> orders;

    public RecentOrderAdapter(List<FirebaseOrder> orders) {
        this.orders = orders;
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
        holder.tvOrderTitle.setText(order.getTitle());
        holder.tvOrderMeta.setText(order.getMeta());
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
}