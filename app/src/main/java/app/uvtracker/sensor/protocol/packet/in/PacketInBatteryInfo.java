package app.uvtracker.sensor.protocol.packet.in;

import androidx.annotation.NonNull;

import app.uvtracker.sensor.protocol.codec.exception.PacketFormatException;
import app.uvtracker.sensor.protocol.packet.base.Packet;
import app.uvtracker.sensor.protocol.packet.base.PacketIn;
import app.uvtracker.sensor.protocol.util.Packing;

public class PacketInBatteryInfo extends PacketIn {

    public enum BatteryStatus {
        DISCHARGING,
        CHARGING,
        CHARGING_DONE,
    }

    private final float battertVoltage;

    private final int batteryPercentage;

    @NonNull
    private final BatteryStatus chargingStatus;

    public PacketInBatteryInfo(Packet basePacket) throws PacketFormatException {
        super(basePacket);
        basePacket.requireLength(4);
        this.battertVoltage = (float)Packing.unpack2(this.payload, 0) / 1000.0f;
        this.batteryPercentage = Math.min(Packing.unpack1(this.payload, 2), 100);
        int state = Packing.unpack1(this.payload, 3);
        switch(state) {
            default:
            case 0:
                this.chargingStatus = BatteryStatus.DISCHARGING;
                break;
            case 1:
                this.chargingStatus = BatteryStatus.CHARGING;
                break;
            case 2:
                this.chargingStatus = BatteryStatus.CHARGING_DONE;
                break;
        }
    }

    public float getBattertVoltage() {
        return battertVoltage;
    }

    public int getBatteryPercentage() {
        return batteryPercentage;
    }

    @NonNull
    public BatteryStatus getChargingStatus() {
        return chargingStatus;
    }

}
