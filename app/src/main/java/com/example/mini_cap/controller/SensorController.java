package com.example.mini_cap.controller;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.mini_cap.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import app.uvtracker.data.type.Record;
import app.uvtracker.sensor.SensorAPI;
import app.uvtracker.sensor.pii.ISensor;
import app.uvtracker.sensor.pii.connection.application.event.NewSampleReceivedEvent;
import app.uvtracker.sensor.pii.connection.shared.event.ConnectionStateChangeEvent;
import app.uvtracker.sensor.pii.event.EventHandler;
import app.uvtracker.sensor.pii.event.EventRegistry;
import app.uvtracker.sensor.pii.event.IEventListener;
import app.uvtracker.sensor.pii.scanner.IScanner;
import app.uvtracker.sensor.pii.scanner.event.SensorScannedEvent;
import app.uvtracker.sensor.pii.scanner.exception.TransceiverException;

public class SensorController extends EventRegistry implements IEventListener {

    private static final int SCAN_TIMEOUT_MS = 10000;

    @NonNull
    private final Handler handler;

    @NonNull
    private final Activity activity;

    @Nullable
    private IScanner scanner;

    private int scanSession = 0;

    @Nullable
    private ISensor sensor;

    public SensorController(@NonNull Activity activity) {
        this.handler = new Handler(Looper.getMainLooper());
        this.activity = activity;
        if(activity instanceof IEventListener)
            this.registerListener((IEventListener)activity);
    }

    @NonNull
    private IScanner getScanner() {
        return Objects.requireNonNull(this.scanner);
    }

    @NonNull
    private ISensor getSensor() {
        return Objects.requireNonNull(this.sensor);
    }

    @Nullable
    public ISensor getSensorIfConnected() {
        return this.sensor;
    }

    public boolean connectToAnySensor() {
        if(this.sensor != null) {
            this.sensor.getConnection().unregisterAll();
            this.sensor.getConnection().disconnect();
            this.sensor = null;
        }
        if(this.scanner == null) {
            BLEPermissions.ensurePermissions(this.activity);
            if(this.runnableWrap(() -> this.scanner = SensorAPI.getScanner(this.activity))) return false;
            this.getScanner().registerListener(this);
        }
        return !this.startScanning();
    }

    private boolean startScanning() {
        if(this.runnableWrap(() -> this.getScanner().startScanning())) return true;
        int currentSession = this.scanSession;
        this.handler.postDelayed(() -> {
            if(currentSession == this.scanSession && this.getScanner().isScanning()) this.stopScanning(true);
        }, SCAN_TIMEOUT_MS);
        this.displayToast(R.string.sensor_scan_start);
        return false;
    }

    private void stopScanning(boolean timeout) {
        if(this.runnableWrap(() -> this.getScanner().stopScanning())) return;
        this.handler.post(() -> this.scanSession++);
        if(timeout) this.displayToast(R.string.sensor_scan_timeout);
    }

    @EventHandler
    protected void onScanResult(@NonNull SensorScannedEvent event) {
        if(this.sensor != null) return;
        this.sensor = event.getSensor();
        this.stopScanning(false);
        this.getSensor().getConnection().registerListener(this);
        this.getSensor().getConnection().connect();
        this.displayToast(this.activity.getString(R.string.sensor_connection_initiated, this.getSensor().getName()));
    }

    @EventHandler
    protected void onConnectionStateUpdate(@NonNull ConnectionStateChangeEvent event) {
        boolean flag = false;
        switch(event.getStage()) {
            case CONNECTING: {
                this.displayToast(this.activity.getString(R.string.sensor_connection_connecting, event.getPercentage()));
                break;
            }
            case ESTABLISHED: {
                this.displayToast(R.string.sensor_connection_established);
                this.dispatch(new SensorConnectedEvent(this.getSensor()));
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
            this.dispatch(new SensorDisconnectedEvent(this.getSensor()));
            this.sensor = null;
        }
    }

    @EventHandler
    protected void onReceivedData(@NonNull NewSampleReceivedEvent event) {
        this.dispatch(new RealTimeDataEvent(this.activity, event.getRecord()));
    }

    public boolean disconnectFromSensor() {
        if(this.sensor == null) return false;
        this.sensor.getConnection().disconnect();
        return true;
    }

    private boolean runnableWrap(TransceiverOperationRunnable r) {
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
        this.displayToast(this.activity.getString(resourceID));
    }

    private void displayToast(@NonNull String message) {
        this.handler.post(() ->
            Toast.makeText(this.activity, message, Toast.LENGTH_SHORT).show()
        );
    }

    protected static abstract class SensorConnectionEvent {

        @NonNull
        protected final ISensor sensor;

        protected SensorConnectionEvent(@NonNull ISensor sensor) {
            this.sensor = sensor;
        }

        @NonNull
        public ISensor getSensor() {
            return this.sensor;
        }

    }

    public static class SensorConnectedEvent extends SensorConnectionEvent {

        protected SensorConnectedEvent(@NonNull ISensor sensor) {
            super(sensor);
        }

    }

    public static class SensorDisconnectedEvent extends SensorConnectionEvent {

        protected SensorDisconnectedEvent(@NonNull ISensor sensor) {
            super(sensor);
        }

    }

    public static class RealTimeDataEvent {

        @NonNull
        private final Context context;

        @NonNull
        private final Record record;

        protected RealTimeDataEvent(@NonNull Context context, @NonNull Record record) {
            this.context = context;
            this.record = record;
        }

        @NonNull
        public Record getRecord() {
            return record;
        }

        @Override
        @NonNull
        public String toString() {
            return this.context.getString(R.string.sensor_record_format, this.record.uvIndex, this.record.illuminance);
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

    public static void ensurePermissions(@NonNull Activity activity) {
        if(!BLEPermissions.hasPermissions(activity))
            BLEPermissions.requestPermissions(activity);
    }

}

interface TransceiverOperationRunnable {

    void run() throws TransceiverException;

}
