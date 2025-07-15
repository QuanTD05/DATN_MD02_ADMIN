package com.example.datn_md02_admim.Adapter;

import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.datn_md02_admim.Model.ChatMessage;
import com.example.datn_md02_admim.R;
import com.google.firebase.database.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_RIGHT = 1;
    private static final int VIEW_TYPE_LEFT = 2;

    private final List<ChatMessage> messages;
    private final String currentUserEmail;

    public ChatAdapter(List<ChatMessage> messages, String currentUserEmail) {
        this.messages = messages;
        this.currentUserEmail = currentUserEmail != null ? currentUserEmail.trim().toLowerCase() : "";
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessage msg = messages.get(position);
        String sender = msg.getSender() != null ? msg.getSender().trim().toLowerCase() : "";
        return sender.equals(currentUserEmail) ? VIEW_TYPE_RIGHT : VIEW_TYPE_LEFT;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_RIGHT) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_chat_sender, parent, false);
            return new RightMessageViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_left, parent, false);
            return new LeftMessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage msg = messages.get(position);

        // ✅ Ưu tiên content, fallback sang message
        String content = msg.getDisplayContent();
        String formattedTime = formatTimestamp(msg.getTimestamp());

        if (holder instanceof RightMessageViewHolder) {
            ((RightMessageViewHolder) holder).tvMessage.setText(content);
            ((RightMessageViewHolder) holder).tvTime.setText(formattedTime);
        } else if (holder instanceof LeftMessageViewHolder) {
            ((LeftMessageViewHolder) holder).tvMessage.setText(content);
            ((LeftMessageViewHolder) holder).tvTime.setText(formattedTime);
        }

        // ✅ Chỉ cho người gửi mới có quyền xóa
        if (msg.getSender() != null && msg.getSender().equalsIgnoreCase(currentUserEmail)) {
            holder.itemView.setOnLongClickListener(v -> {
                new AlertDialog.Builder(v.getContext())
                        .setTitle("Xóa tin nhắn")
                        .setMessage("Bạn có chắc chắn muốn xóa tin nhắn này?")
                        .setPositiveButton("Xóa", (dialog, which) -> deleteMessage(msg))
                        .setNegativeButton("Hủy", null)
                        .show();
                return true;
            });
        } else {
            holder.itemView.setOnLongClickListener(null); // không cho người nhận xóa
        }
    }

    @Override
    public int getItemCount() {
        return messages != null ? messages.size() : 0;
    }

    private String formatTimestamp(long timestamp) {
        if (timestamp <= 0) return "";
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    private void deleteMessage(ChatMessage message) {
        DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("messages");

        chatRef.orderByChild("timestamp").equalTo(message.getTimestamp())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot child : snapshot.getChildren()) {
                            ChatMessage snapMsg = child.getValue(ChatMessage.class);

                            // Đảm bảo là đúng message cần xóa (theo cả sender + receiver + timestamp)
                            if (snapMsg != null &&
                                    snapMsg.getSender() != null &&
                                    snapMsg.getReceiver() != null &&
                                    snapMsg.getSender().equals(message.getSender()) &&
                                    snapMsg.getReceiver().equals(message.getReceiver()) &&
                                    snapMsg.getTimestamp() == message.getTimestamp()) {

                                child.getRef().removeValue(); // Xóa trong Firebase

                                int index = messages.indexOf(message);
                                if (index != -1) {
                                    messages.remove(index);
                                    notifyItemRemoved(index);
                                }
                                break;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    static class RightMessageViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage, tvTime;

        RightMessageViewHolder(View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvTime = itemView.findViewById(R.id.tvTime);
        }
    }

    static class LeftMessageViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage, tvTime;

        LeftMessageViewHolder(View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tvMessageLeft);
            tvTime = itemView.findViewById(R.id.tvTimeLeft);
        }
    }
}
