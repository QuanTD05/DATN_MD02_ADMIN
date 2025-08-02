package com.example.datn_md02_admim.Oder;

import android.Manifest;
import android.os.Build;
import android.os.Bundle;
import android.view.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.datn_md02_admim.Adapter.OrderAdapter;
import com.example.datn_md02_admim.Helper.NotificationHelper;
import com.example.datn_md02_admim.Model.Order;
import com.example.datn_md02_admim.R;
import com.google.firebase.database.*;

import java.util.*;

public class PendingOrderFragment extends Fragment {
    private RecyclerView recyclerOrders;
    private List<Order> orderList;
    private OrderAdapter adapter;

    private final Set<String> knownOrderIds = new HashSet<>();

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

        requestNotificationPermission();
        loadOrders("pending");
        return view;
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                    requireActivity(),
                    new String[]{Manifest.permission.POST_NOTIFICATIONS},
                    101
            );
        }
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

                            // Gửi thông báo nếu đơn hàng mới
                            if (!knownOrderIds.contains(order.getOrderId())) {
                                knownOrderIds.add(order.getOrderId());
                                NotificationHelper.showOrderNotification(
                                        requireContext(),
                                        "Đơn hàng mới",
                                        "Khách hàng " + order.getReceiverName() + " vừa đặt hàng"
                                );
                            }
                        }
                    }
                }
                // 🔽 Sắp xếp đơn hàng mới nhất lên đầu
                orderList.sort((o1, o2) -> Long.compare(o2.getTimestamp(), o1.getTimestamp()));
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}
