package app.uvtracker.sensor.pii.connection.application.event;

import androidx.annotation.NonNull;

import java.util.List;

import app.uvtracker.data.optical.OpticalRecord;
import app.uvtracker.data.optical.TimedOpticalRecord;

public class SyncDataReceivedEvent {

    @NonNull
    private final List<TimedOpticalRecord> data;

    public SyncDataReceivedEvent(@NonNull List<TimedOpticalRecord> data) {
        this.data = data;
    }

    @NonNull
    public List<TimedOpticalRecord> getData() {
        return this.data;
    }

}
