package com.example.datn_md02_admim;



import android.os.Bundle;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.example.datn_md02_admim.Adapter.CartItemAdapter;
import com.example.datn_md02_admim.Model.CartItem;
import com.example.datn_md02_admim.Model.Order;

import java.util.List;

public class OrderDetailActivity extends AppCompatActivity {

    private TextView tvOrderId, tvReceiver, tvAddress, tvTotal;
    private RecyclerView recyclerItems;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);

        tvOrderId = findViewById(R.id.tvOrderIdDetail);
        tvReceiver = findViewById(R.id.tvReceiverDetail);
        tvAddress = findViewById(R.id.tvAddressDetail);
        tvTotal = findViewById(R.id.tvTotalDetail);
        recyclerItems = findViewById(R.id.recyclerOrderItems);

        Order order = (Order) getIntent().getSerializableExtra("order");
        if (order != null) {
            tvOrderId.setText("Mã đơn: #" + order.getOrderId());
            tvReceiver.setText("Người nhận: " + order.getReceiverName());
            tvAddress.setText("Địa chỉ: " + order.getReceiverAddress());
            tvTotal.setText("Tổng tiền: " + order.getTotalAmount() + " VND");

            List<CartItem> itemList = order.getItems();
            recyclerItems.setLayoutManager(new LinearLayoutManager(this));
            recyclerItems.setAdapter(new CartItemAdapter(this, itemList));
        }
    }
}
