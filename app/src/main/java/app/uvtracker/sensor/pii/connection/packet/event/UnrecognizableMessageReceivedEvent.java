package app.uvtracker.sensor.pii.connection.packet.event;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.nio.charset.StandardCharsets;

public class UnrecognizableMessageReceivedEvent {

    @NonNull
    private final String message;

    @Nullable
    private String unicodeMessage;

    public UnrecognizableMessageReceivedEvent(@NonNull String message) {
        this.message = message;
    }

    @NonNull
    public String getMessage() {
        return message;
    }

    @NonNull
    public byte[] getMessageAsBytes() {
        return message.getBytes(StandardCharsets.US_ASCII);
    }

    @NonNull
    public String getMessageAsUnicode() {
        if(this.unicodeMessage == null)
            this.unicodeMessage = new String(message.trim().getBytes(StandardCharsets.US_ASCII), StandardCharsets.UTF_8);
        return this.unicodeMessage;
    }

}
