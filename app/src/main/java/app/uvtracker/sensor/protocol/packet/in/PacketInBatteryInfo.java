package app.uvtracker.sensor.protocol.packet.in;

import androidx.annotation.NonNull;

import app.uvtracker.data.battery.BatteryRecord;
import app.uvtracker.data.battery.BatteryStatus;
import app.uvtracker.sensor.protocol.codec.exception.PacketFormatException;
import app.uvtracker.sensor.protocol.packet.base.Packet;
import app.uvtracker.sensor.protocol.packet.base.PacketIn;
import app.uvtracker.sensor.protocol.util.Packing;

public class PacketInBatteryInfo extends PacketIn {

    private final BatteryRecord record;

    public PacketInBatteryInfo(Packet basePacket) throws PacketFormatException {
        super(basePacket);
        basePacket.requireLength(4);
        float batteryVoltage = (float)Packing.unpack2(this.payload, 0) / 1000.0f;
        int batteryPercentage = Math.min(Packing.unpack1(this.payload, 2), 100);
        int state = Packing.unpack1(this.payload, 3);
        BatteryStatus chargingStatus;
        switch(state) {
            default:
            case 0:
                chargingStatus = BatteryStatus.DISCHARGING;
                break;
            case 1:
                chargingStatus = BatteryStatus.CHARGING;
                break;
            case 2:
                chargingStatus = BatteryStatus.CHARGING_DONE;
                break;
        }
        this.record = new BatteryRecord(batteryVoltage, batteryPercentage, chargingStatus);
    }

    @NonNull
    public BatteryRecord getRecord() {
        return this.record;
    }

}
