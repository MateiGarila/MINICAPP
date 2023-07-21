package app.uvtracker.sensor.protocol.packet.out;

import app.uvtracker.sensor.protocol.packet.base.PacketOut;
import app.uvtracker.sensor.protocol.packet.type.PacketType;
import app.uvtracker.sensor.protocol.util.Packing;

public class PacketOutKeepAlive extends PacketOut {

    public PacketOutKeepAlive() {
        super(PacketType.OUT.KEEP_ALIVE);
    }

    public PacketOutKeepAlive(int keepAliveID) {
        super(PacketType.OUT.KEEP_ALIVE, 4);
        this.setKeepAliveID(keepAliveID);
    }

    public void setKeepAliveID(int keepAliveID) {
        if(this.payload.length != 4)
            this.payload = new byte[4];
        Packing.pack4(this.payload, 0, keepAliveID);
    }

}
