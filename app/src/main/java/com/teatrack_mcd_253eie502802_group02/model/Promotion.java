package com.teatrack_mcd_253eie502802_group02.model;

import java.io.Serializable;

public class Promotion implements Serializable {
    private String id;
    private String code;
    private String description;
    private double minSubtotal;
    private String type; // "amount" or "percent"
    private double value;

    public Promotion() {
        // Required for Firebase
    }

    public Promotion(String id, String code, String description, double minSubtotal, String type, double value) {
        this.id = id;
        this.code = code;
        this.description = description;
        this.minSubtotal = minSubtotal;
        this.type = type;
        this.value = value;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getMinSubtotal() { return minSubtotal; }
    public void setMinSubtotal(double minSubtotal) { this.minSubtotal = minSubtotal; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public double getValue() { return value; }
    public void setValue(double value) { this.value = value; }
}
