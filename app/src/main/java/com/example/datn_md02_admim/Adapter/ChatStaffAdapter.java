package com.example.datn_md02_admim.Adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.datn_md02_admim.Model.ChatStaff;
import com.example.datn_md02_admim.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatStaffAdapter extends RecyclerView.Adapter<ChatStaffAdapter.VH> {

    private final List<ChatStaff> staffList;
    private final OnStaffClickListener listener;
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    public interface OnStaffClickListener {
        void onStaffClick(ChatStaff staff);
    }

    public ChatStaffAdapter(List<ChatStaff> staffList, OnStaffClickListener listener) {
        this.staffList = staffList;
        this.listener = listener;
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat_user, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        ChatStaff s = staffList.get(position);
        Context ctx = h.itemView.getContext();

        // Avatar
        Glide.with(ctx)
                .load(s.getAvatar())
                .placeholder(R.drawable.ic_avatar_placeholder)
                .circleCrop()
                .into(h.ivAvatar);

        // Tên + thời gian
        h.tvName.setText(safe(s.getFullName()));
        long ts = s.getLastMessageTimestamp();
        h.tvTime.setText(ts > 0 ? sdf.format(new Date(ts)) : "");

        // Last message: ảnh -> [Hình ảnh]
        String last = safe(s.getLastMessageText());
        if (TextUtils.isEmpty(last)) last = "Chưa có tin nhắn";
        h.tvLast.setText(last);

        // Chấm online (statusDot dùng circle_bg — nhớ để shape hỗ trợ tint)
        int dot = s.isOnline() ? Color.parseColor("#34D399") : Color.parseColor("#A0AEC0");
        h.vDot.setBackgroundTintList(ColorStateList.valueOf(dot));

        // Unread highlight + đậm/màu
        boolean unread = s.isUnread() || s.isHasUnread();
        h.itemView.setBackgroundColor(unread ? Color.parseColor("#E0F2FF") : Color.TRANSPARENT);

        if (unread) {
            setBold(h);
            setTextColor(h, ContextCompat.getColor(ctx, android.R.color.black));
        } else {
            setNormal(h);
            setTextColor(h, Color.parseColor("#666666")); // giống màu bạn dùng cho last message
            h.tvName.setTextColor(Color.parseColor("#212121"));
            h.tvTime.setTextColor(Color.parseColor("#D0C7C7"));
        }

        h.itemView.setOnClickListener(v -> listener.onStaffClick(s));
    }

    @Override public int getItemCount() { return staffList != null ? staffList.size() : 0; }

    private static String safe(String s) { return s == null ? "" : s; }

    private void setBold(VH h) {
        h.tvName.setTypeface(null, Typeface.BOLD);
        h.tvLast.setTypeface(null, Typeface.BOLD);
        h.tvTime.setTypeface(null, Typeface.BOLD);
    }

    private void setNormal(VH h) {
        h.tvName.setTypeface(null, Typeface.NORMAL);
        h.tvLast.setTypeface(null, Typeface.NORMAL);
        h.tvTime.setTypeface(null, Typeface.NORMAL);
    }

    private void setTextColor(VH h, int color) {
        h.tvName.setTextColor(color);
        h.tvLast.setTextColor(color);
        h.tvTime.setTextColor(color);
        // tvEmail không có trong layout này, bỏ qua
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView ivAvatar;
        View vDot;
        TextView tvName, tvLast, tvTime;

        VH(@NonNull View v) {
            super(v);
            ivAvatar = v.findViewById(R.id.imgAvatar);
            vDot     = v.findViewById(R.id.statusDot);
            // map ĐÚNG ID theo XML bạn gửi
            tvName   = v.findViewById(R.id.tvStaffName);
            tvTime   = v.findViewById(R.id.tvTimestamp);
            tvLast   = v.findViewById(R.id.tvLastMessage);
        }
    }
}
