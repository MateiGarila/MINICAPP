package app.uvtracker.sensor.pii.connection.application;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import app.uvtracker.data.battery.BatteryRecord;
import app.uvtracker.data.optical.OpticalRecord;
import app.uvtracker.data.optical.TimedOpticalRecord;
import app.uvtracker.sensor.BLEOptions;
import app.uvtracker.sensor.pii.ISensor;
import app.uvtracker.sensor.pii.connection.application.event.NewBatteryInfoReceivedEvent;
import app.uvtracker.sensor.pii.connection.application.event.NewEstimationReceivedEvent;
import app.uvtracker.sensor.pii.connection.application.event.NewSampleReceivedEvent;
import app.uvtracker.sensor.pii.connection.application.event.SyncDataReceivedEvent;
import app.uvtracker.sensor.pii.connection.application.event.SyncProgressChangedEvent;
import app.uvtracker.sensor.pii.connection.packet.ISensorPacketConnection;
import app.uvtracker.sensor.pii.connection.packet.event.ParsedPacketReceivedEvent;
import app.uvtracker.sensor.pii.connection.shared.event.ConnectionStateChangeEvent;
import app.uvtracker.sensor.pii.event.EventHandler;
import app.uvtracker.sensor.pii.event.EventRegistry;
import app.uvtracker.sensor.pii.event.IEventListener;
import app.uvtracker.sensor.pii.event.IEventSource;
import app.uvtracker.sensor.protocol.packet.in.PacketInBatteryInfo;
import app.uvtracker.sensor.protocol.packet.in.PacketInNewOpticalEstimation;
import app.uvtracker.sensor.protocol.packet.in.PacketInNewOpticalSample;
import app.uvtracker.sensor.protocol.packet.in.PacketInSyncData;
import app.uvtracker.sensor.protocol.packet.in.PacketInSyncInfo;
import app.uvtracker.sensor.protocol.packet.out.PacketOutRequestSyncData;
import app.uvtracker.sensor.protocol.packet.out.PacketOutRequestSyncInfo;

public class PIISensorConnectionImpl extends EventRegistry implements ISensorConnection, IEventListener {

    @NonNull
    private final ISensorPacketConnection baseConnection;

    @NonNull
    private final EventRegistry packetEventRegistry;

    @NonNull
    private final LatestDataStorage latestDataStorage;

    @NonNull
    private final SyncManager syncManager;

    public PIISensorConnectionImpl(@NonNull ISensorPacketConnection baseConnection) {
        this.packetEventRegistry = new EventRegistry();
        this.baseConnection = baseConnection;
        this.latestDataStorage = new LatestDataStorage();
        this.syncManager = new SyncManager(new Handler(Looper.getMainLooper()), this);
        this.baseConnection.registerListener(this);
        this.packetEventRegistry.registerListener(this);
    }

    @NonNull
    protected ISensorPacketConnection getBaseConnection() {
        return this.baseConnection;
    }

    // Base connection implementation
    @Override
    @NonNull
    public ISensor getSensor() {
        return this.baseConnection.getSensor();
    }

    @Override
    public void reset() {
        this.baseConnection.reset();
    }

    @Override
    public boolean connect() {
        return this.baseConnection.connect();
    }

    @Override
    public boolean disconnect() {
        return this.baseConnection.disconnect();
    }

    @Override
    public boolean isSyncing() {
        return this.syncManager.isSyncing();
    }

    @Override
    public boolean startSync() {
        return this.syncManager.startSync();
    }

    @Override
    public boolean forceSync() {
        return this.syncManager.forceSync();
    }

    @Override
    public boolean abortSync() {
        return this.syncManager.abortSync();
    }

    // TODO: any better way to propagate these events up?
    @EventHandler // Source: IConnectable
    protected void onConnectionStateChange(@NonNull ConnectionStateChangeEvent event) {
        this.dispatch(event);
    }

    // Packet event registry
    @EventHandler // Source: ISensorPacketConnection
    protected void onParsedPacketReception(@NonNull ParsedPacketReceivedEvent event) {
        this.packetEventRegistry.dispatch(event.getPacket());
    }

    @NonNull
    public IEventSource getPacketEventManager() {
        return this.packetEventRegistry;
    }

    // Packet handling
    @EventHandler // Source: PacketEventRegistry
    protected void onPacketInNewOpticalSample(PacketInNewOpticalSample packet) {
        this.latestDataStorage.sample = packet.getRecord();
        this.dispatch(new NewSampleReceivedEvent(packet));
    }

    @EventHandler // Source: PacketEventRegistry
    protected void onPacketInNewOpticalEstimation(PacketInNewOpticalEstimation packet) {
        this.latestDataStorage.estimation = packet.getRecord();
        this.dispatch(new NewEstimationReceivedEvent(packet));
    }

    @EventHandler // Source: PacketEventRegistry
    protected void onPacketInBatteryInfo(PacketInBatteryInfo packet) {
        this.latestDataStorage.batteryRecord = packet.getRecord();
        this.dispatch(new NewBatteryInfoReceivedEvent(packet));
    }

