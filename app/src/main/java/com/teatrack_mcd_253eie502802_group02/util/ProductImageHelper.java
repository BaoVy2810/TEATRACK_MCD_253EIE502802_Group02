package com.teatrack_mcd_253eie502802_group02.util;

import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.teatrack_mcd_253eie502802_group02.R;
import com.teatrack_mcd_253eie502802_group02.model.Product;

import java.util.List;

public final class ProductImageHelper {

    private ProductImageHelper() {
    }

    public static void load(ImageView imageView, Product product) {
        if (product == null) return;

        // Ưu tiên URL (Cloudinary)
        String imageUrl = product.getImage();
        if (imageUrl != null && imageUrl.trim().startsWith("http")) {
            loadFromUrl(imageView, imageUrl.trim());
            return;
        }

        // Sau đó đến danh sách ảnh (nếu có URL)
        List<String> images = product.getImages();
        if (images != null && !images.isEmpty()) {
            String firstImage = images.get(0);
            if (firstImage != null && firstImage.trim().startsWith("http")) {
                loadFromUrl(imageView, firstImage.trim());
                return;
            }
        }

        // Nếu có resource ID cứng
        if (product.getImageRes() != 0) {
            imageView.setImageResource(product.getImageRes());
            return;
        }

        // Cuối cùng là tìm trong drawable/mipmap dựa trên tên ảnh hoặc fallback
        loadFromSource(imageView, product.getImage(), R.mipmap.logo_ngo_gia);
    }

    public static void loadFromUrl(ImageView imageView, String url) {
        Glide.with(imageView.getContext())
                .load(url)
                .apply(new RequestOptions()
                        .placeholder(R.mipmap.logo_ngo_gia)
                        .error(R.mipmap.logo_ngo_gia)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .centerCrop())
                .into(imageView);
    }

    public static void loadFromSource(ImageView imageView, String source, int fallbackRes) {
        if (source == null || source.isEmpty()) {
            if (fallbackRes != 0) {
                imageView.setImageResource(fallbackRes);
            }
            return;
        }

        Context context = imageView.getContext();
        String resourceName = source;
        int slash = resourceName.lastIndexOf('/');
        if (slash >= 0 && slash < resourceName.length() - 1) {
            resourceName = resourceName.substring(slash + 1);
        }
        resourceName = resourceName.replaceAll("\\.(png|jpg|jpeg|webp)$", "");
        int resourceId = context.getResources()
                .getIdentifier(resourceName, "mipmap", context.getPackageName());
        if (resourceId == 0) {
            resourceId = context.getResources()
                    .getIdentifier(resourceName, "drawable", context.getPackageName());
        }
        if (resourceId != 0) {
            imageView.setImageResource(resourceId);
        } else if (fallbackRes != 0) {
            imageView.setImageResource(fallbackRes);
        } else {
            imageView.setImageResource(R.mipmap.tra_yakult);
        }
    }
}
