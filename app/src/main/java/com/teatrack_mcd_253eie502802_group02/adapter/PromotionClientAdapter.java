package com.teatrack_mcd_253eie502802_group02.adapter;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.teatrack_mcd_253eie502802_group02.R;
import com.teatrack_mcd_253eie502802_group02.model.Promotion;

import java.util.List;

public class PromotionClientAdapter extends RecyclerView.Adapter<PromotionClientAdapter.PromotionViewHolder> {

    private static final int[] BANNER_IMAGES = {
            R.mipmap.banner1,
            R.mipmap.banner2,
            R.mipmap.thuc_uong_moi,
            R.mipmap.cac_mon_hot
    };

    private final List<Promotion> promotionList;

    public PromotionClientAdapter(List<Promotion> promotionList) {
        this.promotionList = promotionList;
    }

    private String formatPrice(int price) {
        return String.format(java.util.Locale.US, "%,d", price).replace(',', '.') + "đ";
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

        String title = !TextUtils.isEmpty(promotion.getTitle())
                ? promotion.getTitle()
                : promotion.getCode();
        holder.tvTitle.setText(title);

        String description = promotion.getDescription();
        if (TextUtils.isEmpty(description)) {
            android.content.Context ctx = holder.itemView.getContext();
            if ("percent".equals(promotion.getType())) {
                description = ctx.getString(R.string.str_desc_percent, (int) promotion.getValue());
                if (promotion.getMax() > 0) {
                    description += ctx.getString(R.string.str_desc_max, formatPrice((int) promotion.getMax()));
                }
            } else if ("per_item".equals(promotion.getType())) {
                description = ctx.getString(R.string.str_desc_per_item, formatPrice((int) promotion.getValue()));
            } else if ("amount".equals(promotion.getType())) {
                description = ctx.getString(R.string.str_desc_amount, formatPrice((int) promotion.getValue()));
            } else if (!TextUtils.isEmpty(promotion.getExpiry())) {
                description = ctx.getString(R.string.loyalty_promo_expiry_format, promotion.getExpiry());
            }
        }
        holder.tvContent.setText(TextUtils.isEmpty(description) ? title : description);

        boolean exclusive = position % 2 == 1;
        holder.tvBadge.setText(holder.itemView.getContext().getString(
                exclusive ? R.string.loyalty_badge_exclusive : R.string.loyalty_badge_hot_deal));
        holder.tvBadge.setBackgroundResource(
                exclusive ? R.drawable.bg_promo_badge_tertiary : R.drawable.bg_promo_badge_primary);

        int placeholderRes = BANNER_IMAGES[position % BANNER_IMAGES.length];
        String imageUrl = promotion.getImage();

        Glide.with(holder.itemView.getContext())
                .load(TextUtils.isEmpty(imageUrl) ? placeholderRes : imageUrl)
                .placeholder(placeholderRes)
                .error(placeholderRes)
                .centerCrop()
                .into(holder.imgBanner);
    }

    @Override
    public int getItemCount() {
        return promotionList.size();
    }

    static class PromotionViewHolder extends RecyclerView.ViewHolder {
        final ImageView imgBanner;
        final TextView tvBadge;
        final TextView tvTitle;
        final TextView tvContent;

        PromotionViewHolder(@NonNull View itemView) {
            super(itemView);
            imgBanner = itemView.findViewById(R.id.imgPromoBanner);
            tvBadge = itemView.findViewById(R.id.tvPromoBadge);
            tvTitle = itemView.findViewById(R.id.tvPromotionTitle);
            tvContent = itemView.findViewById(R.id.tvPromotionContent);
        }
    }
}
