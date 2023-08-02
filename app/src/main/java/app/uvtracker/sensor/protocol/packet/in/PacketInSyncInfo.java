package app.uvtracker.sensor.protocol.packet.in;

import androidx.annotation.NonNull;

import app.uvtracker.sensor.protocol.codec.exception.PacketFormatException;
import app.uvtracker.sensor.protocol.packet.base.Packet;
import app.uvtracker.sensor.protocol.packet.base.PacketIn;
import app.uvtracker.sensor.protocol.util.Packing;

public class PacketInSyncInfo extends PacketIn {

    // Start sample number
    private final int sampleStart;

    // End sample number
    private final int sampleCount;

    // Current second counter value
    private final int currentSecondCounter;

    // Sample interval in seconds
    private final int sampleInterval;

    public PacketInSyncInfo(@NonNull Packet packetBase) throws PacketFormatException {
        super(packetBase);
        packetBase.requireLength(10);
        this.sampleStart =          Packing.unpack4(packetBase.getPayload(), 0);
        this.sampleCount =          Packing.unpack4(packetBase.getPayload(), 4);
        this.currentSecondCounter = Packing.unpack1(packetBase.getPayload(), 8);
        this.sampleInterval =       Packing.unpack1(packetBase.getPayload(), 9);

    }

    public int getSampleStart() {
        return this.sampleStart;
    }

    public int getSampleCount() {
        return this.sampleCount;
    }

    public int getCurrentSecondCounter() {
        return this.currentSecondCounter;
    }

    public int getSampleInterval() {
        return this.sampleInterval;
    }

}
