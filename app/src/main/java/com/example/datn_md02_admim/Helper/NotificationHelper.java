package com.example.datn_md02_admim.Helper;

import android.app.*;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.example.datn_md02_admim.AdminActivity;
import com.example.datn_md02_admim.R;
import com.example.datn_md02_admim.StaffActivity;

public class NotificationHelper {
    public static void showOrderNotification(Context context, String title, String message) {
        String CHANNEL_ID = "order_channel";

        // Tạo channel cho Android 8+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    "Đơn hàng mới", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Thông báo khi có đơn hàng mới");
            channel.enableLights(true);
            channel.setLightColor(Color.RED);
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

        // Intent khi nhấn vào thông báo
        Intent intent = new Intent(context, StaffActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_lock) // icon cần có
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify((int) System.currentTimeMillis(), builder.build());
    }
}
