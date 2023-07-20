package app.uvtracker.sensor.protocol.packet.out;

import app.uvtracker.sensor.protocol.packet.base.PacketOut;
import app.uvtracker.sensor.protocol.packet.type.PacketType;

public class PacketOutFlashLED extends PacketOut {

    public PacketOutFlashLED() {
        super(PacketType.OUT.FLASH_LED);
    }

}
