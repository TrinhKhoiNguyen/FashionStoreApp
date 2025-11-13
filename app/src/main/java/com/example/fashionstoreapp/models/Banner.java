package com.example.fashionstoreapp.models;

public class Banner {
    private String id;
    private String title;
    private String subtitle;
    private String imageUrl; // URL từ internet
    private int imageResource; // Resource ID từ drawable
    private int backgroundColor;
    private String actionUrl;

    public Banner() {
    }

    // Constructor với URL
    public Banner(String id, String title, String subtitle, String imageUrl, int backgroundColor) {
        this.id = id;
        this.title = title;
        this.subtitle = subtitle;
        this.imageUrl = imageUrl;
        this.imageResource = 0;
        this.backgroundColor = backgroundColor;
    }

    // Constructor với drawable resource
    public Banner(String id, String title, String subtitle, int imageResource, int backgroundColor) {
        this.id = id;
        this.title = title;
        this.subtitle = subtitle;
        this.imageUrl = null;
        this.imageResource = imageResource;
        this.backgroundColor = backgroundColor;
    }

    // Overloaded constructor: accept both imageUrl and imageResource (imageUrl may
    // be empty)
    public Banner(String id, String title, String subtitle, String imageUrl, int imageResource, int backgroundColor) {
        this.id = id;
        this.title = title;
        this.subtitle = subtitle;
        this.imageUrl = (imageUrl != null && !imageUrl.isEmpty()) ? imageUrl : null;
        this.imageResource = imageResource;
        this.backgroundColor = backgroundColor;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public int getImageResource() {
        return imageResource;
    }

    public void setImageResource(int imageResource) {
        this.imageResource = imageResource;
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public String getActionUrl() {
        return actionUrl;
    }

    public void setActionUrl(String actionUrl) {
        this.actionUrl = actionUrl;
    }
}
