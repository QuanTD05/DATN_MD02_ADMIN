package com.example.datn_md02_admim.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


import com.example.datn_md02_admim.Util.NotificationUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

public class MessageForegroundService extends Service {
    private static final int FG_ID = 4441;
    private static final String TAG = "MsgFG";

    private DatabaseReference chatsRef;
    private ChildEventListener listener;
    private String meEmail;

    @Override
    public void onCreate() {
        super.onCreate();
        NotificationUtils.ensureChannels(this);

        Notification ongoing = NotificationUtils
                .buildOngoing(this, "Đang nghe tin nhắn…")
                .build();
        startForeground(FG_ID, ongoing);

        meEmail = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getEmail() : null;

        if (TextUtils.isEmpty(meEmail)) {
            Log.w(TAG, "No user logged in, stopping service");
            stopSelf();
            return;
        }

        // Chỉ nghe tin MỚI gửi tới mình
        chatsRef = FirebaseDatabase.getInstance().getReference("chats");
        Query q = chatsRef.orderByChild("receiver").equalTo(meEmail).limitToLast(1);

        listener = new ChildEventListener() {
            @Override public void onChildAdded(@NonNull DataSnapshot ds, String prevKey) {
                String sender   = ds.child("sender").getValue(String.class);
                String receiver = ds.child("receiver").getValue(String.class);
                String content  = ds.child("content").getValue(String.class);
                Boolean image   = ds.child("image").getValue(Boolean.class);
                Long ts         = ds.child("timestamp").getValue(Long.class);

                if (sender == null || receiver == null || ts == null) return;
                if (!meEmail.equalsIgnoreCase(receiver)) return;     // không phải tin đến mình
                if (meEmail.equalsIgnoreCase(sender)) return;         // bỏ tin mình gửi

                String a = sanitize(meEmail), b = sanitize(sender);
                String chatId = a.compareTo(b) < 0 ? a + "_" + b : b + "_" + a;
                String peerName = sender.contains("@")
                        ? sender.substring(0, sender.indexOf('@')) : sender;

                String deep = "myapp://chat?chatId=" + chatId
                        + "&peerId=" + sanitize(sender)
                        + "&peerName=" + Uri.encode(peerName);

                String title = sender;
                String body  = (Boolean.TRUE.equals(image) ? "[Hình ảnh]" :
                        (content != null ? content : ""));

                Notification n = NotificationUtils
                        .buildIncoming(MessageForegroundService.this, title, body, deep)
                        .build();
                ((NotificationManager) getSystemService(NOTIFICATION_SERVICE))
                        .notify((int) System.currentTimeMillis(), n);

                Log.d(TAG, "Notified new msg from " + sender);
            }

            @Override public void onChildChanged(@NonNull DataSnapshot d, String p) {}
            @Override public void onChildRemoved(@NonNull DataSnapshot d) {}
            @Override public void onChildMoved(@NonNull DataSnapshot d, String p) {}
            @Override public void onCancelled(@NonNull DatabaseError e) { Log.e(TAG, "cancelled: " + e); }
        };
        q.addChildEventListener(listener);

        Log.d(TAG, "Service started for " + meEmail);
    }

    @Override public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY; // giữ service sống
    }

    @Override public void onDestroy() {
        if (chatsRef != null && listener != null) chatsRef.removeEventListener(listener);
        super.onDestroy();
    }

    @Nullable @Override public IBinder onBind(Intent intent) { return null; }

    private String sanitize(String e) { return e == null ? "" : e.replaceAll("[.#$\\[\\]]", ","); }
}
