package com.example.fashionstoreapp;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class OrdersActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager2 viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders);

        initViews();
        setupToolbar();
        setupTabs();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Đơn hàng");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupTabs() {
        viewPager.setAdapter(new OrdersPagerAdapter(this));

        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {
                    switch (position) {
                        case 0:
                            tab.setText("Tất cả");
                            break;
                        case 1:
                            tab.setText("Đang xử lý");
                            break;
                        case 2:
                            tab.setText("Đang giao");
                            break;
                        case 3:
                            tab.setText("Hoàn thành");
                            break;
                        case 4:
                            tab.setText("Đã hủy");
                            break;
                    }
                }).attach();
    }

    private static class OrdersPagerAdapter extends FragmentStateAdapter {
        public OrdersPagerAdapter(@NonNull AppCompatActivity activity) {
            super(activity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            String[] statuses = { "all", "Đang xử lý", "Đang giao", "Hoàn thành", "Đã hủy" };
            return OrdersFragment.newInstance(statuses[position]);
        }

        @Override
        public int getItemCount() {
            return 5;
        }
    }
}
