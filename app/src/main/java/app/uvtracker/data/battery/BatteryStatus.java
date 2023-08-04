package app.uvtracker.data.battery;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public enum BatteryStatus {
    DISCHARGING("DSG"),
    CHARGING("CHG"),
    CHARGING_DONE("FULL");

    @Nullable
    private final String abbr;

    BatteryStatus() {
        this.abbr = null;
    }

    BatteryStatus(@NonNull String abbr) {
        this.abbr = abbr;
    }

    @NonNull
    @Override
    public String toString() {
        if(this.abbr != null) return this.abbr;
        return super.toString();
    }

}
