package com.example.datn_md02_admim.StaffFragment;


import android.os.Bundle;
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

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class HomeFragment extends Fragment {

    private TabLayout tabLayout;
    private ViewPager2 viewPager2;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home_staff, container, false);
        tabLayout = view.findViewById(R.id.tabLayout);
        viewPager2 = view.findViewById(R.id.viewPager2);

        // Set adapter cho ViewPager
        viewPager2.setAdapter(new OrderPagerAdapter(requireActivity()));

        // Kết nối TabLayout với ViewPager2
        new TabLayoutMediator(tabLayout, viewPager2,
                (tab, position) -> {
                    switch (position) {
                        case 0:
                            tab.setText("Đang xử lý");
                            break;
                        case 1:
                            tab.setText("Đã hoàn thành");
                            break;
                        case 2:
                            tab.setText("Đã huỷ");
                            break;
                    }
                }
        ).attach();

        return view;
    }

    private static class OrderPagerAdapter extends FragmentStateAdapter {
        public OrderPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0:
                    return new PendingOrderFragment();
                case 1:
                    return new CompletedOrderFragmen();
                case 2:
                    return new CancelledOrderFragment();
                default:
                    return new PendingOrderFragment();
            }
        }

        @Override
        public int getItemCount() {
            return 3; // 3 tab
        }
    }
}
