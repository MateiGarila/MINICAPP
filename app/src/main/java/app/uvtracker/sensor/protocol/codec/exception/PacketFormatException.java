package app.uvtracker.sensor.protocol.codec.exception;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import app.uvtracker.sensor.protocol.packet.base.Packet;

public class PacketFormatException extends CodecException {

    public static void requireLength(Packet packet, int length) throws PacketFormatException {
        if(packet.getPayload().length != length)
            throw new PacketFormatException("Expecting " + length + " byte(s)." + packet.getPayload().length, packet);
    }

    @Nullable
    private final byte[] data;

    @Nullable
    private final Packet packet;

    public PacketFormatException(String message, @NonNull byte[] data) {
        super(message);
        this.data = data;
        this.packet = null;
    }

    public PacketFormatException(String message, @NonNull Packet packet) {
        super(message);
        this.data = null;
        this.packet = packet;
    }

    @Nullable
    @Override
    public String getMessage() {
        String superMessage = super.getMessage();
        if(superMessage == null) return null;
        StringBuilder sb = new StringBuilder(superMessage);
        if(this.data != null) {
            sb.append(" Packet dump: {");
            for(byte a : this.data) {
                sb.append(String.format("%02x, ", a));
            }
            sb.append("}");
        }
        if (this.packet != null) {
            sb.append(" Packet dump: ");
            sb.append(this.packet);
        }
        return sb.toString();
    }
}
