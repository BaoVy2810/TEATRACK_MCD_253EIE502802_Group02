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
import com.teatrack_mcd_253eie502802_group02.util.ReviewStatsHelper;
import com.teatrack_mcd_253eie502802_group02.util.VipPriceUiHelper;

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
        holder.tvRating.setText(ReviewStatsHelper.formatRating(item.getRating()));
        String reviewCount = item.getReviewCount() != null ? item.getReviewCount() : "0";
        holder.tvReviews.setText(holder.itemView.getContext().getString(
                R.string.product_card_reviews_format, reviewCount));
        holder.tvPriceM.setText(formatPrice(item.getPrice()));
        holder.tvPriceL.setText(formatPrice(item.getPriceL()));
        holder.tvVipPriceM.setText(formatPrice(item.getVipPriceM()));
        holder.tvVipPriceL.setText(formatPrice(item.getVipPriceL()));
        VipPriceUiHelper.applyCardPrices(holder.itemView.getContext(),
                holder.tvSizeM, holder.tvPriceM, holder.tvVipM, holder.tvVipPriceM,
                holder.tvSizeL, holder.tvPriceL, holder.tvVipL, holder.tvVipPriceL);
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
        TextView tvSizeM, tvSizeL, tvPriceM, tvPriceL, tvVipPriceM, tvVipPriceL;
        TextView tvVipM, tvVipL;

        ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            btnAddToCartMini = itemView.findViewById(R.id.btnAddToCartMini);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvRating = itemView.findViewById(R.id.tvRating);
            tvReviews = itemView.findViewById(R.id.tvReviews);
            tvSizeM = itemView.findViewById(R.id.tvSizeM);
            tvSizeL = itemView.findViewById(R.id.tvSizeL);
            tvPriceM = itemView.findViewById(R.id.tvPriceM);
            tvPriceL = itemView.findViewById(R.id.tvPriceL);
            tvVipPriceM = itemView.findViewById(R.id.tvVipPriceM);
            tvVipPriceL = itemView.findViewById(R.id.tvVipPriceL);
            tvVipM = itemView.findViewById(R.id.tvVipM);
            tvVipL = itemView.findViewById(R.id.tvVipL);
        }
    }
}
