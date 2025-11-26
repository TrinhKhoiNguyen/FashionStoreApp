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

    public FilterCriteria() {
        this.categories = new ArrayList<>();
        this.sizes = new ArrayList<>();
        this.inStockOnly = false;
        this.sortBy = "newest";
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
    }

    /**
     * Kiểm tra xem có filter nào đang được áp dụng không
     */
    public boolean hasActiveFilters() {
        return getActiveFilterCount() > 0;
    }
}
