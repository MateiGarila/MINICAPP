package com.example.mini_cap.view;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.mini_cap.R;
import com.example.mini_cap.controller.NotificationController;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.squareup.picasso.Picasso;

import org.json.JSONException;

import app.uvtracker.sensor.pii.event.IEventListener;

public class MainActivity extends AppCompatActivity implements INavigationBar, BottomNavigationView.OnItemSelectedListener, IEventListener {

    private static final String DEFAULT_CITY = "Montreal";

    private static final int SETTINGS_REQUEST_CODE = 1;

    // Main activity UI components
    private TextView cityNameTV, temp, uvIndex, conditionTV;
    private ImageView currentWeather;


/*
    private BroadcastReceiver uvNotificationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            checkAndSendUVNotification();
        }
    };
*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Navigation bar UI component initialization
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnItemSelectedListener(this);

        // Weather UI component initialization
        cityNameTV = findViewById(R.id.cityName);
        temp = findViewById(R.id.temp);
        uvIndex = findViewById(R.id.uvIndex);
        currentWeather = findViewById(R.id.currentWeather);
        conditionTV = findViewById(R.id.condition);
        getWeatherInfo(DEFAULT_CITY);

        // Notification initialization
        this.initializeNotificationServices();

/*
        createNotificationChannel();
        registerReceiver(uvNotificationReceiver, new IntentFilter("UV_NOTIFICATION_ACTION"));

        // Start the UV index service for offline notification
        startService(new Intent(this, UVIndexService.class));

        // Schedule repeated notifications every 2 hours
        scheduleRepeatingNotifications();

 */
    }


    /* -------- Navigation bar component -------- */

    // Event handler
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.button_stats) {
            toStatsActivity();
            return true;
        } else if (itemId == R.id.button_presets) {
            toSessionActivity();
            return true;
        } else if (itemId == R.id.button_settings) {
            toSettingsActivity();
            return true;
        }
        return false;
    }

    // Helper
    private void toSessionActivity(){
        Intent intent = new Intent(this, SessionActivity.class);
        startActivity(intent);
    }

    // Helper
    private void toStatsActivity(){
        Intent intent = new Intent(this, StatsActivity.class);
        startActivity(intent);
    }

    // Helper
    private void toSettingsActivity(){
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivityForResult(intent, SETTINGS_REQUEST_CODE);
    }


    /* -------- Weather component --------*/

    // Render
    private void getWeatherInfo(String city){
        String url = "https://api.weatherapi.com/v1/current.json?key=d868a85293a44ef8bb5184707230108&q=" + city + "&aqi=no";
        Log.d("WeatherAPI", "URL: " + url); // Print URL to logcat
        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, response -> {
            Log.d("WeatherAPI", "API Response: " + response.toString());
            try{
                String temperature = response.getJSONObject("current").getString("temp_c");
                temp.setText(temperature+"°C");
                String condition = response.getJSONObject("current").getJSONObject("condition").getString("text");
                conditionTV.setText(condition);
                String conditionIcon = response.getJSONObject("current").getJSONObject("condition").getString("icon");
                Picasso.get().load("https:".concat(conditionIcon)).into(currentWeather);
                String uv = response.getJSONObject("current").getString("uv");
                uvIndex.setText("UV index: "+uv);

            } catch (JSONException e) {
                throw new RuntimeException(e);
            }

        }, error -> Toast.makeText(MainActivity.this, "Please enter valid city name", Toast.LENGTH_SHORT).show());
        requestQueue.add(jsonObjectRequest);
    }


    /* -------- Notifications --------*/

    private void initializeNotificationServices() {
        NotificationController.NotificationPublisher publisher = new NotificationController.NotificationPublisher(this);

        NotificationController controller = NotificationController.get(this);

        controller.registerGenericNotificationCallback(controller::postNotification);

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
            publisher.displayNotification(notificationMessage);
        });

        controller.registerReminderCallback(() -> {
            String message = "Please apply sunscreen.";
            publisher.displayNotification(message);
        });

        controller.start();
    }


/*

    private void checkAndSendUVNotification() {
        // Get the current time
        DBHelper dbHelper = DBHelper.get(this);
        // Date currentDate = new Date();
        Calendar calendar = Calendar.getInstance();
        TimeZone timeZone = calendar.getTimeZone();

        int hours = calendar.get(Calendar.HOUR_OF_DAY);
        int minutes = calendar.get(Calendar.MINUTE);

        // Fetch the current UV index from DBHelper
        float currentUVIndex = dbHelper.getMinuteAvg(new Day(calendar.getTime()), minutes, hours, false);
        //float currentUVIndex = (int) dbHelper.getMinuteAvg(new Day(currentDate), minutes, hours, false);
        long currentTime = System.currentTimeMillis();

        // Show the UV notification based on the fetched UV index
        showUVNotification(currentUVIndex);
    }
    private void showUVNotification(float currentUVIndex) {
        String notificationMessage;
        if (currentUVIndex <= 2) {
            notificationMessage = "Low risk of UV exposure, don't forget to wear sunscreen.";
        } else if (currentUVIndex <= 5) {
            notificationMessage = "Moderate risk of UV exposure. Please wear sunscreen.";
        } else if (currentUVIndex <= 7) {
            notificationMessage = "High risk of skin damage. Wear sunscreen and seek shade.";
        } else if (currentUVIndex <= 10) {
            notificationMessage = "Very High Risk! Wear sunscreen, seek shade or stay indoors.";
        } else {
            notificationMessage = "Extreme Risk! Stay indoors. If not possible then wear protective clothing, sunscreen and sunglasses, and seek shade.";
        }

        Notification notification = new Notification.Builder(this, UV_INDEX_NOTIFICATION_CHANNEL_ID)
                .setContentTitle("UV Index Alert")
                .setContentText(notificationMessage)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setAutoCancel(true)
                .build();

        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        if (notificationManager != null) {
            notificationManager.notify(UV_INDEX_NOTIFICATION_ID, notification);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                // Retrieve the city name from the SettingsActivity
                String city = data.getStringExtra("CITY_NAME");
                if (city != null) {
                    // Update the cityNameTV text view with the city name
                    cityNameTV.setText(city);
                    getWeatherInfo(city);
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(uvNotificationReceiver);
        super.onDestroy();
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
        long repeatInterval = 10 * 1000; // 10sec in milliseconds for the demo,
        if (alarmManager != null) {
            alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    System.currentTimeMillis() + repeatInterval,
                    repeatInterval,
                    pendingIntent
            );
        }
    }

     */


}

