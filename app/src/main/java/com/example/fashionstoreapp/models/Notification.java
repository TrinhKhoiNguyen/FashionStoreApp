package com.example.fashionstoreapp.models;

import java.io.Serializable;
import java.util.Date;

public class Notification implements Serializable {
    private String id;
    private String userId; // User ID this notification belongs to (empty for all users)
    private String title;
    private String message;
    private String type; // "order", "promotion", "system", "product"
    private String imageUrl;
    private Date timestamp;
    private boolean isRead;
    private String actionUrl; // Deep link or action to perform when clicked
    private String orderId; // Optional: for order-related notifications
    private String productId; // Optional: for product-related notifications

    public Notification() {
        this.timestamp = new Date();
        this.isRead = false;
    }

    public Notification(String title, String message, String type) {
        this();
        this.title = title;
        this.message = message;
        this.type = type;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public String getActionUrl() {
        return actionUrl;
    }

    public void setActionUrl(String actionUrl) {
        this.actionUrl = actionUrl;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    /**
     * Get time ago string (e.g., "2 giờ trước")
     */
    public String getTimeAgo() {
        if (timestamp == null)
            return "";

        long diff = new Date().getTime() - timestamp.getTime();
        long seconds = diff / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (days > 0) {
            return days + " ngày trước";
        } else if (hours > 0) {
            return hours + " giờ trước";
        } else if (minutes > 0) {
            return minutes + " phút trước";
        } else {
            return "Vừa xong";
        }
    }
}
