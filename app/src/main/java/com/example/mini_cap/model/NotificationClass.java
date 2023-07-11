package com.example.mini_cap.model;



import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class NotificationClass {

    NotificationManagerCompat notificationManagerCompat;
    Notification notification;
    private Context context;

    public NotificationClass(Context context){
        this.context = context;
    };

    public void create_notification(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel("channel_id", "notification_channel", NotificationManager.IMPORTANCE_DEFAULT);

            NotificationManager manager = context.getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);

        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "channel_id")
                .setSmallIcon(android.R.drawable.stat_notify_sync)
                .setContentTitle("Notification Title")
                .setContentText("This is the body");

        notification = builder.build();
        notificationManagerCompat = NotificationManagerCompat.from(context);

        //USE THIS TO NOTIFY:
        //notificationManagerCompat.notify(1 notification);
    }
}
