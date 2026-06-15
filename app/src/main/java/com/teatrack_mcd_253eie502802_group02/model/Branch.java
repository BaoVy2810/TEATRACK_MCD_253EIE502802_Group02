package com.teatrack_mcd_253eie502802_group02.model;

public class Branch {
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

    public String getAddress() { return address; }
    public int getNumOrders() { return numOrders; }
    public String getRevenue() { return revenue; }
}