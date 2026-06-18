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

public class MenuProductAdapter extends RecyclerView.Adapter<MenuProductAdapter.ProductViewHolder> {

    public interface OnProductClickListener {
        void onProductClick(Product product);
    }

    public interface OnAddToCartClickListener {
        void onAddToCart(Product product);
    }

    private final List<Product> products;
    private final OnProductClickListener productClickListener;
    private final OnAddToCartClickListener addToCartClickListener;

    public MenuProductAdapter(
            List<Product> products,
            OnProductClickListener productClickListener,
            OnAddToCartClickListener addToCartClickListener
    ) {
        this.products = products;
        this.productClickListener = productClickListener;
        this.addToCartClickListener = addToCartClickListener;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_menu_product_card, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = products.get(position);
        ProductImageHelper.load(holder.imgProduct, product);
        holder.tvProductName.setText(product.getName());
        holder.tvRating.setText(String.valueOf(product.getRating()));
        holder.tvReviews.setText(product.getReviewCount() + " đánh giá");
        holder.tvPriceM.setText(formatPrice(product.getPriceM()));
        holder.tvPriceL.setText(formatPrice(product.getPriceL()));
        holder.tvVipPriceM.setText(formatPrice(product.getVipPriceM()));
        holder.tvVipPriceL.setText(formatPrice(product.getVipPriceL()));

        holder.itemView.setOnClickListener(v -> {
            if (productClickListener != null) {
                productClickListener.onProductClick(product);
            }
        });

        holder.btnAddToCartMini.setOnClickListener(v -> {
            if (addToCartClickListener != null) {
                addToCartClickListener.onAddToCart(product);
            }
        });
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    private String formatPrice(Object price) {
        String p = String.valueOf(price);
        return p.endsWith("đ") ? p : p + "đ";
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        final ImageView imgProduct;
        final TextView tvProductName;
        final TextView tvRating;
        final TextView tvReviews;
        final TextView tvPriceM;
        final TextView tvPriceL;
        final TextView tvVipPriceM;
        final TextView tvVipPriceL;
        final ImageButton btnAddToCartMini;

        ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvRating = itemView.findViewById(R.id.tvRating);
            tvReviews = itemView.findViewById(R.id.tvReviews);
            tvPriceM = itemView.findViewById(R.id.tvPriceM);
            tvPriceL = itemView.findViewById(R.id.tvPriceL);
            tvVipPriceM = itemView.findViewById(R.id.tvVipPriceM);
            tvVipPriceL = itemView.findViewById(R.id.tvVipPriceL);
            btnAddToCartMini = itemView.findViewById(R.id.btnAddToCartMini);
        }
    }
}
