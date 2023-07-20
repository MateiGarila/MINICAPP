package app.uvtracker.sensor.pii.connection.bytestream;

import androidx.annotation.NonNull;

import app.uvtracker.sensor.pii.connection.shared.IConnectable;

public interface ISensorBytestreamConnection extends IConnectable {

    // Emits event: BytesReceivedEvent

    boolean write(@NonNull byte[] data);

}
