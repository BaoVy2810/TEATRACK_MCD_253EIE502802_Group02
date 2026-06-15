package com.teatrack_mcd_253eie502802_group02.util;

import android.content.Context;
import android.widget.ImageView;

import com.teatrack_mcd_253eie502802_group02.R;
import com.teatrack_mcd_253eie502802_group02.model.Product;

public final class ProductImageHelper {

    private ProductImageHelper() {
    }

    public static void load(ImageView imageView, Product product) {
        if (product.getImageRes() != 0) {
            imageView.setImageResource(product.getImageRes());
            return;
        }

        Context context = imageView.getContext();
        String imageName = product.getImage();
        if (imageName == null || imageName.isEmpty()) {
            imageView.setImageResource(R.mipmap.tra_yakult);
            return;
        }

        String resourceName = imageName.replaceAll("\\.(png|jpg|jpeg|webp)$", "");
        int resourceId = context.getResources()
                .getIdentifier(resourceName, "mipmap", context.getPackageName());
        if (resourceId != 0) {
            imageView.setImageResource(resourceId);
        } else {
            imageView.setImageResource(R.mipmap.tra_yakult);
        }
    }
}
