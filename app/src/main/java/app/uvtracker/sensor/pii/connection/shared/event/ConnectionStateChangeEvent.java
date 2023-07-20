package app.uvtracker.sensor.pii.connection.shared.event;

import androidx.annotation.NonNull;

public class ConnectionStateChangeEvent {

    public enum State {
        CONNECTING,
        ESTABLISHED,
        DISCONNECTING,
        DISCONNECTED,
        FAILED_RETRY,
        FAILED_NO_RETRY,
    }

    @NonNull
    private final State state;

    private final int percentage;

    public ConnectionStateChangeEvent(@NonNull State state, int estimatedPercentage) {
        this.state = state;
        this.percentage = estimatedPercentage;
    }

    @NonNull
    public State getStage() {
        return this.state;
    }

    public int getPercentage() {
        return this.percentage;
    }

}
