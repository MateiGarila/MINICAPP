package app.uvtracker.sensor.pii.connection.bytestream.event;

import androidx.annotation.NonNull;

public class BytesReceivedEvent {

    @NonNull
    private final byte[] data;

    public BytesReceivedEvent(@NonNull byte[] data) {
        this.data = data;
    }

    @NonNull
    public byte[] getData() {
        return this.data;
    }

}
