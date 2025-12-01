package com.example.fashionstoreapp.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class FilterCriteria implements Serializable {
    private Double minPrice;
    private Double maxPrice;
    private List<String> categories;
    private List<String> sizes;
    private boolean inStockOnly;
    private String sortBy; // "name_asc", "name_desc", "price_asc", "price_desc", "newest"
    private float minRating = 0f; // Rating filter (0-5 stars)

    public FilterCriteria() {
        this.categories = new ArrayList<>();
        this.sizes = new ArrayList<>();
        this.inStockOnly = false;
        this.sortBy = "newest";
        this.minRating = 0f;
    }

    public Double getMinPrice() {
        return minPrice;
    }

    public void setMinPrice(Double minPrice) {
        this.minPrice = minPrice;
    }

    public Double getMaxPrice() {
        return maxPrice;
    }

    public void setMaxPrice(Double maxPrice) {
        this.maxPrice = maxPrice;
    }

    public List<String> getCategories() {
        return categories;
    }

    public void setCategories(List<String> categories) {
        this.categories = categories;
    }

    public List<String> getSizes() {
        return sizes;
    }

    public void setSizes(List<String> sizes) {
        this.sizes = sizes;
    }

    public boolean isInStockOnly() {
        return inStockOnly;
    }

    public void setInStockOnly(boolean inStockOnly) {
        this.inStockOnly = inStockOnly;
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public float getMinRating() {
        return minRating;
    }

    public void setMinRating(float minRating) {
        this.minRating = minRating;
    }

    /**
     * Đếm số filter đang được áp dụng (không tính sort)
     */
    public int getActiveFilterCount() {
        int count = 0;
        if (minPrice != null || maxPrice != null)
            count++;
        if (!categories.isEmpty())
            count++;
        if (!sizes.isEmpty())
            count++;
        if (inStockOnly)
            count++;
        if (minRating > 0f)
            count++;
        return count;
    }

    /**
     * Reset tất cả filter về mặc định
     */
    public void reset() {
        minPrice = null;
        maxPrice = null;
        categories.clear();
        sizes.clear();
        inStockOnly = false;
        sortBy = "newest";
        minRating = 0f;
    }

    /**
     * Kiểm tra xem có filter nào đang được áp dụng không
     */
    public boolean hasActiveFilters() {
        return getActiveFilterCount() > 0;
    }

    /**
     * Kiểm tra xem product có thỏa mãn tất cả filter criteria không
     * Dùng cho client-side filtering
     */
    public boolean matches(Product product) {
        // Price filter
        if (minPrice != null && product.getCurrentPrice() < minPrice) {
            return false;
        }
        if (maxPrice != null && product.getCurrentPrice() > maxPrice) {
            return false;
        }

        // Category filter
        if (!categories.isEmpty() && !categories.contains(product.getCategory())) {
            return false;
        }

        // Size filter (product must have at least one of the selected sizes)
        if (!sizes.isEmpty()) {
            boolean hasMatchingSize = false;
            if (product.getAvailableSizes() != null) {
                for (String size : sizes) {
                    if (product.getAvailableSizes().contains(size)) {
                        hasMatchingSize = true;
                        break;
                    }
                }
            }
            if (!hasMatchingSize) {
                return false;
            }
        }

        // Stock filter
        if (inStockOnly && product.getTotalStock() <= 0) {
            return false;
        }

        // Rating filter
        if (minRating > 0f && product.getAverageRating() < minRating) {
            return false;
        }

        return true;
    }
}
