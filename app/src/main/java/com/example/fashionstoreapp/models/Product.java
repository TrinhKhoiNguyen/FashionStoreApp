package com.example.fashionstoreapp.models;

import java.io.Serializable;

public class Product implements Serializable {
    private String id;
    private String name;
    private String description;
    private double currentPrice;
    private double originalPrice;
    private int discountPercent;
    private String imageUrl;
    private String category;
    private boolean isNew;
    private boolean hasVoucher;
    private String voucherText;
    private boolean isFavorite;
    private int stockQuantity;

    // Constructor
    public Product() {
    }

    public Product(String id, String name, String description, double currentPrice,
            double originalPrice, String imageUrl, String category) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.currentPrice = currentPrice;
        this.originalPrice = originalPrice;
        this.imageUrl = imageUrl;
        this.category = category;
        this.discountPercent = calculateDiscountPercent();
        this.isFavorite = false;
        this.stockQuantity = 100;
    }

    // Calculate discount percentage
    private int calculateDiscountPercent() {
        if (originalPrice > 0 && currentPrice < originalPrice) {
            return (int) (((originalPrice - currentPrice) / originalPrice) * 100);
        }
        return 0;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(double currentPrice) {
        this.currentPrice = currentPrice;
        this.discountPercent = calculateDiscountPercent();
    }

    public double getOriginalPrice() {
        return originalPrice;
    }

    public void setOriginalPrice(double originalPrice) {
        this.originalPrice = originalPrice;
        this.discountPercent = calculateDiscountPercent();
    }

    public int getDiscountPercent() {
        return discountPercent;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public boolean isNew() {
        return isNew;
    }

    public void setNew(boolean isNew) {
        this.isNew = isNew;
    }

    public boolean isHasVoucher() {
        return hasVoucher;
    }

    public void setHasVoucher(boolean hasVoucher) {
        this.hasVoucher = hasVoucher;
    }

    public String getVoucherText() {
        return voucherText;
    }

    public void setVoucherText(String voucherText) {
        this.voucherText = voucherText;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(int stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    // Formatted price strings
    public String getFormattedCurrentPrice() {
        return String.format("%,.0f₫", currentPrice);
    }

    public String getFormattedOriginalPrice() {
        return String.format("%,.0f₫", originalPrice);
    }

    // Check if product has discount
    public boolean hasDiscount() {
        return discountPercent > 0;
    }
}
