package com.teatrack_mcd_253eie502802_group02.model;

public class PointTransaction {
    private String createdAt;
    private String description;
    private long pointsChange;
    private String type; // "earn" or "redeem"

    public PointTransaction() {
        // Required for Firebase
    }

    public PointTransaction(String createdAt, String description, long pointsChange, String type) {
        this.createdAt = createdAt;
        this.description = description;
        this.pointsChange = pointsChange;
        this.type = type;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getPointsChange() {
        return pointsChange;
    }

    public void setPointsChange(long pointsChange) {
        this.pointsChange = pointsChange;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
