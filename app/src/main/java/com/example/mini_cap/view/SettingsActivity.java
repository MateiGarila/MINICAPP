package com.example.mini_cap.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mini_cap.R;
import com.example.mini_cap.controller.DBHelper;
import com.example.mini_cap.controller.NotificationController;
import com.example.mini_cap.controller.SensorController;
import com.google.android.material.snackbar.Snackbar;

import app.uvtracker.data.battery.BatteryRecord;
import app.uvtracker.sensor.pii.ISensor;
import app.uvtracker.sensor.pii.connection.application.event.NewBatteryInfoReceivedEvent;
import app.uvtracker.sensor.pii.connection.application.event.SyncProgressChangedEvent;
import app.uvtracker.sensor.pii.connection.shared.event.ConnectionStateChangeEvent;
import app.uvtracker.sensor.pii.event.EventHandler;
import app.uvtracker.sensor.pii.event.IEventListener;

public class SettingsActivity extends AppCompatActivity implements IEventListener {

    private static final String TAG = SettingsActivity.class.getSimpleName();

    // Persistent state workaround
    // TODO: make this more MVC
    private static Bundle bundle;

    // App configs
    private EditText cityNameText;
    private Switch notificationSwitch;

    // Sensor configs
    private TextView sensorStatusText;
    private TextView sensorBatteryText;
    private ProgressBar sensorProgressBar;
    private Button sensorConnectButton;

    // Sensor controller
    private SensorController sensorController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Initialize local bundle
        if(SettingsActivity.bundle == null)
            SettingsActivity.bundle = new Bundle();

        // Initialize sensor controller
        this.sensorController = SensorController.get(this);
        this.sensorController.registerListenerClass(DBHelper.get(this));
        this.sensorController.registerListenerClass(this);

        // Initialize UI - App configurations
        this.cityNameText = this.findViewById(R.id.cityInput);
        this.findViewById(R.id.search).setOnClickListener(v -> this.handleSearchConfiguration());
        this.notificationSwitch = this.findViewById(R.id.settings_notification_switch);
        this.notificationSwitch.setOnClickListener(v -> this.handleNotificationSwitch());
        this.updateNotificationSwitch();

