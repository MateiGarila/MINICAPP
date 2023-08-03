package app.uvtracker.data.optical;

import androidx.annotation.NonNull;

public class TimedOpticalRecord {

    @NonNull
    private final OpticalRecord data;

    @NonNull
    private final Timestamp timestamp;

    public TimedOpticalRecord(@NonNull OpticalRecord data, @NonNull Timestamp timestamp) {
        this.data = data;
        this.timestamp = timestamp;
    }

    public TimedOpticalRecord(@NonNull OpticalRecord data, long deviceBootTime, int globalSampleNumber, int sampleInterval) {
        this(data, new Timestamp(deviceBootTime, globalSampleNumber, sampleInterval));
    }

    @NonNull
    public OpticalRecord getData() {
        return this.data;
    }

    @NonNull
    public Timestamp getTimestamp() {
        return this.timestamp;
    }

    @Override
    @NonNull
    public String toString() {
        return this.timestamp + ": " + this.data;
    }

}
