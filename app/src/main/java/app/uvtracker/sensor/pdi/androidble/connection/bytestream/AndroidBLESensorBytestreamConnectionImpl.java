package app.uvtracker.sensor.pdi.androidble.connection.bytestream;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import app.uvtracker.sensor.pii.event.EventRegistry;
import app.uvtracker.sensor.pdi.BLEOptions;
import app.uvtracker.sensor.pii.connection.bytestream.event.BytesReceivedEvent;
import app.uvtracker.sensor.pii.connection.shared.event.ConnectionStateChangeEvent;
import app.uvtracker.sensor.pii.connection.bytestream.ISensorBytestreamConnection;

@SuppressLint("MissingPermission")
public class AndroidBLESensorBytestreamConnectionImpl extends EventRegistry implements ISensorBytestreamConnection {

    @NonNull
    private static final String TAG = AndroidBLESensorBytestreamConnectionImpl.class.getSimpleName();

    @NonNull
    private final Handler handler;

    @NonNull
    private final Context context;

    @Nullable
    private DelayedTask delayedTask;

    @NonNull
    private final BluetoothDevice device;

    @NonNull
    private Stage internalStage;

    @NonNull
    private final BluetoothGattCallbackImpl bleCallback;

    @Nullable
    private BluetoothGatt gattObj;

    @Nullable
    private CharacteristicStore endpoints;

    @NonNull
    private final FlowControlledBuffer writeStream;

    public AndroidBLESensorBytestreamConnectionImpl(@NonNull BluetoothDevice device, @NonNull Context context) {
        this.handler = new Handler(Looper.getMainLooper()); // TODO: which thread to use?
        this.context = context;
        this.device = device;
        this.internalStage = Stage.DISCONNECTED;
        this.bleCallback = new BluetoothGattCallbackImpl(this);
        this.writeStream = new FlowControlledBuffer(
                this.handler::postDelayed,
                this::writeCore,
                new FlowControlledBuffer.Config(
                        BLEOptions.Device.Serial.Write.Buffer.CAPACITY,
                        BLEOptions.Device.MTU_REQUIRED - 3,
                        BLEOptions.Device.Serial.Write.Buffer.SPEED_BPS,
                        BLEOptions.Device.Serial.Write.Buffer.STICKY_DLY,
                        BLEOptions.Device.Serial.Write.Buffer.MIN_DLY
                )
        );
        this.wipeReferences();
    }

    private void debug(@NonNull String msg, Object... args) {
        if(BLEOptions.TRACE_ENABLED) Log.d(TAG, String.format(msg, args));
    }

    private BluetoothGatt getGatt() {
        return Objects.requireNonNull(this.gattObj);
    }

    private CharacteristicStore getEndpoints() {
        return Objects.requireNonNull(this.endpoints);
    }

    private void wipeReferences() {
        this.gattObj = null;
        this.endpoints = null;
        this.writeStream.clear();
    }

    private void setStage(@NonNull Stage stage) {
        this.debug(">> New stage: %1$s", stage);
        this.internalStage = stage;
    }

    private boolean ensureStage(Stage... stages) {
        for(Stage stage : stages) {
            if(this.internalStage == stage)
                return true;
        }
        return false;
    }

    protected void postTask(@NonNull Runnable r) {
        this.handler.post(r);
    }

    protected void setDelayedTask(@NonNull Runnable r, int delayms) {
        this.cancelDelayedTask();
        this.delayedTask = new DelayedTask(r);
        this.handler.postDelayed(this.delayedTask, delayms);
    }

    protected void cancelDelayedTask() {
        if(this.delayedTask != null)
            this.delayedTask.cancel();
    }

    private void dispatch(ConnectionStateChangeEvent.State state) {
        this.dispatch(new ConnectionStateChangeEvent(state, this.internalStage.getPercentage()));
    }

    @Override
    public void reset() {
        this.debug("reset()");
        this.cancelDelayedTask();
        if(this.gattObj != null) {
            this.getGatt().disconnect();
            this.getGatt().close();
            this.debug("- Force closed everything");
        }
        this.wipeReferences();
        this.setStage(Stage.CRASHED);
        this.dispatch(ConnectionStateChangeEvent.State.FAILED_RETRY);
    }

    @Override
    public boolean connect() {
        this.debug("connect()");
        if(!this.ensureStage(Stage.DISCONNECTED, Stage.CRASHED)) {
            this.debug("- Request ignored.");
            return false;
        }
        this.device.connectGatt(this.context, false, this.bleCallback, BluetoothDevice.TRANSPORT_LE);
        this.setStage(Stage.CONNECTING);
        this.setDelayedTask(this::reset, BLEOptions.Connection.CONNECTION_TIMEOUT);
        this.dispatch(ConnectionStateChangeEvent.State.CONNECTING);
        return true;
    }