        // Initialize UI - Sensor configurations
        this.sensorStatusText = this.findViewById(R.id.sensor_status_text);
        this.sensorBatteryText = this.findViewById(R.id.sensor_battery_text);
        this.sensorProgressBar = this.findViewById(R.id.sensor_progress_bar);
        this.sensorConnectButton = findViewById(R.id.sensor_connect_button);
        this.sensorConnectButton.setOnClickListener((v) -> this.handleConnectButtonClick());
        this.restoreUI(SettingsActivity.bundle);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.saveUI(SettingsActivity.bundle);
    }

    private void saveUI(Bundle state) {
        state.putString ("UI_SensorStatus_Text",        this.sensorStatusText.getText().toString());
        state.putString ("UI_SensorBattery_Text",       this.sensorBatteryText.getText().toString());
        state.putString ("UI_SensorConnectButton_Text", this.sensorConnectButton.getText().toString());
    }

    private void restoreUI(Bundle state) {
        if(!(this.sensorController.isConnecting() || this.sensorController.isConnected())) {
            this.initiateSensorConnectionUI();
            return;
        }
        String defaultText                  = "(no further info)";
        String UI_SensorStatus_Text         = state.getString("UI_SensorStatus_Text",        defaultText);
        String UI_SensorBattery_Text        = state.getString("UI_SensorBattery_Text",       defaultText);
        String UI_SensorConnectButton_Text  = state.getString("UI_SensorConnectButton_Text", defaultText);

        if(this.sensorController.isSyncing()) UI_SensorStatus_Text = this.getString(R.string.sensor_status_connected);

        this.sensorStatusText.setText   (UI_SensorStatus_Text);
        this.sensorBatteryText.setText  (UI_SensorBattery_Text);
        this.sensorConnectButton.setText(UI_SensorConnectButton_Text);

        this.sensorProgressBar.setVisibility(View.INVISIBLE);
    }


    /* -------- UI initialization -------- */

    private void initiateSensorConnectionUI() {
        this.sensorStatusText.setText(R.string.sensor_status_not_connected);
        this.sensorBatteryText.setText("");
        this.sensorProgressBar.setVisibility(View.INVISIBLE);
        this.sensorConnectButton.setText(R.string.sensor_button_connect);
    }


    /* -------- UI handlers - search -------- */

    private void handleSearchConfiguration() {
        if(this.sensorController.isConnecting()) {
            this.showBusyPrompt();
            return;
        }
        String city = this.cityNameText.getText().toString().trim();
        if(city.isEmpty()) {
            Toast.makeText(this, "Please enter a city name", Toast.LENGTH_SHORT ).show();
        }
        else{
            // Send city name back to MainActivity
            Intent intent = new Intent();
            intent.putExtra("CITY_NAME", city);
            setResult(Activity.RESULT_OK, intent);
            finish();
        }
    }

    /* -------- UI handlers - notification switch -------- */

    // Event handler
    void handleNotificationSwitch() {
        NotificationController.get(this).setEnabled(this.notificationSwitch.isChecked());
        this.updateNotificationSwitch();
    }

    // Render
    void updateNotificationSwitch() {
        boolean enabled = NotificationController.get(this).isEnabled();
        this.notificationSwitch.setChecked(enabled);
        this.notificationSwitch.setText(enabled ? "On" : "Off");
    }


    /* -------- UI handlers - sensor -------- */

    private void handleConnectButtonClick() {
        if(this.sensorController.isConnecting()) {
            this.showBusyPrompt();
            return;
        }
        if(!this.sensorController.isConnected()) {
            this.sensorController.connectToAnySensor(this);
        }
        else {
            this.sensorController.disconnectFromSensor();
        }
    }

    @Override
    public void onBackPressed() {
        if(this.sensorController.isConnecting()) {
            this.showBusyPrompt();
            return;
        }
        super.onBackPressed();
    }


    /* -------- Helpers -------- */

    private void showBusyPrompt() {
        Snackbar.make(this.sensorConnectButton, R.string.sensor_activity_busy, Snackbar.LENGTH_SHORT).show();
    }


    /* -------- External events -------- */

    @EventHandler
    private void updateSensorConnectionUI(@NonNull SensorController.ConnectionFlowStage event) {
        switch(event) {
            case SCANNING: {
                this.sensorStatusText.setText(R.string.sensor_status_scanning);
                this.sensorBatteryText.setText("");
                this.sensorProgressBar.setVisibility(View.VISIBLE);
                this.sensorProgressBar.setIndeterminate(true);
                this.sensorConnectButton.setText(R.string.sensor_button_scanning);
                break;
            }
            case CONNECTING: {
                this.sensorStatusText.setText(R.string.sensor_status_connecting);
                this.sensorBatteryText.setText("");
                this.sensorProgressBar.setVisibility(View.VISIBLE);
                this.sensorProgressBar.setIndeterminate(true);
                this.sensorConnectButton.setText(R.string.sensor_button_connecting);

                String name = null;
                ISensor sensor = this.sensorController.getSensor();
                if(sensor != null) {
                    name = sensor.getName();
                    if(name.equals("null")) name = null;
                }

                String message;
                if(name == null)
                    message = this.getString(R.string.sensor_activity_found_noname);
                else
                    message = this.getString(R.string.sensor_activity_found, name);

                Snackbar.make(this.sensorConnectButton, message, Snackbar.LENGTH_SHORT).show();
                break;
            }
            case SCAN_TIMEOUT: {
                this.sensorStatusText.setText(R.string.sensor_status_scan_timeout);
                this.sensorBatteryText.setText("");
                this.sensorProgressBar.setVisibility(View.INVISIBLE);
                this.sensorConnectButton.setText(R.string.sensor_button_retry);
                break;
            }
        }
    }

    @EventHandler
    private void updateSensorConnectionUI(@NonNull ConnectionStateChangeEvent event) {
        switch(event.getStage()) {
            case CONNECTING: {
                if(this.sensorController.getSensor() == null) {
                    Log.d(TAG, "Received " + event.getStage() + " when there's no sensor! Glitched?");
                    break;
                }
                this.sensorStatusText.setText(R.string.sensor_status_connecting);
                this.sensorBatteryText.setText("");
                this.sensorProgressBar.setVisibility(View.VISIBLE);
                this.sensorProgressBar.setIndeterminate(false);
                this.sensorProgressBar.setProgress(Math.max(event.getPercentage(), 2), true);
                this.sensorConnectButton.setText(R.string.sensor_button_connecting);
                break;
            }
            case ESTABLISHED: {
                if(this.sensorController.getSensor() == null) {
                    Log.d(TAG, "Received " + event.getStage() + " when there's no sensor! Glitched?");
                    break;
                }
                String name = null;
                ISensor sensor = this.sensorController.getSensor();
                if(sensor != null) {
                    name = sensor.getName();
                    if(name.equals("null")) name = null;
                }

                this.sensorStatusText.setText(R.string.sensor_status_connected);
                this.sensorProgressBar.setVisibility(View.INVISIBLE);
                if(name != null)
                    this.sensorConnectButton.setText(this.getString(R.string.sensor_button_disconnect_from, name));
                else
                    this.sensorConnectButton.setText(R.string.sensor_button_disconnect);
                break;
            }
            case DISCONNECTING: {
                this.sensorStatusText.setText(R.string.sensor_status_disconnecting);
                this.sensorBatteryText.setText("");
                this.sensorProgressBar.setVisibility(View.VISIBLE);
                this.sensorProgressBar.setIndeterminate(false);
                this.sensorProgressBar.setProgress(Math.max(event.getPercentage(), 2), true);
                this.sensorConnectButton.setText(R.string.sensor_button_disconnecting);
                break;
            }
            default:
            case DISCONNECTED: {
                this.sensorStatusText.setText(R.string.sensor_status_disconnected);
                this.sensorBatteryText.setText("");
                this.sensorProgressBar.setVisibility(View.INVISIBLE);
                this.sensorConnectButton.setText(R.string.sensor_button_connect);
                break;
            }
            case FAILED_RETRY: {
                this.sensorStatusText.setText(R.string.sensor_status_failed_retry);
                this.sensorBatteryText.setText("");
                this.sensorProgressBar.setVisibility(View.INVISIBLE);
                this.sensorConnectButton.setText(R.string.sensor_button_retry);
                break;
            }
            case FAILED_NO_RETRY: {
                this.sensorStatusText.setText(R.string.sensor_status_failed_noretry);
                this.sensorBatteryText.setText("");
                this.sensorProgressBar.setVisibility(View.INVISIBLE);
                this.sensorConnectButton.setText(R.string.sensor_button_connect);
                break;
            }
        }
    }

    @EventHandler
    protected void updateSensorConnectionUI(@NonNull SyncProgressChangedEvent event) {
        if(!this.sensorController.isConnected()) return;
        switch(event.getStage()) {
            case PROCESSING: {
                this.sensorStatusText.setText(R.string.sensor_status_downloading);
                this.sensorProgressBar.setVisibility(View.VISIBLE);
                this.sensorProgressBar.setIndeterminate(false);
                this.sensorProgressBar.setProgress(Math.max(event.getProgress(), 2), true);
                break;
            }
            case ABORTED:
            case DONE_NOUPDATE:
            case DONE: {
                this.sensorStatusText.setText(R.string.sensor_status_downloaded);
                this.sensorProgressBar.setVisibility(View.INVISIBLE);
                break;
            }
        }
    }

    @EventHandler
    protected void updateSensorConnectionUI(@NonNull NewBatteryInfoReceivedEvent event) {
        if(!this.sensorController.isConnected()) return;
        BatteryRecord record = event.getRecord();
        switch(record.chargingStatus) {
            default:
                this.sensorBatteryText.setText(this.getString(R.string.sensor_battery_discharging, record.percentage));
                break;
            case CHARGING:
                this.sensorBatteryText.setText(this.getString(R.string.sensor_battery_charging, record.percentage));
                break;
            case CHARGING_DONE:
                this.sensorBatteryText.setText(R.string.sensor_battery_charged);
                break;
        }
    }

    @EventHandler
    protected void displayFeedback(String message) {
        Snackbar.make(this.sensorConnectButton, message, Snackbar.LENGTH_LONG).show();
    }

}
