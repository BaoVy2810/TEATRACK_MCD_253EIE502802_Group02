package com.teatrack_mcd_253eie502802_group02.util;

import android.content.Context;
import android.widget.ImageView;

import com.teatrack_mcd_253eie502802_group02.R;
import com.teatrack_mcd_253eie502802_group02.model.Product;

import java.util.List;

public final class ProductImageHelper {

    private ProductImageHelper() {
    }

    public static void load(ImageView imageView, Product product) {
        if (product.getImageRes() != 0) {
            imageView.setImageResource(product.getImageRes());
            return;
        }

        List<String> images = product.getImages();
        if (images != null && !images.isEmpty()) {
            loadFromSource(imageView, images.get(0), R.mipmap.tra_yakult);
            return;
        }

        loadFromSource(imageView, product.getImage(), R.mipmap.tra_yakult);
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
