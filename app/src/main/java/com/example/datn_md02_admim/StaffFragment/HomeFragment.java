package com.example.datn_md02_admim.StaffFragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.example.datn_md02_admim.Oder.CancelledOrderFragment;
import com.example.datn_md02_admim.Oder.CompletedOrderFragmen;
import com.example.datn_md02_admim.Oder.PendingOrderFragment;
import com.example.datn_md02_admim.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class HomeFragment extends Fragment
        implements PendingOrderFragment.OnOrderCountChangeListener,
        CancelledOrderFragment.OnCancelledCountChangeListener,
        CompletedOrderFragmen.OnCompletedCountChangeListener {

    private TabLayout tabLayout;
    private ViewPager2 viewPager2;
    private int pendingCount = 0;
    private int cancelledCount = 0;
    private int completedCount = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home_staff, container, false);
        tabLayout = view.findViewById(R.id.tabLayout);
        viewPager2 = view.findViewById(R.id.viewPager2);

        viewPager2.setAdapter(new OrderPagerAdapter(requireActivity(), this));

        new TabLayoutMediator(tabLayout, viewPager2,
                (tab, position) -> {
                    if (position == 0) tab.setText(getPendingTabTitle());
                    else if (position == 1) tab.setText(getCancelledTabTitle());
                    else if (position == 2) tab.setText(getCompletedTabTitle());
                }).attach();

        return view;
    }

    private String getPendingTabTitle() {
        return pendingCount > 0 ? "Đơn chờ xác nhận (" + pendingCount + ")" : "Đơn chờ xác nhận";
    }

    private String getCancelledTabTitle() {
        return cancelledCount > 0 ? "Đơn đang giao (" + cancelledCount + ")" : "Đơn đang giao";
    }

    private String getCompletedTabTitle() {
        return completedCount > 0 ? "Đơn đã hoàn thành (" + completedCount + ")" : "Đơn đã hoàn thành";
    }

    @Override
    public void onOrderCountChanged(int count) {
        pendingCount = count;
        TabLayout.Tab tab = tabLayout.getTabAt(0);
        if (tab != null) tab.setText(getPendingTabTitle());
    }

    @Override
    public void onCancelledCountChanged(int count) {
        cancelledCount = count;
        TabLayout.Tab tab = tabLayout.getTabAt(1);
        if (tab != null) tab.setText(getCancelledTabTitle());
    }

    @Override
    public void onCompletedCountChanged(int count) {
        completedCount = count;
        TabLayout.Tab tab = tabLayout.getTabAt(2);
        if (tab != null) tab.setText(getCompletedTabTitle());
    }

    private static class OrderPagerAdapter extends FragmentStateAdapter {
        private final HomeFragment parent;

        public OrderPagerAdapter(@NonNull FragmentActivity fragmentActivity, HomeFragment parent) {
            super(fragmentActivity);
            this.parent = parent;
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            if (position == 0) {
                return new PendingOrderFragment();
            } else if (position == 1) {
                return new CancelledOrderFragment();
            } else {
                return new CompletedOrderFragmen();
            }
        }

        @Override
        public int getItemCount() {
            return 3;
        }
    }
}
