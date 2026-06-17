package com.teatrack_mcd_253eie502802_group02.model;

import java.util.List;
import android.content.Context;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import com.teatrack_mcd_253eie502802_group02.R;

@IgnoreExtraProperties
public class Product {
    private String id;
    private String code;
    private String name;
    private String image;
    private List<String> images;
    private String category;
    private int price; // Size M price
    private int priceL;
    private int vipPriceM;
    private int vipPriceL;
    private String description; // Product Information
    private String detail;      // Product Description
    private boolean visible;
    private boolean special;

    private int imageRes;
    private float rating = 4.9f;
    private String reviewCount = "1k";
    private float rating;
    private String reviewCount;

    public Product() {
    }


    public Product(int imageRes,float rating, String reviewCount, String id, String code, String name, String category, int price, int priceL, int vipPriceM, int vipPriceL, String description, String detail, String image, boolean visible, boolean special) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.category = category;
        this.imageRes = imageRes;
        this.rating = rating;
        this.reviewCount = reviewCount;
        this.price = price;
        this.priceL = priceL;
        this.vipPriceM = vipPriceM;
        this.vipPriceL = vipPriceL;
        this.description = description;
        this.detail = detail;
        this.image = image;
        this.visible = visible;
        this.special = special;
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

    public int getVipPriceM() { return vipPriceM; }
    public void setVipPriceM(int vipPriceM) { this.vipPriceM = vipPriceM; }

    public int getVipPriceL() { return vipPriceL; }
    public void setVipPriceL(int vipPriceL) { this.vipPriceL = vipPriceL; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getDetail() { return detail; }
    public void setDetail(String detail) { this.detail = detail; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    public boolean isVisible() { return visible; }
    public void setVisible(boolean visible) { this.visible = visible; }

    public boolean isSpecial() { return special; }
    public void setSpecial(boolean special) { this.special = special; }

    @Exclude
    public int getImageRes(Context context) {
        if (imageRes != 0) return imageRes;
        if (image != null && !image.isEmpty()) {
            String imageName = image.toLowerCase().split("\\.")[0];
            int resId = context.getResources().getIdentifier(imageName, "mipmap", context.getPackageName());
            if (resId != 0) return resId;
        }
        return R.mipmap.logo_ngo_gia;
    }

    @Exclude
    public float getRating() { return rating; }
    public void setRating(float rating) { this.rating = rating; }

    @Exclude
    public String getReviewCount() { return reviewCount; }
    public void setReviewCount(String reviewCount) { this.reviewCount = reviewCount; }
}
