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

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.mini_cap.R;

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
    protected TextView sensorView;
    protected Button connectBTN;
    private IScanner iScanner;
    private ISensor iSensor;
    private final int ENABLE_BLUETOOTH_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        //Attaching the UI elements to their respective objects
        //mainView = findViewById(R.id.mainTextView);
        connectBTN = findViewById(R.id.connectBTN);
        cityName = findViewById(R.id.cityInput);
        search = findViewById(R.id.search);


        if(!hasRequiredRuntimePermissions())
            requestRunTimePermissions();

        if(this.iScanner == null) {
            try {
                this.iScanner = SensorAPI.getScanner(this);
            } catch (TransceiverException e) {
                throw new RuntimeException(e);
            }
        }

        this.iScanner.registerListener(this);

        connectBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    iScanner.startScanning();
                    (new Handler(Looper.getMainLooper()))
                            .postDelayed(() -> {
                                try {
                                    iScanner.stopScanning();
                                } catch (TransceiverException ignored) {

                                }
                            }, 10000);
                } catch (TransceiverException e) {
                    Toast.makeText(SettingsActivity.this, "Sensor oopsie", Toast.LENGTH_SHORT).show();
                }
            }
        });

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


    @EventHandler
    public void onScanUpdate(SensorScannedEvent event) {
        try {
            iScanner.stopScanning();
        } catch (TransceiverException e) {
            Toast.makeText(SettingsActivity.this, "Sensor oopsie", Toast.LENGTH_SHORT).show();
        }
        if(iSensor != null) return;
        iSensor = event.getSensor();
        iSensor.getConnection().registerListener(this);
        iSensor.getConnection().connect();
        Toast.makeText(SettingsActivity.this, "Connecting..", Toast.LENGTH_SHORT).show();
    }

    @EventHandler
    public void onSensorConnectionStatusUpdate(ConnectionStateChangeEvent event) {
        if(event.getStage() != ConnectionStateChangeEvent.State.ESTABLISHED) return;
        Toast.makeText(SettingsActivity.this, "Connection established", Toast.LENGTH_SHORT).show();
    }

    @EventHandler
    public void onNewSample(NewSampleReceivedEvent event) {
        OpticalRecord record = event.getRecord();
        float illuminance = record.illuminance;
        float uvIndex = record.uvIndex;
        sensorView.setText(String.format("Light: %1$.1f, UVI: %2$.2f", illuminance, uvIndex));
    }

    public boolean hasPermission(String permissionType) {
        return ContextCompat.checkSelfPermission(this, permissionType) ==
                PackageManager.PERMISSION_GRANTED;
    }

    public boolean hasRequiredRuntimePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return hasPermission(Manifest.permission.BLUETOOTH_SCAN) &&
                    hasPermission(Manifest.permission.BLUETOOTH_CONNECT);
        } else {
            return hasPermission(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    public void requestRunTimePermissions(){

        String[] permission;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permission = new String[] {
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT
            };
        } else {
            permission = new String[] {
                    Manifest.permission.ACCESS_FINE_LOCATION
            };
        }
        ActivityCompat.requestPermissions(this, permission, 1);

    }

}
