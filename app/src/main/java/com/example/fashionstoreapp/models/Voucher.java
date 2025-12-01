package com.example.fashionstoreapp.models;

import com.google.firebase.firestore.Exclude;

public class Voucher {
    private String id;
    private String code; // Mã voucher (uppercase, unique)
    private String type; // "fixed" hoặc "percent"
    private double amount; // Giá trị giảm (số tiền hoặc %)
    private double maxDiscount; // Giảm tối đa (cho type=percent)
    private double minOrder; // Đơn hàng tối thiểu
    private int quantity; // Số lượng voucher
    private int usedCount; // Số lượng đã sử dụng
    private long startAt; // Thời gian bắt đầu (epoch ms)
    private long endAt; // Thời gian kết thúc (epoch ms)
    private boolean active; // Trạng thái active/inactive
    private long createdAt;
    private long updatedAt;
    private String description; // Mô tả voucher

    public Voucher() {
        // Required empty constructor for Firestore
    }

    @Exclude
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public double getMaxDiscount() {
        return maxDiscount;
    }

    public void setMaxDiscount(double maxDiscount) {
        this.maxDiscount = maxDiscount;
    }

    public double getMinOrder() {
        return minOrder;
    }

    public void setMinOrder(double minOrder) {
        this.minOrder = minOrder;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getUsedCount() {
        return usedCount;
    }

    public void setUsedCount(int usedCount) {
        this.usedCount = usedCount;
    }

    public long getStartAt() {
        return startAt;
    }

    public void setStartAt(long startAt) {
        this.startAt = startAt;
    }

    public long getEndAt() {
        return endAt;
    }

    public void setEndAt(long endAt) {
        this.endAt = endAt;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    // Helper methods
    @Exclude
    public boolean isValid() {
        long now = System.currentTimeMillis();
        return active && now >= startAt && now <= endAt && (quantity - usedCount) > 0;
    }

    @Exclude
    public boolean isExpired() {
        return System.currentTimeMillis() > endAt;
    }

    @Exclude
    public int getRemainingQuantity() {
        return Math.max(0, quantity - usedCount);
    }

    @Exclude
    public String getTypeDisplayName() {
        if ("percent".equals(type)) {
            return "Giảm " + (int) amount + "%";
        } else if ("fixed".equals(type)) {
            return "Giảm " + String.format("%,.0f₫", amount);
        }
        return type;
    }

    @Exclude
    public String getStatusText() {
        if (!active)
            return "Inactive";
        if (isExpired())
            return "Hết hạn";
        if (getRemainingQuantity() <= 0)
            return "Hết lượt";
        return "Active";
    }
}
