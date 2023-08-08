package app.uvtracker.data.battery;

import androidx.annotation.NonNull;

import java.util.Locale;

public class BatteryRecord {

    public final float voltage;

    public final int percentage;

    @NonNull
    public final BatteryStatus chargingStatus;

    public BatteryRecord(float voltage, int percentage, @NonNull BatteryStatus chargingStatus) {
        this.voltage = voltage;
        this.percentage = Math.min(percentage, 100);
        this.chargingStatus = chargingStatus;
    }

    @NonNull
    @Override
    public String toString() {
        return String.format(Locale.getDefault(),
                "%.02f(%d),%s",
                this.voltage,
                this.percentage,
                this.chargingStatus
        );
    }

}
