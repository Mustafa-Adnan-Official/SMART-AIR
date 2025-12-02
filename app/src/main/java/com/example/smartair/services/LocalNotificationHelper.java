package com.example.smartair.services;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.annotation.RequiresPermission;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.smartair.R;

public class LocalNotificationHelper {

    private Context context;
    private String channelId = "smartair_alerts";
    private String channelName = "SmartAir Alerts";

    public LocalNotificationHelper(Context context) {
        this.context = context.getApplicationContext();
    }

    private void ensureChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager m = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (m == null) return;
            NotificationChannel c = m.getNotificationChannel(channelId);
            if (c == null) {
                NotificationChannel ch = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH);
                m.createNotificationChannel(ch);
            }
        }
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    public void show(String title, String body) {
        ensureChannel();
        NotificationCompat.Builder b = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(body)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);
        NotificationManagerCompat m = NotificationManagerCompat.from(context);
        int id = (int) System.currentTimeMillis();
        m.notify(id, b.build());
    }
}
