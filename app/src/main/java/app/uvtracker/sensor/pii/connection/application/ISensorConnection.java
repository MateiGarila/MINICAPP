package app.uvtracker.sensor.pii.connection.application;

import androidx.annotation.Nullable;

import app.uvtracker.data.battery.BatteryRecord;
import app.uvtracker.data.optical.OpticalRecord;
import app.uvtracker.sensor.pii.connection.shared.IConnectable;

public interface ISensorConnection extends IConnectable {

    /* -------- RT data notifications -------- */

    // Emits event: NewSampleReceivedEvent, NewEstimationReceivedEvent


    /* -------- Sync features -------- */

    // Emits event: SyncProgressChangedEvent, SyncDataReceivedEvent

    boolean isSyncing();

    boolean startSync();

    boolean forceSync();

    boolean abortSync();


    /* -------- Best-effort data getters -------- */

    @Nullable
    OpticalRecord getLatestSample();

    @Nullable
    OpticalRecord getLatestEstimation();

    @Nullable
    BatteryRecord getLatestBatteryRecord();


    // TODO: more features

}
