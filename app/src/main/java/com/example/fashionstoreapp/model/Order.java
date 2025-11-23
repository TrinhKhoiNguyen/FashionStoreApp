package com.example.fashionstoreapp.model;

import android.graphics.Color;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Order {
    private String orderId;
    private String userId;
    private List<OrderItem> items;
    private double total;
    private String status; // "Đang xử lý", "Đang giao", "Hoàn thành", "Đã hủy"
    private Long createdAt; // Stored as timestamp (milliseconds)
    private String shippingAddress;
    private String phoneNumber;
    private String paymentMethod;

    public Order() {
        // Required empty constructor for Firestore
    }

    public Order(String orderId, String userId, List<OrderItem> items, double total, String status, Long createdAt,
            String shippingAddress, String phoneNumber, String paymentMethod) {
        this.orderId = orderId;
        this.userId = userId;
        this.items = items;
        this.total = total;
        this.status = status;
        this.createdAt = createdAt;
        this.shippingAddress = shippingAddress;
        this.phoneNumber = phoneNumber;
        this.paymentMethod = paymentMethod;
    }

    // Getters
    public String getOrderId() {
        return orderId;
    }

    public String getUserId() {
        return userId;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public double getTotal() {
        return total;
    }

    public String getStatus() {
        return status;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public String getShippingAddress() {
        return shippingAddress;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    // Setters
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    // Helper methods
    public String getStatusText() {
        return status != null ? status : "Không xác định";
    }

    public String getFormattedCreatedDate() {
        if (createdAt == null)
            return "";
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", new Locale("vi", "VN"));
        return sdf.format(new Date(createdAt));
    }

    public int getTotalItems() {
        if (items == null)
            return 0;
        int total = 0;
        for (OrderItem item : items) {
            total += item.getQuantity();
        }
        return total;
    }

    public String getFormattedTotalAmount() {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        return formatter.format(total);
    }

    public int getStatusColor() {
        if (status == null)
            return Color.parseColor("#9E9E9E");
        switch (status) {
            case "Đang xử lý":
                return Color.parseColor("#FF9800");
            case "Đang giao":
                return Color.parseColor("#2196F3");
            case "Hoàn thành":
                return Color.parseColor("#4CAF50");
            case "Đã hủy":
                return Color.parseColor("#F44336");
            default:
                return Color.parseColor("#9E9E9E");
        }
    }

    public static class OrderItem {
        private String productId;
        private String productName;
        private String imageUrl;
        private int quantity;
        private double price;
        private String size;
        private String color;

        public OrderItem() {
            // Required empty constructor for Firestore
        }

        public OrderItem(String productId, String productName, String imageUrl, int quantity, double price, String size,
                String color) {
            this.productId = productId;
            this.productName = productName;
            this.imageUrl = imageUrl;
            this.quantity = quantity;
            this.price = price;
            this.size = size;
            this.color = color;
        }

        // Getters
        public String getProductId() {
            return productId;
        }

        public String getProductName() {
            return productName;
        }

        public String getImageUrl() {
            return imageUrl;
        }

        public int getQuantity() {
            return quantity;
        }

        public double getPrice() {
            return price;
        }

        public String getSize() {
            return size;
        }

        public String getColor() {
            return color;
        }

        // Setters
        public void setProductId(String productId) {
            this.productId = productId;
        }

        public void setProductName(String productName) {
            this.productName = productName;
        }

        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }

        public void setPrice(double price) {
            this.price = price;
        }

        public void setSize(String size) {
            this.size = size;
        }

        public void setColor(String color) {
            this.color = color;
        }
    }
}
