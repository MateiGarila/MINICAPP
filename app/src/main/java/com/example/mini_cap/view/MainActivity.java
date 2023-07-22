package com.example.mini_cap.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import android.widget.Button;
import android.widget.TextView;

import com.example.mini_cap.R;
import com.example.mini_cap.controller.SensorController;
import com.example.mini_cap.view.helper.IntentDataHelper;

import app.uvtracker.sensor.pii.event.EventHandler;
import app.uvtracker.sensor.pii.event.IEventListener;

public class MainActivity extends AppCompatActivity implements IEventListener {

    //Declaration of all UI elements
    protected TextView mainView;
    protected Button sessionActivity, statsActivity, connectBTN;
    // Sensor controller
    private SensorController sensorController;
    private boolean isConnecting = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.sensorController = new SensorController(this);

        //Attaching the UI elements to their respective objects
        mainView = findViewById(R.id.mainTextView);
        sessionActivity = findViewById(R.id.toSessionActivity);
        statsActivity = findViewById(R.id.toStatsActivity);
        connectBTN = findViewById(R.id.connectBTN);

        sessionActivity.setOnClickListener(v -> toSessionActivity());
        statsActivity.setOnClickListener(v -> toStatsActivity());
        this.connectBTN.setText("Connect to sensor");
        connectBTN.setOnClickListener(v -> onConnectionButtonClicked());
    }

    private void toSessionActivity(){
        Intent intent = new Intent(this, SessionActivity.class);
        startActivity(intent);
    }

    private void toStatsActivity(){
        Intent intent = new Intent(this, StatsActivity.class);
        IntentDataHelper.writeSensorController(this.sensorController);
        startActivity(intent);
    }

    protected void onConnectionButtonClicked() {
        boolean flag;
        if(!this.isConnecting)
            flag = this.sensorController.connectToAnySensor();
        else
            flag = this.sensorController.disconnectFromSensor();
        if(flag) this.isConnecting = !this.isConnecting;
    }

    @EventHandler
    protected void onSensorConnected(@NonNull SensorController.SensorConnectedEvent event) {
        this.connectBTN.setText("Disconnect from sensor");
    }

    @EventHandler
    protected void onSensorDisconnected(@NonNull SensorController.SensorDisconnectedEvent event) {
        this.connectBTN.setText("Reconnect to sensor");
    }

}

