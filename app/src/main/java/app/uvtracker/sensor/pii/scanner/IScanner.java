package app.uvtracker.sensor.pii.scanner;

import androidx.annotation.NonNull;

import app.uvtracker.sensor.pii.ISensor;
import app.uvtracker.sensor.pii.event.IEventSource;
import app.uvtracker.sensor.pii.scanner.exception.TransceiverException;

public interface IScanner extends IEventSource {

    // Emits event: SensorScannedEvent

    boolean isScanning();

    void startScanning() throws TransceiverException;

    void stopScanning() throws TransceiverException;

    @NonNull
    ISensor[] getSnapshot();

}
