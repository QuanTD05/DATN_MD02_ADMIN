package com.example.datn_md02_admim.Oder;

import android.content.Context;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class CancelledOrderFragment extends Fragment {

    public interface OnCancelledCountChangeListener {
        void onCancelledCountChanged(int count);
    }

    private OnCancelledCountChangeListener countListener;
    private RecyclerView recyclerOrders;
    private List<Order> orderList;
    private OrderAdapter adapter;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnCancelledCountChangeListener) {
            countListener = (OnCancelledCountChangeListener) context;
        } else if (getParentFragment() instanceof OnCancelledCountChangeListener) {
            countListener = (OnCancelledCountChangeListener) getParentFragment();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cancelled_order, container, false);
        recyclerOrders = view.findViewById(R.id.recyclerCancelledOrders);
        recyclerOrders.setLayoutManager(new LinearLayoutManager(getContext()));

        orderList = new ArrayList<>();
        adapter = new OrderAdapter(getContext(), orderList, (orderId, oldStatus, newStatus) -> {
            loadOrders("ondelivery");
        });
        recyclerOrders.setAdapter(adapter);

        loadOrders("ondelivery");
        return view;
    }

    private void loadOrders(String status) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("orders");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                orderList.clear();
                for (DataSnapshot userSnap : snapshot.getChildren()) {
                    for (DataSnapshot orderSnap : userSnap.getChildren()) {
                        Order order = orderSnap.getValue(Order.class);
                        if (order != null && status.equals(order.getStatus())) {
                            order.setUserId(userSnap.getKey());
                            orderList.add(order);
                        }
                    }
                }
                orderList.sort((o1, o2) -> Long.compare(o2.getTimestamp(), o1.getTimestamp()));
                adapter.notifyDataSetChanged();
                if (countListener != null) {
                    countListener.onCancelledCountChanged(orderList.size());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}
