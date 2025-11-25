package com.example.fashionstoreapp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.fashionstoreapp.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

/**
 * Admin Products Fragment with 2 tabs:
 * - Tab 1: Products Management
 * - Tab 2: Categories Management
 */
public class AdminProductsFragment extends Fragment {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_products, container, false);

        initViews(view);
        setupViewPager();

        return view;
    }

    private void initViews(View view) {
        tabLayout = view.findViewById(R.id.productTabLayout);
        viewPager = view.findViewById(R.id.productViewPager);
    }

    private void setupViewPager() {
        AdminProductsPagerAdapter pagerAdapter = new AdminProductsPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);

        // Connect TabLayout with ViewPager2
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("🛍️ Sản Phẩm");
                    break;
                case 1:
                    tab.setText("📂 Danh Mục");
                    break;
            }
        }).attach();
    }

    /**
     * ViewPager2 Adapter for Products and Categories tabs
     */
    private static class AdminProductsPagerAdapter extends androidx.viewpager2.adapter.FragmentStateAdapter {

        public AdminProductsPagerAdapter(@NonNull Fragment fragment) {
            super(fragment);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0:
                    return new ProductsTabFragment();
                case 1:
                    return new CategoriesTabFragment();
                default:
                    return new ProductsTabFragment();
            }
        }

        @Override
        public int getItemCount() {
            return 2; // 2 tabs
        }
    }
}
