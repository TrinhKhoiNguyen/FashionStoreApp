package com.example.fashionstoreapp.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
    private List<String> availableSizes;
    private double rating;
    private int reviewCount;

    // New fields for enhanced admin management
    private List<SizeStock> sizeStocks; // Stock management by size
    private List<String> colors; // Available colors
    private boolean isVisible = true; // Product visibility (shown/hidden)
    private int lowStockThreshold = 10; // Alert threshold for low stock
    private int totalSold = 0; // Track total units sold

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
        this.availableSizes = Arrays.asList("S", "M", "L", "XL");
        this.rating = 0.0;
        this.reviewCount = 0;
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

    public List<String> getAvailableSizes() {
        if (availableSizes == null) {
            availableSizes = Arrays.asList("S", "M", "L", "XL");
        }
        return availableSizes;
    }

    public void setAvailableSizes(List<String> availableSizes) {
        this.availableSizes = availableSizes;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public int getReviewCount() {
        return reviewCount;
    }

    public void setReviewCount(int reviewCount) {
        this.reviewCount = reviewCount;
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

    // Alias for category name (for admin compatibility)
    public String getCategoryName() {
        return category;
    }

    // Alias for product ID (for admin compatibility)
    public String getProductId() {
        return id;
    }

    // ==================== NEW GETTERS/SETTERS FOR ENHANCED ADMIN
    // ====================

    public List<SizeStock> getSizeStocks() {
        // Initialize with default sizes if null
        if (sizeStocks == null || sizeStocks.isEmpty()) {
            sizeStocks = new ArrayList<>();
            // Create default size stocks based on availableSizes or standard sizes
            List<String> sizes = availableSizes != null ? availableSizes : Arrays.asList("S", "M", "L", "XL");
            int stockPerSize = stockQuantity > 0 ? stockQuantity / sizes.size() : 0;
            for (String size : sizes) {
                sizeStocks.add(new SizeStock(size, stockPerSize));
            }
        }
        return sizeStocks;
    }

    public void setSizeStocks(List<SizeStock> sizeStocks) {
        this.sizeStocks = sizeStocks;
    }

    public List<String> getColors() {
        return colors;
    }

    public void setColors(Object colors) {
        // Handle both String (old format) and List<String> (new format)
        if (colors instanceof String) {
            this.colors = new ArrayList<>();
            if (!((String) colors).isEmpty()) {
                this.colors.add((String) colors);
            }
        } else if (colors instanceof List) {
            this.colors = (List<String>) colors;
        } else if (colors == null) {
            this.colors = new ArrayList<>();
        }
    }

    public boolean isVisible() {
        return isVisible;
    }

    public void setVisible(boolean visible) {
        isVisible = visible;
    }

    public int getLowStockThreshold() {
        return lowStockThreshold;
    }

    public void setLowStockThreshold(int lowStockThreshold) {
        this.lowStockThreshold = lowStockThreshold;
    }

    public int getTotalSold() {
        return totalSold;
    }

    public void setTotalSold(int totalSold) {
        this.totalSold = totalSold;
    }

    // ==================== UTILITY METHODS ====================

    /**
     * Get total stock across all sizes
     */
    public int getTotalStock() {
        if (sizeStocks == null || sizeStocks.isEmpty()) {
            return stockQuantity; // Fallback to legacy stock
        }
        int total = 0;
        for (SizeStock sizeStock : sizeStocks) {
            total += sizeStock.getStock();
        }
        return total;
    }

    /**
     * Check if product is low on stock
     */
    public boolean isLowStock() {
        return getTotalStock() <= lowStockThreshold && getTotalStock() > 0;
    }

    /**
     * Check if product is out of stock
     */
    public boolean isOutOfStock() {
        return getTotalStock() == 0;
    }

    /**
     * Get stock for specific size
     */
    public int getStockForSize(String size) {
        if (sizeStocks == null)
            return 0;
        for (SizeStock sizeStock : sizeStocks) {
            if (sizeStock.getSize().equals(size)) {
                return sizeStock.getStock();
            }
        }
        return 0;
    }

    /**
     * Update stock for specific size
     */
    public void updateStockForSize(String size, int newStock) {
        if (sizeStocks == null) {
            sizeStocks = new ArrayList<>();
        }
        boolean found = false;
        for (SizeStock sizeStock : sizeStocks) {
            if (sizeStock.getSize().equals(size)) {
                sizeStock.setStock(newStock);
                found = true;
                break;
            }
        }
        if (!found) {
            sizeStocks.add(new SizeStock(size, newStock));
        }
    }

    /**
     * Get stock status text for admin
     */
    public String getStockStatusText() {
        int total = getTotalStock();
        if (total == 0)
            return "Hết hàng";
        if (total <= lowStockThreshold)
            return "Sắp hết (" + total + ")";
        return "Còn hàng (" + total + ")";
    }

    /**
     * Get stock status color for UI
     * 
     * @return 0=red (out), 1=orange (low), 2=green (ok)
     */
    public int getStockStatusColor() {
        int total = getTotalStock();
        if (total == 0)
            return 0; // Red
        if (total <= lowStockThreshold)
            return 1; // Orange
        return 2; // Green
    }
}
