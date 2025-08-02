package com.example.datn_md02_admim.Adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.datn_md02_admim.Model.ChatStaff;
import com.example.datn_md02_admim.R;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ChatStaffAdapter extends RecyclerView.Adapter<ChatStaffAdapter.ChatStaffViewHolder> {

    private final List<ChatStaff> staffList;
    private final OnStaffClickListener listener;

    public interface OnStaffClickListener {
        void onStaffClick(ChatStaff staff);
    }

    public ChatStaffAdapter(List<ChatStaff> staffList, OnStaffClickListener listener) {
        this.staffList = staffList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ChatStaffViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_user, parent, false);
        return new ChatStaffViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatStaffViewHolder holder, int position) {
        ChatStaff staff = staffList.get(position);
        Context context = holder.itemView.getContext();

        holder.tvName.setText(staff.getFullName());
        holder.tvEmail.setText(staff.getEmail());
        holder.tvLastMessage.setText(staff.getLastMessageText());

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd/MM", Locale.getDefault());
        holder.tvTime.setText(
                staff.getLastMessageTimestamp() > 0 ?
                        sdf.format(staff.getLastMessageTimestamp()) : "Chưa có tin nhắn"
        );

        if (staff.isUnread()) {
            // Tin chưa đọc → in đậm, màu đen
            setBold(holder);
            setTextColor(holder, ContextCompat.getColor(context, android.R.color.black));
        } else {
            // Tin đã đọc → bình thường, màu xám
            setNormal(holder);
            setTextColor(holder, ContextCompat.getColor(context, android.R.color.darker_gray));
        }

        holder.itemView.setOnClickListener(v -> {
            listener.onStaffClick(staff);
        });
    }

    private void setBold(ChatStaffViewHolder holder) {
        holder.tvName.setTypeface(null, Typeface.BOLD);
        holder.tvEmail.setTypeface(null, Typeface.BOLD);
        holder.tvLastMessage.setTypeface(null, Typeface.BOLD);
    }

    private void setNormal(ChatStaffViewHolder holder) {
        holder.tvName.setTypeface(null, Typeface.NORMAL);
        holder.tvEmail.setTypeface(null, Typeface.NORMAL);
        holder.tvLastMessage.setTypeface(null, Typeface.NORMAL);
    }

    private void setTextColor(ChatStaffViewHolder holder, int color) {
        holder.tvName.setTextColor(color);
        holder.tvEmail.setTextColor(color);
        holder.tvLastMessage.setTextColor(color);
        holder.tvTime.setTextColor(color);
    }

    public void updateOrAdd(ChatStaff updatedStaff) {
        int index = -1;
        for (int i = 0; i < staffList.size(); i++) {
            if (staffList.get(i).getEmail().equalsIgnoreCase(updatedStaff.getEmail())) {
                index = i;
                break;
            }
        }

        if (index != -1) {
            staffList.remove(index);
        }

        staffList.add(0, updatedStaff); // Đưa lên đầu danh sách
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return staffList != null ? staffList.size() : 0;
    }

    static class ChatStaffViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvEmail, tvTime, tvLastMessage;

        public ChatStaffViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvLastMessage = itemView.findViewById(R.id.tvLastMessage);
        }
    }
}
