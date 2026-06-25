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

        // Ưu tiên URL (Cloudinary / Firebase Storage)
        String imageUrl = product.getImage();
        if (imageUrl != null && imageUrl.trim().startsWith("http")) {
            loadFromUrl(imageView, imageUrl.trim());
            return;
        }

        // Danh sách ảnh nếu có URL
        List<String> images = product.getImages();
        if (images != null && !images.isEmpty()) {
            String firstImage = images.get(0);
            if (firstImage != null && firstImage.trim().startsWith("http")) {
                loadFromUrl(imageView, firstImage.trim());
                return;
            }
        }

        // Resource ID cứng — dùng Glide (không dùng setImageResource để tránh decode trên main thread)
        if (product.getImageRes() != 0) {
            Glide.with(imageView.getContext())
                    .load(product.getImageRes())
                    .apply(new RequestOptions()
                            .placeholder(R.mipmap.logo_ngo_gia)
                            .error(R.mipmap.logo_ngo_gia)
                            .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                            .centerCrop())
                    .into(imageView);
            return;
        }

        // Tìm mipmap theo tên file — dùng Glide
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
        Context context = imageView.getContext();
        int resolvedId = resolveResourceId(context, source);
        int target = resolvedId != 0 ? resolvedId : (fallbackRes != 0 ? fallbackRes : R.mipmap.logo_ngo_gia);
        Glide.with(context)
                .load(target)
                .apply(new RequestOptions()
                        .placeholder(R.mipmap.logo_ngo_gia)
                        .error(R.mipmap.logo_ngo_gia)
                        .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                        .centerCrop())
                .into(imageView);
    }

    // getIdentifier() chỉ được gọi một lần per image-name và kết quả nên được cache ở tầng gọi
    private static int resolveResourceId(Context context, String source) {
        if (source == null || source.isEmpty()) return 0;
        String name = source;
        int slash = name.lastIndexOf('/');
        if (slash >= 0 && slash < name.length() - 1) name = name.substring(slash + 1);
        name = name.replaceAll("\\.(png|jpg|jpeg|webp)$", "");
        int id = context.getResources().getIdentifier(name, "mipmap", context.getPackageName());
        if (id == 0) id = context.getResources().getIdentifier(name, "drawable", context.getPackageName());
        return id;
    }
}
