package app.uvtracker.sensor.protocol.packet.base;

import androidx.annotation.NonNull;

import app.uvtracker.sensor.protocol.packet.type.PacketType;

public class Packet {

    @NonNull
    protected final PacketType type;

    @NonNull
    protected byte[] payload;

    public Packet(@NonNull PacketType type, @NonNull byte[] payload) {
        this.type = type;
        this.payload = payload;
    }

    public Packet(@NonNull PacketType type, int payloadSize) {
        this(type, new byte[payloadSize]);
    }

    public Packet(@NonNull PacketType type) {
        this(type, 0);
    }

    public Packet(@NonNull Packet obj) {
        // Note: this constructor does not copy internal payload array
        this(obj.type, obj.payload);
    }

    @NonNull
    public PacketType getType() {
        return type;
    }

    public boolean isBaseType() {
        return true;
    }

    @NonNull
    public byte[] getPayload() {
        // Note: this method does not copy internal payload array
        return payload;
    }

    public void setPayload(@NonNull byte[] payload) {
        this.payload = payload;
    }

    @Override
    @NonNull
    public String toString() {
        StringBuilder sb = new StringBuilder(this.payload.length * 3 + 1);
        sb.append("[");
        for(byte data : this.payload) sb.append(String.format("%02x, ", data));
        sb.append("]");
        if(this.isBaseType())
            return this.type + "(BASE)" + sb;
        else
            return this.type + sb.toString();
    }

}
