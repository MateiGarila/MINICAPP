package app.uvtracker.sensor.pii.connection.application.event;

import androidx.annotation.NonNull;

import java.util.Date;

import app.uvtracker.data.type.Record;
import app.uvtracker.sensor.protocol.packet.in.PacketInNewSample;

public class NewSampleReceivedEvent {

    @NonNull
    private final Date localTimestamp;

    private final int remoteTimestamp;

    @NonNull
    private final Record record;

    public NewSampleReceivedEvent(@NonNull PacketInNewSample packet) {
        this.localTimestamp = new Date();
        this.remoteTimestamp = packet.getRemoteTimestamp();
        this.record = packet.getRecord();
    }

    @NonNull
    public Date getLocalTimestamp() {
        return localTimestamp;
    }

    public int getRemoteTimestamp() {
        return remoteTimestamp;
    }

    @NonNull
    public Record getRecord() {
        return record;
    }

}
