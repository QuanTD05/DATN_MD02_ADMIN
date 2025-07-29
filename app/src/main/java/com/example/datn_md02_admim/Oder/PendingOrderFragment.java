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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
public class PendingOrderFragment extends Fragment {
    private RecyclerView recyclerOrders;
    private List<Order> orderList;
    private OrderAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pending_order, container, false);
        recyclerOrders = view.findViewById(R.id.recyclerPendingOrders);
        recyclerOrders.setLayoutManager(new LinearLayoutManager(getContext()));

        orderList = new ArrayList<>();
        adapter = new OrderAdapter(getContext(), orderList);
        recyclerOrders.setAdapter(adapter);

        loadOrders("pending");
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
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}
