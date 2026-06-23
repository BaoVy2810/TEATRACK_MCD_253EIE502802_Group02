package com.teatrack_mcd_253eie502802_group02.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.teatrack_mcd_253eie502802_group02.R;
import com.teatrack_mcd_253eie502802_group02.model.Product;
import com.teatrack_mcd_253eie502802_group02.util.ProductImageHelper;

import java.util.List;

public class ProductCardAdapter extends RecyclerView.Adapter<ProductCardAdapter.ProductViewHolder> {

    public interface OnProductClickListener {
        void onProductClick(Product product);
    }

    public interface OnAddToCartClickListener {
        void onAddToCart(Product product);
    }

    private final List<Product> products;
    private final OnProductClickListener clickListener;
    private final OnAddToCartClickListener addToCartClickListener;
    private final int layoutId;

    public ProductCardAdapter(List<Product> products) {
        this(products, R.layout.item_product_card, null, null);
    }

    public ProductCardAdapter(List<Product> products, OnProductClickListener clickListener) {
        this(products, R.layout.item_product_card, clickListener, null);
    }

    public ProductCardAdapter(
            List<Product> products,
            OnProductClickListener clickListener,
            OnAddToCartClickListener addToCartClickListener
    ) {
        this(products, R.layout.item_product_card, clickListener, addToCartClickListener);
    }

    public ProductCardAdapter(List<Product> products, int layoutId, OnProductClickListener clickListener) {
        this(products, layoutId, clickListener, null);
    }

    public ProductCardAdapter(
            List<Product> products,
            int layoutId,
            OnProductClickListener clickListener,
            OnAddToCartClickListener addToCartClickListener
    ) {
        this.products = products;
        this.layoutId = layoutId;
        this.clickListener = clickListener;
        this.addToCartClickListener = addToCartClickListener;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(layoutId, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product item = products.get(position);
        ProductImageHelper.load(holder.imgProduct, item);
        holder.tvProductName.setText(item.getName());
        holder.tvRating.setText(String.valueOf(item.getRating()));
        holder.tvReviews.setText(item.getReviewCount() + " Đánh giá");
        holder.tvPriceM.setText(formatPrice(item.getPrice()));
        holder.tvPriceL.setText(formatPrice(item.getPriceL()));
        holder.tvVipPriceM.setText(formatPrice(item.getVipPriceM()));
        holder.tvVipPriceL.setText(formatPrice(item.getVipPriceL()));
        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onProductClick(item);
            }
        });

        if (holder.btnAddToCartMini != null) {
            holder.btnAddToCartMini.setOnClickListener(v -> {
                if (addToCartClickListener != null) {
                    addToCartClickListener.onAddToCart(item);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    private String formatPrice(int price) {
        return String.format("%,dđ", price).replace(',', '.');
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct;
        ImageButton btnAddToCartMini;
        TextView tvProductName, tvRating, tvReviews;
        TextView tvPriceM, tvPriceL, tvVipPriceM, tvVipPriceL;

        ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            btnAddToCartMini = itemView.findViewById(R.id.btnAddToCartMini);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvRating = itemView.findViewById(R.id.tvRating);
            tvReviews = itemView.findViewById(R.id.tvReviews);
            tvPriceM = itemView.findViewById(R.id.tvPriceM);
            tvPriceL = itemView.findViewById(R.id.tvPriceL);
            tvVipPriceM = itemView.findViewById(R.id.tvVipPriceM);
            tvVipPriceL = itemView.findViewById(R.id.tvVipPriceL);
        }
    }
}
