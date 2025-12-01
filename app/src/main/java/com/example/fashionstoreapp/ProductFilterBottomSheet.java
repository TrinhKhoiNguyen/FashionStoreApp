package com.example.fashionstoreapp;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.fashionstoreapp.models.FilterCriteria;
import com.example.fashionstoreapp.models.Product;
import com.example.fashionstoreapp.models.SortOption;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.slider.RangeSlider;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Bottom Sheet Dialog for advanced product filtering and sorting
 * Features:
 * - RangeSlider for price selection with debouncing
 * - Multi-select ChipGroups for categories and sizes
 * - Stock availability checkbox
 * - Rating filter
 * - Live product count preview
 * - Sort options
 */
public class ProductFilterBottomSheet extends BottomSheetDialogFragment {

    // UI Components
    private RangeSlider priceRangeSlider;
    private TextView priceRangeTv;
    private ChipGroup categoryChipGroup;
    private ChipGroup sizeChipGroup;
    private ChipGroup sortChipGroup;
    private CheckBox inStockCb;
    private TextView resultCountTv;
    private Button applyBtn;
    private Button resetBtn;

    // Data
    private FilterCriteria currentCriteria;
    private List<Product> allProducts;
    private float maxProductPrice = 10000000f; // 10 million default

    // Callback
    private OnFilterAppliedListener listener;

    // Debounce handler for RangeSlider changes
    private final Handler debounceHandler = new Handler(Looper.getMainLooper());
    private Runnable debounceRunnable;
    private static final int DEBOUNCE_DELAY_MS = 500;

    public interface OnFilterAppliedListener {
        void onFilterApplied(FilterCriteria criteria);

        void onFilterReset();
    }

    public static ProductFilterBottomSheet newInstance(
            List<Product> allProducts,
            FilterCriteria currentCriteria) {
        ProductFilterBottomSheet fragment = new ProductFilterBottomSheet();
        fragment.allProducts = allProducts;
        fragment.currentCriteria = currentCriteria != null ? currentCriteria : new FilterCriteria();
        return fragment;
    }

    public void setOnFilterAppliedListener(OnFilterAppliedListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_product_filter, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        calculateMaxPrice();
        setupPriceSlider();
        setupChips();
        setupButtons();
        restoreCurrentFilters();
        updateResultCount();
    }

    private void initViews(View view) {
        priceRangeSlider = view.findViewById(R.id.priceRangeSlider);
        priceRangeTv = view.findViewById(R.id.priceRangeTv);
        categoryChipGroup = view.findViewById(R.id.categoryChipGroup);
        sizeChipGroup = view.findViewById(R.id.sizeChipGroup);
        sortChipGroup = view.findViewById(R.id.sortChipGroup);
        inStockCb = view.findViewById(R.id.inStockCb);
        resultCountTv = view.findViewById(R.id.resultCountTv);
        applyBtn = view.findViewById(R.id.applyFilterBtn);
        resetBtn = view.findViewById(R.id.resetFilterBtn);
    }

    /**
     * Calculate maximum price from all products for slider range
     */
    private void calculateMaxPrice() {
        if (allProducts == null || allProducts.isEmpty()) {
            maxProductPrice = 10000000f; // Default 10M
            return;
        }

        double max = 0;
        for (Product p : allProducts) {
            if (p.getCurrentPrice() > max) {
                max = p.getCurrentPrice();
            }
        }
        // Round up to nearest 100k
        maxProductPrice = (float) (Math.ceil(max / 100000) * 100000);
        if (maxProductPrice < 100000)
            maxProductPrice = 1000000f; // Min 1M
    }

    /**
     * Setup RangeSlider with debouncing for performance
     */
    private void setupPriceSlider() {
        priceRangeSlider.setValueFrom(0f);
        priceRangeSlider.setValueTo(maxProductPrice);
        priceRangeSlider.setStepSize(50000f); // 50k step

        // Set initial values
        float minPrice = currentCriteria.getMinPrice() != null ? currentCriteria.getMinPrice().floatValue() : 0f;
        float maxPrice = currentCriteria.getMaxPrice() != null ? currentCriteria.getMaxPrice().floatValue()
                : maxProductPrice;

        priceRangeSlider.setValues(minPrice, maxPrice);
        updatePriceRangeText(minPrice, maxPrice);

        // Debounced listener to avoid excessive filtering during drag
        priceRangeSlider.addOnChangeListener((slider, value, fromUser) -> {
            if (debounceRunnable != null) {
                debounceHandler.removeCallbacks(debounceRunnable);
            }

            List<Float> values = slider.getValues();
            float min = values.get(0);
            float max = values.get(1);
            updatePriceRangeText(min, max);

            debounceRunnable = () -> updateResultCount();
            debounceHandler.postDelayed(debounceRunnable, DEBOUNCE_DELAY_MS);
        });
    }

