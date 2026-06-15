package com.teatrack_mcd_253eie502802_group02.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.teatrack_mcd_253eie502802_group02.R;
import com.teatrack_mcd_253eie502802_group02.model.Product;

import java.text.DecimalFormat;
import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private final Context context;
    private List<Product> productList;
    private final DecimalFormat formatter = new DecimalFormat("#,###");

    public ProductAdapter(Context context, List<Product> productList) {
        this.context = context;
        this.productList = productList;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);

        // Bind data
        holder.tvProductName.setText(product.getName());
        holder.tvCategory.setText(product.getCategory());

        // Price Mapping and Formatting
        holder.tvPriceM.setText(formatter.format(product.getPriceM()).replace(',', '.') + "đ");
        holder.tvPriceL.setText(formatter.format(product.getPriceL()).replace(',', '.') + "đ");
        holder.tvVipPriceM.setText(formatter.format(product.getVipPriceM()).replace(',', '.') + "đ");
        holder.tvVipPriceL.setText(formatter.format(product.getVipPriceL()).replace(',', '.') + "đ");


        // Image Loading from Mipmap
        holder.imgProduct.setImageResource(product.getImageRes(context));

        // Visibility Icon
        holder.btnToggleVisibility.setImageResource(product.isVisible() ? R.drawable.eye : R.drawable.hide);

        // --- Event Listeners ---

        // Toggle Visibility
        holder.btnToggleVisibility.setOnClickListener(v -> {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference(context.getString(R.string.firebase_collection_products)).child(product.getId());
            ref.child(context.getString(R.string.firebase_field_visible)).setValue(!product.isVisible())
                    .addOnFailureListener(e -> Toast.makeText(context, context.getString(R.string.error_prefix, e.getMessage()), Toast.LENGTH_SHORT).show());
        });

        // Edit Product
        holder.btnEdit.setOnClickListener(v -> {
            // Intent intent = new Intent(context, EditProductActivity.class);
            // intent.putExtra("productId", product.getId());
            // context.startActivity(intent);
            Toast.makeText(context, R.string.msg_edit_update, Toast.LENGTH_SHORT).show();
        });

        // Delete Product
        holder.btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle(R.string.modal_delete_title)
                    .setMessage(R.string.confirm_delete_msg)
                    .setPositiveButton(R.string.btn_delete, (dialog, which) -> {
                        FirebaseDatabase.getInstance().getReference(context.getString(R.string.firebase_collection_products))
                                .child(product.getId()).removeValue()
                                .addOnSuccessListener(aVoid -> Toast.makeText(context, R.string.msg_product_deleted, Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e -> Toast.makeText(context, context.getString(R.string.error_prefix, e.getMessage()), Toast.LENGTH_SHORT).show());
                    })
                    .setNegativeButton(R.string.btn_cancel, null)
                    .show();
        });

    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public void updateList(List<Product> newList) {
        this.productList = newList;
        notifyDataSetChanged();
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct, btnToggleVisibility, btnEdit, btnDelete;
        TextView tvProductName, tvCategory;
        TextView tvPriceM, tvPriceL, tvVipPriceM, tvVipPriceL;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            btnToggleVisibility = itemView.findViewById(R.id.btnToggleVisibility);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvPriceM = itemView.findViewById(R.id.tvPriceM);
            tvPriceL = itemView.findViewById(R.id.tvPriceL);
            tvVipPriceM = itemView.findViewById(R.id.tvVipPriceM);
            tvVipPriceL = itemView.findViewById(R.id.tvVipPriceL);
        }
    }
}
