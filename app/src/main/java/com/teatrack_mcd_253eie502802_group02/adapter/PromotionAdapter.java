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

public class PromotionAdapter extends RecyclerView.Adapter<PromotionAdapter.PromotionViewHolder> {

    private static final int[] BANNER_IMAGES = {
            R.mipmap.banner1,
            R.mipmap.banner2,
            R.mipmap.thuc_uong_moi,
            R.mipmap.cac_mon_hot,
            R.mipmap.banner_monmoi,
            R.mipmap.banner_aboutus
    };

    private final List<Promotion> promotions;
    private OnItemClickListener listener;
    private int itemWidthPx = 0;

    public interface OnItemClickListener {
        void onItemClick(Promotion item);
    }

    public PromotionAdapter(List<Promotion> promotions) {
        this.promotions = promotions;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setItemWidthPx(int itemWidthPx) {
        if (itemWidthPx <= 0 || this.itemWidthPx == itemWidthPx) {
            return;
        }
        this.itemWidthPx = itemWidthPx;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PromotionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_home_promotion_mini, parent, false);
        if (itemWidthPx > 0) {
            ViewGroup.LayoutParams params = view.getLayoutParams();
            if (params == null) {
                params = new RecyclerView.LayoutParams(itemWidthPx, ViewGroup.LayoutParams.WRAP_CONTENT);
            } else {
                params.width = itemWidthPx;
            }
            view.setLayoutParams(params);
        }
        return new PromotionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PromotionViewHolder holder, int position) {
        if (itemWidthPx > 0) {
            ViewGroup.LayoutParams params = holder.itemView.getLayoutParams();
            if (params != null && params.width != itemWidthPx) {
                params.width = itemWidthPx;
                holder.itemView.setLayoutParams(params);
            }
        }

        Promotion item = promotions.get(position);
        holder.tvTitle.setText(getShortTitle(item));

        int placeholderRes = item.getImageRes() != 0
                ? item.getImageRes()
                : BANNER_IMAGES[position % BANNER_IMAGES.length];
        String imageUrl = item.getImage();

        Glide.with(holder.itemView.getContext())
                .load(TextUtils.isEmpty(imageUrl) ? placeholderRes : imageUrl)
                .placeholder(placeholderRes)
                .error(placeholderRes)
                .centerCrop()
                .into(holder.imgPromotion);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return promotions.size();
    }

    static String getShortTitle(Promotion promotion) {
        if (promotion == null) {
            return "";
        }
        String title = !TextUtils.isEmpty(promotion.getTitle())
                ? promotion.getTitle()
                : promotion.getCode();
        if (TextUtils.isEmpty(title)) {
            return "";
        }
        title = title.trim();
        if (title.length() > 34) {
            return title.substring(0, 32) + "...";
        }
        return title;
    }

    static class PromotionViewHolder extends RecyclerView.ViewHolder {
        final ImageView imgPromotion;
        final TextView tvTitle;

        PromotionViewHolder(@NonNull View itemView) {
            super(itemView);
            imgPromotion = itemView.findViewById(R.id.imgPromotion);
            tvTitle = itemView.findViewById(R.id.tvPromotionTitle);
        }
    }
}
