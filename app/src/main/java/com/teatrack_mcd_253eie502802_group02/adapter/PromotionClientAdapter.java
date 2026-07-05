package com.teatrack_mcd_253eie502802_group02.adapter;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.teatrack_mcd_253eie502802_group02.R;
import com.teatrack_mcd_253eie502802_group02.model.Promotion;
import com.teatrack_mcd_253eie502802_group02.util.PriceFormatHelper;

import java.util.List;

public class PromotionClientAdapter extends RecyclerView.Adapter<PromotionClientAdapter.PromotionViewHolder> {

    private final List<Promotion> promotionList;

    public PromotionClientAdapter(List<Promotion> promotionList) {
        this.promotionList = promotionList;
    }

    @NonNull
    @Override
    public PromotionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_promotion_client, parent, false);
        return new PromotionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PromotionViewHolder holder, int position) {
        Promotion promotion = promotionList.get(position);

        String code = promotion.getCode();
        if (TextUtils.isEmpty(code)) {
            code = promotion.getId();
        }
        holder.tvTitle.setText(!TextUtils.isEmpty(promotion.getTitle()) ? promotion.getTitle() : code);

        String description = promotion.getDescription();
        holder.tvContent.setText(TextUtils.isEmpty(description) ? code : description);

        String valueLabel;
        if ("percent".equals(promotion.getType())) {
            valueLabel = "-" + (int) promotion.getValue() + "%";
        } else {
            valueLabel = "-" + PriceFormatHelper.formatVnd((int) promotion.getValue());
        }

        StringBuilder meta = new StringBuilder(valueLabel);
        if (promotion.getMinSubtotal() > 0) {
            meta.append(" • ")
                    .append(holder.itemView.getContext().getString(
                            R.string.str_min_order,
                            PriceFormatHelper.formatVnd((int) promotion.getMinSubtotal())
                                    .replace("đ", "").trim()));
        }
        if (!TextUtils.isEmpty(promotion.getExpiry())) {
            meta.append(" • HSD: ").append(promotion.getExpiry());
        }
        holder.tvMeta.setText(meta.toString());
    }

    @Override
    public int getItemCount() {
        return promotionList.size();
    }

    static class PromotionViewHolder extends RecyclerView.ViewHolder {
        final TextView tvTitle;
        final TextView tvContent;
        final TextView tvMeta;

        PromotionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvPromotionTitle);
            tvContent = itemView.findViewById(R.id.tvPromotionContent);
            tvMeta = itemView.findViewById(R.id.tvPromotionMeta);
        }
    }
}