    private void updatePriceRangeText(float min, float max) {
        String minStr = String.format("%,.0f₫", min);
        String maxStr = String.format("%,.0f₫", max);
        priceRangeTv.setText(minStr + " - " + maxStr);
    }

    /**
     * Setup ChipGroups for categories, sizes, and sort options
     */
    private void setupChips() {
        // Populate size chips from unique product sizes
        Set<String> uniqueSizes = new LinkedHashSet<>();
        if (allProducts != null) {
            for (Product p : allProducts) {
                if (p.getAvailableSizes() != null) {
                    uniqueSizes.addAll(p.getAvailableSizes());
                }
            }
        }

        sizeChipGroup.removeAllViews();
        for (String size : uniqueSizes) {
            Chip chip = createFilterChip(size, size);
            sizeChipGroup.addView(chip);
        }

        // Category chips will be loaded from Firestore in the Activity
        // Sort chips
        sortChipGroup.setSingleSelection(true);
        for (SortOption option : SortOption.values()) {
            Chip chip = createSortChip(option);
            sortChipGroup.addView(chip);
        }

        // Add change listeners for live count update
        categoryChipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> updateResultCount());
        sizeChipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> updateResultCount());
        inStockCb.setOnCheckedChangeListener((buttonView, isChecked) -> updateResultCount());
    }

    private Chip createFilterChip(String text, String tag) {
        Chip chip = new Chip(requireContext());
        chip.setText(text);
        chip.setCheckable(true);
        chip.setTag(tag);
        chip.setId(View.generateViewId());
        return chip;
    }

    private Chip createSortChip(SortOption option) {
        Chip chip = new Chip(requireContext());
        chip.setText(option.getDisplayName());
        chip.setCheckable(true);
        chip.setTag(option);
        chip.setId(View.generateViewId());
        return chip;
    }

    /**
     * Setup Apply and Reset buttons
     */
    private void setupButtons() {
        applyBtn.setOnClickListener(v -> {
            if (listener != null) {
                FilterCriteria criteria = buildFilterCriteria();
                listener.onFilterApplied(criteria);
            }
            dismiss();
        });

        resetBtn.setOnClickListener(v -> {
            resetAllFilters();
            if (listener != null) {
                listener.onFilterReset();
            }
            dismiss();
        });
    }

    /**
     * Restore previously applied filters to UI
     */
    private void restoreCurrentFilters() {
        // Price range already set in setupPriceSlider()

        // In-stock checkbox
        inStockCb.setChecked(currentCriteria.isInStockOnly());

        // Selected sizes
        if (currentCriteria.getSizes() != null) {
            for (String size : currentCriteria.getSizes()) {
                for (int i = 0; i < sizeChipGroup.getChildCount(); i++) {
                    View child = sizeChipGroup.getChildAt(i);
                    if (child instanceof Chip) {
                        Chip chip = (Chip) child;
                        if (size.equals(chip.getTag())) {
                            chip.setChecked(true);
                        }
                    }
                }
            }
        }

        // Selected categories (will be set after Firestore load)

        // Sort option
        String sortBy = currentCriteria.getSortBy();
        for (int i = 0; i < sortChipGroup.getChildCount(); i++) {
            View child = sortChipGroup.getChildAt(i);
            if (child instanceof Chip) {
                Chip chip = (Chip) child;
                SortOption option = (SortOption) chip.getTag();
                if (option != null && option.name().equalsIgnoreCase(sortBy)) {
                    chip.setChecked(true);
                    break;
                }
            }
        }
    }

    /**
     * Build FilterCriteria from current UI state
     */
    private FilterCriteria buildFilterCriteria() {
        FilterCriteria criteria = new FilterCriteria();

        // Price range
        List<Float> priceValues = priceRangeSlider.getValues();
        double minPrice = priceValues.get(0);
        double maxPrice = priceValues.get(1);

        if (minPrice > 0)
            criteria.setMinPrice(minPrice);
        if (maxPrice < maxProductPrice)
            criteria.setMaxPrice(maxPrice);

        // Categories
        List<String> selectedCategories = new ArrayList<>();
        for (int i = 0; i < categoryChipGroup.getChildCount(); i++) {
            View child = categoryChipGroup.getChildAt(i);
            if (child instanceof Chip) {
                Chip chip = (Chip) child;
                if (chip.isChecked() && chip.getTag() != null) {
                    selectedCategories.add(String.valueOf(chip.getTag()));
                }
            }
        }
        criteria.setCategories(selectedCategories);

        // Sizes
        List<String> selectedSizes = new ArrayList<>();
        for (int i = 0; i < sizeChipGroup.getChildCount(); i++) {
            View child = sizeChipGroup.getChildAt(i);
            if (child instanceof Chip) {
                Chip chip = (Chip) child;
                if (chip.isChecked() && chip.getTag() != null) {
                    selectedSizes.add(String.valueOf(chip.getTag()));
                }
            }
        }
        criteria.setSizes(selectedSizes);

        // In-stock filter
        criteria.setInStockOnly(inStockCb.isChecked());

        // Sort option
        int selectedSortId = sortChipGroup.getCheckedChipId();
        if (selectedSortId != View.NO_ID) {
            Chip selectedChip = sortChipGroup.findViewById(selectedSortId);
            if (selectedChip != null && selectedChip.getTag() instanceof SortOption) {
                SortOption option = (SortOption) selectedChip.getTag();
                criteria.setSortBy(option.name());
            }
        }

        return criteria;
    }

    /**
     * Reset all filters to default state
     */
    private void resetAllFilters() {
        priceRangeSlider.setValues(0f, maxProductPrice);
        updatePriceRangeText(0f, maxProductPrice);

        // Uncheck all chips
        for (int i = 0; i < categoryChipGroup.getChildCount(); i++) {
            View child = categoryChipGroup.getChildAt(i);
            if (child instanceof Chip)
                ((Chip) child).setChecked(false);
        }
        for (int i = 0; i < sizeChipGroup.getChildCount(); i++) {
            View child = sizeChipGroup.getChildAt(i);
            if (child instanceof Chip)
                ((Chip) child).setChecked(false);
        }

        inStockCb.setChecked(false);

        // Reset sort to default
        for (int i = 0; i < sortChipGroup.getChildCount(); i++) {
            View child = sortChipGroup.getChildAt(i);
            if (child instanceof Chip) {
                Chip chip = (Chip) child;
                SortOption option = (SortOption) chip.getTag();
                if (option == SortOption.DEFAULT) {
                    chip.setChecked(true);
                    break;
                }
            }
        }

        updateResultCount();
    }

    /**
     * Update live product count based on current filter settings
     * This provides immediate feedback to users before applying filters
     */
    private void updateResultCount() {
        if (allProducts == null || allProducts.isEmpty()) {
            resultCountTv.setText("0 sản phẩm");
            return;
        }

        FilterCriteria tempCriteria = buildFilterCriteria();
        int count = 0;

        for (Product product : allProducts) {
            if (tempCriteria.matches(product)) {
                count++;
            }
        }

        resultCountTv.setText(count + " sản phẩm");
        applyBtn.setEnabled(count > 0);
    }

    /**
     * Public method to populate category chips from Firestore
     * Called from the Activity after loading categories
     */
    public void setCategoryChips(List<String> categoryIds, List<String> categoryNames) {
        if (categoryChipGroup == null)
            return;

        categoryChipGroup.removeAllViews();
        for (int i = 0; i < categoryIds.size(); i++) {
            String id = categoryIds.get(i);
            String name = categoryNames.get(i);
            Chip chip = createFilterChip(name, id);

            // Restore selection if previously selected
            if (currentCriteria.getCategories().contains(id)) {
                chip.setChecked(true);
            }

            categoryChipGroup.addView(chip);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (debounceRunnable != null) {
            debounceHandler.removeCallbacks(debounceRunnable);
        }
    }
}
