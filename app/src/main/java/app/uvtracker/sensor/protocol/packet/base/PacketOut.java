package app.uvtracker.sensor.protocol.packet.base;

import androidx.annotation.NonNull;

import app.uvtracker.sensor.protocol.packet.type.PacketType;

public abstract class PacketOut extends Packet {

    public PacketOut(@NonNull PacketType type, @NonNull byte[] payload) {
        super(type, payload);
    }

    public PacketOut(@NonNull PacketType type, int payloadSize) {
        super(type, payloadSize);
    }

    public PacketOut(@NonNull PacketType type) {
        super(type);
    }

    public PacketOut(@NonNull Packet obj) {
        super(obj);
    }

    @Override
    public boolean isBaseType() {
        return false;
    }

}
