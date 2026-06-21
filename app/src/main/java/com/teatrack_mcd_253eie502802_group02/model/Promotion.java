package com.teatrack_mcd_253eie502802_group02.model;

public class Promotion {
    private String id;
    private int imageRes;

    public Promotion(String id, int imageRes) {
        this.id = id;
        this.imageRes = imageRes;
    }

    public Promotion(int imageRes) {
        this(null, imageRes);
    }

    public String getId() {
        return id;
    }

    public int getImageRes() {
        return imageRes;
    }
}