
// === OrderAdapter.java ===
package com.example.datn_md02_admim.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.example.datn_md02_admim.Model.Order;
import com.example.datn_md02_admim.OrderDetailActivity;
import com.example.datn_md02_admim.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {
    private Context context;
    private List<Order> orderList;

    public OrderAdapter(Context context, List<Order> orderList) {
        this.context = context;
        this.orderList = orderList;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orderList.get(position);
        holder.tvOrderId.setText("Mã đơn: #" + order.getOrderId());
        holder.tvReceiver.setText("Người nhận: " + order.getReceiverName());
        holder.tvAddress.setText("Địa chỉ: " + order.getReceiverAddress());
        holder.tvTotal.setText("Tổng tiền: " + order.getTotalAmount() + " VND");
        holder.btnViewDetail.setOnClickListener(v -> {
            Intent intent = new Intent(context, OrderDetailActivity.class);
            intent.putExtra("order", order); // Order implements Serializable
            context.startActivity(intent);
        });

        switch (order.getStatus()) {
            case "pending":
                holder.btnAction.setText("Xác nhận");
                holder.btnAction.setOnClickListener(v ->
                        updateOrderStatus(order.getUserId(), order.getOrderId(), "ondelivery"));
                break;
            case "ondelivery":
                holder.btnAction.setText("Hoàn thành");
                holder.btnAction.setOnClickListener(v ->
                        updateOrderStatus(order.getUserId(), order.getOrderId(), "completed"));
                break;
            case "completed":
                holder.btnAction.setText("✔ Hoàn tất");
                holder.btnAction.setEnabled(false);
                break;
        }
    }

    private void updateOrderStatus(String userId, String orderId, String newStatus) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("orders")
                .child(userId).child(orderId);
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", newStatus);
        ref.updateChildren(updates)
                .addOnSuccessListener(aVoid -> Toast.makeText(context, "Cập nhật trạng thái thành công", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(context, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderId, tvReceiver, tvAddress, tvTotal;
        Button btnAction,btnViewDetail;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            tvReceiver = itemView.findViewById(R.id.tvReceiver);
            tvAddress = itemView.findViewById(R.id.tvAddress);
            tvTotal = itemView.findViewById(R.id.tvTotal);
            btnAction = itemView.findViewById(R.id.btnAction);
            btnViewDetail = itemView.findViewById(R.id.btnViewDetail);
        }
    }
}