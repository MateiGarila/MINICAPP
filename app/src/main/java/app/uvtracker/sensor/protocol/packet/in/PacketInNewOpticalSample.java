package app.uvtracker.sensor.protocol.packet.in;

import androidx.annotation.NonNull;

import app.uvtracker.data.optical.OpticalRecord;
import app.uvtracker.sensor.protocol.codec.exception.PacketFormatException;
import app.uvtracker.sensor.protocol.packet.base.Packet;
import app.uvtracker.sensor.protocol.packet.base.PacketIn;
import app.uvtracker.sensor.protocol.util.Packing;

public class PacketInNewOpticalSample extends PacketIn {

    private final int seconds;

    @NonNull
    private final OpticalRecord record;

    public PacketInNewOpticalSample(@NonNull Packet packetBase) throws PacketFormatException {
        super(packetBase);
        packetBase.requireLength(6);
        this.seconds = Packing.unpack4(this.payload, 0);
        this.record = Packing.unpackOpticalRecord(this.payload, 4);
    }

    public int getSampleSeconds() {
        return this.seconds;
    }

    @NonNull
    public OpticalRecord getRecord() {
        return record;
    }

}
