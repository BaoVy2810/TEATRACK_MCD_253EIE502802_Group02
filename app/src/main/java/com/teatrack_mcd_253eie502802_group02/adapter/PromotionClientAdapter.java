package com.teatrack_mcd_253eie502802_group02.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.teatrack_mcd_253eie502802_group02.R;
import com.teatrack_mcd_253eie502802_group02.model.Promotion;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class PromotionClientAdapter extends RecyclerView.Adapter<PromotionClientAdapter.PromotionViewHolder> {

    private final List<Promotion> promotionList;
    private final NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

    public PromotionClientAdapter(List<Promotion> promotionList) {
        this.promotionList = promotionList;
    }

    @NonNull
    @Override
    public PromotionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_promotion_admin, parent, false);
        return new PromotionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PromotionViewHolder holder, int position) {
        Promotion promotion = promotionList.get(position);
        holder.tvCode.setText(promotion.getCode());
        holder.tvDescription.setText(promotion.getDescription());
        
        String valueStr;
        if ("percent".equals(promotion.getType())) {
            valueStr = "-" + (int)promotion.getValue() + "%";
        } else {
            valueStr = "-" + currencyFormatter.format(promotion.getValue());
        }
        holder.tvValue.setText(valueStr);
        
        String minSubtotalStr = currencyFormatter.format(promotion.getMinSubtotal()).replace("₫", "").trim();
        holder.tvMinSubtotal.setText(holder.itemView.getContext().getString(R.string.str_min_order, minSubtotalStr));

        // Hide admin buttons
        holder.btnEdit.setVisibility(View.GONE);
        holder.btnDelete.setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() {
        return promotionList.size();
    }

    static class PromotionViewHolder extends RecyclerView.ViewHolder {
        TextView tvCode, tvDescription, tvValue, tvMinSubtotal;
        View btnEdit, btnDelete;

        public PromotionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCode = itemView.findViewById(R.id.tvPromotionCode);
            tvDescription = itemView.findViewById(R.id.tvPromotionDescription);
            tvValue = itemView.findViewById(R.id.tvPromotionValue);
            tvMinSubtotal = itemView.findViewById(R.id.tvMinSubtotal);
            btnEdit = itemView.findViewById(R.id.btnEditPromotion);
            btnDelete = itemView.findViewById(R.id.btnDeletePromotion);
        }
    }
}