    @Override
    public boolean disconnect() {
        this.debug("disconnect()");
        if(this.ensureStage(Stage.DISCONNECTING, Stage.DISCONNECTED, Stage.CRASHED)) {
            this.debug("- Request ignored.");
            return false;
        }
        if(this.gattObj != null) {
            this.getGatt().disconnect();
            this.setStage(Stage.DISCONNECTING);
            this.setDelayedTask(this::reset, BLEOptions.Connection.DISCONNECTION_TIMEOUT);
            this.dispatch(ConnectionStateChangeEvent.State.DISCONNECTING);
        }
        else {
            this.gracefullyClose(true, true);
        }
        return true;
    }

    protected void onDeviceConnected(BluetoothGatt gatt) {
        this.debug("onDeviceConnected() [EV]");
        // TODO: when the device turns on after application-level timeout, the device still gets connected.
        this.gattObj = gatt;
        this.getGatt().requestMtu(BLEOptions.Device.MTU_REQUIRED);
        this.setStage(Stage.REQUESTING_MTU);
        this.dispatch(ConnectionStateChangeEvent.State.CONNECTING);
    }

    protected void onDeviceMTURequested(int mtu) {
        this.debug("onDeviceMTURequested() [EV]");
        this.debug("- New MTU: %1$d", mtu);
        if(mtu < BLEOptions.Device.MTU_REQUIRED) {
            this.debug("- Device MTU not large enough.");
            this.gracefullyClose(true, false);
        }
        if(!this.ensureStage(Stage.REQUESTING_MTU)) {
            this.debug("- Spuriously changed MTU.");
            return;
        }
        this.getGatt().discoverServices();
        this.setStage(Stage.ENUMERATING);
        this.dispatch(ConnectionStateChangeEvent.State.CONNECTING);
    }

    protected void onDeviceEnumerated() {
        this.debug("onDeviceEnumerated() [EV]");
        try {
            this.obtainEndpoints();
            this.subscribeToEndpoints();
        }
        catch(UnsupportedOperationException ex) {
            this.debug("- The device is not supported: %1$s", ex.getMessage());
            this.gracefullyClose(true, false);
            return;
        }
        this.setStage(Stage.SUBSCRIBING);
        this.dispatch(ConnectionStateChangeEvent.State.CONNECTING);
    }

    private void obtainEndpoints() throws UnsupportedOperationException {
        this.debug("- obtainEndpoints()");
        BluetoothGattService service = this.getGatt().getService(BLEOptions.Device.SERVICE);
        if(service == null) throw new UnsupportedOperationException("No such service");
        BluetoothGattCharacteristic read  = service.getCharacteristic(BLEOptions.Device.Serial.Read.ENDPOINT);
        BluetoothGattCharacteristic write = service.getCharacteristic(BLEOptions.Device.Serial.Write.ENDPOINT);
        if(read == null || write == null) throw new UnsupportedOperationException("No read/write endpoints");
        if((read.getProperties() & BLEOptions.Device.Serial.Read.PROPERTY) == 0) throw new UnsupportedOperationException("Read endpoint property mismatch");
        if((write.getProperties() & BLEOptions.Device.Serial.Write.PROPERTY) == 0) throw new UnsupportedOperationException("Write endpoint property mismatch");
        this.endpoints = new CharacteristicStore(service, read, write);
    }

    private void subscribeToEndpoints() throws UnsupportedOperationException {
        this.debug("- subscribeToEndpoints()");
        BluetoothGattDescriptor descriptor = this.getEndpoints().read.getDescriptor(BLEOptions.Device.Serial.Read.DESCRIPTOR);
        if(descriptor == null) throw new UnsupportedOperationException("No CCCD descriptor");
        if(!this.getGatt().setCharacteristicNotification(this.getEndpoints().read, true)) throw new UnsupportedOperationException("Failed to enable notifications locally");
        descriptor.setValue(BLEOptions.Device.Serial.Read.DESCRIPTOR_VAL);
        this.getGatt().writeDescriptor(descriptor);
    }

    protected void onDescriptorWritten(BluetoothGattDescriptor descriptor) {
        this.debug("onDescriptorWritten() [EV]");
        if(     descriptor == null
            || !descriptor.getCharacteristic().getUuid()
                    .equals(this.getEndpoints().read.getUuid())
            || !descriptor.getUuid()
                    .equals(BLEOptions.Device.Serial.Read.DESCRIPTOR))
            return;
        this.onNotificationSubscribed();
    }

    private void onNotificationSubscribed() {
        this.debug("- onNotificationSubscribed()");
        this.setDelayedTask(this::onConnectionStabilized, BLEOptions.Connection.CONNECTION_GRACE_PERIOD);
        this.setStage(Stage.CONNECTION_GRACE_PERIOD);
        this.dispatch(ConnectionStateChangeEvent.State.CONNECTING);
    }

