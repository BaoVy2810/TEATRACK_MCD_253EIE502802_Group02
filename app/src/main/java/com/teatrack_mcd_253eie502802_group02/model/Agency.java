package com.teatrack_mcd_253eie502802_group02.model;

public class Agency {
    private String id;
    private String name;
    private String address;
    private String phone;
    private String image;
    private String createdAt;
    private String mapEmbed;
    private boolean visible;

    // Constructor rỗng bắt buộc cho Firebase
    public Agency() {
    }

    public Agency(String id, String name, String address, String phone, String image, String createdAt, String mapEmbed, boolean visible) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.image = image;
        this.createdAt = createdAt;
        this.mapEmbed = mapEmbed;
        this.visible = visible;
    }

    // Getter và Setter
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getMapEmbed() { return mapEmbed; }
    public void setMapEmbed(String mapEmbed) { this.mapEmbed = mapEmbed; }

    public boolean isVisible() { return visible; }
    public void setVisible(boolean visible) { this.visible = visible; }
}