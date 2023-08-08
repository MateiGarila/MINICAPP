package app.uvtracker.sensor.pdi.androidble;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanResult;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Date;

import app.uvtracker.sensor.pdi.androidble.connection.bytestream.AndroidBLESensorBytestreamConnectionImpl;
import app.uvtracker.sensor.pii.ISensor;
import app.uvtracker.sensor.pii.connection.application.ISensorConnection;
import app.uvtracker.sensor.pii.connection.application.PIISensorConnectionImpl;
import app.uvtracker.sensor.pii.connection.bytestream.ISensorBytestreamConnection;
import app.uvtracker.sensor.pii.connection.packet.ISensorPacketConnection;
import app.uvtracker.sensor.pii.connection.packet.PIISensorPacketConnectionImpl;

public class AndroidBLESensorImpl implements ISensor {

    public static class ConnectionFactory {

        @NonNull
        public final ISensorBytestreamConnection bytestreamBased;

        @NonNull
        public final ISensorPacketConnection packetBased;

        @NonNull
        public final ISensorConnection applicationHandle;

        public ConnectionFactory(@NonNull AndroidBLESensorImpl sensor, @NonNull BluetoothDevice device, @NonNull Context context) {
            this.bytestreamBased = new AndroidBLESensorBytestreamConnectionImpl(sensor, device, context);
            this.packetBased = new PIISensorPacketConnectionImpl(this.bytestreamBased);
            this.applicationHandle = new PIISensorConnectionImpl(this.packetBased);
        }

    }

    @NonNull
    private final Context context;

    @NonNull
    private final BluetoothDevice platformDevice;

    @NonNull
    private final String address;

    @NonNull
    private final String name;

    private int rssi;

    @NonNull
    private Date lastSeenAt;

    @Nullable
    private ConnectionFactory factory;

    @SuppressLint("MissingPermission")
    public AndroidBLESensorImpl(@NonNull ScanResult result, @NonNull Context context) {
        this.context = context;
        this.platformDevice = result.getDevice();
        this.address = this.platformDevice.getAddress();
        String name = this.platformDevice.getName();
        this.name = name == null ? "" : name;
        this.rssi = result.getRssi();
        this.lastSeenAt = new Date();
    }

    @NonNull
    @Override
    public String getAddress() {
        return this.address;
    }

    @NonNull
    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public int getRssi() {
        return this.rssi;
    }

    @NonNull
    @Override
    public Date getLastSeenAt() {
        return this.lastSeenAt;
    }

    @NonNull
    @Override
    public ISensorConnection getConnection() {
        return this.getFactoryBuilds().applicationHandle;
    }

    public void update(@NonNull ScanResult result) {
        this.rssi = result.getRssi();
        this.lastSeenAt = new Date();
    }

    @NonNull
    public BluetoothDevice getPlatformDevice() {
        return this.platformDevice;
    }

    @NonNull
    public ConnectionFactory getFactoryBuilds() {
        if(this.factory == null)
            this.factory = new ConnectionFactory(this, this.platformDevice, this.context);
        return this.factory;
    }

}

