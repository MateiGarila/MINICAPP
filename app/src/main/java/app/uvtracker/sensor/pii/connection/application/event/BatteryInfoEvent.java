package app.uvtracker.sensor.pii.connection.application.event;

import androidx.annotation.NonNull;

import app.uvtracker.sensor.protocol.packet.in.PacketInBatteryInfo;

public class BatteryInfoEvent {

    private final float batteryVoltage;

    private final int batteryPercentage;

    private final boolean isPowerConnected;

    private final boolean isChargingCompleted;


    public BatteryInfoEvent(PacketInBatteryInfo packet) {
        this.batteryVoltage = packet.getBattertVoltage();
        this.batteryPercentage = packet.getBatteryPercentage();
        switch(packet.getChargingStatus()) {
            default:
            case DISCHARGING:
                this.isPowerConnected = false;
                this.isChargingCompleted = false;
                break;
            case CHARGING:
                this.isPowerConnected = true;
                this.isChargingCompleted = false;
                break;
            case CHARGING_DONE:
                this.isPowerConnected = true;
                this.isChargingCompleted = true;
                break;
        }
    }

    public float getBatteryVoltage() {
        return batteryVoltage;
    }

    public int getBatteryPercentage() {
        return batteryPercentage;
    }

    public boolean isPowerConnected() {
        return isPowerConnected;
    }

    public boolean isChargingCompleted() {
        return isChargingCompleted;
    }

    @NonNull
    @Override
    public String toString() {
        String msg;
        if(!this.isPowerConnected)
            msg = "DSG";
        else if(!this.isChargingCompleted)
            msg = "CHG";
        else
            msg = "FUL";
        return String.format("%.2fV(%d%%),%s", this.batteryVoltage, this.batteryPercentage, msg);
    }

}
