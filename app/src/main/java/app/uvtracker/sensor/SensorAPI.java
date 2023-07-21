package app.uvtracker.sensor;

import android.content.Context;

import androidx.annotation.NonNull;

import app.uvtracker.sensor.pdi.androidble.scanner.AndroidBLEScannerImpl;
import app.uvtracker.sensor.pii.scanner.IScanner;
import app.uvtracker.sensor.pii.scanner.exception.TransceiverException;

public class SensorAPI {

    @NonNull
    public static IScanner getScanner(@NonNull Context context) throws TransceiverException {
        return new AndroidBLEScannerImpl(context);
    }

}
