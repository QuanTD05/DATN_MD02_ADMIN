package com.example.datn_md02_admim.Util;

import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.content.ContextCompat;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.datn_md02_admim.service.MessageForegroundService;
import com.example.datn_md02_admim.work.PollUnreadWorker;


public class BgInit {
    public static void startAll(Context ctx) {
        // Start Foreground Service an toàn trên Android 8+
        Intent svc = new Intent(ctx, MessageForegroundService.class);
        if (Build.VERSION.SDK_INT >= 26) {
            ContextCompat.startForegroundService(ctx, svc);
        } else {
            ctx.startService(svc);
        }

        // Schedule WorkManager polling
        PeriodicWorkRequest req = PollUnreadWorker.buildPeriodicRequest();
        WorkManager.getInstance(ctx).enqueueUniquePeriodicWork(
                "poll_unread_work",
                ExistingPeriodicWorkPolicy.UPDATE,
                req
        );
    }

    public static void stopAll(Context ctx) {
        ctx.stopService(new Intent(ctx, MessageForegroundService.class));
        WorkManager.getInstance(ctx).cancelUniqueWork("poll_unread_work");
    }
}
