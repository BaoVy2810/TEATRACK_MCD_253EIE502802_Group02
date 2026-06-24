package com.teatrack_mcd_253eie502802_group02.model;

public class Order {
    private String id;
    private String title;
    private String meta;

    public Order() {
        // Required for Firebase
    }

    public Order(String id, String title, String meta) {
        this.id = id;
        this.title = title;
        this.meta = meta;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getMeta() { return meta; }

    public void setId(String id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setMeta(String meta) { this.meta = meta; }
}