    private void onConnectionStabilized() {
        this.debug("onConnectionStabilized() [EV]");
        this.setStage(Stage.ESTABLISHED);
        this.dispatch(ConnectionStateChangeEvent.State.ESTABLISHED);
    }

    protected void onCharacteristicChanged(BluetoothGattCharacteristic characteristic) {
        this.debug("onCharacteristicChanged() [EV]");
        if(!this.ensureStage(Stage.ESTABLISHED)) {
            this.debug("- Connection flow still not done, we are at stage %1$s", this.internalStage);
            return;
        }
        if(characteristic.getUuid().equals(Objects.requireNonNull(this.getEndpoints().read).getUuid())) {
            this.onDataReceived();
        }
    }

    private void onDataReceived() {
        this.debug("- onDataReceived()");
        byte[] data = Objects.requireNonNull(this.getEndpoints().read.getValue());
        this.debug("- Received %1$d bytes.", data.length);
        this.dispatch(new BytesReceivedEvent(data));
    }

    @Override
    public boolean write(@NonNull byte[] data) {
        this.debug("write()");
        if(!this.ensureStage(Stage.ESTABLISHED)) {
            this.debug("- Connection flow still not done, we are at stage %1$s", this.internalStage);
            return false;
        }
        this.writeStream.write(data);
        return true;
    }

    private void writeCore(@NonNull byte[] data) {
        this.debug("- writeCore()");
        BluetoothGattCharacteristic write = this.getEndpoints().write;
        write.setWriteType(BLEOptions.Device.Serial.Write.WRITE_TYPE);
        write.setValue(data);
        this.getGatt().writeCharacteristic(write);
        this.debug("- Written %1$d bytes.", data.length);
    }

    protected void onDeviceDisconnected() {
        this.debug("onDeviceDisconnected() [EV]");
        this.gracefullyClose(false, false);
    }

    protected void onBluetoothError() {
        this.debug("onBluetoothError() [EV]");
        this.gracefullyClose(true, true);
    }

    private void gracefullyClose(boolean failed, boolean retryAdvised) {
        this.debug(">> Gracefully closing, failed: %1$s, retry advised: %2$s", failed, retryAdvised);
        this.cancelDelayedTask();
        if(this.gattObj != null) {
            this.getGatt().close();
        }
        else {
            failed = true;
            retryAdvised = true;
            this.debug(">> Gatt object is null.");
        }
        this.setStage(failed ? Stage.CRASHED : Stage.DISCONNECTED);
        this.wipeReferences();
        this.dispatch(!failed ? ConnectionStateChangeEvent.State.DISCONNECTED : retryAdvised ? ConnectionStateChangeEvent.State.FAILED_RETRY : ConnectionStateChangeEvent.State.FAILED_NO_RETRY);
    }

}


enum Stage {
    DISCONNECTED(100),
    CONNECTING(0),
    REQUESTING_MTU(30),
    ENUMERATING(50),
    SUBSCRIBING(70),
    CONNECTION_GRACE_PERIOD(90),
    ESTABLISHED(100),
    DISCONNECTING(0),
    CRASHED(100),
    ;

    private final int percentage;

    Stage(int percentage) {
        this.percentage = percentage;
    }

    public int getPercentage() {
        return this.percentage;
    }

}


class CharacteristicStore {

    @NonNull
    public final BluetoothGattService service;

    @NonNull
    public final BluetoothGattCharacteristic read;

    @NonNull
    public final BluetoothGattCharacteristic write;

    public CharacteristicStore(@NonNull BluetoothGattService service, @NonNull BluetoothGattCharacteristic read, @NonNull BluetoothGattCharacteristic write) {
        this.service = service;
        this.read = read;
        this.write = write;
    }

}


class DelayedTask implements Runnable {

    @NonNull
    private final Runnable core;

    private boolean isCancelled = false;

    public DelayedTask(@NonNull Runnable core) {
        this.core = core;
    }

    public void cancel() {
        this.isCancelled = true;
    }

    @Override
    public void run() {
        if(this.isCancelled) return;
        this.core.run();
        this.isCancelled = true;
    }

}


class BluetoothGattCallbackImpl extends BluetoothGattCallback {

    @NonNull
    private final AndroidBLESensorBytestreamConnectionImpl connection;

    public BluetoothGattCallbackImpl(@NonNull AndroidBLESensorBytestreamConnectionImpl connection) {
        this.connection = connection;
    }

