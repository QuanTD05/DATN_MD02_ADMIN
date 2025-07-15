package com.example.datn_md02_admim;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.datn_md02_admim.Adapter.ChatAdapter;
import com.example.datn_md02_admim.Model.ChatMessage;
import com.example.datn_md02_admim.Model.ChatStaff;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView rvMessages;
    private EditText etMessage;
    private Button btnSend;
    private TextView tvChatWith;

    private ChatAdapter adapter;
    private List<ChatMessage> messageList;
    private Set<String> notifiedMessageIds = new HashSet<>();

    private String senderEmail;
    private String receiverEmail;

    private DatabaseReference chatRef;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "chat_channel", "Chat Notifications", NotificationManager.IMPORTANCE_HIGH);
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }

        rvMessages = findViewById(R.id.rvMessages);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);
        tvChatWith = findViewById(R.id.tvChatWith);

        senderEmail = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getEmail() : null;
        receiverEmail = getIntent().getStringExtra("receiver_email");

        if (receiverEmail == null || senderEmail == null) {
            Toast.makeText(this, "Không thể xác định người gửi/nhận", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        usersRef = FirebaseDatabase.getInstance().getReference("users");
        loadReceiverFullName(receiverEmail);

        messageList = new ArrayList<>();
        adapter = new ChatAdapter(messageList, senderEmail);
        rvMessages.setLayoutManager(new LinearLayoutManager(this));
        rvMessages.setAdapter(adapter);

        chatRef = FirebaseDatabase.getInstance().getReference("chats");
        loadMessages();

        markMessagesAsRead();

        btnSend.setOnClickListener(v -> sendMessage());
    }

    private void loadReceiverFullName(String email) {
        usersRef.orderByChild("email").equalTo(email)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot data : snapshot.getChildren()) {
                            ChatStaff staff = data.getValue(ChatStaff.class);
                            if (staff != null) {
                                tvChatWith.setText("Đang chat với " + staff.getFullName());
                                break;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(ChatActivity.this, "Không thể tải tên người nhận", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadMessages() {
        chatRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messageList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    ChatMessage message = data.getValue(ChatMessage.class);
                    if (message == null) continue;

                    String msgSender = message.getSender();
                    String msgReceiver = message.getReceiver();

                    if (msgSender == null || msgReceiver == null ||
                            senderEmail == null || receiverEmail == null)
                        continue;

                    boolean isCurrentChat =
                            (msgSender.equalsIgnoreCase(senderEmail) && msgReceiver.equalsIgnoreCase(receiverEmail)) ||
                                    (msgSender.equalsIgnoreCase(receiverEmail) && msgReceiver.equalsIgnoreCase(senderEmail));

                    if (isCurrentChat) {
                        messageList.add(message);

                        if (msgReceiver.equalsIgnoreCase(senderEmail)) {
                            String msgId = data.getKey();
                            if (msgId != null && !notifiedMessageIds.contains(msgId)) {
                                sendNotification(msgSender, message.getMessage());
                                notifiedMessageIds.add(msgId);
                            }
                        }
                    }
                }

                adapter.notifyDataSetChanged();
                rvMessages.scrollToPosition(messageList.size() - 1);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ChatActivity.this, "Lỗi tải tin nhắn", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendMessage() {
        String text = etMessage.getText().toString().trim();
        if (TextUtils.isEmpty(text)) return;

        long timestamp = System.currentTimeMillis();

        ChatMessage msg = new ChatMessage(senderEmail, receiverEmail, text, timestamp);
        chatRef.push().setValue(msg);
        etMessage.setText("");

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users");
        userRef.orderByChild("email").equalTo(receiverEmail)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot data : snapshot.getChildren()) {
                            data.getRef().child("lastMessageText").setValue(text);
                            data.getRef().child("lastMessageTimestamp").setValue(timestamp);
                            data.getRef().child("unread").setValue(true);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
    }

    private void markMessagesAsRead() {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users");
        userRef.orderByChild("email").equalTo(senderEmail)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot data : snapshot.getChildren()) {
                            data.getRef().child("unread").setValue(false);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
    }

    private void sendNotification(String sender, String message) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "chat_channel")
                .setSmallIcon(R.drawable.ic_message)
                .setContentTitle("Tin nhắn mới từ " + sender)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManagerCompat manager = NotificationManagerCompat.from(this);
        manager.notify((int) System.currentTimeMillis(), builder.build());
    }
}

