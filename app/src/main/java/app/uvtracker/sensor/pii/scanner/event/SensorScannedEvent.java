package app.uvtracker.sensor.pii.scanner.event;

import androidx.annotation.NonNull;

import java.util.Collection;

import app.uvtracker.sensor.pii.ISensor;

public class SensorScannedEvent {

    @NonNull
    private final ISensor sensor;

    private final boolean isFirstTime;

    @NonNull
    private final Collection<? extends ISensor> sensors;

    public SensorScannedEvent(@NonNull ISensor sensor, boolean isFirstTime, @NonNull Collection<? extends ISensor> sensors) {
        this.sensor = sensor;
        this.isFirstTime = isFirstTime;
        this.sensors = sensors;
    }

    @NonNull
    public ISensor getSensor() {
        return sensor;
    }

    public boolean isFirstTime() {
        return isFirstTime;
    }

    @NonNull
    public Collection<? extends ISensor> getSensors() {
        return sensors;
    }

}
