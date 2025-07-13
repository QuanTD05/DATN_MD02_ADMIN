package com.example.datn_md02_admim.Adapter;

import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.datn_md02_admim.Model.Notification;
import com.example.datn_md02_admim.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    public interface OnNotificationClickListener {
        void onNotificationClick(Notification notification);
    }

    private final List<Notification> notificationList;
    private final OnNotificationClickListener listener;

    public NotificationAdapter(List<Notification> notificationList, OnNotificationClickListener listener) {
        this.notificationList = notificationList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        Notification noti = notificationList.get(position);

        holder.tvTitle.setText(noti.getTitle());
        holder.tvContent.setText(noti.getContent());

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault());
        holder.tvTime.setText(sdf.format(new Date(noti.getTimestamp())));

        // Nếu chưa đọc thì in đậm
        if (!noti.isRead()) {
            holder.tvTitle.setTypeface(null, Typeface.BOLD);
            holder.tvContent.setTypeface(null, Typeface.BOLD);
        } else {
            holder.tvTitle.setTypeface(null, Typeface.NORMAL);
            holder.tvContent.setTypeface(null, Typeface.NORMAL);
        }

        holder.itemView.setOnClickListener(v -> listener.onNotificationClick(noti));
    }

    @Override
    public int getItemCount() {
        return notificationList != null ? notificationList.size() : 0;
    }

    static class NotificationViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvContent, tvTime;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvNotiTitle);
            tvContent = itemView.findViewById(R.id.tvNotiContent);
            tvTime = itemView.findViewById(R.id.tvNotiTime);
        }
    }
}