    private void post(@NonNull Runnable r) {
        this.connection.postTask(r);
    }

    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        super.onConnectionStateChange(gatt, status, newState);
        if(status == BluetoothGatt.GATT_SUCCESS) {
            switch(newState) {
                case BluetoothGatt.STATE_CONNECTED: {
                    this.post(() -> this.connection.onDeviceConnected(gatt));
                    break;
                }
                case BluetoothGatt.STATE_DISCONNECTED: {
                    this.post(this.connection::onDeviceDisconnected);
                    break;
                }
            }
        }
        else {
            this.post(this.connection::onBluetoothError);
        }
    }

    public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
        super.onMtuChanged(gatt, mtu, status);
        if (status == BluetoothGatt.GATT_SUCCESS) {
            this.post(() -> this.connection.onDeviceMTURequested(mtu));
        } else {
            this.post(this.connection::onBluetoothError);
        }
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        super.onServicesDiscovered(gatt, status);
        if(status == BluetoothGatt.GATT_SUCCESS) {
            this.post(this.connection::onDeviceEnumerated);
        }
        else {
            this.post(this.connection::onBluetoothError);
        }
    }

    @Override
    public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        super.onDescriptorWrite(gatt, descriptor, status);
        if(status == BluetoothGatt.GATT_SUCCESS) {
            this.post(() -> this.connection.onDescriptorWritten(descriptor));
        }
        else {
            this.post(this.connection::onBluetoothError);
        }
    }

    @Override
    public void onCharacteristicChanged(@NonNull BluetoothGatt gatt, @NonNull BluetoothGattCharacteristic characteristic) {
        super.onCharacteristicChanged(gatt, characteristic);
        this.post(() -> this.connection.onCharacteristicChanged(characteristic));
    }

}


class FlowControlledBuffer {

    @NonNull
    private static final String TAG = FlowControlledBuffer.class.toString();

    public static class Config {

        public final int capacity;      // Capacity in bytes
        public final int speedLimit;    // Speed limit in bytes per second. Set as 0 to disable
        public final int mtu;           // MTU in bytes
        public final int stickyDelay;   // Initial sticky delay in ms
        public final int minDelay;      // Minimum throttling to apply

        public Config(int capacity, int mtu, int speedLimit, int stickyDelay, int minDelay) {
            this.capacity = capacity;
            this.speedLimit = speedLimit;
            this.mtu = mtu;
            this.stickyDelay = stickyDelay;
            this.minDelay = minDelay;
        }

    }

    @NonNull
    private final BiConsumer<Runnable, Integer> scheduler;

    @NonNull
    private final Consumer<byte[]> dataSink;

    @NonNull
    private final Config config;

    @NonNull
    private final ArrayBlockingQueue<Byte> stream;

    private boolean isActive;

    public FlowControlledBuffer(@NonNull BiConsumer<Runnable, Integer> scheduler, @NonNull Consumer<byte[]> dataSink, @NonNull Config config) {
        this.scheduler = scheduler;
        this.dataSink = dataSink;
        this.config = config;
        this.stream = new ArrayBlockingQueue<>(config.capacity);
        this.isActive = false;
    }

    public void write(@NonNull byte[] data) {
        Log.d(TAG, "write(" + data.length + " bytes)");
        int counter = 0;
        try {
            for (byte ch : data) {
                this.stream.add(ch);
                counter++;
            }
            Log.d(TAG, "- Processed " + data.length + " bytes.");
        }
        catch(IllegalStateException ex) {
            Log.w(TAG, "- Flow control buffer overflowed! Processed only " + counter + " of " + data.length + " bytes.");
        }
        this.triggerExecutor();
    }

    private void triggerExecutor() {
        synchronized(this) {
            if(this.isActive) return;
            this.isActive = true;
        }
        Log.d(TAG, "- Starting executor...");
        this.scheduler.accept(this::execute, this.config.stickyDelay);
    }

    private void execute() {
        Log.d(TAG, "execute()");
        int bytesAtBuffer;
        synchronized(this) {
            bytesAtBuffer = this.stream.size();
            if(bytesAtBuffer == 0) {
                this.isActive = false;
                return;
            }
        }
        int bytesToWrite = Math.min(bytesAtBuffer, this.config.mtu);
        byte[] buffer = new byte[bytesToWrite];
        for (int i = 0; i < bytesToWrite; i++) {
            Byte got = this.stream.poll();
            if(got == null) {
                buffer = Arrays.copyOf(buffer, i);
                break;
            }
            buffer[i] = got;
        }
        int delay = 0;
        if(this.config.speedLimit != 0)
            delay = Math.max(bytesToWrite * 1000 / this.config.speedLimit, this.config.minDelay);
        Log.d(TAG, String.format("- Executor decided to write %1$d out of %2$d bytes and imposed a %3$dms delay.", bytesToWrite, bytesAtBuffer, delay));
        this.dataSink.accept(buffer);
        synchronized(this) {
            if(this.stream.isEmpty()) {
                this.isActive = false;
                return;
            }
        }
        Log.d(TAG, "- Executor scheduled next execution.");
        this.scheduler.accept(this::execute, delay);
    }

    public void clear() {
        this.stream.clear();
    }

}

