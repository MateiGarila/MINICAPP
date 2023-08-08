package com.example.mini_cap.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
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
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.squareup.picasso.Picasso;

import org.json.JSONException;



public class MainActivity extends AppCompatActivity implements INavigationBar, BottomNavigationView.OnItemSelectedListener {

    //Declaration of all UI elements

    private TextView cityNameTV, temp, uvIndex, conditionTV;
    private ImageView currentWeather, refresh;
    private String currentCity = "Montreal";
    private static final int SETTINGS_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Attaching the UI elements to their respective objects

        cityNameTV = findViewById(R.id.cityName);
        temp = findViewById(R.id.temp);
        uvIndex = findViewById(R.id.uvIndex);
        currentWeather = findViewById(R.id.currentWeather);
        conditionTV = findViewById(R.id.condition);
        refresh = findViewById(R.id.refresh);

        getWeatherInfo(currentCity);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnItemSelectedListener(this);

        refresh.setOnClickListener(v -> {
            getWeatherInfo(currentCity);
            Snackbar.make(refresh, "Weather refreshed.", Snackbar.LENGTH_SHORT).show();
        });


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
                    currentCity = city;
                    getWeatherInfo(city);
                }
            }
        }
    }

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

    private void toSessionActivity(){
        Intent intent = new Intent(this, SessionActivity.class);
        startActivity(intent);
    }

    private void toStatsActivity(){
        Intent intent = new Intent(this, StatsActivity.class);
        startActivity(intent);
    }


    private void toSettingsActivity(){
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivityForResult(intent, SETTINGS_REQUEST_CODE);
    }

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
                String cityName = response.getJSONObject("location").getString("name");
                cityNameTV.setText(cityName);

            } catch (JSONException e) {
                throw new RuntimeException(e);
            }

        }, error -> Toast.makeText(MainActivity.this, "Please enter valid city name", Toast.LENGTH_SHORT).show());
        requestQueue.add(jsonObjectRequest);
    }

}

