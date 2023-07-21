package app.uvtracker.sensor.protocol.codec;

import androidx.annotation.NonNull;

import app.uvtracker.sensor.protocol.codec.exception.CodecException;
import app.uvtracker.sensor.protocol.codec.impl.PacketCodecBase93NoAESImpl;
import app.uvtracker.sensor.protocol.packet.base.Packet;

public interface IPacketCodec {

    static IPacketCodec get() {
        return PacketCodecBase93NoAESImpl.getInstance();
    }

    @NonNull
    String encode(@NonNull Packet packet) throws CodecException;

    @NonNull
    Packet decode(@NonNull String packet) throws CodecException;

}
