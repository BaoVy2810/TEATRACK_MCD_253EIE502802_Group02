package com.teatrack_mcd_253eie502802_group02.model;

public class BeverageCategory {
    private String name;
    private int imageRes;

    public BeverageCategory(String name, int imageRes) {
        this.name = name;
        this.imageRes = imageRes;
    }

    public String getName() {
        return name;
    }

    public int getImageRes() {
        return imageRes;
    }
}