    // Data getters

    @Nullable
    @Override
    public OpticalRecord getLatestSample() {
        return this.latestDataStorage.sample;
    }

    @Nullable
    @Override
    public OpticalRecord getLatestEstimation() {
        return this.latestDataStorage.estimation;
    }

    @Nullable
    @Override
    public BatteryRecord getLatestBatteryRecord() {
        return this.latestDataStorage.batteryRecord;
    }

}

class LatestDataStorage {

    @Nullable
    public OpticalRecord sample;

    @Nullable
    public OpticalRecord estimation;

    @Nullable
    public BatteryRecord batteryRecord;

}

class SyncManager implements IEventListener {

    private static final String TAG = SyncManager.class.getSimpleName();

    enum Stage {
        DISCONNECTED,
        IDLE,
        INITIATING,
        PROCESSING,
    }

    @NonNull
    private final Handler handler;

    @NonNull
    private final PIISensorConnectionImpl connection;

    private Stage stage;

    @NonNull
    private final TimeoutTask timeoutTask;

    @Nullable
    private PacketInSyncInfo latestSyncInfo;

    private int sampleInterval;
    private long deviceBootTime;

    private int firstSample;
    private int lastSample;

    private int progressInfoCount;
    private int progressInfoTotalCount;

    public SyncManager(@NonNull Handler handler, @NonNull PIISensorConnectionImpl connection) {
        this.handler = handler;
        this.connection = connection;
        this.stage = Stage.DISCONNECTED;
        this.timeoutTask = new TimeoutTask(BLEOptions.Sync.SYNC_TIMEOUT, this::abortSync);
        this.connection.getBaseConnection().registerListener(this);
        this.connection.getPacketEventManager().registerListener(this);
        this.resetSyncStates();
    }

    private void resetSyncStates() {
        this.latestSyncInfo = null;
        this.sampleInterval = 0;
        this.deviceBootTime = 0;
        this.firstSample = -1;
        this.lastSample = -1;
        this.progressInfoCount = 0;
        this.progressInfoTotalCount = 0;
    }

    public boolean isSyncing() {
        return this.stage == Stage.INITIATING || this.stage == Stage.PROCESSING;
    }

    public boolean startSync() {
        Log.d(TAG, "startSync()");
        if(this.stage != Stage.IDLE) return false;
        if(!this.connection.getBaseConnection().write(new PacketOutRequestSyncInfo())) return false;
        Log.d(TAG, "Sync info request packet sent.");
        this.stage = Stage.INITIATING;
        this.timeoutTask.refresh();
        this.connection.dispatch(new SyncProgressChangedEvent(SyncProgressChangedEvent.Stage.INITIATING));
        return true;
    }

    public boolean forceSync() {
        Log.d(TAG, "forceSync()");
        if(this.stage != Stage.IDLE) return false;
        if(!this.connection.getBaseConnection().write(new PacketOutRequestSyncInfo())) return false;
        Log.d(TAG, "Sync info request packet sent, DB info cleared.");
        this.resetSyncStates();
        this.stage = Stage.INITIATING;
        this.timeoutTask.refresh();
        this.connection.dispatch(new SyncProgressChangedEvent(SyncProgressChangedEvent.Stage.INITIATING));
        return true;
    }

    public boolean abortSync() {
        Log.d(TAG, "abortSync()");
        if(this.stage == Stage.INITIATING || this.stage == Stage.PROCESSING) {
            this.stage = Stage.IDLE;
            this.connection.dispatch(new SyncProgressChangedEvent(SyncProgressChangedEvent.Stage.ABORTED));
            return true;
        }
        return false;
    }

    @EventHandler
    private void onSyncInfoReceived(PacketInSyncInfo packet) {
        Log.d(TAG, "onSyncInfoReceived() [EV]");
        if(this.stage != Stage.INITIATING) return;
        this.stage = Stage.PROCESSING;
        this.timeoutTask.refresh();
        Log.d(TAG, "Processing sync info packet...");
        if(this.latestSyncInfo == null) {
            this.sampleInterval = packet.getSampleInterval();
            this.deviceBootTime = new Date().getTime() - (long) packet.getSampleCount() * packet.getSampleInterval() * 1000 - packet.getCurrentSecondCounter();
            Log.d(TAG, "Device boot time: " + new Date(this.deviceBootTime));
        }
        this.latestSyncInfo = packet;
        this.connection.dispatch(new SyncProgressChangedEvent(SyncProgressChangedEvent.Stage.PROCESSING, 0));
        this.progressInfoCount = 0;
        this.processSync(true);
    }

