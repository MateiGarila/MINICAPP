package com.example.mini_cap.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mini_cap.R;
import com.example.mini_cap.controller.DBHelper;
import com.example.mini_cap.controller.SensorController;
import com.google.android.material.snackbar.Snackbar;

import app.uvtracker.sensor.pii.event.EventHandler;
import app.uvtracker.sensor.pii.event.IEventListener;

public class SettingsActivity extends AppCompatActivity implements IEventListener {
    private EditText cityName;
    private ImageView search;
    protected Button connectBTN;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Initialize sensor controller
        SensorController.get(this).registerListenerClass(DBHelper.get(this));
        SensorController.get(this).registerListenerClass(this);

        //Attaching the UI elements to their respective objects
        cityName = findViewById(R.id.cityInput);
        search = findViewById(R.id.search);

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

        // Connect button - connection flow handling
        this.connectBTN = findViewById(R.id.connectBTN);
        this.updateConnectButton(SensorController.get(this).getStage());
        connectBTN.setOnClickListener((v) -> this.handleConnectButtonClick());
    }

    private void updateConnectButton(SensorController.ConnectionFlowStage stage) {
        switch(stage) {
            case DISCONNECTED:
                this.connectBTN.setText("Connect to sensor");
                break;
            case CONNECTING:
                this.connectBTN.setText("Connecting...");
                break;
            case CONNECTED:
                this.connectBTN.setText("Disconnect from sensor");
                break;
        }
    }

    private void handleConnectButtonClick() {
        SensorController controller = SensorController.get(this);
        if(controller.getStage() == SensorController.ConnectionFlowStage.DISCONNECTED) {
            controller.connectToAnySensor(this);
        }
        else if(controller.getStage() == SensorController.ConnectionFlowStage.CONNECTED) {
            controller.disconnectFromSensor();
        }
    }

    @EventHandler
    protected void onConnectionFlow(SensorController.ConnectionFlowEvent event) {
        this.updateConnectButton(event.getStage());
    }

    @EventHandler
    protected void onMessage(String message) {
        Snackbar.make(this.connectBTN, message, Snackbar.LENGTH_SHORT).show();
    }

}
