package app.uvtracker.sensor.protocol.packet.base;

import androidx.annotation.NonNull;

import app.uvtracker.sensor.protocol.packet.type.PacketType;

public abstract class PacketIn extends Packet {

    public PacketIn(@NonNull PacketType type, @NonNull byte[] payload) {
        super(type, payload);
    }

    public PacketIn(@NonNull PacketType type, int payloadSize) {
        super(type, payloadSize);
    }

    public PacketIn(@NonNull PacketType type) {
        super(type);
    }

    public PacketIn(@NonNull Packet obj) {
        super(obj);
    }

    @Override
    public boolean isBaseType() {
        return false;
    }

}
