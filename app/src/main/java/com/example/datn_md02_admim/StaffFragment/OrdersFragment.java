package com.example.datn_md02_admim.StaffFragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.datn_md02_admim.FragmentOder.CancelledOrdersFragment;
import com.example.datn_md02_admim.FragmentOder.DoneOrdersFragment;
import com.example.datn_md02_admim.FragmentOder.PendingOrdersFragment; // ✅ Thêm dòng này
import com.example.datn_md02_admim.R;

public class OrdersFragment extends Fragment {

    private LinearLayout tabPending, tabDone, tabCancelled;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_orders, container, false);

        tabPending = view.findViewById(R.id.tab_pending);
        tabDone = view.findViewById(R.id.tab_done);
        tabCancelled = view.findViewById(R.id.tab_cancelled);

        // Load mặc định Fragment đang xử lý
        loadFragment(new PendingOrdersFragment());

        tabPending.setOnClickListener(v -> loadFragment(new PendingOrdersFragment()));
        tabDone.setOnClickListener(v -> loadFragment(new DoneOrdersFragment()));
        tabCancelled.setOnClickListener(v -> loadFragment(new CancelledOrdersFragment()));

        return view;
    }

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.replace(R.id.recycler_orders, fragment); // recycler_orders acting as container
        transaction.commit();
    }
}
