package app.uvtracker.sensor.protocol.packet.in;

import androidx.annotation.NonNull;

import app.uvtracker.data.optical.OpticalRecord;
import app.uvtracker.sensor.protocol.codec.exception.PacketFormatException;
import app.uvtracker.sensor.protocol.packet.base.Packet;
import app.uvtracker.sensor.protocol.packet.base.PacketIn;
import app.uvtracker.sensor.protocol.util.Packing;

public class PacketInSyncData extends PacketIn {

    private final int startSample;

    @NonNull
    private final OpticalRecord[] records;

    public PacketInSyncData(Packet packetBase) throws PacketFormatException {
        super(packetBase);
        packetBase.requireAtLeastLength(5);
        this.startSample = Packing.unpack4(this.payload, 0);
        int length = Packing.unpack1(this.payload, 4);
        packetBase.requireLength(5 + 2 * length);
        this.records = new OpticalRecord[length];
        for(int i = 0; i < length; i++) {
            this.records[i] = Packing.unpackOpticalRecord(this.payload, 5 + 2 * i);
        }
    }

    public int getStartSample() {
        return this.startSample;
    }

    @NonNull
    public OpticalRecord[] getRecords() {
        return this.records;
    }

}
