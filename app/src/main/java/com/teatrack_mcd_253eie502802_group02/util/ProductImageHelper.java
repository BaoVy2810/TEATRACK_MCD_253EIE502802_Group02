package com.teatrack_mcd_253eie502802_group02.util;

import android.content.Context;
import android.content.Intent;
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

    /** Firebase RTDB product id/code (NG01–NG47) -> mipmap basename. */
    private static final Map<String, String> PRODUCT_CODE_IMAGE = new HashMap<>();

    static {
        PRODUCT_CODE_IMAGE.put("NG01", "traolongmochuong");
        PRODUCT_CODE_IMAGE.put("NG02", "hongtrabidao");
        PRODUCT_CODE_IMAGE.put("NG03", "traxanhbidao");
        PRODUCT_CODE_IMAGE.put("NG04", "traxanhhoanhai");
        PRODUCT_CODE_IMAGE.put("NG05", "hongtravaithieu");
        PRODUCT_CODE_IMAGE.put("NG06", "trabidaongogia");
        PRODUCT_CODE_IMAGE.put("NG07", "hongtradailoan");
        PRODUCT_CODE_IMAGE.put("NG08", "trasuauyenuong");
        PRODUCT_CODE_IMAGE.put("NG09", "trasuaolong");
        PRODUCT_CODE_IMAGE.put("NG10", "trasuasocola");
        PRODUCT_CODE_IMAGE.put("NG11", "trasuatranchauduongden");
        PRODUCT_CODE_IMAGE.put("NG12", "traxanhsua");
        PRODUCT_CODE_IMAGE.put("NG13", "trasuavaithieu");
        PRODUCT_CODE_IMAGE.put("NG14", "trasuabidao");
        PRODUCT_CODE_IMAGE.put("NG15", "trasuadailoan");
        PRODUCT_CODE_IMAGE.put("NG16", "suatuoikhoaimonnghien");
        PRODUCT_CODE_IMAGE.put("NG17", "olonglatte");
        PRODUCT_CODE_IMAGE.put("NG18", "suatuoitranchauduongden");
        PRODUCT_CODE_IMAGE.put("NG19", "tranchauduongdenlatte");
        PRODUCT_CODE_IMAGE.put("NG20", "traxanhlatte");
        PRODUCT_CODE_IMAGE.put("NG21", "hongtralattevaithieu");
        PRODUCT_CODE_IMAGE.put("NG22", "bidaolatte");
        PRODUCT_CODE_IMAGE.put("NG23", "hongtralattedailoan");
        PRODUCT_CODE_IMAGE.put("NG24", "olongnho");
        PRODUCT_CODE_IMAGE.put("NG25", "traolongdao");
        PRODUCT_CODE_IMAGE.put("NG26", "mauhonglangman");
        PRODUCT_CODE_IMAGE.put("NG27", "traxanhdaongogia");
        PRODUCT_CODE_IMAGE.put("NG28", "hongtradaongogia");
        PRODUCT_CODE_IMAGE.put("NG29", "traxanhchanh");
        PRODUCT_CODE_IMAGE.put("NG30", "hongtrachanhvaithieu");
        PRODUCT_CODE_IMAGE.put("NG31", "trabidaochanh");
        PRODUCT_CODE_IMAGE.put("NG32", "hongtrachanhdailoan");
        PRODUCT_CODE_IMAGE.put("NG33", "hongtratrungkhunglong");
        PRODUCT_CODE_IMAGE.put("NG34", "trasualongchau");
        PRODUCT_CODE_IMAGE.put("NG35", "olongomaichanhday");
        PRODUCT_CODE_IMAGE.put("NG36", "traxanhomaichanhday");
        PRODUCT_CODE_IMAGE.put("NG37", "trasuaolongkhoaimonnghien");
        PRODUCT_CODE_IMAGE.put("NG38", "trasuakhoaimonnghien");
        PRODUCT_CODE_IMAGE.put("NG39", "traolongbidao");
        PRODUCT_CODE_IMAGE.put("NG40", "batbaongogiangot");
        PRODUCT_CODE_IMAGE.put("NG41", "hongtrakemtuoi");
        PRODUCT_CODE_IMAGE.put("NG42", "olongkemcheese");
        PRODUCT_CODE_IMAGE.put("NG43", "traxanhkemcheese");
        PRODUCT_CODE_IMAGE.put("NG44", "hongtrakemcheese");
        PRODUCT_CODE_IMAGE.put("NG45", "traximuoiolong");
        PRODUCT_CODE_IMAGE.put("NG46", "traximuoibidao");
        PRODUCT_CODE_IMAGE.put("NG47", "traximuoingogia");
        PRODUCT_CODE_IMAGE.put("prod_hongtra_ngogia", "hongtradailoan");
        PRODUCT_CODE_IMAGE.put("prod_olong_mochuong", "traolongmochuong");
        PRODUCT_CODE_IMAGE.put("prod_hongtra_bidao", "hongtrabidao");
        PRODUCT_CODE_IMAGE.put("prod_traxanh_bidao", "traxanhbidao");
        PRODUCT_CODE_IMAGE.put("prod_suatuoi_khoaimon", "suatuoikhoaimonnghien");
        PRODUCT_CODE_IMAGE.put("prod_olong_latte", "olonglatte");
        PRODUCT_CODE_IMAGE.put("prod_trasuatranchau", "trasuatranchauduongden");
    }

    private ProductImageHelper() {
    }

    public static void putDetailExtras(Intent intent, Context context, Product product) {
        if (product == null || intent == null) {
            return;
        }
        enrichFromFirebase(product);
        if (product.getImage() != null && !product.getImage().trim().isEmpty()) {
            intent.putExtra("productImage", product.getImage().trim());
        }
        if (product.getCode() != null && !product.getCode().trim().isEmpty()) {
            intent.putExtra("productCode", product.getCode().trim());
        }
        intent.putExtra("imageRes", product.getImageRes(context));
    }

    /** Fill missing Firebase metadata (code, image filename) from NG product id. */
    public static void enrichFromFirebase(Product product) {
        if (product == null) {
            return;
        }
        String key = product.getId();
        if ((product.getCode() == null || product.getCode().isEmpty()) && key != null && !key.isEmpty()) {
            product.setCode(key);
        }
        String image = product.getImage();
        if (image != null && !image.trim().isEmpty()) {
            product.setImage(image.trim());
            return;
        }
        String basename = resolveBasenameForProduct(product);
        if (basename != null) {
            product.setImage(basename + ".png");
        }
    }

    public static void prepareForDisplay(Context context, Product product) {
        if (product == null || context == null) {
            return;
        }
        enrichFromFirebase(product);
        applyTrustedImageRes(context, product);
    }

    public static void load(ImageView imageView, Product product) {
        if (product == null) {
            return;
        }

        Context context = imageView.getContext();
        prepareForDisplay(context, product);
        Glide.with(context).clear(imageView);

        int localRes = resolveTrustedLocalRes(context, product);
        if (localRes != 0) {
            imageView.setImageResource(localRes);
            return;
        }

        String imageUrl = firstHttpUrl(product);
        if (imageUrl != null) {
            loadFromUrl(imageView, imageUrl, R.mipmap.logo_ngo_gia);
            return;
        }

        imageView.setImageResource(R.mipmap.logo_ngo_gia);
    }

    public static int resolveLocalRes(Context context, Product product) {
        return resolveTrustedLocalRes(context, product);
    }

    /**
     * Priority:
     * 1) Firebase {@code image} / {@code images} filename
     * 2) Product {@code id} / {@code code} map (NG01–NG47)
     * 3) Exact catalog name (offline fallback)
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

        int fromCode = resolveFromProductCode(context, product.getId());
        if (fromCode != 0) {
            return fromCode;
        }

        fromCode = resolveFromProductCode(context, product.getCode());
        if (fromCode != 0) {
            return fromCode;
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

    private static int resolveFromProductCode(Context context, String productCode) {
        if (productCode == null || productCode.isEmpty()) {
            return 0;
        }
        String basename = resolveBasenameForCode(productCode.trim());
        if (basename == null) {
            return 0;
        }
        return resolveResourceId(context, basename);
    }

    private static String resolveBasenameForProduct(Product product) {
        if (product == null) {
            return null;
        }
        String fromId = resolveBasenameForCode(product.getId());
        if (fromId != null) {
            return fromId;
        }
        return resolveBasenameForCode(product.getCode());
    }

    private static String resolveBasenameForCode(String productCode) {
        if (productCode == null || productCode.isEmpty()) {
            return null;
        }
        return PRODUCT_CODE_IMAGE.get(productCode.trim());
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
