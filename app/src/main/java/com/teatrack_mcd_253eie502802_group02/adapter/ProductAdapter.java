package com.teatrack_mcd_253eie502802_group02.adapter;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.teatrack_mcd_253eie502802_group02.R;
import com.teatrack_mcd_253eie502802_group02.model.Product;
import com.teatrack_mcd_253eie502802_group02.util.ProductImageHelper;

import java.text.DecimalFormat;
import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private final Context context;
    private List<Product> productList;
    private final DecimalFormat formatter = new DecimalFormat("#,###");
    private OnEditClickListener editClickListener;

    public interface OnEditClickListener {
        void onEditClick(Product product);
    }

    public ProductAdapter(Context context, List<Product> productList) {
        this.context = context;
        this.productList = productList;
    }

    public void setOnEditClickListener(OnEditClickListener listener) {
        this.editClickListener = listener;
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

        holder.tvProductId.setText("ID: #" + product.getId());
        holder.tvProductName.setText(product.getName());
        holder.tvCategory.setText(product.getCategory());

        holder.tvPriceM.setText(formatter.format(product.getPrice()).replace(',', '.') + "đ");
        holder.tvPriceL.setText(formatter.format(product.getPriceL()).replace(',', '.') + "đ");
        holder.tvVipPriceM.setText(formatter.format(product.getVipPriceM()).replace(',', '.') + "đ");
        holder.tvVipPriceL.setText(formatter.format(product.getVipPriceL()).replace(',', '.') + "đ");

        // Hiển thị ảnh sử dụng ProductImageHelper để đồng bộ logic
        ProductImageHelper.load(holder.imgProduct, product);

        holder.btnToggleVisibility.setImageResource(product.isVisible() ? R.drawable.eye : R.drawable.hide);

        holder.btnToggleVisibility.setOnClickListener(v -> {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("products").child(product.getId());
            ref.child("visible").setValue(!product.isVisible());
        });

        holder.btnEdit.setOnClickListener(v -> {
            if (editClickListener != null) {
                editClickListener.onEditClick(product);
            }
        });

        holder.btnDelete.setOnClickListener(v -> showDeleteDialog(product));
    }

    private void showDeleteDialog(Product product) {
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_delete_confirm);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
            params.width = (int) (context.getResources().getDisplayMetrics().widthPixels * 0.9);
            dialog.getWindow().setAttributes(params);
        }

        TextView tvMessage = dialog.findViewById(R.id.tvDeleteMessage);
        String fullMessage = "The product <font color='#0088ff'><b>" + product.getName() + "</b></font> will be permanently deleted.";
        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            tvMessage.setText(Html.fromHtml(fullMessage, Html.FROM_HTML_MODE_LEGACY));
        } else {
            tvMessage.setText(Html.fromHtml(fullMessage));
        }

        dialog.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());
        dialog.findViewById(R.id.btnConfirmDelete).setOnClickListener(v -> {
            FirebaseDatabase.getInstance().getReference("products")
                    .child(product.getId()).removeValue()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(context, "Product deleted", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    });
        });

        dialog.show();
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public void updateList(List<Product> newList) {
        this.productList = newList;
        notifyDataSetChanged();
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct, btnToggleVisibility, btnEdit, btnDelete;
        TextView tvProductName, tvCategory, tvProductId;
        TextView tvPriceM, tvPriceL, tvVipPriceM, tvVipPriceL;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            btnToggleVisibility = itemView.findViewById(R.id.btnToggleVisibility);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            tvProductId = itemView.findViewById(R.id.tvProductId);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvPriceM = itemView.findViewById(R.id.tvPriceM);
            tvPriceL = itemView.findViewById(R.id.tvPriceL);
            tvVipPriceM = itemView.findViewById(R.id.tvVipPriceM);
            tvVipPriceL = itemView.findViewById(R.id.tvVipPriceL);
        }
    }
}
