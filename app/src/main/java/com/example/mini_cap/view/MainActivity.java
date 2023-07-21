package com.example.mini_cap.view;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import android.Manifest;
import com.example.mini_cap.R;

import app.uvtracker.data.type.Record;
import app.uvtracker.sensor.SensorAPI;
import app.uvtracker.sensor.pii.ISensor;
import app.uvtracker.sensor.pii.connection.application.event.NewSampleReceivedEvent;
import app.uvtracker.sensor.pii.connection.shared.event.ConnectionStateChangeEvent;
import app.uvtracker.sensor.pii.event.EventHandler;
import app.uvtracker.sensor.pii.event.IEventListener;
import app.uvtracker.sensor.pii.scanner.IScanner;
import app.uvtracker.sensor.pii.scanner.event.SensorScannedEvent;
import app.uvtracker.sensor.pii.scanner.exception.TransceiverException;

public class MainActivity extends AppCompatActivity implements IEventListener {

    //Declaration of all UI elements
    protected TextView mainView, sensorView;
    protected Button sessionActivity, statsActivity, connectBTN;
    private IScanner iScanner;
    private ISensor iSensor;
    private final int ENABLE_BLUETOOTH_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Attaching the UI elements to their respective objects
        mainView = findViewById(R.id.mainTextView);
        sessionActivity = findViewById(R.id.toSessionActivity);
        statsActivity = findViewById(R.id.toStatsActivity);
        connectBTN = findViewById(R.id.connectBTN);
        sensorView = findViewById(R.id.sensorTextView);

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

        sessionActivity.setOnClickListener(v -> toSessionActivity());
        statsActivity.setOnClickListener(v -> toStatsActivity());
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
                    Toast.makeText(MainActivity.this, "Sensor oopsie", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    @EventHandler
    public void onScanUpdate(SensorScannedEvent event) {
        try {
            iScanner.stopScanning();
        } catch (TransceiverException e) {
            Toast.makeText(MainActivity.this, "Sensor oopsie", Toast.LENGTH_SHORT).show();
        }
        if(iSensor != null) return;
        iSensor = event.getSensor();
        iSensor.getConnection().registerListener(this);
        iSensor.getConnection().connect();
        Toast.makeText(MainActivity.this, "Connecting..", Toast.LENGTH_SHORT).show();
    }

    @EventHandler
    public void onSensorConnectionStatusUpdate(ConnectionStateChangeEvent event) {
        if(event.getStage() != ConnectionStateChangeEvent.State.ESTABLISHED) return;
        Toast.makeText(MainActivity.this, "Connection established", Toast.LENGTH_SHORT).show();
    }

    @EventHandler
    public void onNewSample(NewSampleReceivedEvent event) {
        Record record = event.getRecord();
        float illuminance = record.illuminance;
        float uvIndex = record.uvIndex;
        sensorView.setText(String.format("Light: %1$.1f, UVI: %2$.2f", illuminance, uvIndex));
    }

    private void toSessionActivity(){
        Intent intent = new Intent(this, SessionActivity.class);
        startActivity(intent);
    }

    private void toStatsActivity(){
        Intent intent = new Intent(this, StatsActivity.class);
        startActivity(intent);
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