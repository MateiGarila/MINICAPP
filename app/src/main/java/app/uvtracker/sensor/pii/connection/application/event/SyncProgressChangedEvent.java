package app.uvtracker.sensor.pii.connection.application.event;

import androidx.annotation.NonNull;

public class SyncProgressChangedEvent {

    public enum Stage {
        INITIATING,
        PROCESSING,
        DONE_NOUPDATE,
        DONE,
        ABORTED
    }

    @NonNull
    private final Stage stage;

    private final int progress;

    public SyncProgressChangedEvent(Stage stage) {
        this.stage = stage;
        switch(stage) {
            case INITIATING:
                this.progress = 0;
                break;
            case DONE_NOUPDATE:
            case DONE:
            case ABORTED:
                this.progress = 100;
                break;
            default:
                this.progress = 1;
                break;
        }
    }

    public SyncProgressChangedEvent(@NonNull Stage stage, int progress) {
        this.stage = stage;
        this.progress = progress;
    }

    @NonNull
    public Stage getStage() {
        return stage;
    }

    public int getProgress() {
        return progress;
    }

}
