package com.example.fashionstoreapp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class NotificationHelper {
    private static final String CHANNEL_ID = "fashionstore_notifications";
    private static final String CHANNEL_NAME = "Thông báo FashionStore";
    private static final String CHANNEL_DESC = "Thông báo về đơn hàng, khuyến mãi và cập nhật";

    private final Context ctx;

    public NotificationHelper(Context context) {
        this.ctx = context.getApplicationContext();
        createChannel();
    }

    private void createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription(CHANNEL_DESC);
            NotificationManager nm = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
            if (nm != null)
                nm.createNotificationChannel(channel);
        }
    }

    public void showNotification(String title, String message, int notificationId) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(ctx, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        NotificationManagerCompat.from(ctx).notify(notificationId, builder.build());
    }
}
