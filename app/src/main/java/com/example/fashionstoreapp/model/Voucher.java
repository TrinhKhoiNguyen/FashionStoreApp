package com.example.fashionstoreapp.model;

import java.util.Date;

public class Voucher {
    private String voucherId;
    private String code;
    private String title;
    private String description;
    private double discountPercent;
    private double discountAmount;
    private double minOrderAmount;
    private Date expiryDate;
    private boolean isActive;

    public Voucher() {
        // Required empty constructor for Firestore
    }

    public Voucher(String voucherId, String code, String title, String description,
            double discountPercent, double discountAmount, double minOrderAmount,
            Date expiryDate, boolean isActive) {
        this.voucherId = voucherId;
        this.code = code;
        this.title = title;
        this.description = description;
        this.discountPercent = discountPercent;
        this.discountAmount = discountAmount;
        this.minOrderAmount = minOrderAmount;
        this.expiryDate = expiryDate;
        this.isActive = isActive;
    }

    // Getters
    public String getVoucherId() {
        return voucherId;
    }

    public String getCode() {
        return code;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public double getDiscountPercent() {
        return discountPercent;
    }

    public double getDiscountAmount() {
        return discountAmount;
    }

    public double getMinOrderAmount() {
        return minOrderAmount;
    }

    public Date getExpiryDate() {
        return expiryDate;
    }

    public boolean isActive() {
        return isActive;
    }

    // Setters
    public void setVoucherId(String voucherId) {
        this.voucherId = voucherId;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDiscountPercent(double discountPercent) {
        this.discountPercent = discountPercent;
    }

    public void setDiscountAmount(double discountAmount) {
        this.discountAmount = discountAmount;
    }

    public void setMinOrderAmount(double minOrderAmount) {
        this.minOrderAmount = minOrderAmount;
    }

    public void setExpiryDate(Date expiryDate) {
        this.expiryDate = expiryDate;
    }

    public void setActive(boolean active) {
        isActive = active;
    }
}
