package app.uvtracker.sensor.pii.event;

public interface IEventSource {

    void registerListener(IEventListener listener);

    default void unregisterAll() {
        throw new UnsupportedOperationException("Unregistering event handlers is not supported.");
    }

}
