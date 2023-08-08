package com.example.mini_cap.view;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mini_cap.R;
import com.example.mini_cap.controller.DBHelper;
import com.example.mini_cap.controller.NotificationController;
import com.example.mini_cap.model.Day;
import com.example.mini_cap.model.Preset;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class SessionActivity extends AppCompatActivity  {

    //Declaration of all UI elements
    protected TextView mainTextView, statusTextView, notificationTierTextView,
            timeRemainingTextView, timerTextView;
    protected Button startPauseBTN, addPresetBTN, editPresetBTN, endSessionBTN;

    //Needed
    private DBHelper dbHelper;
    private final static String TAG = "SessionActivity";
    private static Bundle bundle;
    private final boolean isCreate = true;
    private boolean isDemo = false;
    private static final String NOTIFICATION_CHANNEL_ID = "UV_INDEX_NOTIFICATION_CHANNEL";
    private static final int MAX_TIER = 5;

    //Countdown timer variables
    //For Testing purposes. This is 10 seconds
    private static final long START_TIME_IN_MILLIS = 10000;
    private static final long DEFAULT_TIMER_IN_MILLIS = 1050000;
    private static long calculatedTimer;
    private CountDownTimer countDownTimer;
    private long timeLeftInMillis = START_TIME_IN_MILLIS;

    private int notificationTier;
    private boolean isSessionStarted;
    private boolean isSessionPaused;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session);

        // Initialize local bundle
        if(SessionActivity.bundle == null)
            SessionActivity.bundle = new Bundle();

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
                Intent intent = new Intent(getBaseContext(), EditActivity.class);
                startActivity(intent);
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

        //The singleton is not yet complete, still has bugs

        // TODO: properly handle state storage
        //SessionActivityStorage.get().loadActivityState(SessionActivity.this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //this.saveUI(SessionActivity.bundle);
        SessionActivityStorage.get().saveActivityState(SessionActivity.this);
        if(countDownTimer != null) countDownTimer.cancel();
    }

    private boolean isValidInput(String input) {
        try {
            int number = Integer.parseInt(input);
            return number >= 1;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * This is the method which receives the selected preset to start the session, which then calls
     * the startSession() method; starting the session
     * @param preset the selected preset
     */
    public void fetchPresetStartSession(Preset preset){

        //Toast.makeText(this, "I got called from fragment: " + preset.getName(), Toast.LENGTH_SHORT).show();
        startSession(preset, 0);
    }

    /**
     * This method handles what happens at the start of a session
     * @param preset that has been selected for the session
     */
    @SuppressLint("SetTextI18n")
    private void startSession(Preset preset, long timeInMillis){

        //Toast.makeText(this, "Session Started", Toast.LENGTH_SHORT).show();

        //First update UI
        statusTextView.setText("Current session with preset: " + preset.getName());
        updateTier();
        startPauseBTN.setText(R.string.pause_session_text);
        endSessionBTN.setVisibility(View.VISIBLE);

        //Second update variables
        isSessionStarted = true;
        SessionActivityStorage.get().setLatestPreset(preset);

        //Third purpose of method
        //For demo purposes (timer will be set to 10 seconds)
        if(timeInMillis == 0){

            if(preset.getName().equalsIgnoreCase("Demo")){
                timeLeftInMillis = START_TIME_IN_MILLIS;
                isDemo = true;
            }else{
                calculatedTimer = timerDurationAlgo(ageParameter(preset.getAge()),
                        skinToneParameter(preset.getSkinTone()), uvParameter());
                timeLeftInMillis = calculatedTimer;
            }

        }else{
            timeInMillis = timeLeftInMillis;
        }


        countDownManager(timeLeftInMillis);

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
        if(countDownTimer != null) countDownTimer.cancel();

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
        countDownManager(timeLeftInMillis);

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
        if(countDownTimer != null) countDownTimer.cancel();
        timerTextView.setText(R.string.default_clock);
    }

    /**
     * This method handles the display timer on SessionActivity
     */
    private void updateCountDownText(){

        int minutes = (int) (timeLeftInMillis / 1000) / 60;
        int seconds = (int) (timeLeftInMillis / 1000) % 60;

        String timeLeftFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes,
                seconds);

        timerTextView.setText(timeLeftFormatted);
    }

    /**
     * This method handles notification tiers. Depending on the tier a different text is applied to
     * the notification
     */
    @SuppressLint("SetTextI18n")
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
    private void countDownManager(long timeInMillis){
        if(timeInMillis == 0) return;

        countDownTimer = new CountDownTimer(timeInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                updateCountDownText();
            }

            @Override
            public void onFinish() {

                String notificationMessage = notificationMessage(notificationTier);
                //Toast.makeText(getBaseContext(), notificationMessage, Toast.LENGTH_SHORT).show();
                //Notifications go here
                //-->
                
                if(isDemo){
                    timeLeftInMillis = START_TIME_IN_MILLIS;
                }else{
                    timeLeftInMillis = calculatedTimer;
                }


                NotificationController.get(SessionActivity.this).postNotification(notificationMessage);

                countDownManager(timeLeftInMillis);
            }

        }.start();

    }

    /**
     * Based on the notification tier this method returns the message that will be displayed in the
     * notification
     * @param notificationTier the current tier of notification (how many times the timer has
     *                         completed it's run
     * @return the string to be displayed in the notification
     */
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
                Toast.makeText(this, "An error has happened in notificationMessage",
                        Toast.LENGTH_SHORT).show();
                break;
        }

        updateTier();

        return message;
    }

    /**
     * Based on the selected preset, the age will be used in the algorithm to determine the timer
     * for that preset
     * @param age the age from the preset
     * @return age parameter to be used in the algorithm
     */
    private double ageParameter(int age){

        double ageParam = 0.0;

        if(age < 13){

            ageParam = -2.0;

        }else if(age < 16){

            ageParam = -1.5;

        }else if(age < 19){

            ageParam = -1.0;

        }else if(age < 22){

            ageParam = -0.5;

        }else if(age < 25){

            ageParam = 0;

        }else if(age < 28){

            ageParam = 0.5;

        }else if(age < 59){

            ageParam = 1.0;

        }else if(age < 62){

            ageParam = 0.5;

        }else if(age < 65){

            ageParam = 0.0;

        }else if(age < 68){

            ageParam = -0.5;

        }else if(age < 71){

            ageParam = -1.0;

        }else if(age < 74){

            ageParam = -1.5;

        }else{

            ageParam = -2.0;

        }

        return ageParam;
    }

    private double skinToneParameter(String skinTone){

        double skinParam = 0.0;

        if(skinTone.equalsIgnoreCase("Light, pale white")){
            skinParam = -2.0;
        }else if(skinTone.equalsIgnoreCase("White, fair")){
            skinParam = -1.5;
        }else if(skinTone.equalsIgnoreCase("Medium white to light brown")){
            skinParam = -1.0;
        }else if(skinTone.equalsIgnoreCase("Olive, moderate brown")){
            skinParam = -0.5;
        }else if(skinTone.equalsIgnoreCase("Brown, dark brown")){
            skinParam = 0.0;
        }else if(skinTone.equalsIgnoreCase("Very dark brown to black")){
            skinParam = 1.0;
        }else {
            skinParam = 0.0;
        }

        return skinParam;
    }

    /**
     * This method calculates the parameter obtained from the average UV index over a minute
     * @return the multiplier parameter used in the timer algorithm
     */
    private double uvParameter(){

        DBHelper dbHelper = DBHelper.get(this);
        Date currentDate = new Date();
        Calendar calendar = Calendar.getInstance();
        TimeZone timeZone = calendar.getTimeZone();

        int hours = calendar.get(Calendar.HOUR_OF_DAY);
        int minutes = calendar.get(Calendar.MINUTE);

        int avgUVIndex = (int) dbHelper.getMinuteAvg(new Day(currentDate), minutes, hours, false);
        double uvParam = 0.0;

        if(avgUVIndex == 0){
            return 1;
        }else if(avgUVIndex < 2){
            uvParam = 0.5;
        } else if(avgUVIndex < 4){
            uvParam = 0.75;
        }else if(avgUVIndex < 6){
            uvParam = 1.0;
        } else if(avgUVIndex < 8){
            uvParam = 1.5;
        }else if(avgUVIndex < 10){
            uvParam = 1.75;
        } else if(avgUVIndex < 12){
            uvParam = 2.0;
        }

        return uvParam;
    }

    /**
     * This method determines the duration of the timer based off 3 parameters
     * @param age parameter, obtained from the preset
     * @param skin parameter, obtained from the preset
     * @param uvIndex parameter, obtained from the minute average
     * @return timer duration
     */
    public long timerDurationAlgo(double age, double skin, double uvIndex){

        long time = 0;
        double presetParam = age + skin;

        if(presetParam == 0.0){
            return DEFAULT_TIMER_IN_MILLIS;
        }

        double timerSelector = presetParam * uvIndex;

        if(timerSelector <= -6){
            //this is 15 minutes
            time = 900000;
        }else if(timerSelector <= -4){
            //this is 16 minutes
            time = 960000;
        }else if(timerSelector <= -2){
            //this is 17 minutes
            time = 1020000;
        }else if(timerSelector <= 0){
            //this is 17 minutes 30 seconds
            time = DEFAULT_TIMER_IN_MILLIS;
        }else if(timerSelector <= 2){
            //this is 18 minutes
            time = 1080000;
        }else{
            //this is 19 minutes
            time = 1140000;
        }

        return time;
    }

    private static class SessionActivityStorage {

        private static SessionActivityStorage instance;

        @NonNull
        private static SessionActivityStorage get() {
            if(SessionActivityStorage.instance == null)
                SessionActivityStorage.instance = new SessionActivityStorage();
            return SessionActivityStorage.instance;
        }

        private SessionActivityStorage() {

        }

        @Nullable
        private Preset latestPreset;
        @Nullable
        private Boolean isSessionStarted;
        @Nullable
        private Boolean isSessionPaused;
        @Nullable
        private Boolean isDemo;
        @Nullable
        private int notificationTier;
        @Nullable
        private long timeLeftInMillis;


        private void setLatestPreset(Preset latestPreset) {
            this.latestPreset = latestPreset;
        }

        @Nullable
        private Preset getLatestPreset() {
            return this.latestPreset;
        }

        // Will be called: onDestroy()
        private void saveActivityState(@NonNull SessionActivity activity) {
            //this.presetButtonSavedText = activity.addPresetBTN.getText().toString();
            this.isSessionStarted = activity.isSessionStarted;
            this.isSessionPaused = activity.isSessionPaused;
            this.isDemo = activity.isDemo;
            this.notificationTier = activity.notificationTier;
            this.timeLeftInMillis = activity.timeLeftInMillis;

        }

        // Will be called: onCreate()
        private void loadActivityState(@NonNull SessionActivity activity) {

            if(this.isSessionStarted != null && this.isSessionStarted == true){

                //activity.countDownManager(this.timeLeftInMillis);
                activity.statusTextView.setText("Current session with preset: " + latestPreset.getName());
                activity.notificationTierTextView.setText("Current notification tier: " + notificationTier);
                //activity.updateCountDownText();
                activity.startPauseBTN.setText(R.string.continue_session_text);
                activity.endSessionBTN.setVisibility(View.VISIBLE);
                Log.d(TAG, "TIME LEFT IN MILLI " + String.valueOf(this.timeLeftInMillis));
                activity.timeLeftInMillis = this.timeLeftInMillis;
                if(this.isSessionPaused != null && this.isSessionPaused == true){
                    activity.pauseSession();
                }else{
                    activity.continueSession();
                }

            }

        }

    }

    /**
     * This is NOT complete the timer NEEDS to be in it's own Singleton
     */
    private static class SessionTimerStorage{

        private static SessionTimerStorage instance;

        @NonNull
        private static SessionTimerStorage get() {
            if(SessionTimerStorage.instance == null)
                SessionTimerStorage.instance = new SessionTimerStorage();
            return SessionTimerStorage.instance;
        }

        private SessionTimerStorage() {

        }

    }
}


