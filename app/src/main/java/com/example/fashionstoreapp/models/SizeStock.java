package com.example.fashionstoreapp.models;

import java.io.Serializable;

/**
 * Model class for managing stock by size
 */
public class SizeStock implements Serializable {
    private String size; // e.g., "S", "M", "L", "XL"
    private int stock; // Available stock for this size

    // Required empty constructor for Firestore
    public SizeStock() {
    }

    public SizeStock(String size, int stock) {
        this.size = size;
        this.stock = stock;
    }

    // Getters
    public String getSize() {
        return size;
    }

    public int getStock() {
        return stock;
    }

    // Setters
    public void setSize(String size) {
        this.size = size;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    // Utility methods
    public boolean isAvailable() {
        return stock > 0;
    }

    public boolean isLowStock(int threshold) {
        return stock > 0 && stock <= threshold;
    }

    @Override
    public String toString() {
        return "SizeStock{" +
                "size='" + size + '\'' +
                ", stock=" + stock +
                '}';
    }
}
