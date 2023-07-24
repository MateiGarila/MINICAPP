package com.example.mini_cap.view;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mini_cap.R;
import com.example.mini_cap.controller.DBHelper;
import com.example.mini_cap.controller.Dict;
import com.example.mini_cap.model.Preset;

public class SessionActivity extends AppCompatActivity  {
    private EditText userInputEditText; // to simulate notification
    private Button NotificationButton;// to simulate notification

    //Declaration of all UI elements
    protected TextView mainTextView, statusTextView;
    protected RecyclerView displayUser;
    protected Button startStopBTN, addPresetBTN, editPresetBTN;

    //Needed
    private DBHelper dbHelper;
    private final static String TAG = "SessionActivity";
    private final boolean isCreate = true;

    private static final String NOTIFICATION_CHANNEL_ID = "UV_INDEX_NOTIFICATION_CHANNEL";
    private static final int NOTIFICATION_ID = 1;

    private final BroadcastReceiver uvNotificationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int uvIndex = intent.getIntExtra("uvIndex", 1);
            showUVNotification(uvIndex);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session);

        dbHelper = new DBHelper(getBaseContext());

        //Attaching the UI elements to their respective objects
        mainTextView = findViewById(R.id.sessionActivityTextView);
        statusTextView = findViewById(R.id.sessionStatusTextView);
        displayUser = findViewById(R.id.sessionUserDisplayRV);
        startStopBTN = findViewById(R.id.startStopSessionBTN);
        addPresetBTN = findViewById(R.id.addUserBTN);
        editPresetBTN = findViewById(R.id.editUserBTN);

        userInputEditText = findViewById(R.id.user_input_edit_text);// notification
        NotificationButton = findViewById(R.id.SendNotification); // notification

        //Temporary until we figure out a better way to navigate - Mat
        mainTextView.setOnClickListener(v -> finish());

        addPresetBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Add Preset PRESSED");
                PresetFragment fragment = PresetFragment.newInstance(null, isCreate);
                fragment.show(getSupportFragmentManager(), "CreatePreset");

            }
        });

        editPresetBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toEditActivity();
            }
        });

        startStopBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Start Session PRESSED");
                StartSessionFragment fragment = new StartSessionFragment();
                fragment.show(getSupportFragmentManager(), "StartSession");
            }
        });
        NotificationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userInput = userInputEditText.getText().toString();
                if (isValidInput(userInput)) {
                    int uvIndex = Integer.parseInt(userInput);
                    showUVNotification(uvIndex);
                } else {
                    // Handle invalid input, e.g., show a toast
                }
            }
        });

        createNotificationChannel();

        // Register the broadcast receiver
        IntentFilter filter = new IntentFilter("UV_NOTIFICATION_ACTION");
        registerReceiver(uvNotificationReceiver, filter);

        // Schedule repeated notifications every 2 hours
        scheduleRepeatingNotifications();
    }

    @Override
    protected void onDestroy() {
        // Unregister the broadcast receiver to avoid memory leaks
        unregisterReceiver(uvNotificationReceiver);
        super.onDestroy();
    }

    private boolean isValidInput(String input) {
        try {
            int number = Integer.parseInt(input);
            return number >= 1;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelName = "UV Index Notifications";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, importance);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    private void showUVNotification(int uvIndex) {
        String notificationMessage;
        switch (uvIndex) {
            case 1:
            case 2:
                notificationMessage = "Low risk of UV exposure, don't forget to wear sunscreen.";
                break;
            case 3:
            case 4:
            case 5:
                notificationMessage = "Moderate risk of UV exposure. Please wear sunscreen.";
                break;
            case 6:
            case 7:
                notificationMessage = "High risk of skin damage. Wear sunscreen and seek shade.";
                break;
            case 8:
            case 9:
            case 10:
                notificationMessage = "Very High Risk! Wear sunscreen, seek shade or stay indoors.";
                break;
            default:
                notificationMessage = "Extreme Risk! Stay indoors. If not possible then wear protective clothing, sunscreen and sunglasses, and seek shade.";
                break;
        }

        Notification notification = createNotification(notificationMessage);
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(NOTIFICATION_ID, notification);
        }
    }

    private Notification createNotification(String contentText) {
        Intent notificationIntent = new Intent(this, SessionActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                NOTIFICATION_ID,
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        return new Notification.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setContentTitle("UV Index Alert")
                .setContentText(contentText)
                .setSmallIcon(R.mipmap.ic_launcher) // Set an appropriate app icon here
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build();
    }

    private void scheduleRepeatingNotifications() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent notificationIntent = new Intent(this, UVNotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                NOTIFICATION_ID,
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Schedule repeating notifications every 2 hours
        // can be modify for the demo for 5000 milliseconds
        long repeatInterval = 2 * 60 * 60 * 1000; // 2 hours in milliseconds,
        if (alarmManager != null) {
            alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    System.currentTimeMillis() + repeatInterval,
                    repeatInterval,
                    pendingIntent
            );
        }

    }

    protected void toEditActivity(){
        Intent intent = new Intent(this, EditActivity.class);
        startActivity(intent);
    }

    public void startSession(Preset preset){
        Toast.makeText(this, "I got called from fragment: " + preset.getName(), Toast.LENGTH_SHORT).show();
    }
}
