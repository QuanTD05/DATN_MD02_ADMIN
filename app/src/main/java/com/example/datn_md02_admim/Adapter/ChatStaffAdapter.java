package com.example.datn_md02_admim.Adapter;

import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
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

        holder.tvName.setText(staff.getFullName());
        holder.tvEmail.setText(staff.getEmail());
        holder.tvLastMessage.setText(staff.getLastMessageText());

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd/MM", Locale.getDefault());
        holder.tvTime.setText(
                staff.getLastMessageTimestamp() > 0 ?
                        sdf.format(staff.getLastMessageTimestamp()) : "Chưa có tin nhắn"
        );

        // In đậm nếu chưa đọc
        if (staff.isUnread()) {
            holder.tvName.setTypeface(null, Typeface.BOLD);
            holder.tvEmail.setTypeface(null, Typeface.BOLD);
            holder.tvLastMessage.setTypeface(null, Typeface.BOLD);
        } else {
            holder.tvName.setTypeface(null, Typeface.NORMAL);
            holder.tvEmail.setTypeface(null, Typeface.NORMAL);
            holder.tvLastMessage.setTypeface(null, Typeface.NORMAL);
        }

        holder.itemView.setOnClickListener(v -> {
            if (staff.isUnread()) {
                staff.setUnread(false); // Đánh dấu đã đọc
                notifyItemChanged(position);
            }
            listener.onStaffClick(staff);
        });
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
