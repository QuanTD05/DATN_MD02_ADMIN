package com.example.datn_md02_admim.StaffFragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment; // ✅ PHẢI là androidx.fragment.app.Fragment

import com.example.datn_md02_admim.R;

public class HomeFragment extends Fragment {

    public HomeFragment() {
        // Bắt buộc để Fragment hoạt động đúng
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home_staff, container, false);
    }
}
