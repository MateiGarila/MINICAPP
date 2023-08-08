package com.example.mini_cap.controller;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.icu.util.Calendar;
import android.icu.util.TimeZone;
import android.os.Build;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import com.example.mini_cap.R;
import com.example.mini_cap.controller.DBHelper;
import com.example.mini_cap.model.Day;

import java.sql.Date;

public class UVIndexService extends IntentService {
    private static final String UV_INDEX_NOTIFICATION_CHANNEL_ID = "UV_INDEX_NOTIFICATION_CHANNEL";
    private static final int UV_INDEX_NOTIFICATION_ID = 1;
    private DBHelper dbHelper;

    public UVIndexService() {
        super("UVIndexService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        DBHelper dbHelper = DBHelper.get(this);
        //Date currentDate = new Date();
        Calendar calendar = android.icu.util.Calendar.getInstance();
        TimeZone timeZone = calendar.getTimeZone();

        int hours = calendar.get(android.icu.util.Calendar.HOUR_OF_DAY);
        int minutes = calendar.get(android.icu.util.Calendar.MINUTE);

        // Fetch the current UV index from DBHelper
        float currentUVIndex = dbHelper.getMinuteAvg(new Day(calendar.getTime()), minutes, hours, false);
        //float currentUVIndex = dbHelper.getMinuteAvg(new Day(currentDate), minutes, hours, false);
        long currentTime = System.currentTimeMillis();

        // Determine the notification message based on UV index
        String notificationMessage = getNotificationMessage(currentUVIndex);

        // Show the notification
        showUVNotification(notificationMessage);
    }

    private String getNotificationMessage(float currentUVIndex) {
        if (currentUVIndex <= 2) {
            return "Low risk of UV exposure, don't forget to wear sunscreen.";
        } else if (currentUVIndex <= 5) {
            return "Moderate risk of UV exposure. Please wear sunscreen.";
        } else if (currentUVIndex <= 7) {
            return "High risk of skin damage. Wear sunscreen and seek shade.";
        } else if (currentUVIndex <= 10) {
            return "Very High Risk! Wear sunscreen, seek shade or stay indoors.";
        } else {
            return "Extreme Risk! Stay indoors. If not possible then wear protective clothing, sunscreen and sunglasses, and seek shade.";
        }
    }

    private void showUVNotification(String notificationMessage) {
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, UV_INDEX_NOTIFICATION_CHANNEL_ID)
                .setContentTitle("UV Index Alert")
                .setContentText(notificationMessage)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setAutoCancel(true);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            createNotificationChannel(notificationManager);
            notificationManager.notify(UV_INDEX_NOTIFICATION_ID, notificationBuilder.build());
        }
    }

    private void createNotificationChannel(NotificationManager notificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(UV_INDEX_NOTIFICATION_CHANNEL_ID, "UV Index Notifications", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Notifications about UV index changes");
            notificationManager.createNotificationChannel(channel);
        }
    }
}

