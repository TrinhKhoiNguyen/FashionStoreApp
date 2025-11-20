package com.example.fashionstoreapp.models;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Order implements Serializable {
    private String orderId;
    private String userId;
    private List<CartItem> items;
    private double totalAmount;
    private String status; // pending, processing, shipping, completed, cancelled
    private String paymentMethod; // cod, bank_transfer, momo
    private String recipientName;
    private String recipientPhone;
    private String shippingAddress;
    private String note;
    private long createdAt;
    private long updatedAt;

    // Constructor
    public Order() {
    }

    public Order(String userId, List<CartItem> items, double totalAmount,
            String paymentMethod, String recipientName, String recipientPhone,
            String shippingAddress, String note) {
        this.orderId = "ORD" + System.currentTimeMillis();
        this.userId = userId;
        this.items = items;
        this.totalAmount = totalAmount;
        this.status = "pending";
        this.paymentMethod = paymentMethod;
        this.recipientName = recipientName;
        this.recipientPhone = recipientPhone;
        this.shippingAddress = shippingAddress;
        this.note = note;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public List<CartItem> getItems() {
        return items;
    }

    public void setItems(List<CartItem> items) {
        this.items = items;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
        this.updatedAt = System.currentTimeMillis();
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getRecipientName() {
        return recipientName;
    }

    public void setRecipientName(String recipientName) {
        this.recipientName = recipientName;
    }

    public String getRecipientPhone() {
        return recipientPhone;
    }

    public void setRecipientPhone(String recipientPhone) {
        this.recipientPhone = recipientPhone;
    }

    public String getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
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

    // Helper methods
    public String getFormattedTotalAmount() {
        return String.format("%,.0f₫", totalAmount);
    }

    public String getFormattedCreatedDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        return sdf.format(new Date(createdAt));
    }

    public String getStatusText() {
        switch (status) {
            case "pending":
                return "Chờ xác nhận";
            case "processing":
                return "Đang xử lý";
            case "shipping":
                return "Đang giao hàng";
            case "completed":
                return "Đã hoàn thành";
            case "cancelled":
                return "Đã hủy";
            default:
                return "Không xác định";
        }
    }

    public int getStatusColor() {
        switch (status) {
            case "pending":
                return 0xFFFFA000; // Orange
            case "processing":
                return 0xFF2196F3; // Blue
            case "shipping":
                return 0xFF9C27B0; // Purple
            case "completed":
                return 0xFF4CAF50; // Green
            case "cancelled":
                return 0xFFF44336; // Red
            default:
                return 0xFF757575; // Gray
        }
    }

    public String getPaymentMethodText() {
        switch (paymentMethod) {
            case "cod":
                return "Thanh toán khi nhận hàng (COD)";
            case "bank_transfer":
                return "Chuyển khoản ngân hàng";
            case "momo":
                return "Ví MoMo";
            default:
                return "Chưa xác định";
        }
    }

    public int getTotalItems() {
        if (items == null)
            return 0;
        int total = 0;
        for (CartItem item : items) {
            total += item.getQuantity();
        }
        return total;
    }
}
