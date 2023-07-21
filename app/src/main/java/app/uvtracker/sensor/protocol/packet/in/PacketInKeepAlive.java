package app.uvtracker.sensor.protocol.packet.in;

import androidx.annotation.NonNull;

import app.uvtracker.sensor.protocol.packet.base.Packet;
import app.uvtracker.sensor.protocol.packet.base.PacketIn;
import app.uvtracker.sensor.protocol.util.Packing;

public class PacketInKeepAlive extends PacketIn {

    private final int keepAliveID;

    public PacketInKeepAlive(Packet basePacket) {
        super(basePacket);
        if(this.payload.length == 4) {
            this.keepAliveID = Packing.unpack4(this.payload, 4);
        }
        else {
            this.keepAliveID = -1;
        }
    }

    public int getKeepAliveID() {
        return this.keepAliveID;
    }

    @Override
    @NonNull
    public String toString() {
        if(this.keepAliveID < 0)
            return this.type + "{}";
        else
            return this.type + "{" + this.keepAliveID + "}";
    }

}
