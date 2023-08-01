package com.example.mini_cap.view;

import androidx.appcompat.app.AppCompatActivity;

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
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mini_cap.R;
import com.example.mini_cap.controller.DBHelper;
import com.example.mini_cap.model.Preset;

import java.util.Locale;

public class SessionActivity extends AppCompatActivity  {
    private EditText userInputEditText; // to simulate notification
    private Button NotificationButton;// to simulate notification

    //Declaration of all UI elements
    protected TextView mainTextView, statusTextView, notificationTierTextView, timeRemainingTextView, timerTextView;
    protected Button startPauseBTN, addPresetBTN, editPresetBTN, endSessionBTN;

    //Needed
    private DBHelper dbHelper;
    private final static String TAG = "SessionActivity";
    private final boolean isCreate = true;
    private static final String NOTIFICATION_CHANNEL_ID = "UV_INDEX_NOTIFICATION_CHANNEL";
    private static final int NOTIFICATION_ID = 1;
    private static final int MAX_TIER = 5;

    //Countdown timer variables
    //For Testing purposes
    private static final long START_TIME_IN_MILLIS = 10000;
    private CountDownTimer countDownTimer;
    private long timeLeftInMillis = START_TIME_IN_MILLIS;


    private int notificationTier;
    private boolean isSessionStarted;
    private boolean isSessionPaused;

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
        //TextViews
        mainTextView = findViewById(R.id.sessionActivityTextView);
        statusTextView = findViewById(R.id.sessionStatusTextView);
        notificationTierTextView = findViewById(R.id.notificationTierTV);
        timeRemainingTextView = findViewById(R.id.timeRemainingTV);
        timerTextView = findViewById(R.id.countdown);

        //Buttons
        startPauseBTN = findViewById(R.id.startPauseSessionBTN);
        addPresetBTN = findViewById(R.id.addPresetBTN);
        editPresetBTN = findViewById(R.id.editUserBTN);
        endSessionBTN = findViewById(R.id.endSessionBTN);

        // notification
        userInputEditText = findViewById(R.id.user_input_edit_text);
        NotificationButton = findViewById(R.id.SendNotification);

        //Tier 0 is the default tier of the notification
        notificationTier = 0;
        isSessionStarted = false;
        isSessionPaused = false;

        //Make end session button invisible when no session is started
        endSessionBTN.setVisibility(View.INVISIBLE);



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

        startPauseBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(isSessionStarted){
                    //Here handle what happens when the session is paused
                    if(isSessionPaused){
                        //if session is paused, then un-pause it
                        continueSession();
                    }else{
                        //else pause session
                        pauseSession();
                    }

                }else{
                    //Session not started need to select Preset first
                    StartSessionFragment fragment = new StartSessionFragment();
                    fragment.show(getSupportFragmentManager(), "StartSession");
                }

            }
        });

        endSessionBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //here handle what happens when a session ends
                endSession();

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

        if(uvIndex <= 2){
            notificationMessage = "Low risk of UV exposure, don't forget to wear sunscreen.";

        } else if(uvIndex > 2 && uvIndex <= 5) {
            notificationMessage = "Moderate risk of UV exposure. Please wear sunscreen.";

        } else if(uvIndex > 5 && uvIndex <= 7) {
            notificationMessage = "High risk of skin damage. Wear sunscreen and seek shade.";

        } else if(uvIndex > 7 && uvIndex <= 10) {
            notificationMessage = "Very High Risk! Wear sunscreen, seek shade or stay indoors.";
        } else {
            notificationMessage = "Extreme Risk! Stay indoors. If not possible then wear protective clothing, sunscreen and sunglasses, and seek shade.";
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

    public void fetchPresetStartSession(Preset preset){

        //Toast.makeText(this, "I got called from fragment: " + preset.getName(), Toast.LENGTH_SHORT).show();
        startSession(preset);

    }

    /**
     * This method handles what happens at the start of a session
     * @param preset that has been selected for the session
     */
    private void startSession(Preset preset){

        //Toast.makeText(this, "Session Started", Toast.LENGTH_SHORT).show();

        //First update UI
        statusTextView.setText("Current session with preset: " + preset.getName());
        updateTier();
        startPauseBTN.setText(R.string.pause_session_text);
        endSessionBTN.setVisibility(View.VISIBLE);

        //Second update variables
        isSessionStarted = true;

        //Third purpose of method
        countDownManager();

    }

    /**
     * This method handles what happens when a session is paused
     */
    private void pauseSession(){

        //Toast.makeText(this, "Session Paused", Toast.LENGTH_SHORT).show();

        //First update UI
        startPauseBTN.setText(R.string.continue_session_text);


        //Second update variables
        isSessionPaused = true;

        //Third purpose of method
        countDownTimer.cancel();

    }

    /**
     * This method handles what happens when a session is continued
     */
    private void continueSession(){

        //Toast.makeText(this, "Session Continued", Toast.LENGTH_SHORT).show();

        //First update UI
        startPauseBTN.setText(R.string.pause_session_text);

        //Second update variables
        isSessionPaused = false;

        //Third purpose of method
        countDownManager();

    }

    /**
     * This method handles what happens when a session ends
     */
    private void endSession(){

        //First update UI
        endSessionBTN.setVisibility(View.INVISIBLE);
        startPauseBTN.setText(R.string.start_session_text);
        statusTextView.setText(R.string.session_status_not_started);
        notificationTierTextView.setText(R.string.notification_tier);

        //Second update variables
        isSessionStarted = false;
        isSessionPaused = false;
        notificationTier = 0;

        //Third purpose of method
        countDownTimer.cancel();
        timerTextView.setText(R.string.default_clock);
    }

    /**
     * This method handles the display timer on SessionActivity
     */
    private void updateCountDownText(){

        int minutes = (int) (timeLeftInMillis / 1000) / 60;
        int seconds = (int) (timeLeftInMillis / 1000) % 60;

        String timeLeftFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);

        timerTextView.setText(timeLeftFormatted);
    }

    /**
     * This method handles notification tiers. Depending on the tier a different text is applied to the notification
     */
    private void updateTier(){

        notificationTier = notificationTier + 1;

        if(notificationTier > MAX_TIER){
            notificationTier = 1;
        }

        notificationTierTextView.setText("Current notification tier: " + notificationTier);

    }

    /**
     * This method manages the countdown. Since there is no countDownTimer.pause() each time the
     * timer is paused it needs to be reset at the timeLeftInMillis
     */
    private void countDownManager(){

        countDownTimer = new CountDownTimer(timeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                updateCountDownText();
            }

            @Override
            public void onFinish() {

                String notificationMessage = notificationMessage(notificationTier);
                Toast.makeText(getBaseContext(), notificationMessage, Toast.LENGTH_SHORT).show();
                //Notifications go here
                //-->
                timeLeftInMillis = START_TIME_IN_MILLIS;
                countDownManager();
            }

        }.start();

    }

    private String notificationMessage(int notificationTier){

        String message = "";

        switch (notificationTier){

            case 1:
                message = "Timer complete! Please hydrate yourself.";
                break;
            case 2:
                message = "Timer complete! Please apply sunscreen (re-apply every 2 hours)";
                break;
            case 3:
                message = "Timer complete! Please hydrate yourself";
                break;
            case 4:
                message = "Timer complete! Please take shelter from the sun for a timer's duration";
                break;
            case 5:
                message = "Timer complete! Enjoy the sun!";
                break;
            default:
                Toast.makeText(this, "An error has happened in notificationMessage", Toast.LENGTH_SHORT).show();
                break;
        }

        updateTier();

        return message;
    }
}
