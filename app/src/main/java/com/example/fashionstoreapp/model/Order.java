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
    private double subtotal;
    private double shippingFee;
    private double voucherDiscount;
    private String status; // "pending", "processing", "shipping", "delivered", "cancelled"
    private Long createdAt; // Stored as timestamp (milliseconds)
    private String shippingAddress;
    private String recipientName;
    private String phoneNumber;
    private String paymentMethod;

    public Order() {
        // Required empty constructor for Firestore
    }

    public Order(String orderId, String userId, List<OrderItem> items, double total, double subtotal,
            double shippingFee, double voucherDiscount, String status, Long createdAt,
            String shippingAddress, String recipientName, String phoneNumber, String paymentMethod) {
        this.orderId = orderId;
        this.userId = userId;
        this.items = items;
        this.total = total;
        this.subtotal = subtotal;
        this.shippingFee = shippingFee;
        this.voucherDiscount = voucherDiscount;
        this.status = status;
        this.createdAt = createdAt;
        this.shippingAddress = shippingAddress;
        this.recipientName = recipientName;
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

    public String getRecipientName() {
        return recipientName;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public double getShippingFee() {
        return shippingFee;
    }

    public double getVoucherDiscount() {
        return voucherDiscount;
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

    // Alias for Firestore auto-mapping (totalAmount -> total)
    public void setTotalAmount(double totalAmount) {
        this.total = totalAmount;
    }

    public double getTotalAmount() {
        return this.total;
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

    public void setRecipientName(String recipientName) {
        this.recipientName = recipientName;
    }

    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }

    public void setShippingFee(double shippingFee) {
        this.shippingFee = shippingFee;
    }

    public void setVoucherDiscount(double voucherDiscount) {
        this.voucherDiscount = voucherDiscount;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    // Helper methods
    public String getStatusText() {
        if (status == null)
            return "Không xác định";
        switch (status) {
            case "pending":
                return "Chờ xác nhận";
            case "processing":
                return "Đang chuẩn bị";
            case "shipping":
                return "Đang giao";
            case "delivered":
                return "Đã giao";
            case "cancelled":
                return "Đã hủy";
            default:
                return status;
        }
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
            case "pending":
                return Color.parseColor("#FFA726"); // Orange - Chờ xác nhận
            case "processing":
                return Color.parseColor("#42A5F5"); // Light Blue - Đang chuẩn bị
            case "shipping":
                return Color.parseColor("#2196F3"); // Blue - Đang giao
            case "delivered":
                return Color.parseColor("#4CAF50"); // Green - Đã giao
            case "cancelled":
                return Color.parseColor("#F44336"); // Red - Đã hủy
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

        // Firestore compatibility - productImage maps to imageUrl
        public void setProductImage(String productImage) {
            this.imageUrl = productImage;
        }

        public String getProductImage() {
            return this.imageUrl;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }

        public void setPrice(double price) {
            this.price = price;
        }

        // Firestore compatibility - productPrice maps to price
        public void setProductPrice(double productPrice) {
            this.price = productPrice;
        }

        public double getProductPrice() {
            return this.price;
        }

        public void setSize(String size) {
            this.size = size;
        }

        public void setColor(String color) {
            this.color = color;
        }

        // Firestore compatibility - totalPrice field (ignored, calculated from price *
        // quantity)
        public void setTotalPrice(double totalPrice) {
            // Ignore this field, we calculate from price * quantity
        }

        public double getTotalPrice() {
            return this.price * this.quantity;
        }
    }
}
