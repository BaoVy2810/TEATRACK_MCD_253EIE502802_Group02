package com.teatrack_mcd_253eie502802_group02.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.imageview.ShapeableImageView;
import com.teatrack_mcd_253eie502802_group02.R;
import com.teatrack_mcd_253eie502802_group02.model.CartItem;
import com.teatrack_mcd_253eie502802_group02.model.Product;
import com.teatrack_mcd_253eie502802_group02.client.ProductDetail;
import com.teatrack_mcd_253eie502802_group02.util.ProductImageHelper;

import java.util.List;
import java.util.Locale;

public class CartItemAdapter extends RecyclerView.Adapter<CartItemAdapter.ViewHolder> {

    public interface CartItemActionListener {
        void onQuantityChanged(int position, int newQuantity);
        void onRemove(int position);
    }

    private final List<CartItem> items;
    private final CartItemActionListener listener;

    public CartItemAdapter(List<CartItem> items, CartItemActionListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cart_line, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CartItem item = items.get(position);
        Context context = holder.itemView.getContext();

        holder.tvName.setText(item.getProductName());
        holder.tvConfig.setText(item.getConfigLine(context));
        holder.tvQtyLine.setText(context.getString(R.string.cart_quantity_line, item.getQuantity()));

        String toppingsBlock = item.getToppingsBlock(context);
        if (holder.tvToppings != null) {
            if (toppingsBlock.isEmpty()) {
                holder.tvToppings.setVisibility(View.GONE);
            } else {
                holder.tvToppings.setVisibility(View.VISIBLE);
                holder.tvToppings.setText(toppingsBlock);
            }
        }

        holder.tvPrice.setText(formatPrice(item.getLineTotal()));
        holder.tvQty.setText(String.valueOf(item.getQuantity()));

        Product product = new Product();
        product.setName(item.getProductName());
        product.setImage(item.getImage());
        product.setImageRes(item.getImageRes());
        ProductImageHelper.load(holder.imgProduct, product);

        holder.imgProduct.setOnClickListener(v -> openProductDetail(context, item));

        updateStepperState(holder, item.getQuantity());

        holder.btnMinus.setOnClickListener(v -> {
            int pos = holder.getBindingAdapterPosition();
            if (pos == RecyclerView.NO_POSITION || listener == null) {
                return;
            }
            listener.onQuantityChanged(pos, item.getQuantity() - 1);
        });

        holder.btnPlus.setOnClickListener(v -> {
            int pos = holder.getBindingAdapterPosition();
            if (pos == RecyclerView.NO_POSITION || listener == null) {
                return;
            }
            listener.onQuantityChanged(pos, item.getQuantity() + 1);
        });

        holder.btnDelete.setOnClickListener(v -> {
            int pos = holder.getBindingAdapterPosition();
            if (pos == RecyclerView.NO_POSITION || listener == null) {
                return;
            }
            listener.onRemove(pos);
        });
    }

    private void updateStepperState(ViewHolder holder, int quantity) {
        Context context = holder.itemView.getContext();
        holder.tvQty.setText(String.valueOf(quantity));
        int activeColor = ContextCompat.getColor(context, R.color.on_surface);
        int inactiveColor = ContextCompat.getColor(context, R.color.nav_inactive);
        boolean canDecrease = quantity > 1;
        holder.tvQty.setTextColor(canDecrease ? activeColor : inactiveColor);
        holder.btnMinus.setColorFilter(canDecrease ? activeColor : inactiveColor);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private String formatPrice(int price) {
        return String.format(Locale.US, "%,d", price).replace(',', '.') + "đ";
    }

    private void openProductDetail(Context context, CartItem item) {
        Intent intent = new Intent(context, ProductDetail.class);
        intent.putExtra("name", item.getProductName());
        intent.putExtra("category", item.getCategory());
        intent.putExtra("priceM", String.valueOf(item.getUnitPrice()));
        intent.putExtra("priceL", String.valueOf(item.getUnitPrice()));
        intent.putExtra("vipM", String.valueOf(item.getVipUnitPrice()));
        intent.putExtra("vipL", String.valueOf(item.getVipUnitPrice()));
        Product product = new Product();
        product.setId(item.getProductId());
        product.setName(item.getProductName());
        product.setCategory(item.getCategory());
        product.setImage(item.getImage());
        product.setImageRes(item.getImageRes());
        ProductImageHelper.putDetailExtras(intent, context, product);
        if (item.getProductId() != null && !item.getProductId().isEmpty()) {
            intent.putExtra("productId", item.getProductId());
        }
        context.startActivity(intent);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ShapeableImageView imgProduct;
        final TextView tvName;
        final TextView tvConfig;
        final TextView tvQtyLine;
        final TextView tvToppings;
        final TextView tvPrice;
        final TextView tvQty;
        final ImageButton btnMinus;
        final ImageButton btnPlus;
        final ImageButton btnDelete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgCartItem);
            tvName = itemView.findViewById(R.id.tvCartItemName);
            tvConfig = itemView.findViewById(R.id.tvCartItemConfig);
            tvQtyLine = itemView.findViewById(R.id.tvCartItemQty);
            tvToppings = itemView.findViewById(R.id.tvCartItemToppings);
            tvPrice = itemView.findViewById(R.id.tvCartItemPrice);
            tvQty = itemView.findViewById(R.id.tvCartQty);
            btnMinus = itemView.findViewById(R.id.btnCartQtyMinus);
            btnPlus = itemView.findViewById(R.id.btnCartQtyPlus);
            btnDelete = itemView.findViewById(R.id.btnCartDelete);
        }
    }
}
