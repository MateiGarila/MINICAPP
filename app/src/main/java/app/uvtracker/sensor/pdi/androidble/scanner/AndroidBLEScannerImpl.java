package app.uvtracker.sensor.pdi.androidble.scanner;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelUuid;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;
import java.util.function.Predicate;

import app.uvtracker.sensor.BLEOptions;
import app.uvtracker.sensor.pdi.androidble.AndroidBLESensorImpl;
import app.uvtracker.sensor.pii.ISensor;
import app.uvtracker.sensor.pii.event.EventRegistry;
import app.uvtracker.sensor.pii.scanner.IScanner;
import app.uvtracker.sensor.pii.scanner.event.SensorScannedEvent;
import app.uvtracker.sensor.pii.scanner.exception.TransceiverException;
import app.uvtracker.sensor.pii.scanner.exception.TransceiverNoPermException;
import app.uvtracker.sensor.pii.scanner.exception.TransceiverOffException;
import app.uvtracker.sensor.pii.scanner.exception.TransceiverUnsupportedException;

public class AndroidBLEScannerImpl extends EventRegistry implements IScanner {

    @NonNull
    private final Handler handler;

    @NonNull
    private final Context context;

    @Nullable
    private final Predicate<Context> permChecker;

    @NonNull
    private final BluetoothAdapter adapter;

    private boolean isScanning = false;

    @NonNull
    private final BluetoothScanCallback callback;

    @NonNull
    private final ConcurrentMap<String, AndroidBLESensorImpl> map;

    public AndroidBLEScannerImpl(@NonNull Context context) throws TransceiverException {
        this(context, null);
    }

    public AndroidBLEScannerImpl(@NonNull Context context, @Nullable Predicate<Context> permChecker) throws TransceiverException {
        this.handler = new Handler(Looper.getMainLooper()); // TODO: which thread to use?
        this.context = context;
        this.permChecker = permChecker;

        this.adapter = context.getSystemService(BluetoothManager.class).getAdapter();
        if(this.adapter == null) throw new TransceiverUnsupportedException();
        if(!this.adapter.isEnabled()) throw new TransceiverOffException();

        this.callback = new BluetoothScanCallback(this::onSensorScanned);
        this.map = new ConcurrentHashMap<>();
    }

    @Override
    public boolean isScanning() {
        return this.isScanning;
    }

    @SuppressLint("MissingPermission")
    @Override
    public synchronized void startScanning() throws TransceiverException {
        if(this.isScanning) return;
        this.ensurePerm();
        this.isScanning = true;
        this.map.clear();
        this.adapter.getBluetoothLeScanner().startScan(this.buildFilter(), this.buildSettings(), this.callback);
    }

    @SuppressLint("MissingPermission")
    @Override
    public synchronized void stopScanning() throws TransceiverException {
        if(!this.isScanning) return;
        this.ensurePerm();
        this.adapter.getBluetoothLeScanner().stopScan(this.callback);
        this.isScanning = false;
    }

    private void ensurePerm() throws TransceiverNoPermException {
        if(this.permChecker == null) return;
        if(!this.permChecker.test(this.context))
            throw new TransceiverNoPermException();
    }

    @NonNull
    private List<ScanFilter> buildFilter() {
        List<ScanFilter> filters = new ArrayList<>(1);
        if(BLEOptions.Scanner.RESTRICTED)
            filters.add(new ScanFilter.Builder().setServiceUuid(new ParcelUuid(BLEOptions.Scanner.FILTER_UUID)).build());
        else
            filters.add(new ScanFilter.Builder().build());
        return filters;
    }

    @NonNull
    private ScanSettings buildSettings() {
        return new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build();
    }

    protected void onSensorScanned(@NonNull ScanResult result) {
        this.handler.post(() -> this.processSensorScanResult(result));
    }

    private synchronized void processSensorScanResult(@NonNull ScanResult result) {
        if(!this.isScanning) return;
        BluetoothDevice device = result.getDevice();
        String address = device.getAddress();
        boolean firstTime = !this.map.containsKey(address);
        AndroidBLESensorImpl pdiSensor;
        if(firstTime) {
            pdiSensor = new AndroidBLESensorImpl(result, this.context);
            this.map.put(address, pdiSensor);
        }
        else {
            pdiSensor = Objects.requireNonNull(this.map.get(address));
            pdiSensor.update(result);
        }
        SensorScannedEvent event = new SensorScannedEvent(pdiSensor, firstTime, this.map.values());
        this.dispatch(event);
    }

    @Override
    @NonNull
    public ISensor[] getSnapshot() {
        return this.map.values().toArray(new ISensor[0]);
    }

}

class BluetoothScanCallback extends ScanCallback {

    @NonNull
    private final Consumer<ScanResult> callback;

    public BluetoothScanCallback(@NonNull Consumer<ScanResult> callback) {
        this.callback = callback;
    }

    @Override
    public void onScanResult(int callbackType, ScanResult result) {
        super.onScanResult(callbackType, result);
        this.callback.accept(Objects.requireNonNull(result));
    }

}