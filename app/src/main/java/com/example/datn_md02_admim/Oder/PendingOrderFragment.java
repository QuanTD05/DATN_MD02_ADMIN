package com.example.datn_md02_admim.Oder;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.datn_md02_admim.Adapter.OrderAdapter;
import com.example.datn_md02_admim.Model.Order;
import com.example.datn_md02_admim.R;

import java.util.ArrayList;
import java.util.List;

public class PendingOrderFragment extends Fragment {
    private RecyclerView recyclerView;
    private OrderAdapter adapter;
    private List<Order> orderList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pending_order, container, false);
        recyclerView = view.findViewById(R.id.recyclerPendingOrders);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Sample data
        orderList = new ArrayList<>();
        orderList.add(new Order("Nguyen Van Quyet", "06/11/2024 11:19", "$22.58", "Đang xử lý"));

        adapter = new OrderAdapter(orderList, getContext());
        recyclerView.setAdapter(adapter);

        return view;
    }
}