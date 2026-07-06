package com.teatrack_mcd_253eie502802_group02.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Objects;

import com.teatrack_mcd_253eie502802_group02.R;
import com.teatrack_mcd_253eie502802_group02.model.Product;
import com.teatrack_mcd_253eie502802_group02.util.ProductImageHelper;
import com.teatrack_mcd_253eie502802_group02.util.ReviewStatsHelper;
import com.teatrack_mcd_253eie502802_group02.util.VipPriceUiHelper;

import java.util.List;

public class MenuProductAdapter extends ListAdapter<Product, MenuProductAdapter.ProductViewHolder> {

    public interface OnProductClickListener {
        void onProductClick(Product product);
    }

    public interface OnAddToCartClickListener {
        void onAddToCart(Product product);
    }

    private final OnProductClickListener productClickListener;
    private final OnAddToCartClickListener addToCartClickListener;

    public MenuProductAdapter(
            OnProductClickListener productClickListener,
            OnAddToCartClickListener addToCartClickListener
    ) {
        super(new ProductDiffCallback());
        this.productClickListener = productClickListener;
        this.addToCartClickListener = addToCartClickListener;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_menu_product_card, parent, false);
        return new ProductViewHolder(view, productClickListener, addToCartClickListener, this);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = getItem(position);
        ProductImageHelper.load(holder.imgProduct, product);
        holder.tvProductName.setText(product.getName());
        holder.tvRating.setText(ReviewStatsHelper.formatRating(product.getRating()));
        String reviewCount = product.getReviewCount() != null ? product.getReviewCount() : "0";
        holder.tvReviews.setText(holder.itemView.getContext().getString(
                R.string.product_card_reviews_format, reviewCount));
        holder.tvPriceM.setText(formatPrice(product.getPriceM()));
        holder.tvPriceL.setText(formatPrice(product.getPriceL()));
        holder.tvVipPriceM.setText(formatPrice(product.getVipPriceM()));
        holder.tvVipPriceL.setText(formatPrice(product.getVipPriceL()));
        VipPriceUiHelper.applyMenuPrices(holder.itemView.getContext(),
                holder.tvSizeBadgeM, holder.tvPriceM, holder.tvVipBadgeM, holder.tvVipPriceM,
                holder.tvSizeBadgeL, holder.tvPriceL, holder.tvVipBadgeL, holder.tvVipPriceL);
    }

    @Override
    public int getItemCount() {
        return getCurrentList().size();
    }

    private String formatPrice(int price) {
        return String.format("%,dđ", price).replace(',', '.');
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        final ImageView imgProduct;
        final TextView tvProductName;
        final TextView tvRating;
        final TextView tvReviews;
        final TextView tvSizeBadgeM;
        final TextView tvSizeBadgeL;
        final TextView tvPriceM;
        final TextView tvPriceL;
        final TextView tvVipPriceM;
        final TextView tvVipPriceL;
        final TextView tvVipBadgeM;
        final TextView tvVipBadgeL;
        final ImageButton btnAddToCartMini;

        ProductViewHolder(@NonNull View itemView,
                          OnProductClickListener productClickListener,
                          OnAddToCartClickListener addToCartClickListener,
                          MenuProductAdapter adapter) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvRating = itemView.findViewById(R.id.tvRating);
            tvReviews = itemView.findViewById(R.id.tvReviews);
            tvSizeBadgeM = itemView.findViewById(R.id.tvSizeBadgeM);
            tvSizeBadgeL = itemView.findViewById(R.id.tvSizeBadgeL);
            tvPriceM = itemView.findViewById(R.id.tvPriceM);
            tvPriceL = itemView.findViewById(R.id.tvPriceL);
            tvVipPriceM = itemView.findViewById(R.id.tvVipPriceM);
            tvVipPriceL = itemView.findViewById(R.id.tvVipPriceL);
            tvVipBadgeM = itemView.findViewById(R.id.tvVipBadgeM);
            tvVipBadgeL = itemView.findViewById(R.id.tvVipBadgeL);
            btnAddToCartMini = itemView.findViewById(R.id.btnAddToCartMini);

            itemView.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && productClickListener != null) {
                    productClickListener.onProductClick(adapter.getItem(pos));
                }
            });

            btnAddToCartMini.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && addToCartClickListener != null) {
                    addToCartClickListener.onAddToCart(adapter.getItem(pos));
                }
            });
        }
    }

    private static class ProductDiffCallback extends DiffUtil.ItemCallback<Product> {
        @Override
        public boolean areItemsTheSame(@NonNull Product oldItem, @NonNull Product newItem) {
            if (oldItem.getId() != null && newItem.getId() != null) {
                return oldItem.getId().equals(newItem.getId());
            }
            return Objects.equals(oldItem.getName(), newItem.getName());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Product oldItem, @NonNull Product newItem) {
            return Objects.equals(oldItem.getId(), newItem.getId())
                    && Objects.equals(oldItem.getImage(), newItem.getImage())
                    && Objects.equals(oldItem.getName(), newItem.getName())
                    && oldItem.getPriceM() == newItem.getPriceM()
                    && oldItem.getPriceL() == newItem.getPriceL()
                    && oldItem.getVipPriceM() == newItem.getVipPriceM()
                    && oldItem.getVipPriceL() == newItem.getVipPriceL()
                    && Float.compare(oldItem.getRating(), newItem.getRating()) == 0
                    && Objects.equals(oldItem.getReviewCount(), newItem.getReviewCount())
                    && oldItem.getImageRes() == newItem.getImageRes();
        }
    }
}
