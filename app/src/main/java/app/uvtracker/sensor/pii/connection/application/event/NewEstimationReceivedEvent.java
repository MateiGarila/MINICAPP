package app.uvtracker.sensor.pii.connection.application.event;

import androidx.annotation.NonNull;

import app.uvtracker.data.optical.OpticalRecord;
import app.uvtracker.sensor.protocol.packet.in.PacketInNewOpticalEstimation;

public class NewEstimationReceivedEvent {

    private final int sampleNumber;

    private final int sampleInterval;

    @NonNull
    private final OpticalRecord record;

    public NewEstimationReceivedEvent(@NonNull PacketInNewOpticalEstimation packet) {
        this.sampleNumber = packet.getSampleNumber();
        this.sampleInterval = packet.getSampleInterval();
        this.record = packet.getRecord();
    }

    public int getSampleNumber() {
        return sampleNumber;
    }

    public int getSampleInterval() {
        return sampleInterval;
    }

    @NonNull
    public OpticalRecord getRecord() {
        return record;
    }

}
