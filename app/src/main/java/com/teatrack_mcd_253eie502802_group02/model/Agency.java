package com.teatrack_mcd_253eie502802_group02.model;

public class Agency {

    private String id;
    private String name;
    private String address;
    private String phone;
    private String image;
    private String mapEmbed;
    private String status;

    public Agency(String id,
                  String name,
                  String address,
                  String phone,
                  String image,
                  String mapEmbed,
                  String status) {

        this.id = id;
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.image = image;
        this.mapEmbed = mapEmbed;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public String getPhone() {
        return phone;
    }

    public String getImage() {
        return image;
    }

    public String getMapEmbed() {
        return mapEmbed;
    }

    public String getStatus() {
        return status;
    }
}