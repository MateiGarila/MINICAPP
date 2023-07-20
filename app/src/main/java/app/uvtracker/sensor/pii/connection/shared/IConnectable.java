package app.uvtracker.sensor.pii.connection.shared;

import app.uvtracker.sensor.pii.event.IEventSource;

public interface IConnectable extends IEventSource {

    // Emits event: ConnectionStateChangeEvent

    void reset();

    boolean connect();

    boolean disconnect();

}
