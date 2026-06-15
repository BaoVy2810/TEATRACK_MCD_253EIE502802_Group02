package com.teatrack_mcd_253eie502802_group02.model;

import android.content.Context;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import com.teatrack_mcd_253eie502802_group02.R;

@IgnoreExtraProperties
public class Product {
    private String id;
    private String code;
    private String name;
    private String category;
    private int price; // This is Size M price in Firebase
    private int priceL;
    private int vipPriceL;
    private String image;
    private boolean visible;
    private int imageRes;
    private float rating;
    private String reviewCount;

    public Product() {
        // Required for Firebase
    }

    public Product(String id, String code, String name, String category, int price, int priceL, int vipPriceL, String image, boolean visible) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.category = category;
        this.price = price;
        this.priceL = priceL;
        this.vipPriceL = vipPriceL;
        this.image = image;
        this.visible = visible;
    }

    public Product(String name, int imageRes, float rating, String reviewCount, String priceM, String priceL, String vipPriceM, String vipPriceL) {
        this.name = name;
        this.imageRes = imageRes;
        this.rating = rating;
        this.reviewCount = reviewCount;
        this.price = Integer.parseInt(priceM.replace(".", ""));
        this.priceL = Integer.parseInt(priceL.replace(".", ""));
        this.vipPriceL = Integer.parseInt(vipPriceL.replace(".", ""));
        this.visible = true;
    }

    @Exclude
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public int getPrice() { return price; }
    public void setPrice(int price) { this.price = price; }

    public int getPriceL() { return priceL; }
    public void setPriceL(int priceL) { this.priceL = priceL; }

    public int getVipPriceL() { return vipPriceL; }
    public void setVipPriceL(int vipPriceL) { this.vipPriceL = vipPriceL; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    public boolean isVisible() { return visible; }
    public void setVisible(boolean visible) { this.visible = visible; }

    @Exclude
    public int getImageRes() { return imageRes; }
    public void setImageRes(int imageRes) { this.imageRes = imageRes; }

    @Exclude
    public int getImageRes(Context context) {
        if (imageRes != 0) {
            return imageRes;
        }
        if (image != null && !image.isEmpty()) {
            String imageName = image.toLowerCase()
                    .replace(".png", "")
                    .replace(".jpg", "")
                    .replace(".jpeg", "")
                    .replace(".webp", "");
            int resId = context.getResources().getIdentifier(
                    imageName,
                    "mipmap",
                    context.getPackageName());
            if (resId != 0) {
                return resId;
            }
        }
        return R.mipmap.logo_ngo_gia;
    }

    @Exclude
    public float getRating() { return rating; }
    public void setRating(float rating) { this.rating = rating; }

    @Exclude
    public String getReviewCount() { return reviewCount; }
    public void setReviewCount(String reviewCount) { this.reviewCount = reviewCount; }

    // Business Logic Getters
    @Exclude
    public int getPriceM() {
        return price;
    }

    @Exclude
    public int getVipPriceM() {
        return price > 3000 ? price - 3000 : 0;
    }
}
