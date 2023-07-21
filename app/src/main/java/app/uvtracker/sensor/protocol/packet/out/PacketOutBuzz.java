package app.uvtracker.sensor.protocol.packet.out;

import app.uvtracker.sensor.protocol.packet.base.PacketOut;
import app.uvtracker.sensor.protocol.packet.type.PacketType;

public class PacketOutBuzz extends PacketOut {

    public PacketOutBuzz() {
        super(PacketType.OUT.BUZZ);
    }

}
