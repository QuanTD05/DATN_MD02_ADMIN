package com.example.datn_md02_admim.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.datn_md02_admim.Model.ChatMessage;
import com.example.datn_md02_admim.R;
import com.google.firebase.database.*;
import com.squareup.picasso.Picasso;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_RIGHT_TEXT = 1;
    private static final int VIEW_TYPE_LEFT_TEXT = 2;
    private static final int VIEW_TYPE_RIGHT_IMAGE = 3;
    private static final int VIEW_TYPE_LEFT_IMAGE = 4;

    private final Context context;
    private final List<ChatMessage> messages;
    private final String currentUserEmail;

    public ChatAdapter(Context context, List<ChatMessage> messages, String currentUserEmail) {
        this.context = context;
        this.messages = messages;
        this.currentUserEmail = currentUserEmail != null ? currentUserEmail.trim().toLowerCase() : "";
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessage msg = messages.get(position);
        String sender = msg.getSender() != null ? msg.getSender().trim().toLowerCase() : "";
        boolean isSender = sender.equals(currentUserEmail);

        if (msg.getImageUrl() != null && !msg.getImageUrl().isEmpty()) {
            return isSender ? VIEW_TYPE_RIGHT_IMAGE : VIEW_TYPE_LEFT_IMAGE;
        } else {
            return isSender ? VIEW_TYPE_RIGHT_TEXT : VIEW_TYPE_LEFT_TEXT;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view;
        switch (viewType) {
            case VIEW_TYPE_RIGHT_IMAGE:
                view = inflater.inflate(R.layout.item_image_sender, parent, false);
                return new RightImageViewHolder(view);
            case VIEW_TYPE_LEFT_IMAGE:
                view = inflater.inflate(R.layout.item_image_receiver, parent, false);
                return new LeftImageViewHolder(view);
            case VIEW_TYPE_RIGHT_TEXT:
                view = inflater.inflate(R.layout.item_chat_sender, parent, false);
                return new RightMessageViewHolder(view);
            default:
                view = inflater.inflate(R.layout.item_message_left, parent, false);
                return new LeftMessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage msg = messages.get(position);
        String formattedTime = formatTimestamp(msg.getTimestamp());

        if (holder instanceof RightMessageViewHolder) {
            ((RightMessageViewHolder) holder).tvMessage.setText(msg.getDisplayContent());
            ((RightMessageViewHolder) holder).tvTime.setText(formattedTime);
        } else if (holder instanceof LeftMessageViewHolder) {
            ((LeftMessageViewHolder) holder).tvMessage.setText(msg.getDisplayContent());
            ((LeftMessageViewHolder) holder).tvTime.setText(formattedTime);
        } else if (holder instanceof RightImageViewHolder) {
            Picasso.get().load(msg.getImageUrl()).into(((RightImageViewHolder) holder).imgMessage);
            ((RightImageViewHolder) holder).tvTime.setText(formattedTime);
        } else if (holder instanceof LeftImageViewHolder) {
            Picasso.get().load(msg.getImageUrl()).into(((LeftImageViewHolder) holder).imgMessage);
            ((LeftImageViewHolder) holder).tvTime.setText(formattedTime);
        }

        // Xóa tin nhắn (chỉ người gửi)
        if (msg.getSender() != null && msg.getSender().equalsIgnoreCase(currentUserEmail)) {
            holder.itemView.setOnLongClickListener(v -> {
                new AlertDialog.Builder(context)
                        .setTitle("Xóa tin nhắn")
                        .setMessage("Bạn có chắc chắn muốn xóa tin nhắn này?")
                        .setPositiveButton("Xóa", (dialog, which) -> deleteMessage(msg))
                        .setNegativeButton("Hủy", null)
                        .show();
                return true;
            });
        } else {
            holder.itemView.setOnLongClickListener(null);
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
                            if (snapMsg != null &&
                                    snapMsg.getSender().equals(message.getSender()) &&
                                    snapMsg.getReceiver().equals(message.getReceiver()) &&
                                    snapMsg.getTimestamp() == message.getTimestamp()) {
                                child.getRef().removeValue();
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

    // ViewHolders
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

    static class RightImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imgMessage;
        TextView tvTime;
        RightImageViewHolder(View itemView) {
            super(itemView);
            imgMessage = itemView.findViewById(R.id.imgMessage);
            tvTime = itemView.findViewById(R.id.tvTime);
        }
    }

    static class LeftImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imgMessage;
        TextView tvTime;
        LeftImageViewHolder(View itemView) {
            super(itemView);
            imgMessage = itemView.findViewById(R.id.imgMessageLeft);
            tvTime = itemView.findViewById(R.id.tvTimeLeft);
        }
    }
}
