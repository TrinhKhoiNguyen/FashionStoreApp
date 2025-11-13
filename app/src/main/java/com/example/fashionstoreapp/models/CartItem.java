package com.example.fashionstoreapp.models;

import java.io.Serializable;

public class CartItem implements Serializable {
    private String id;
    private Product product;
    private int quantity;
    private String size;
    private String color;
    private boolean isSelected;

    // Constructor
    public CartItem() {
    }

    public CartItem(Product product, int quantity) {
        this.id = System.currentTimeMillis() + "_" + product.getId();
        this.product = product;
        this.quantity = quantity;
        this.isSelected = true;
        this.size = "M"; // Default size
        this.color = "Default";
    }

    public CartItem(Product product, int quantity, String size, String color) {
        this.id = System.currentTimeMillis() + "_" + product.getId();
        this.product = product;
        this.quantity = quantity;
        this.size = size;
        this.color = color;
        this.isSelected = true;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    // Calculate total price for this item
    public double getTotalPrice() {
        return product.getCurrentPrice() * quantity;
    }

    // Formatted total price
    public String getFormattedTotalPrice() {
        return String.format("%,.0f₫", getTotalPrice());
    }

    // Increase quantity
    public void increaseQuantity() {
        if (quantity < product.getStockQuantity()) {
            quantity++;
        }
    }

    // Decrease quantity
    public void decreaseQuantity() {
        if (quantity > 1) {
            quantity--;
        }
    }

    // Check if can increase quantity
    public boolean canIncreaseQuantity() {
        return quantity < product.getStockQuantity();
    }

    // Check if can decrease quantity
    public boolean canDecreaseQuantity() {
        return quantity > 1;
    }
}
