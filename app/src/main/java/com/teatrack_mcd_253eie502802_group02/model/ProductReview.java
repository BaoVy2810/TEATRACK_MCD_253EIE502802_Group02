package com.teatrack_mcd_253eie502802_group02.model;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class ProductReview {

    private String id;
    private String productId;
    private String userId;
    private String userName;
    private float rating;
    private String title;
    private String comment;
    private String createdAt;

    public ProductReview() {
    }

    public ProductReview(String id, String productId, String userId, String userName,
                         float rating, String title, String comment, String createdAt) {
        this.id = id;
        this.productId = productId;
        this.userId = userId;
        this.userName = userName;
        this.rating = rating;
        this.title = title;
        this.comment = comment;
        this.createdAt = createdAt;
    }

    @Exclude
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public float getRating() { return rating; }
    public void setRating(float rating) { this.rating = rating; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
