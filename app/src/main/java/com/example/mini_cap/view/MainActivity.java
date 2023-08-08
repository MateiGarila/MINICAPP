package com.example.mini_cap.view;

import android.app.Activity;
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
import com.google.android.material.snackbar.Snackbar;
import com.squareup.picasso.Picasso;

import org.json.JSONException;

import app.uvtracker.sensor.pii.event.IEventListener;

public class MainActivity extends AppCompatActivity implements INavigationBar, BottomNavigationView.OnItemSelectedListener, IEventListener {

    private static final String DEFAULT_CITY = "Montreal";

    private static final int SETTINGS_REQUEST_CODE = 1;

    private TextView cityNameTextView;
    private TextView temperatureTextView;
    private TextView uvIndexTextView;
    private TextView weatherConditionTextView;
    private ImageView weatherTextView;

    private String currentlySelectedCity;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Navigation bar UI component initialization
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnItemSelectedListener(this);

        // Weather UI component initialization
        // Main activity UI components
        cityNameTextView = findViewById(R.id.cityName);
        temperatureTextView = findViewById(R.id.temp);
        uvIndexTextView = findViewById(R.id.uvIndex);
        weatherTextView = findViewById(R.id.currentWeather);
        weatherConditionTextView = findViewById(R.id.condition);

        this.currentlySelectedCity = DEFAULT_CITY;
        getWeatherInfo(DEFAULT_CITY);

        ImageView refresh = findViewById(R.id.refresh);
        refresh.setOnClickListener(v -> {
            getWeatherInfo(currentlySelectedCity);
            Snackbar.make(refresh, "Weather refreshed.", Snackbar.LENGTH_SHORT).show();
        });

        // Notification initialization
        this.initializeNotificationServices();

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

    // Event handler
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                // Retrieve the city name from the SettingsActivity
                String city = data.getStringExtra("CITY_NAME");
                if (city != null) {
                    // Update the cityNameTV text view with the city name
                    currentlySelectedCity = city;
                    cityNameTextView.setText(city); 
                    getWeatherInfo(city);
                }
            }
        }
    }


    // Render
    private void getWeatherInfo(String city){
        String url = "https://api.weatherapi.com/v1/current.json?key=d868a85293a44ef8bb5184707230108&q=" + city + "&aqi=no";
        Log.d("WeatherAPI", "URL: " + url); // Print URL to logcat
        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, response -> {
            Log.d("WeatherAPI", "API Response: " + response.toString());
            try{
                String temperature = response.getJSONObject("current").getString("temp_c");
                temperatureTextView.setText(temperature+"°C");
                String condition = response.getJSONObject("current").getJSONObject("condition").getString("text");
                weatherConditionTextView.setText(condition);
                String conditionIcon = response.getJSONObject("current").getJSONObject("condition").getString("icon");
                Picasso.get().load("https:".concat(conditionIcon)).into(weatherTextView);
                String uv = response.getJSONObject("current").getString("uv");
                uvIndexTextView.setText("UV index: "+uv);

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

}

