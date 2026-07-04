package com.teatrack_mcd_253eie502802_group02.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.teatrack_mcd_253eie502802_group02.R;
import com.teatrack_mcd_253eie502802_group02.model.PointTransaction;

import java.util.List;

public class PointHistoryAdapter extends RecyclerView.Adapter<PointHistoryAdapter.ViewHolder> {

    private List<PointTransaction> transactions;

    public PointHistoryAdapter(List<PointTransaction> transactions) {
        this.transactions = transactions;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_point_transaction, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PointTransaction transaction = transactions.get(position);
        holder.tvDescription.setText(transaction.getDescription());
        holder.tvDate.setText(transaction.getCreatedAt());
        
        long change = transaction.getPointsChange();
        String changeText = (change > 0 ? "+" : "") + change;
        holder.tvPointsChange.setText(changeText);
        
        if (change > 0) {
            holder.tvPointsChange.setTextColor(Color.parseColor("#4CAF50")); // Green
        } else {
            holder.tvPointsChange.setTextColor(Color.parseColor("#F44336")); // Red
        }
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDescription, tvDate, tvPointsChange;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvPointsChange = itemView.findViewById(R.id.tvPointsChange);
        }
    }
}
