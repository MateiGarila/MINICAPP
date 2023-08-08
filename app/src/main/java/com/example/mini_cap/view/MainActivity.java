package com.example.mini_cap.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.mini_cap.R;
import com.example.mini_cap.controller.NotificationController;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import app.uvtracker.sensor.pii.event.IEventListener;

public class MainActivity extends AppCompatActivity implements INavigationBar, BottomNavigationView.OnItemSelectedListener, IEventListener {

    private static final String DEFAULT_CITY = "Montreal";

    private static final String API_KEY = "d868a85293a44ef8bb5184707230108";
    private static final int SETTINGS_REQUEST_CODE = 1;

    private TextView cityNameTextView;
    private TextView temperatureTextView;
    private TextView uvIndexTextView;
    private TextView weatherConditionTextView;
    private ImageView weatherTextView;
    private ImageView refreshIcon;

    private String currentlySelectedCity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Navigation bar UI component initialization
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnItemSelectedListener(this);

        // Weather UI component initialization
        this.cityNameTextView = findViewById(R.id.cityName);
        this.temperatureTextView = findViewById(R.id.temp);
        this.uvIndexTextView = findViewById(R.id.uvIndex);
        this.weatherTextView = findViewById(R.id.currentWeather);
        this.weatherConditionTextView = findViewById(R.id.condition);

        this.refreshIcon = findViewById(R.id.refresh);
        this.refreshIcon.setOnClickListener(v -> this.refreshWeatherDisplay());

        // Notification initialization
        this.initializeNotificationServices();

        // Refresh UI
        this.refreshWeatherDisplay(DEFAULT_CITY);
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
        if(requestCode != SETTINGS_REQUEST_CODE) return;
        if(resultCode != Activity.RESULT_OK) return;
        String city = data.getStringExtra("CITY_NAME");
        if(city == null) return;
        this.refreshWeatherDisplay(city);
    }

    // Render
    private void refreshWeatherDisplay() {
        this.refreshWeatherDisplay(this.currentlySelectedCity);
    }

    // Render
    private void refreshWeatherDisplay(String city){
        String requestURL = "https://api.weatherapi.com/v1/current.json?key=" + API_KEY + "&q=" + city + "&aqi=no";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                requestURL,
                null,
                this::handleWeatherAPIResponse,
                this::handleWeatherAPIException
        );
        Volley.newRequestQueue(MainActivity.this).add(jsonObjectRequest);
    }

    private void handleWeatherAPIResponse(JSONObject response) {
        try {
            String temperature = response.getJSONObject("current").getString("temp_c");
            String condition = response.getJSONObject("current").getJSONObject("condition").getString("text");
            String uv = response.getJSONObject("current").getString("uv");
            String conditionIcon = response.getJSONObject("current").getJSONObject("condition").getString("icon");
            String city = response.getJSONObject("location").getString("name");

            this.temperatureTextView.setText(temperature + "°C");
            this.weatherConditionTextView.setText(condition);
            this.uvIndexTextView.setText("UV index: " + uv);
            Picasso.get().load("https:".concat(conditionIcon)).into(this.weatherTextView);

            String oldCity = this.currentlySelectedCity;
            this.currentlySelectedCity = city;
            this.cityNameTextView.setText(city);

            if(oldCity == null) return;

            String message;
            if(oldCity.equalsIgnoreCase(city)) message = "Weather refreshed.";
            else message = "Weather updated.";
            Snackbar.make(this.refreshIcon, message, Snackbar.LENGTH_SHORT).show();
        }
        catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private void handleWeatherAPIException(VolleyError errorIgnored) {
        Snackbar.make(this.refreshIcon, "City name is not valid.", Snackbar.LENGTH_SHORT).show();
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

