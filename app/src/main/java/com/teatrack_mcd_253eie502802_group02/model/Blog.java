package com.teatrack_mcd_253eie502802_group02.model;

import java.io.Serializable;

public class Blog implements Serializable {
    private String id;
    private String title;
    private String heading;
    private String headingColor;
    private String content;
    private String image;
    private String thumbnailImage;
    private String date;
    private String layoutType;
    private String category; // Giữ lại nếu cần phân loại
    private String status;   // Giữ lại để quản lý hiển thị

    public Blog() {
        // Required for Firebase
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getHeading() { return heading; }
    public void setHeading(String heading) { this.heading = heading; }

    public String getHeadingColor() { return headingColor; }
    public void setHeadingColor(String headingColor) { this.headingColor = headingColor; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    public String getThumbnailImage() { return thumbnailImage; }
    public void setThumbnailImage(String thumbnailImage) { this.thumbnailImage = thumbnailImage; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getLayoutType() { return layoutType; }
    public void setLayoutType(String layoutType) { this.layoutType = layoutType; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