    @EventHandler
    private void onSyncDataReceived(PacketInSyncData packet) {
        Log.d(TAG, "onSyncDataReceived() [EV]");
        if(this.stage != Stage.PROCESSING) return;
        this.timeoutTask.refresh();
        int first = packet.getStartSample();
        int count = packet.getRecords().length;
        Log.d(TAG, "Received remote DB data: " + first + ", " + count);
        if(count == 0) {
            this.stage = Stage.IDLE;
            this.timeoutTask.cancel();
            this.connection.dispatch(new SyncProgressChangedEvent(SyncProgressChangedEvent.Stage.ABORTED));
            return;
        }

        int last = first + count - 1;
        if(this.firstSample < 0 || this.lastSample < 0) {
            this.firstSample = first;
            this.lastSample = last;
        }
        else {
            if(first < this.firstSample) this.firstSample = first;
            if(last > this.lastSample) this.lastSample = last;
        }

        List<TimedOpticalRecord> list = new ArrayList<>(count);
        for(int i = count - 1; i >= 0; i--) {
            OpticalRecord record = packet.getRecords()[i];
            if(!record.valid) {
                Log.d(TAG, "Received invalid record " + (first + i) + ". This may be a normal race condition and not a bug.");
                continue;
            }
            TimedOpticalRecord sample = new TimedOpticalRecord(record, this.deviceBootTime, first + i, this.sampleInterval);
            list.add(sample);
        }
        this.progressInfoCount += count;
        this.connection.dispatch(new SyncProgressChangedEvent(SyncProgressChangedEvent.Stage.PROCESSING, Math.round((float)this.progressInfoCount / (float)this.progressInfoTotalCount * 100.0f)));
        this.handler.post(() -> this.connection.dispatch(new SyncDataReceivedEvent(list)));
        this.processSync(false);
    }

    private void processSync(boolean firstTime) {
        Log.d(TAG, "processSync()");
        Objects.requireNonNull(this.latestSyncInfo);
        if(this.latestSyncInfo.getSampleCount() == 0) {
            this.stage = Stage.IDLE;
            this.timeoutTask.cancel();
            this.connection.dispatch(new SyncProgressChangedEvent(SyncProgressChangedEvent.Stage.DONE_NOUPDATE));
            return;
        }

        int first = this.latestSyncInfo.getSampleStart();
        int last = first + this.latestSyncInfo.getSampleCount() - 1;

        if(firstTime) {
            int count = 0;
            if(this.firstSample < 0 || this.lastSample < 0) {
                count = last - first + 1;
            }
            else {
                if (first < this.firstSample) count += this.firstSample - first;
                if (last > this.lastSample) count += last - this.lastSample;
            }
            this.progressInfoTotalCount = count;
        }

        //noinspection StatementWithEmptyBody
        if(this.firstSample < 0 || this.lastSample < 0) {
            // IF statement left empty on-purpose
            // to stop ELSE statement from processing this case
        }
        else if(last > this.lastSample) {
            first = this.lastSample + 1;
        }
        else if(first < this.firstSample) {
            last = this.firstSample - 1;
        }
        else {
            this.stage = Stage.IDLE;
            this.timeoutTask.cancel();
            this.connection.dispatch(new SyncProgressChangedEvent(firstTime ? SyncProgressChangedEvent.Stage.DONE_NOUPDATE : SyncProgressChangedEvent.Stage.DONE));
            return;
        }
        int count = Integer.min(last - first + 1, PacketOutRequestSyncData.MAX_COUNT);
        first = last - count + 1;
        Log.d(TAG, "Requesting remote DB: " + first + ", " + count);
        this.timeoutTask.refresh();
        this.connection.getBaseConnection().write(new PacketOutRequestSyncData(first, count));
    }

    @EventHandler
    private void onConnectionStateChange(ConnectionStateChangeEvent event) {
        Log.d(TAG, "onConnectionStateChange() [EV]");
        switch(event.getStage()) {
            case ESTABLISHED:
                Log.d(TAG, "Connection established. Stage change to IDLE");
                this.resetSyncStates();
                this.stage = Stage.IDLE;
                break;
            case DISCONNECTED:
            case FAILED_RETRY:
            case FAILED_NO_RETRY:
                Log.d(TAG, "Connection lost. Stage change to DISCONNECTED");
                boolean flag = this.isSyncing();
                this.stage = Stage.DISCONNECTED;
                if(flag) this.connection.dispatch(new SyncProgressChangedEvent(SyncProgressChangedEvent.Stage.ABORTED));
                break;
        }
    }

}

class TimeoutTask {

    @NonNull
    private final Handler handler;

    private int session;

    private final int timeout;

    @NonNull
    private final Runnable callback;

    public TimeoutTask(int timeout, @NonNull Runnable callback) {
        if(timeout == 0) throw new IllegalArgumentException();
        this.handler = new Handler(Looper.getMainLooper());
        this.session = 0;
        this.timeout = timeout;
        this.callback = callback;
    }

    public synchronized void refresh() {
        if(this.timeout <= 0) return;
        this.session++;
        int sessionCapture = this.session;
        this.handler.postDelayed(() -> this.execute(sessionCapture), this.timeout);
    }

    public synchronized void cancel() {
        this.session++;
    }

    private synchronized void execute(int session) {
        synchronized(this) {
            if (this.session > session) return;
        }
        this.callback.run();
    }

}
