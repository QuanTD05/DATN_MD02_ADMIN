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
import androidx.appcompat.app.AlertDialog;
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

    public interface OrderActionListener {
        void onOrderStatusChanged(String orderId, String oldStatus, String newStatus);
    }

    private final Context context;
    private final List<Order> orderList;
    private final OrderActionListener actionListener;

    public OrderAdapter(Context context, List<Order> orderList, OrderActionListener listener) {
        this.context = context;
        this.orderList = orderList;
        this.actionListener = listener;
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
        if (order == null) return;

        holder.tvOrderId.setText("Mã đơn: #" + order.getOrderId());
        holder.tvReceiver.setText("Người nhận: " + order.getReceiverName());
        holder.tvAddress.setText("Địa chỉ: " + order.getReceiverAddress());
        holder.tvTotal.setText("Tổng tiền: " + order.getTotalAmount() + " VND");

        holder.btnViewDetail.setOnClickListener(v -> {
            Intent intent = new Intent(context, OrderDetailActivity.class);
            intent.putExtra("order", order);
            context.startActivity(intent);
        });

        String status = order.getStatus() != null ? order.getStatus().toLowerCase() : "";

        holder.btnCancel.setVisibility(View.GONE);

        switch (status) {
            case "pending":
                holder.btnAction.setText("Xác nhận");
                holder.btnAction.setEnabled(true);
                holder.btnAction.setOnClickListener(v ->
                        new AlertDialog.Builder(context)
                                .setTitle("Xác nhận đơn")
                                .setMessage("Chuyển đơn sang trạng thái Đang giao?")
                                .setPositiveButton("Xác nhận", (d, w) ->
                                        updateOrderStatus(order, "ondelivery"))
                                .setNegativeButton("Huỷ", null)
                                .show()
                );
                holder.btnCancel.setVisibility(View.VISIBLE);
                holder.btnCancel.setText("Hủy đơn");
                holder.btnCancel.setOnClickListener(v ->
                        new AlertDialog.Builder(context)
                                .setTitle("Hủy đơn")
                                .setMessage("Bạn có chắc muốn hủy đơn này?")
                                .setPositiveButton("Hủy", (d, w) ->
                                        updateOrderStatus(order, "cancelled"))
                                .setNegativeButton("Không", null)
                                .show()
                );
                break;

            case "ondelivery":
                holder.btnAction.setText("Hoàn thành");
                holder.btnAction.setEnabled(true);
                holder.btnAction.setOnClickListener(v ->
                        new AlertDialog.Builder(context)
                                .setTitle("Hoàn thành đơn")
                                .setMessage("Xác nhận đơn đã giao xong?")
                                .setPositiveButton("Đồng ý", (d, w) ->
                                        updateOrderStatus(order, "completed"))
                                .setNegativeButton("Huỷ", null)
                                .show()
                );
                break;

            case "completed":
                holder.btnAction.setText("✔ Hoàn tất");
                holder.btnAction.setEnabled(false);
                break;

            case "cancelled":
                holder.btnAction.setText("Đã hủy");
                holder.btnAction.setEnabled(false);
                break;

            default:
                holder.btnAction.setText("Không rõ");
                holder.btnAction.setEnabled(false);
                break;
        }
    }

    private void updateOrderStatus(Order order, String newStatus) {
        String oldStatus = order.getStatus();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("orders")
                .child(order.getUserId()).child(order.getOrderId());

        Map<String, Object> updates = new HashMap<>();
        updates.put("status", newStatus);

        ref.updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    // Gửi thông báo cho user
                    sendOrderNotification(order, newStatus);

                    Toast.makeText(context, "Cập nhật trạng thái thành công", Toast.LENGTH_SHORT).show();
                    if (actionListener != null) {
                        actionListener.onOrderStatusChanged(order.getOrderId(), oldStatus, newStatus);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(context, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private void sendOrderNotification(Order order, String newStatus) {
        DatabaseReference notiRef = FirebaseDatabase.getInstance()
                .getReference("notifications")
                .child(order.getUserId())
                .push();

        String message;
        switch (newStatus) {
            case "ondelivery":
                message = "Đơn hàng #" + order.getOrderId() + " đã được xác nhận và đang giao.";
                break;
            case "completed":
                message = "Đơn hàng #" + order.getOrderId() + " đã giao thành công.";
                break;
            case "cancelled":
                message = "Đơn hàng #" + order.getOrderId() + " đã bị hủy.";
                break;
            default:
                message = "Đơn hàng #" + order.getOrderId() + " đã cập nhật trạng thái.";
        }

        Map<String, Object> notiData = new HashMap<>();
        notiData.put("title", "Thông báo đơn hàng");
        notiData.put("message", message);
        notiData.put("type", "order");
        notiData.put("timestamp", System.currentTimeMillis());

        notiRef.setValue(notiData);
    }

    @Override
    public int getItemCount() {
        return orderList != null ? orderList.size() : 0;
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderId, tvReceiver, tvAddress, tvTotal;
        Button btnAction, btnViewDetail, btnCancel;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            tvReceiver = itemView.findViewById(R.id.tvReceiver);
            tvAddress = itemView.findViewById(R.id.tvAddress);
            tvTotal = itemView.findViewById(R.id.tvTotal);
            btnAction = itemView.findViewById(R.id.btnAction);
            btnViewDetail = itemView.findViewById(R.id.btnViewDetail);
            btnCancel = itemView.findViewById(R.id.btnCancel);
        }
    }
}
