package app.uvtracker.sensor.protocol.packet.out;

import app.uvtracker.sensor.protocol.packet.base.PacketOut;
import app.uvtracker.sensor.protocol.packet.type.PacketType;

public class PacketOutRequestSyncInfo extends PacketOut {

    public PacketOutRequestSyncInfo() {
        super(PacketType.OUT.REQUEST_SYNC_INFO);
    }

}
