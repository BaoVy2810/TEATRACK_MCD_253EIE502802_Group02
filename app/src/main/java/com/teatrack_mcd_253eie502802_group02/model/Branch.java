package com.teatrack_mcd_253eie502802_group02.model;

import com.google.firebase.database.Exclude;

public class Branch {
    private String id;
    private String address;
    private int numOrders;
    private String revenue;

    public Branch() {
        // Required for Firebase
    }

    public Branch(String address, int numOrders, String revenue) {
        this.address = address;
        this.numOrders = numOrders;
        this.revenue = revenue;
    }

    @Exclude
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public int getNumOrders() { return numOrders; }
    public void setNumOrders(int numOrders) { this.numOrders = numOrders; }

    public String getRevenue() { return revenue; }
    public void setRevenue(String revenue) { this.revenue = revenue; }
}