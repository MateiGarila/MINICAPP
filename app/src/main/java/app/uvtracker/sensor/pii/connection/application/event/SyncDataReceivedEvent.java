package app.uvtracker.sensor.pii.connection.application.event;

import androidx.annotation.NonNull;

import java.util.List;

import app.uvtracker.data.optical.OpticalRecord;
import app.uvtracker.data.optical.TimedRecord;

public class SyncDataReceivedEvent {

    @NonNull
    private final List<TimedRecord<OpticalRecord>> data;

    public SyncDataReceivedEvent(@NonNull List<TimedRecord<OpticalRecord>> data) {
        this.data = data;
    }

    @NonNull
    public List<TimedRecord<OpticalRecord>> getData() {
        return this.data;
    }

}
