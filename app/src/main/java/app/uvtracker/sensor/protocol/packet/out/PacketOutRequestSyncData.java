package app.uvtracker.sensor.protocol.packet.out;

import app.uvtracker.sensor.protocol.packet.base.PacketOut;
import app.uvtracker.sensor.protocol.packet.type.PacketType;
import app.uvtracker.sensor.protocol.util.Packing;

public class PacketOutRequestSyncData extends PacketOut {

    public static final int MAX_COUNT = 125;

    public PacketOutRequestSyncData(int start, int count) {
        super(PacketType.OUT.REQUEST_SYNC_DATA, 5);
        if(start < 0) start = 0;
        if(count > MAX_COUNT || count < 0) count = MAX_COUNT;
        Packing.pack4(this.payload, 0, start);
        Packing.pack1(this.payload, 4, count);
    }

}
