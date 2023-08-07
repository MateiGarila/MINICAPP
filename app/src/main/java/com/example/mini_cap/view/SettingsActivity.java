package com.example.mini_cap.view;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.mini_cap.R;
import com.example.mini_cap.controller.SensorController;
import com.example.mini_cap.view.helper.IntentDataHelper;

import app.uvtracker.data.optical.OpticalRecord;
import app.uvtracker.sensor.SensorAPI;
import app.uvtracker.sensor.pii.ISensor;
import app.uvtracker.sensor.pii.connection.application.event.NewSampleReceivedEvent;
import app.uvtracker.sensor.pii.connection.shared.event.ConnectionStateChangeEvent;
import app.uvtracker.sensor.pii.event.EventHandler;
import app.uvtracker.sensor.pii.event.IEventListener;
import app.uvtracker.sensor.pii.scanner.IScanner;
import app.uvtracker.sensor.pii.scanner.event.SensorScannedEvent;
import app.uvtracker.sensor.pii.scanner.exception.TransceiverException;

public class SettingsActivity extends AppCompatActivity implements IEventListener {
    private EditText cityName;
    private ImageView search;
    protected Button connectBTN;
    private IScanner iScanner;
    private ISensor iSensor;

    private SensorController sensorController;
    private Handler handler = new Handler();
    private boolean isSensorConnected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        this.sensorController = new SensorController(this);
        IntentDataHelper.writeSensorController(this.sensorController);

        //Attaching the UI elements to their respective objects
        //mainView = findViewById(R.id.mainTextView);
        connectBTN = findViewById(R.id.connectBTN);
        cityName = findViewById(R.id.cityInput);
        search = findViewById(R.id.search);
        
        connectBTN.setOnClickListener((v) -> sensorUpdate());

        search.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                String city = cityName.getText().toString();
                if (city.isEmpty()){
                    Toast.makeText(SettingsActivity.this, "Please enter a city name", Toast.LENGTH_SHORT ).show();
                }else{
                    // Send city name back to MainActivity
                    Intent intent = new Intent();
                    intent.putExtra("CITY_NAME", city);
                    setResult(Activity.RESULT_OK, intent);
                    finish();
                }
            }
        });

    }

    private void sensorUpdate(){

        if(isSensorConnected){

            //sensorController.disconnectFromSensor();
            connectBTN.setText(R.string.connect_sensor);

        }else{

            //sensorController.connectToAnySensor();
            connectBTN.setEnabled(false);

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {

                    connectBTN.setText(R.string.disconnect_sensor);
                    connectBTN.setEnabled(true);
                }
            },3000);



        }
    }

}
