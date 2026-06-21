package com.teatrack_mcd_253eie502802_group02.model;

import java.util.List;

public class Blog {
    private String id;
    private String title;
    private String heading;
    private String subheading;
    private String date;
    private String thumbnailImage;
    private String image;
    private List<String> images;
    private String headingColor;
    private String layoutType;
    private String content;
    private String description;

    public Blog() {}

    public Blog(String id, String title, String heading, String subheading,
                String date, String thumbnailImage, String image,
                List<String> images, String headingColor,
                String layoutType, String content, String description) {
        this.id = id;
        this.title = title;
        this.heading = heading;
        this.subheading = subheading;
        this.date = date;
        this.thumbnailImage = thumbnailImage;
        this.image = image;
        this.images = images;
        this.headingColor = headingColor;
        this.layoutType = layoutType;
        this.content = content;
        this.description = description;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getHeading() { return heading; }
    public String getSubheading() { return subheading; }
    public String getDate() { return date; }
    public String getThumbnailImage() { return thumbnailImage; }
    public String getImage() { return image; }
    public List<String> getImages() { return images; }
    public String getHeadingColor() { return headingColor; }
    public String getLayoutType() { return layoutType; }
    public String getContent() { return content; }
    public String getDescription() { return description; }

    public void setId(String id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setHeading(String heading) { this.heading = heading; }
    public void setSubheading(String subheading) { this.subheading = subheading; }
    public void setDate(String date) { this.date = date; }
    public void setThumbnailImage(String thumbnailImage) { this.thumbnailImage = thumbnailImage; }
    public void setImage(String image) { this.image = image; }
    public void setImages(List<String> images) { this.images = images; }
    public void setHeadingColor(String headingColor) { this.headingColor = headingColor; }
    public void setLayoutType(String layoutType) { this.layoutType = layoutType; }
    public void setContent(String content) { this.content = content; }
    public void setDescription(String description) { this.description = description; }

    public String getDisplayImage() {
        if (thumbnailImage != null && !thumbnailImage.isEmpty()) return thumbnailImage;
        if (image != null && !image.isEmpty()) return image;
        if (images != null && !images.isEmpty()) return images.get(0);
        return "";
    }
}