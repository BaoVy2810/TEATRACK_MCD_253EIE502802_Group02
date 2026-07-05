package com.teatrack_mcd_253eie502802_group02.util;

import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.teatrack_mcd_253eie502802_group02.R;
import com.teatrack_mcd_253eie502802_group02.client.CategoryProductData;
import com.teatrack_mcd_253eie502802_group02.model.Product;

import java.text.Normalizer;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class ProductImageHelper {

    /** Legacy order/product ids from seed data -> mipmap basename (without extension). */
    private static final Map<String, String> LEGACY_ID_IMAGE = new HashMap<>();

    static {
        LEGACY_ID_IMAGE.put("prod_hongtra_ngogia", "hongtradailoan");
        LEGACY_ID_IMAGE.put("prod_olong_mochuong", "traolongmochuong");
        LEGACY_ID_IMAGE.put("prod_hongtra_bidao", "hongtrabidao");
        LEGACY_ID_IMAGE.put("prod_traxanh_bidao", "traxanhbidao");
        LEGACY_ID_IMAGE.put("prod_suatuoi_khoaimon", "suatuoikhoaimonnghien");
        LEGACY_ID_IMAGE.put("prod_olong_latte", "olonglatte");
        LEGACY_ID_IMAGE.put("prod_trasuatranchau", "trasuatranchauduongden");
    }

    private ProductImageHelper() {
    }

    public static void load(ImageView imageView, Product product) {
        if (product == null) {
            return;
        }

        Context context = imageView.getContext();
        Glide.with(context).clear(imageView);

        int localRes = resolveTrustedLocalRes(context, product);
        if (localRes != 0) {
            loadResource(imageView, localRes);
            return;
        }

        String imageUrl = firstHttpUrl(product);
        if (imageUrl != null) {
            loadFromUrl(imageView, imageUrl, R.mipmap.logo_ngo_gia);
            return;
        }

        loadResource(imageView, R.mipmap.logo_ngo_gia);
    }

    public static int resolveLocalRes(Context context, Product product) {
        return resolveTrustedLocalRes(context, product);
    }

    /**
     * Priority:
     * 1) Firebase {@code image} / {@code images} local filename (source of truth in RTDB)
     * 2) Legacy {@code prod_*} ids from older order seeds
     * 3) Exact catalog name match (offline menu fallback only)
     * 4) Cached {@code imageRes} from navigation intent
     */
    public static int resolveTrustedLocalRes(Context context, Product product) {
        if (product == null || context == null) {
            return 0;
        }

        int fromFirebase = resolveFromFirebaseFields(context, product);
        if (fromFirebase != 0) {
            return fromFirebase;
        }

        int fromLegacyId = resolveFromLegacyProductId(context, product.getId());
        if (fromLegacyId != 0) {
            return fromLegacyId;
        }

        int fromCatalog = resolveFromCatalogExact(product.getName());
        if (fromCatalog != 0) {
            return fromCatalog;
        }

        if (product.getImageRes() != 0) {
            return product.getImageRes();
        }

        return 0;
    }

    public static int resolveCatalogRes(String productName) {
        return resolveFromCatalogExact(productName);
    }

    public static void applyTrustedImageRes(Context context, Product product) {
        if (product == null) {
            return;
        }
        int resolved = resolveTrustedLocalRes(context, product);
        if (resolved != 0) {
            product.setImageRes(resolved);
        }
    }

    public static void loadFromUrl(ImageView imageView, String url) {
        loadFromUrl(imageView, url, R.mipmap.logo_ngo_gia);
    }

    public static void loadFromUrl(ImageView imageView, String url, int fallbackRes) {
        Context context = imageView.getContext();
        Glide.with(context).clear(imageView);
        Glide.with(context)
                .load(url)
                .apply(new RequestOptions()
                        .placeholder(fallbackRes)
                        .error(fallbackRes)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .centerCrop())
                .into(imageView);
    }

    public static void loadFromSource(ImageView imageView, String source, int fallbackRes) {
        Context context = imageView.getContext();
        Glide.with(context).clear(imageView);
        if (isHttpUrl(source)) {
            loadFromUrl(imageView, source.trim(), fallbackRes);
            return;
        }
        int resolvedId = resolveResourceId(context, source);
        int target = resolvedId != 0 ? resolvedId : (fallbackRes != 0 ? fallbackRes : R.mipmap.logo_ngo_gia);
        loadResource(imageView, target);
    }

    public static int resolveResourceId(Context context, String source) {
        if (source == null || source.isEmpty() || isHttpUrl(source)) {
            return 0;
        }
        String name = source;
        int slash = name.lastIndexOf('/');
        if (slash >= 0 && slash < name.length() - 1) {
            name = name.substring(slash + 1);
        }
        name = name.replaceAll("(?i)\\.(png|jpg|jpeg|webp)$", "");
        int id = context.getResources().getIdentifier(name, "mipmap", context.getPackageName());
        if (id == 0) {
            id = context.getResources().getIdentifier(name, "drawable", context.getPackageName());
        }
        return id;
    }

    private static int resolveFromFirebaseFields(Context context, Product product) {
        int fromImage = resolveResourceId(context, product.getImage());
        if (fromImage != 0) {
            return fromImage;
        }

        List<String> images = product.getImages();
        if (images != null) {
            for (String image : images) {
                if (isHttpUrl(image)) {
                    continue;
                }
                int res = resolveResourceId(context, image);
                if (res != 0) {
                    return res;
                }
            }
        }
        return 0;
    }

    private static int resolveFromLegacyProductId(Context context, String productId) {
        if (productId == null || productId.isEmpty()) {
            return 0;
        }
        String basename = LEGACY_ID_IMAGE.get(productId.trim());
        if (basename == null) {
            return 0;
        }
        return resolveResourceId(context, basename);
    }

    private static int resolveFromCatalogExact(String productName) {
        if (productName == null || productName.trim().isEmpty()) {
            return 0;
        }
        String target = normalizeProductName(productName);
        for (Map.Entry<String, List<Product>> entry : CategoryProductData.getCategoryProductsMap().entrySet()) {
            List<Product> products = entry.getValue();
            if (products == null) {
                continue;
            }
            for (Product product : products) {
                if (product.getName() == null) {
                    continue;
                }
                if (normalizeProductName(product.getName()).equals(target)) {
                    return product.getImageRes();
                }
            }
        }
        return 0;
    }

    private static String firstHttpUrl(Product product) {
        if (isHttpUrl(product.getImage())) {
            return product.getImage().trim();
        }
        List<String> images = product.getImages();
        if (images != null) {
            for (String image : images) {
                if (isHttpUrl(image)) {
                    return image.trim();
                }
            }
        }
        return null;
    }

    private static String normalizeProductName(String name) {
        String trimmed = name.trim().replaceAll("\\s+", " ");
        String normalized = Normalizer.normalize(trimmed, Normalizer.Form.NFD);
        normalized = normalized.replaceAll("\\p{M}", "");
        return normalized.toLowerCase(Locale.ROOT);
    }

    private static void loadResource(ImageView imageView, int resId) {
        Glide.with(imageView.getContext())
                .load(resId)
                .apply(new RequestOptions()
                        .placeholder(R.mipmap.logo_ngo_gia)
                        .error(R.mipmap.logo_ngo_gia)
                        .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                        .centerCrop())
                .into(imageView);
    }

    private static boolean isHttpUrl(String value) {
        if (value == null) {
            return false;
        }
        String trimmed = value.trim();
        return trimmed.startsWith("http://") || trimmed.startsWith("https://");
    }
}
