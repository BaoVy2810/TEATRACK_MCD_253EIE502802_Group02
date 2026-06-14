package com.teatrack_mcd_253eie502802_group02.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.teatrack_mcd_253eie502802_group02.R;
import com.teatrack_mcd_253eie502802_group02.model.Product;

import java.util.List;

public class CategoryProductAdapter extends RecyclerView.Adapter<CategoryProductAdapter.ProductViewHolder> {

    public interface OnProductClickListener {
        void onProductClick(int position);
    }

    private final List<Product> products;
    private final OnProductClickListener listener;
    private int selectedPosition = 0;

    public CategoryProductAdapter(List<Product> products, OnProductClickListener listener) {
        this.products = products;
        this.listener = listener;
    }

    public void setSelectedPosition(int position) {
        this.selectedPosition = position;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product item = products.get(position);
        holder.imgProduct.setImageResource(item.getImageRes());
        holder.tvName.setText(item.getName());

        holder.imgProduct.setAlpha(position == selectedPosition ? 1f : 0.7f);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onProductClick(position);
        });
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct;
        TextView tvName;

        ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgCategoryProduct);
            tvName = itemView.findViewById(R.id.tvCategoryProductName);
        }
    }
}