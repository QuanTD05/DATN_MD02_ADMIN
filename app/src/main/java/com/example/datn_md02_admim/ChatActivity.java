package com.example.datn_md02_admim;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.widget.*;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.datn_md02_admim.Adapter.ChatAdapter;
import com.example.datn_md02_admim.Model.ChatMessage;
import com.example.datn_md02_admim.Model.ChatStaff;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.*;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView rvMessages;
    private EditText etMessage;
    private Button btnSend;
    private ImageButton btnImage;
    private TextView tvChatWith;

    private ChatAdapter adapter;
    private final List<ChatMessage> messageList = new ArrayList<>();

    private String senderEmail;
    private String receiverEmail;

    private DatabaseReference chatRef;
    private DatabaseReference usersRef;

    private Uri selectedImageUri = null;

    private static final String CHANNEL_ID = "chat_channel";
    private static final String PREFS_NAME = "chat_prefs";
    private static final String KEY_LAST_NOTIFIED_TIMESTAMP = "last_notified_timestamp";

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    confirmSendImage();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        createNotificationChannel();
        requestNotificationPermission();

        initViews();

        senderEmail = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getEmail()
                : null;
        receiverEmail = getIntent().getStringExtra("receiver_email");

        if (senderEmail == null || receiverEmail == null) {
            Toast.makeText(this, "Không xác định được người gửi hoặc người nhận", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        usersRef = FirebaseDatabase.getInstance().getReference("users");
        chatRef = FirebaseDatabase.getInstance().getReference("chats");

        loadReceiverFullName();
        setupRecyclerView();
        loadMessages();
        markMessagesAsRead();

        btnSend.setOnClickListener(v -> sendTextMessage());
        btnImage.setOnClickListener(v -> openGallery());
    }

    private void initViews() {
        rvMessages = findViewById(R.id.rvMessages);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);
        btnImage = findViewById(R.id.btnImage);
        tvChatWith = findViewById(R.id.tvChatWith);
    }

    private void setupRecyclerView() {
        adapter = new ChatAdapter(this, messageList, senderEmail);
        rvMessages.setLayoutManager(new LinearLayoutManager(this));
        rvMessages.setAdapter(adapter);
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    private void confirmSendImage() {
        if (selectedImageUri == null) return;

        new android.app.AlertDialog.Builder(this)
                .setTitle("Gửi ảnh")
                .setMessage("Bạn có muốn gửi ảnh này không?")
                .setPositiveButton("Gửi", (dialog, which) -> uploadImage(selectedImageUri))
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void uploadImage(Uri uri) {
        if (uri == null) return;

        StorageReference storageRef = FirebaseStorage.getInstance().getReference("chat_images");
        String fileName = UUID.randomUUID().toString() + ".jpg";
        storageRef.child(fileName).putFile(uri)
                .addOnSuccessListener(task -> storageRef.child(fileName).getDownloadUrl()
                        .addOnSuccessListener(downloadUri -> {
                            long timestamp = System.currentTimeMillis();
                            ChatMessage msg = new ChatMessage(senderEmail, receiverEmail, downloadUri.toString(), true, timestamp);
                            chatRef.push().setValue(msg)
                                    .addOnSuccessListener(aVoid -> updateLastMessageForBothUsers("[Hình ảnh]", timestamp));
                            etMessage.setText("");
                        }))
                .addOnFailureListener(e -> Toast.makeText(this, "Gửi ảnh thất bại", Toast.LENGTH_SHORT).show());
    }

    private void sendTextMessage() {
        String text = etMessage.getText().toString().trim();
        if (TextUtils.isEmpty(text)) return;

        long timestamp = System.currentTimeMillis();
        ChatMessage msg = new ChatMessage(senderEmail, receiverEmail, text, false, timestamp);
        chatRef.push().setValue(msg)
                .addOnSuccessListener(aVoid -> updateLastMessageForBothUsers(text, timestamp));
        etMessage.setText("");
    }

    private void updateLastMessageForBothUsers(String content, long timestamp) {
        updateLastMessageForUser(receiverEmail, content, timestamp, true);  // Đánh dấu unread = true cho người nhận
        updateLastMessageForUser(senderEmail, content, timestamp, false);  // Không cần unread cho người gửi
    }

    private void updateLastMessageForUser(String email, String content, long timestamp, boolean setUnread) {
        usersRef.orderByChild("email").equalTo(email)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot data : snapshot.getChildren()) {
                            data.getRef().child("lastMessageText").setValue(content);
                            data.getRef().child("lastMessageTimestamp").setValue(timestamp);
                            if (setUnread) {
                                data.getRef().child("unread").setValue(true);
                            }
                        }
                    }

                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void markMessagesAsRead() {
        usersRef.orderByChild("email").equalTo(receiverEmail)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot data : snapshot.getChildren()) {
                            data.getRef().child("unread").setValue(false);
                        }
                    }

                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void loadReceiverFullName() {
        usersRef.orderByChild("email").equalTo(receiverEmail)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot data : snapshot.getChildren()) {
                            ChatStaff staff = data.getValue(ChatStaff.class);
                            if (staff != null) {
                                tvChatWith.setText(" " + staff.getFullName());
                                break;
                            }
                        }
                    }

                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(ChatActivity.this, "Lỗi tải tên người nhận", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadMessages() {
        chatRef.addValueEventListener(new ValueEventListener() {
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messageList.clear();
                long lastNotified = getLastNotifiedTimestamp();

                for (DataSnapshot data : snapshot.getChildren()) {
                    ChatMessage msg = data.getValue(ChatMessage.class);
                    if (msg == null) continue;

                    String msgSender = msg.getSender();
                    String msgReceiver = msg.getReceiver();

                    boolean isInCurrentChat =
                            (msgSender.equalsIgnoreCase(senderEmail) && msgReceiver.equalsIgnoreCase(receiverEmail)) ||
                                    (msgSender.equalsIgnoreCase(receiverEmail) && msgReceiver.equalsIgnoreCase(senderEmail));

                    if (isInCurrentChat) {
                        messageList.add(msg);

                        if (msgReceiver.equalsIgnoreCase(senderEmail) && msg.getTimestamp() > lastNotified) {
                            sendNotification(msgSender, msg.getDisplayContent());
                            setLastNotifiedTimestamp(msg.getTimestamp());
                        }
                    }
                }

                adapter.notifyDataSetChanged();
                if (!messageList.isEmpty()) {
                    rvMessages.scrollToPosition(messageList.size() - 1);
                }
            }

            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ChatActivity.this, "Lỗi tải tin nhắn", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendNotification(String sender, String message) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_message)
                .setContentTitle("Tin nhắn mới từ " + sender)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManagerCompat.from(this).notify((int) System.currentTimeMillis(), builder.build());
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "Chat Notifications", NotificationManager.IMPORTANCE_HIGH);
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1001);
        }
    }

    private long getLastNotifiedTimestamp() {
        return getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .getLong(KEY_LAST_NOTIFIED_TIMESTAMP, 0L);
    }

    private void setLastNotifiedTimestamp(long timestamp) {
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit()
                .putLong(KEY_LAST_NOTIFIED_TIMESTAMP, timestamp)
                .apply();
    }
}
