package app.uvtracker.sensor.pii.connection.application.event;

import androidx.annotation.NonNull;

import app.uvtracker.data.optical.OpticalRecord;
import app.uvtracker.sensor.protocol.packet.in.PacketInNewOpticalSample;

public class NewSampleReceivedEvent {

    private final int seconds;

    @NonNull
    private final OpticalRecord record;

    public NewSampleReceivedEvent(@NonNull PacketInNewOpticalSample packet) {
        this.seconds = packet.getSampleSeconds();
        this.record = packet.getRecord();
    }

    public int getSeconds() {
        return seconds;
    }

    @NonNull
    public OpticalRecord getRecord() {
        return record;
    }

}
