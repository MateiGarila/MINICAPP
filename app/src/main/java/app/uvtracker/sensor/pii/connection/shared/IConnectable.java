package app.uvtracker.sensor.pii.connection.shared;

import androidx.annotation.NonNull;

import app.uvtracker.sensor.pii.ISensor;
import app.uvtracker.sensor.pii.event.IEventSource;

public interface IConnectable extends IEventSource {

    @NonNull
    ISensor getSensor();

    // Emits event: ConnectionStateChangeEvent

    void reset();

    boolean connect();

    boolean disconnect();

}
