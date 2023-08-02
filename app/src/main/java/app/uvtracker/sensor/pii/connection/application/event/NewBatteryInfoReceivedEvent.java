package app.uvtracker.sensor.pii.connection.application.event;

import androidx.annotation.NonNull;

import app.uvtracker.data.battery.BatteryRecord;
import app.uvtracker.sensor.protocol.packet.in.PacketInBatteryInfo;

public class NewBatteryInfoReceivedEvent {

    @NonNull
    private final BatteryRecord record;

    public NewBatteryInfoReceivedEvent(PacketInBatteryInfo packet) {
        this.record = packet.getRecord();
    }

    @NonNull
    public BatteryRecord getRecord() {
        return this.record;
    }

    @NonNull
    @Override
    public String toString() {
        return this.record.toString();
    }

}
