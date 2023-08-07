package com.example.mini_cap.controller;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.mini_cap.R;

import java.util.ArrayList;
import java.util.List;

import app.uvtracker.sensor.SensorAPI;
import app.uvtracker.sensor.pii.ISensor;

import app.uvtracker.sensor.pii.connection.application.event.NewEstimationReceivedEvent;

import app.uvtracker.sensor.pii.connection.shared.event.ConnectionStateChangeEvent;
import app.uvtracker.sensor.pii.event.EventHandler;
import app.uvtracker.sensor.pii.event.EventRegistry;
import app.uvtracker.sensor.pii.event.IEventListener;
import app.uvtracker.sensor.pii.scanner.IScanner;
import app.uvtracker.sensor.pii.scanner.event.SensorScannedEvent;
import app.uvtracker.sensor.pii.scanner.exception.TransceiverException;

public class SensorController extends EventRegistry implements IEventListener {

    private static final String TAG = SensorController.class.getSimpleName();

    private static final int SCAN_TIMEOUT_MS = 10000;

    @SuppressLint("StaticFieldLeak")
    @Nullable
    private static SensorController instance;

    @NonNull
    public static SensorController get(@NonNull Context context) {
        if(SensorController.instance == null)
            SensorController.instance = new SensorController(context);
        return SensorController.instance;
    }

    public static void release() {
        SensorController.instance = null;
    }

    @NonNull
    private final Handler handler;

    @NonNull
    private final Context context;

    @Nullable
    private IScanner scanner;

    private int scanSession = 0;

    @Nullable
    private ISensor connectedSensor;

    private boolean connecting;
    private boolean connected;


    private SensorController(@NonNull Context context) {
        this.registerListenerClass(this);
        this.handler = new Handler(Looper.getMainLooper());
        this.context = context;
        this.connecting = false;
        this.connected = false;
    }

    @Nullable
    public ISensor getSensor() {
        return this.connectedSensor;
    }

    public boolean isConnecting() {
        return connecting;
    }

    public boolean isConnected() {
        return connected;
    }

    /* -------- Connection flow logic -------- */

    public boolean connectToAnySensor(Activity activity) {
        if(this.connectedSensor != null) {
            this.connectedSensor.getConnection().unregisterAll();
            this.connectedSensor.getConnection().disconnect();
            this.connectedSensor = null;
        }
        if(!BLEPermissions.ensurePermissions(activity)) {
            this.displayFeedback(R.string.sensor_bluetooth_permission_request);
            return false;
        }
        if(this.scanner == null) {
            if(this.bleExceptionWrap(() -> this.scanner = SensorAPI.getScanner(this.context))) return false;
            this.scanner.registerListener(new IEventListener() {
                @EventHandler
                private void onAnyEvent(Object object) {
                    SensorController.this.dispatch(object);
                }
            });
        }
        if(this.bleExceptionWrap(() -> this.scanner.startScanning())) return false;
        int currentSession = this.scanSession;
        this.handler.postDelayed(() -> {
            if(currentSession == this.scanSession && this.scanner.isScanning()) this.stopScanning(true);
        }, SCAN_TIMEOUT_MS);
        this.dispatch(ConnectionFlowStage.SCANNING);
        this.connecting = true;
        this.connected = false;
        return true;
    }

    private void stopScanning(boolean timeout) {
        if(this.scanner == null) return;
        if(this.bleExceptionWrap(() -> this.scanner.stopScanning())) return;
        this.handler.post(() -> this.scanSession++);
        if(this.connectedSensor != null) return;
        this.connecting = false;
        this.connected = false;
        if(timeout) this.dispatch(ConnectionFlowStage.SCAN_TIMEOUT);
    }

    private void initiateSensorConnection(@NonNull ISensor sensor) {
        if(this.connectedSensor != null) return;
        this.connectedSensor = sensor;
        this.stopScanning(false);
        this.connectedSensor.getConnection().registerListener(new IEventListener() {
            @EventHandler
            private void onAnyEvent(Object object) {
                SensorController.this.dispatch(object);
            }
        });
        this.connectedSensor.getConnection().connect();
        this.connecting = true;
        this.connected = false;
        this.dispatch(ConnectionFlowStage.CONNECTING);
    }

    private void handleSensorConnection() {
        if(this.connectedSensor == null) {
            Log.d(TAG, "Handling sensor connection but there's no sensor. Glitched?");
            return;
        }
        this.handler.post(() -> this.connectedSensor.getConnection().startSync());
        this.connecting = false;
        this.connected = true;
    }

    public boolean disconnectFromSensor() {
        if(this.connectedSensor == null) return false;
        this.connectedSensor.getConnection().disconnect();
        return true;
    }


    /* -------- Application event implementation -------- */

    @EventHandler
    protected void onScanResult(@NonNull SensorScannedEvent event) {
        this.initiateSensorConnection(event.getSensor());
    }

    @EventHandler
    protected void onConnectionStateUpdate(@NonNull ConnectionStateChangeEvent event) {
        switch(event.getStage()) {
            case ESTABLISHED: {
                if (this.connectedSensor != null) this.handler.post(this::handleSensorConnection);
                break;
            }
            case DISCONNECTED:
            case FAILED_RETRY:
            case FAILED_NO_RETRY: {
                this.connectedSensor = null;
                this.connecting = false;
                this.connected = false;
                break;
            }
        }
    }

    @EventHandler
    protected void onNewEstimation(@NonNull NewEstimationReceivedEvent event) {
        if(!this.connected || this.connectedSensor == null) return;
        this.connectedSensor.getConnection().startSync();
    }


    /* -------- Internal helper methods -------- */

    private boolean bleExceptionWrap(TransceiverOperationRunnable r) {
        try {
            r.run();
            return false;
        }
        catch(TransceiverException ignored) {
            this.displayFeedback(R.string.sensor_bluetooth_exception);
            return true;
        }
    }

    private void displayFeedback(int resourceID) {
        this.displayFeedback(this.context.getString(resourceID));
    }

    private void displayFeedback(@NonNull String message) {
        this.handler.post(() -> this.dispatch(message));
    }


    /* -------- Inner classes --------*/

    public enum ConnectionFlowStage {
        SCANNING,
        CONNECTING,
        SCAN_TIMEOUT,
    }

}

class BLEPermissions {

    private static final int REQUEST_CODE = 1001;

    @NonNull
    private static List<String> getVersionDependentPermissions() {
        List<String> permissions = new ArrayList<>(2);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions.add(Manifest.permission.BLUETOOTH_SCAN);
            permissions.add(Manifest.permission.BLUETOOTH_CONNECT);
        }
        else
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        return permissions;
    }

    private static boolean hasPermissions(@NonNull Activity activity) {
        return BLEPermissions
                .getVersionDependentPermissions()
                .stream()
                .noneMatch(p ->
                        activity.checkSelfPermission(p)
                                != PackageManager.PERMISSION_GRANTED
                );
    }

    private static void requestPermissions(@NonNull Activity activity) {
        activity.requestPermissions(BLEPermissions.getVersionDependentPermissions().toArray(new String[0]), REQUEST_CODE);
    }

    public static boolean ensurePermissions(@NonNull Activity activity) {
        if(!BLEPermissions.hasPermissions(activity)) {
            BLEPermissions.requestPermissions(activity);
            return false;
        }
        return true;
    }

}

interface TransceiverOperationRunnable {

    void run() throws TransceiverException;

}
