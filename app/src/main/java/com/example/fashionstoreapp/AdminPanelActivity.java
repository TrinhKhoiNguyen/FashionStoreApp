package com.example.fashionstoreapp;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.example.fashionstoreapp.fragments.AdminDashboardFragment;
import com.example.fashionstoreapp.fragments.AdminProductsFragment;
import com.example.fashionstoreapp.fragments.AdminOrdersFragment;
import com.example.fashionstoreapp.fragments.AdminUsersFragment;
import com.example.fashionstoreapp.fragments.AdminVouchersFragment;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class AdminPanelActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private AdminPagerAdapter pagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_panel);

        initViews();
        setupToolbar();
        setupViewPager();
    }

    private void initViews() {
        toolbar = findViewById(R.id.adminToolbar);
        tabLayout = findViewById(R.id.adminTabLayout);
        viewPager = findViewById(R.id.adminViewPager);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Admin Panel");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupViewPager() {
        pagerAdapter = new AdminPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);

        // Connect TabLayout with ViewPager2
        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {
                    switch (position) {
                        case 0:
                            tab.setText("Thống kê");
                            break;
                        case 1:
                            tab.setText("Sản phẩm");
                            break;
                        case 2:
                            tab.setText("Đơn hàng");
                            break;
                        case 3:
                            tab.setText("Vouchers");
                            break;
                        case 4:
                            tab.setText("Người dùng");
                            break;
                    }
                }).attach();
    }

    // ViewPager2 Adapter
    private static class AdminPagerAdapter extends FragmentStateAdapter {

        public AdminPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0:
                    return new AdminDashboardFragment();
                case 1:
                    return new AdminProductsFragment();
                case 2:
                    return new AdminOrdersFragment();
                case 3:
                    return new AdminVouchersFragment();
                case 4:
                    return new AdminUsersFragment();
                default:
                    return new AdminDashboardFragment();
            }
        }

        @Override
        public int getItemCount() {
            return 5;
        }
    }
}
