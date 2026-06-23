package com.teatrack_mcd_253eie502802_group02.admin;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.teatrack_mcd_253eie502802_group02.R;

import java.util.List;

public class CategoryDialogAdapter extends RecyclerView.Adapter<CategoryDialogAdapter.ViewHolder> {
    private final List<String> categories;
    private final String selectedCategory;
    private final OnCategorySelectedListener listener;

    public CategoryDialogAdapter(List<String> categories, String selectedCategory, OnCategorySelectedListener listener) {
        this.categories = categories;
        this.selectedCategory = selectedCategory;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category_dialog, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String category = categories.get(position);
        holder.tvName.setText(category);

        boolean isSelected = category.equals(selectedCategory);
        if (isSelected) {
            holder.itemView.setBackgroundResource(R.drawable.bg_time_range_active);
            holder.tvName.setTextColor(Color.WHITE);
            holder.tvName.setTypeface(null, android.graphics.Typeface.BOLD);
            holder.icChecked.setVisibility(View.VISIBLE);
            holder.icChecked.setColorFilter(Color.WHITE);
        } else {
            holder.itemView.setBackgroundResource(android.R.color.transparent);
            holder.tvName.setTextColor(Color.parseColor("#0088FF"));
            holder.tvName.setTypeface(null, android.graphics.Typeface.NORMAL);
            holder.icChecked.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> listener.onCategorySelected(category));
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        ImageView icChecked;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvCategoryItemName);
            icChecked = itemView.findViewById(R.id.icChecked);
        }
    }
}
