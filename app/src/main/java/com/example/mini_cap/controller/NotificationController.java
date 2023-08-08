package com.example.mini_cap.controller;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.icu.util.Calendar;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.mini_cap.R;
import com.example.mini_cap.model.Day;

import java.util.Arrays;
import java.util.function.Consumer;

import app.uvtracker.sensor.pii.event.EventRegistry;

public class NotificationController {

    private static final String TAG = NotificationController.class.getSimpleName();

    private static final int TICK_DELAY = 60000;
    private static final int TICKS_PER_REMINDER = 5;
    private static final int AVERAGEING = 2;


    private static NotificationController instance;

    @NonNull
    public static NotificationController get(@NonNull Context context) {
        if(NotificationController.instance == null)
            NotificationController.instance = new NotificationController(context);
        return NotificationController.instance;
    }

    public static void release() {
        NotificationController.instance = null;
    }

    @NonNull
    private final Handler handler;

    @NonNull
    private final DBHelper dbHelper;

    private int tickCount;
    private int session;
    private boolean running;

    @Nullable
    private Severity lastSeverity = Severity.LOW;

    @Nullable
    private Consumer<String> genericNotificationCallback;

    @Nullable
    private Consumer<Severity> severityNotificationCallback;

    @Nullable
    private Runnable reminderNotificationCallback;

    public NotificationController(@NonNull Context context) {
        this.handler = new Handler(Looper.getMainLooper());
        this.dbHelper = DBHelper.get(context);
        this.tickCount = 1;
        this.session = 0;
        this.running = false;
    }

    public void start() {
        if(this.running) return;
        this.running = true;
        int sessionCapture = this.session;
        this.handler.postDelayed(() -> this.run(sessionCapture), TICK_DELAY);
    }

    public void stop() {
        if(!this.running) return;
        this.running = false;
        this.session++;
    }

    public void registerGenericNotificationCallback(@NonNull Consumer<String> callback) {
        this.genericNotificationCallback = callback;
    }

    public void registerSeverityCallback(@NonNull Consumer<Severity> callback) {
        this.severityNotificationCallback = callback;
    }

    public void registerReminderCallback(@NonNull Runnable callback) {
        this.reminderNotificationCallback = callback;
    }

    private void run(int session) {
        if(this.session != session) return;

        Log.d(TAG, "Tick: " + this.tickCount);

        if(this.tickCount % TICKS_PER_REMINDER == 0)
            if(this.reminderNotificationCallback != null)
                this.reminderNotificationCallback.run();

        this.checkUV();

        this.tickCount++;
        this.handler.postDelayed(() -> this.run(session), TICK_DELAY);
    }

    private void checkUV() {
        Calendar calendar = Calendar.getInstance();
        int hours = calendar.get(Calendar.HOUR_OF_DAY);
        int minutes = calendar.get(Calendar.MINUTE);

        float sum = 0.0f;
        for(int i = 0; i < AVERAGEING; i++) {
            float value = this.dbHelper.getMinuteAvg(new Day(calendar.getTime()), minutes - i, hours, false);
            if(Float.isNaN(value)) {
                Log.d(TAG, "There's a null in recent minute averages! " + i);
                return;
            }
            sum += value;
        }
        sum /= AVERAGEING;
        Log.d(TAG, "Notification controller obtained " + sum);
        Severity severity = Severity.getFromIndex(sum);

        Severity lastSeverity = this.lastSeverity;
        this.lastSeverity = severity;
        if(severity == lastSeverity) return;

        Log.d(TAG, "Notification controller decided to post " + severity);
        if(this.severityNotificationCallback != null)
            this.severityNotificationCallback.accept(severity);
    }

    public void postNotification(@NonNull String message) {
        if(this.genericNotificationCallback != null)
            this.genericNotificationCallback.accept(message);
    }

    public static class NotificationPublisher {

        private static final String UV_INDEX_NOTIFICATION_CHANNEL_ID = "UV_INDEX_NOTIFICATION_CHANNEL";
        private static final int UV_INDEX_NOTIFICATION_ID = 1;

        @NonNull
        private final Context context;

        public NotificationPublisher(@NonNull Context context) {
            this.context = context;
            this.initializeNotificationServices();
        }

        private void initializeNotificationServices() {
            NotificationChannel channel = new NotificationChannel(
                    UV_INDEX_NOTIFICATION_CHANNEL_ID,
                    "UV Index Notifications",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("Notifications about UV index changes");
            NotificationManager notificationManager = this.context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }

            NotificationController controller = NotificationController.get(this.context);
            controller.registerSeverityCallback(severity -> {
                String notificationMessage;
                switch(severity) {
                    default:
                    case LOW:
                        notificationMessage = "Low risk of UV exposure, don't forget to wear sunscreen.";
                        break;
                    case MEDIUM:
                        notificationMessage = "Moderate risk of UV exposure. Please wear sunscreen.";
                        break;
                    case HIGH:
                        notificationMessage = "High risk of skin damage. Wear sunscreen and seek shade.";
                        break;
                    case VERY_HIGH:
                        notificationMessage = "Very High Risk! Wear sunscreen, seek shade or stay indoors.";
                        break;
                    case EXTREME:
                        notificationMessage = "Extreme Risk! Stay indoors. If not possible then wear protective clothing, sunscreen and sunglasses, and seek shade.";
                        break;
                }
                this.displayNotification(notificationMessage);
            });

            controller.start();
        }

        public void displayNotification(@NonNull String message) {
            NotificationManager notificationManager = this.context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.notify(
                        UV_INDEX_NOTIFICATION_ID,
                        new Notification.Builder(this.context, UV_INDEX_NOTIFICATION_CHANNEL_ID)
                                .setContentTitle("UV Index Alert")
                                .setContentText(message)
                                .setSmallIcon(R.mipmap.ic_launcher)
                                .setAutoCancel(true)
                                .build()
                );
            }
        }

    }

    public enum Severity {

        LOW(-1, 2),
        MEDIUM(2, 5),
        HIGH(5, 7),
        VERY_HIGH(7, 11),
        EXTREME(11, -1),
        ;

        private static final String TAG = Severity.class.getSimpleName();

        @NonNull
        public static Severity getFromIndex(float uvIndex0) {
            int uvIndex = (int)Math.floor(uvIndex0);
            for(Severity s : Severity.values()) {
                if (s.upBound >= 0 && s.upBound <= uvIndex) continue;
                if (s.lowBound >= 0 && s.lowBound > uvIndex) continue;
                return s;
            }
            Log.d(TAG, "Could not found severity enum from UV index " + uvIndex0);
            return LOW;
        }

        private final int lowBound;
        private final int upBound;

        Severity(int lowBoundInclusive, int upBoundExclusive) {
            this.lowBound = lowBoundInclusive;
            this.upBound = upBoundExclusive ;
        }

        public int getLowBound() {
            return lowBound;
        }

        public int getUpBound() {
            return upBound;
        }

    }

}

