package com.teatrack_mcd_253eie502802_group02.model;

import java.io.Serializable;

public class Promotion implements Serializable {
    private String id;
    private String code;
    private String title;
    private String description;
    private String category;
    private String expiry;
    private double minSubtotal;
    private double max;
    private boolean isActive;
    private String type;
    private double value;
    private int imageRes;
    private String createdAt;
    private String updatedAt;

    public Promotion() {}

    public Promotion(int imageRes) {
        this.imageRes = imageRes;
    }

    public Promotion(String id, int imageRes) {
        this.id = id;
        this.imageRes = imageRes;
    }

    public Promotion(String id, String code, String description, double minSubtotal, String type, double value) {
        this.id = id;
        this.code = code;
        this.description = description;
        this.minSubtotal = minSubtotal;
        this.type = type;
        this.value = value;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getTitle() { return title == null ? "" : title; }
    public void setTitle(String title) { this.title = title; }

    public String getCategory() { return category == null ? "" : category; }
    public void setCategory(String category) { this.category = category; }

    public String getExpiry() { return expiry == null ? "" : expiry; }
    public void setExpiry(String expiry) { this.expiry = expiry; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getMinSubtotal() { return minSubtotal; }
    public void setMinSubtotal(double minSubtotal) { this.minSubtotal = minSubtotal; }

    public double getMax() { return max; }
    public void setMax(double max) { this.max = max; }

    public boolean getIsActive() { return isActive; }
    public void setIsActive(boolean isActive) { this.isActive = isActive; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public double getValue() { return value; }
    public void setValue(double value) { this.value = value; }

    public int getImageRes() { return imageRes; }
    public void setImageRes(int imageRes) { this.imageRes = imageRes; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
}