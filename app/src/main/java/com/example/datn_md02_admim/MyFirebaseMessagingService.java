package com.example.datn_md02_admim;


import android.app.PendingIntent;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        // Lấy dữ liệu từ tin nhắn gửi đến
        String senderId = remoteMessage.getData().get("senderId");
        String message = remoteMessage.getData().get("message");

        // Tạo Intent mở ChatActivity với đúng người gửi
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("senderId", senderId);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE
        );

        // Hiển thị thông báo
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "chat_channel")
                .setSmallIcon(R.drawable.ic_message) // bạn có thể thay icon này
                .setContentTitle("Tin nhắn mới")
                .setContentText(message)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManagerCompat manager = NotificationManagerCompat.from(this);
        manager.notify(1001, builder.build());
    }

    @Override
    public void onNewToken(@NonNull String token) {
        // Gửi token này lên server nếu cần
    }
}