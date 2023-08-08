package app.uvtracker.sensor.protocol.codec.exception;

public class MTUExceededException extends CodecException {

    public MTUExceededException() {
    }

    public MTUExceededException(String message) {
        super(message);
    }

}
