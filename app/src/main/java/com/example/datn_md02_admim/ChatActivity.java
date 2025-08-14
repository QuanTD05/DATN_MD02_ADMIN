// File: app/src/main/java/com/example/datn_md02_admim/ChatActivity.java
package com.example.datn_md02_admim;

import android.Manifest;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ChatActivity extends AppCompatActivity {
    private static final String CHANNEL_ID = "chat_channel";
    private static final String PREFS = "chat_prefs";
    private static final String KEY_LAST_TS = "last_ts";

    private RecyclerView rvMessages;
    private EditText etMessage;
    private Button btnSend;
    private ImageButton btnImage;
    private TextView tvChatWith;

    private ChatAdapter adapter;
    private final List<ChatMessage> messageList = new ArrayList<>();

    private String senderEmail, senderUid, receiverEmail, receiverName;
    private DatabaseReference chatRef, usersRef;
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private Uri selectedImageUri;
    private long lastTimestamp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // 1) Tạo Notification Channel & xin permission
        createNotificationChannel();
        requestNotificationPermission();

        // 2) Đọc lastTimestamp từ prefs
        lastTimestamp = getSharedPreferences(PREFS, MODE_PRIVATE).getLong(KEY_LAST_TS, 0L);

        // 3) Lấy currentUser
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) { finish(); return; }
        senderUid   = user.getUid();
        senderEmail = user.getEmail();

        initViews();

        // 4) Lấy receiverEmail từ Intent (hoặc deep link) và khởi tạo Firebase refs
        receiverEmail = getIntent().getStringExtra("receiver_email");
        receiverName  = getIntent().getStringExtra("receiver_name");
        handleDeepLink(getIntent()); // có thể override receiverEmail/receiverName từ myapp://chat

        if (TextUtils.isEmpty(receiverEmail)) {
            Toast.makeText(this, "Không xác định được người nhận", Toast.LENGTH_SHORT).show();
            finish(); return;
        }

        usersRef = FirebaseDatabase.getInstance().getReference("users");
        chatRef  = FirebaseDatabase.getInstance().getReference("chats");

        // 5) Thiết lập UI và lắng nghe chat
        loadReceiverName();
        setupRecyclerView();
        setupImagePicker();
        listenMessages();

        btnSend .setOnClickListener(v -> sendTextMessage());
        btnImage.setOnClickListener(v -> openGallery());
    }

    // -------- Notification & Permission ----------
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Chat Notifications",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Thông báo tin nhắn mới");
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{ Manifest.permission.POST_NOTIFICATIONS },
                    1001
            );
        }
    }

    // -------- Views ----------
    private void initViews() {
        rvMessages = findViewById(R.id.rvMessages);
        etMessage  = findViewById(R.id.etMessage);
        btnSend    = findViewById(R.id.btnSend);
        btnImage   = findViewById(R.id.btnImage);
        tvChatWith = findViewById(R.id.tvChatWith);
    }

    private void setupRecyclerView() {
        adapter = new ChatAdapter(this, messageList, senderEmail);
        rvMessages.setLayoutManager(new LinearLayoutManager(this));
        rvMessages.setAdapter(adapter);
    }

    private void setupImagePicker() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        confirmSendImage();
                    }
                }
        );
    }

    private void openGallery() {
        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(i);
    }

    private void confirmSendImage() {
        if (selectedImageUri == null) return;
        new AlertDialog.Builder(this)
                .setTitle("Gửi ảnh")
                .setMessage("Bạn có muốn gửi ảnh này không?")
                .setPositiveButton("Gửi", (d, w) -> uploadImage())
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void uploadImage() {
        StorageReference ref = FirebaseStorage.getInstance().getReference("chat_images");
        String name = UUID.randomUUID().toString() + ".jpg";
        ref.child(name).putFile(selectedImageUri)
                .addOnSuccessListener(task -> ref.child(name).getDownloadUrl()
                        .addOnSuccessListener(url -> {
                            long ts = System.currentTimeMillis();
                            ChatMessage msg = new ChatMessage(
                                    senderEmail, receiverEmail, url.toString(), true, ts
                            );
                            chatRef.push().setValue(msg);
                            etMessage.setText("");
                        }))
                .addOnFailureListener(e -> Toast.makeText(
                        this, "Gửi ảnh thất bại", Toast.LENGTH_SHORT).show()
                );
    }

    private void sendTextMessage() {
        String text = etMessage.getText().toString().trim();
        if (TextUtils.isEmpty(text)) return;
        long ts = System.currentTimeMillis();
        ChatMessage msg = new ChatMessage(senderEmail, receiverEmail, text, false, ts);
        chatRef.push().setValue(msg);
        etMessage.setText("");
    }

    // -------- Listen & Notify ----------
    private void listenMessages() {
        chatRef.addValueEventListener(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snap) {
                messageList.clear();
                for (DataSnapshot ds : snap.getChildren()) {
                    ChatMessage m = ds.getValue(ChatMessage.class);
                    if (m == null) continue;
                    boolean inChat =
                            (m.getSender().equalsIgnoreCase(senderEmail) && m.getReceiver().equalsIgnoreCase(receiverEmail)) ||
                                    (m.getSender().equalsIgnoreCase(receiverEmail) && m.getReceiver().equalsIgnoreCase(senderEmail));
                    if (inChat) {
                        messageList.add(m);
                        // Nếu không phải mình gửi & mới hơn lastTimestamp -> hiển thị local notification (có deep link)
                        if (!m.getSender().equalsIgnoreCase(senderEmail)
                                && m.getTimestamp() > lastTimestamp) {
                            String shown = (m.isImage() ? "[Hình ảnh]" :
                                    (m.getDisplayContent() != null && !m.getDisplayContent().isEmpty()
                                            ? m.getDisplayContent() : m.getContent()));
                            sendLocalNotification(m.getSender(), shown);
                            lastTimestamp = m.getTimestamp();
                            // Lưu lại vào prefs
                            getSharedPreferences(PREFS, MODE_PRIVATE)
                                    .edit()
                                    .putLong(KEY_LAST_TS, lastTimestamp)
                                    .apply();
                        }
                    }
                }
                adapter.notifyDataSetChanged();
                if (!messageList.isEmpty())
                    rvMessages.scrollToPosition(messageList.size() - 1);
            }
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        });
    }

    private void sendLocalNotification(String sender, String message) {
        // Deep link để mở đúng phòng chat
        String deep = "myapp://chat?peerId=" + sanitizeEmail(sender) +
                "&peerName=" + Uri.encode(getNameFromEmail(sender));
        Intent open = new Intent(Intent.ACTION_VIEW, Uri.parse(deep));
        PendingIntent pi = PendingIntent.getActivity(
                this, (int) System.currentTimeMillis(), open,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_message)
                .setContentTitle("Tin nhắn mới từ " + sender)
                .setContentText(message)
                .setContentIntent(pi)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManagerCompat.from(this)
                .notify((int) System.currentTimeMillis(), builder.build());
    }

    // -------- Load tên người nhận ----------
    private void loadReceiverName() {
        if (!TextUtils.isEmpty(receiverName)) {
            tvChatWith.setText(" " + receiverName);
            return;
        }
        usersRef.orderByChild("email").equalTo(receiverEmail)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override public void onDataChange(@NonNull DataSnapshot snap) {
                        for (DataSnapshot d : snap.getChildren()) {
                            ChatStaff staff = d.getValue(ChatStaff.class);
                            if (staff != null) {
                                receiverName = staff.getFullName();
                                tvChatWith.setText(" " + receiverName);
                                break;
                            }
                        }
                        if (TextUtils.isEmpty(receiverName)) {
                            tvChatWith.setText(" " + getNameFromEmail(receiverEmail));
                        }
                    }
                    @Override public void onCancelled(@NonNull DatabaseError e) {}
                });
    }

    // -------- Deep link handler ----------
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleDeepLink(intent);
    }

    private void handleDeepLink(Intent intent) {
        Uri data = intent.getData();
        if (data != null && "myapp".equals(data.getScheme()) && "chat".equals(data.getHost())) {
            String peerIdSanitized = data.getQueryParameter("peerId");
            String peerNameParam   = data.getQueryParameter("peerName");
            if (!TextUtils.isEmpty(peerIdSanitized)) {
                receiverEmail = peerIdSanitized.replace(",", ".");
            }
            if (!TextUtils.isEmpty(peerNameParam)) {
                receiverName = peerNameParam;
                tvChatWith.setText(" " + receiverName);
            }
        }
    }

    // -------- Utils ----------
    private String sanitizeEmail(String e) {
        return e == null ? "" : e.replaceAll("[.#$\\[\\]]", ",");
    }

    private String getNameFromEmail(String email) {
        if (TextUtils.isEmpty(email)) return "";
        int at = email.indexOf('@');
        return at > 0 ? email.substring(0, at) : email;
    }
}
