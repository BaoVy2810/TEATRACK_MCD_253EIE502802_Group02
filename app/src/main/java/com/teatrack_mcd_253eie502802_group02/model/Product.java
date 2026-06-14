package com.teatrack_mcd_253eie502802_group02.model;

public class Product {
    private String name;
    private int imageRes;
    private float rating;
    private String reviewCount;
    private String priceM;
    private String priceL;
    private String vipPriceM;
    private String vipPriceL;

    public Product(String name, int imageRes, float rating, String reviewCount,
                   String priceM, String priceL, String vipPriceM, String vipPriceL) {
        this.name = name;
        this.imageRes = imageRes;
        this.rating = rating;
        this.reviewCount = reviewCount;
        this.priceM = priceM;
        this.priceL = priceL;
        this.vipPriceM = vipPriceM;
        this.vipPriceL = vipPriceL;
    }

    public String getName() { return name; }
    public int getImageRes() { return imageRes; }
    public float getRating() { return rating; }
    public String getReviewCount() { return reviewCount; }
    public String getPriceM() { return priceM; }
    public String getPriceL() { return priceL; }
    public String getVipPriceM() { return vipPriceM; }
    public String getVipPriceL() { return vipPriceL; }
}