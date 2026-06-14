package com.teatrack_mcd_253eie502802_group02.model;

public class Product {
    private String id;
    private String name;
    private String image;
    private String category;
    private int imageRes;
    private float rating = 4.9f;
    private String reviewCount = "1k";
    private long price;
    private long priceL;
    private long vipPriceM;
    private long vipPriceL;
    private String priceMText;
    private String priceLText;
    private String vipPriceMText;
    private String vipPriceLText;

    public Product() {
    }

    public Product(String name, int imageRes, float rating, String reviewCount,
                   String priceM, String priceL, String vipPriceM, String vipPriceL) {
        this.name = name;
        this.imageRes = imageRes;
        this.rating = rating;
        this.reviewCount = reviewCount;
        this.priceMText = priceM;
        this.priceLText = priceL;
        this.vipPriceMText = vipPriceM;
        this.vipPriceLText = vipPriceL;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getImageRes() {
        return imageRes;
    }

    public float getRating() {
        return rating;
    }

    public String getReviewCount() {
        return reviewCount;
    }

    public String getPriceM() {
        return priceMText != null ? priceMText : formatPrice(price);
    }

    public String getPriceL() {
        return priceLText != null ? priceLText : formatPrice(priceL);
    }

    public String getVipPriceM() {
        return vipPriceMText != null ? vipPriceMText : formatPrice(vipPriceM);
    }

    public String getVipPriceL() {
        return vipPriceLText != null ? vipPriceLText : formatPrice(vipPriceL);
    }

    public void setPrice(long price) {
        this.price = price;
    }

    public void setPriceL(long priceL) {
        this.priceL = priceL;
    }

    public void setVipPriceM(long vipPriceM) {
        this.vipPriceM = vipPriceM;
    }

    public void setVipPriceL(long vipPriceL) {
        this.vipPriceL = vipPriceL;
    }

    private static String formatPrice(long value) {
        if (value <= 0) {
            return "0";
        }
        String raw = String.valueOf(value);
        StringBuilder builder = new StringBuilder();
        int groupLength = raw.length() % 3;
        if (groupLength == 0) {
            groupLength = 3;
        }
        builder.append(raw, 0, groupLength);
        for (int i = groupLength; i < raw.length(); i += 3) {
            builder.append('.').append(raw, i, i + 3);
        }
        return builder.toString();
    }
}
