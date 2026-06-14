package com.teatrack_mcd_253eie502802_group02.model;

public class NewsItem {
    private String title;
    private String dateRange;
    private int imageRes;

    public NewsItem(String title, String dateRange, int imageRes) {
        this.title = title;
        this.dateRange = dateRange;
        this.imageRes = imageRes;
    }

    public String getTitle() { return title; }
    public String getDateRange() { return dateRange; }
    public int getImageRes() { return imageRes; }
}