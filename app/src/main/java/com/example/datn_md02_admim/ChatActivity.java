package com.example.datn_md02_admim;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView rvMessages;
    private EditText etMessage;
    private Button btnSend;
    private TextView tvChatWith;

    private ChatAdapter adapter;
    private List<ChatMessage> messageList;

    private String senderEmail;
    private String receiverEmail;

    private DatabaseReference chatRef;
    private DatabaseReference usersRef; // Dùng để lấy tên từ email

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Ánh xạ view
        rvMessages = findViewById(R.id.rvMessages);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);
        tvChatWith = findViewById(R.id.tvChatWith);

        // Lấy email người gửi và người nhận
        senderEmail = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getEmail() : null;
        receiverEmail = getIntent().getStringExtra("receiver_email");

        if (receiverEmail == null || senderEmail == null) {
            Toast.makeText(this, "Không thể xác định người gửi/nhận", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Lấy tên người nhận từ Firebase
        usersRef = FirebaseDatabase.getInstance().getReference("users"); // hoặc "staffs" tùy app
        loadReceiverFullName(receiverEmail);

        // Thiết lập chat
        messageList = new ArrayList<>();
        adapter = new ChatAdapter(messageList, senderEmail);
        rvMessages.setLayoutManager(new LinearLayoutManager(this));
        rvMessages.setAdapter(adapter);

        chatRef = FirebaseDatabase.getInstance().getReference("chats");
        loadMessages();

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

        ChatMessage msg = new ChatMessage(senderEmail, receiverEmail, text, System.currentTimeMillis());
        chatRef.push().setValue(msg);
        etMessage.setText("");
    }
}
