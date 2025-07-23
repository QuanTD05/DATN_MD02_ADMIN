
// === OrderAdapter.java ===
package com.example.datn_md02_admim.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.datn_md02_admim.Model.Order;
import com.example.datn_md02_admim.R;
import java.util.List;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {
    private List<Order> orderList;
    private Context context;

    public OrderAdapter(List<Order> orderList, Context context) {
        this.orderList = orderList;
        this.context = context;
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
        holder.tvCustomer.setText(order.getCustomerName());
        holder.tvDate.setText(order.getOrderTime());
        holder.tvTotal.setText("Thành tiền: " + order.getTotalAmount());
        holder.tvStatus.setText("Trạng thái: " + order.getStatus());

        // Tuỳ chỉnh màu trạng thái
        switch (order.getStatus()) {
            case "Đã hoàn thành":
                holder.tvStatus.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));
                break;
            case "Đã huỷ":
                holder.tvStatus.setTextColor(context.getResources().getColor(R.color.green_dark));
                break;
            case "Đang xử lý":
                holder.tvStatus.setTextColor(context.getResources().getColor(R.color.pink));
                break;
        }
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvCustomer, tvDate, tvTotal, tvStatus;
        Button btnDetail;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCustomer = itemView.findViewById(R.id.tv_customer);
            tvDate = itemView.findViewById(R.id.tv_date);
            tvTotal = itemView.findViewById(R.id.tv_total);
            tvStatus = itemView.findViewById(R.id.tv_status);
            btnDetail = itemView.findViewById(R.id.btn_detail);
        }
    }
}
