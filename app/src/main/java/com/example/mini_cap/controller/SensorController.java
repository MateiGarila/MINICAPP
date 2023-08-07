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

import app.uvtracker.sensor.pii.connection.application.event.SyncProgressChangedEvent;
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

    @NonNull
    private ConnectionFlowStage stage;

    private boolean syncCompleteMessagePending;

    @NonNull
    private final NoSpammingDispatcher syncMessageDispatcher;

    private SensorController(@NonNull Context context) {
        this.registerListenerClass(this);
        this.handler = new Handler(Looper.getMainLooper());
        this.context = context;
        this.stage = ConnectionFlowStage.DISCONNECTED;
        this.syncMessageDispatcher = new NoSpammingDispatcher();
    }

    @Nullable
    public ISensor getSensor() {
        return this.connectedSensor;
    }

    @NonNull
    public ConnectionFlowStage getStage() {
        return this.stage;
    }


    /* -------- Connection flow logic -------- */

    public boolean connectToAnySensor(Activity activity) {
        if(this.connectedSensor != null) {
            this.connectedSensor.getConnection().unregisterAll();
            this.connectedSensor.getConnection().disconnect();
            this.connectedSensor = null;
        }
        if(!BLEPermissions.ensurePermissions(activity)) {
            this.displayToast(R.string.sensor_bluetooth_permission_request, true);
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
        return !this.startScanning();
    }

    private boolean startScanning() {
        if(this.scanner == null) return true;
        if(this.bleExceptionWrap(() -> this.scanner.startScanning())) return true;
        int currentSession = this.scanSession;
        this.handler.postDelayed(() -> {
            if(currentSession == this.scanSession && this.scanner.isScanning()) this.stopScanning(true);
        }, SCAN_TIMEOUT_MS);
        this.displayToast(R.string.sensor_scan_start);
        this.setStage(ConnectionFlowStage.CONNECTING);
        return false;
    }

    private void stopScanning(boolean timeout) {
        if(this.scanner == null) return;
        if(this.bleExceptionWrap(() -> this.scanner.stopScanning())) return;
        this.handler.post(() -> this.scanSession++);
        if(this.connectedSensor != null) return;
        if(timeout) this.displayToast(R.string.sensor_scan_timeout);
        this.setStage(ConnectionFlowStage.DISCONNECTED);
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
        this.displayToast(this.context.getString(R.string.sensor_connection_initiated, this.connectedSensor.getName()));
    }

    private void handleSensorConnection() {
        if(this.connectedSensor == null) {
            Log.d(TAG, "Handling sensor connection but there's no sensor. Glitched?");
            return;
        }
        this.syncCompleteMessagePending = false;
        this.handler.post(() -> this.connectedSensor.getConnection().startSync());
        this.setStage(ConnectionFlowStage.CONNECTED);
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
        boolean flag = false;
        switch(event.getStage()) {
            case CONNECTING: {
                if(this.connectedSensor == null) {
                    Log.d(TAG, "Received " + event.getStage() + " when there's no sensor! Glitched?");
                    break;
                }
                this.displayToast(this.context.getString(R.string.sensor_connection_connecting, event.getPercentage()));
                break;
            }
            case ESTABLISHED: {
                if(this.connectedSensor == null) {
                    Log.d(TAG, "Received " + event.getStage() + " when there's no sensor! Glitched?");
                    break;
                }
                this.displayToast(R.string.sensor_connection_established);
                this.handler.post(this::handleSensorConnection);
                break;
            }
            case DISCONNECTED: {
                this.displayToast(R.string.sensor_connection_disconnected);
                flag = true;
                break;
            }
            case FAILED_RETRY: {
                this.displayToast(R.string.sensor_connection_failed_retry);
                flag = true;
                break;
            }
            case FAILED_NO_RETRY: {
                this.displayToast(R.string.sensor_connection_failed_noretry);
                flag = true;
                break;
            }
        }
        if(flag) {
            this.setStage(ConnectionFlowStage.DISCONNECTED);
            this.connectedSensor = null;
        }
    }

    @EventHandler
    protected void onNewEstimation(@NonNull NewEstimationReceivedEvent event) {
        if(this.connectedSensor == null) return;
        this.connectedSensor.getConnection().startSync();
    }

    @EventHandler
    protected void onSyncProgress(SyncProgressChangedEvent event) {
        if(event.getStage() != SyncProgressChangedEvent.Stage.PROCESSING) return;
        if(event.getProgress() == 100 && this.syncCompleteMessagePending) {
            this.syncCompleteMessagePending = false;
            this.syncMessageDispatcher.dispatchFinal(() -> this.displayToast(R.string.sensor_sync_done));
        }
        if(event.getProgress() > 0 && event.getProgress() < 100) {
            this.syncCompleteMessagePending = true;
            this.syncMessageDispatcher.dispatch(() -> this.displayToast(this.context.getString(R.string.sensor_sync_in_progress, event.getProgress())));
        }
    }


    /* -------- Internal helper methods -------- */

    private void setStage(@NonNull  ConnectionFlowStage stage) {
        this.stage = stage;
        this.dispatch(new ConnectionFlowEvent(this.connectedSensor, stage));
    }

    private boolean bleExceptionWrap(TransceiverOperationRunnable r) {
        try {
            r.run();
            return false;
        }
        catch(TransceiverException ignored) {
            this.displayToast(R.string.sensor_bluetooth_exception);
            return true;
        }
    }

    private void displayToast(int resourceID) {
        this.displayToast(resourceID, false);
    }

    private void displayToast(@NonNull String message) {
        this.displayToast(message, false);
    }

    private void displayToast(int resourceID, boolean isLong) {
        this.displayToast(this.context.getString(resourceID), isLong);
    }

    private void displayToast(@NonNull String message, boolean isLong) {
        // TODO: any better way to surface this to the user?
        this.handler.post(() -> this.dispatch(message));
    }


    /* -------- Inner classes --------*/

    public enum ConnectionFlowStage {
        DISCONNECTED,
        CONNECTING,
        CONNECTED,
    }

    public static class ConnectionFlowEvent {

        @Nullable
        private final ISensor sensor;

        @NonNull
        private final ConnectionFlowStage stage;

        public ConnectionFlowEvent(@Nullable ISensor sensor, @NonNull ConnectionFlowStage stage) {
            this.sensor = sensor;
            this.stage = stage;
        }

        @Nullable
        public ISensor getSensor() {
            return sensor;
        }

        @NonNull
        public ConnectionFlowStage getStage() {
            return stage;
        }

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

class NoSpammingDispatcher {

    private static final long INTERVAL = 1000;

    private long targetTime;

    public NoSpammingDispatcher() {
        this.targetTime = -1;
    }

    public void reset() {
        this.targetTime = -1;
    }

    public void dispatch(@NonNull Runnable runnable) {
        long now = System.currentTimeMillis();
        if(this.targetTime > now) return;
        this.targetTime = now + INTERVAL;
        runnable.run();
    }

    public void dispatchFinal(@NonNull Runnable runnable) {
        this.reset();
        this.dispatch(runnable);
        this.reset();
    }

}
