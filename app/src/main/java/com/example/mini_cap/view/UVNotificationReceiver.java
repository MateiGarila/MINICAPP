package com.example.mini_cap.view;
import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.example.mini_cap.R;


public class UVNotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // Notify the user to reapply sunscreen every 2 hours
        String notificationMessage = "Don't forget to reapply sunscreen.";
        Intent uvIntent = new Intent("UV_NOTIFICATION_ACTION");
        uvIntent.putExtra("notificationMessage", notificationMessage);
        context.sendBroadcast(uvIntent);
    }
}

