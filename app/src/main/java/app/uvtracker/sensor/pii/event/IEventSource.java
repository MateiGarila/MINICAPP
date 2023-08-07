package app.uvtracker.sensor.pii.event;

public interface IEventSource {

    void registerListener(IEventListener listener);

    void registerListenerClass(IEventListener listener);

    boolean unregisterListener(IEventListener listener);

    boolean unregisterListenerClass(IEventListener listener);

    boolean unregisterListenerClass(Class<? extends IEventListener> clazz);

    default void unregisterAll() {
        throw new UnsupportedOperationException("Unregistering event handlers is not supported.");
    }

}
