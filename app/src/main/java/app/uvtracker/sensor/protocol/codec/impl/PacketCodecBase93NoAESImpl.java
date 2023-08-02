package app.uvtracker.sensor.protocol.codec.impl;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import app.uvtracker.sensor.protocol.codec.IPacketCodec;
import app.uvtracker.sensor.protocol.codec.exception.CodecException;
import app.uvtracker.sensor.protocol.codec.exception.MTUExceededException;
import app.uvtracker.sensor.protocol.codec.exception.PacketFormatException;
import app.uvtracker.sensor.protocol.codec.helper.Base93Helper;
import app.uvtracker.sensor.protocol.codec.helper.CRC8Helper;
import app.uvtracker.sensor.protocol.packet.base.Packet;
import app.uvtracker.sensor.protocol.packet.type.PacketDirection;
import app.uvtracker.sensor.protocol.packet.type.PacketType;

public class PacketCodecBase93NoAESImpl implements IPacketCodec {

    private static final String TAG = PacketCodecBase93NoAESImpl.class.getSimpleName();

    private static PacketCodecBase93NoAESImpl instance;

    public static PacketCodecBase93NoAESImpl getInstance() {
        if(PacketCodecBase93NoAESImpl.instance == null)
            PacketCodecBase93NoAESImpl.instance = new PacketCodecBase93NoAESImpl();
        return PacketCodecBase93NoAESImpl.instance;
    }

    public static final int MTU = 122; // 128 - 3 (BLE) - 3 (FSM)

    @NonNull
    @Override
    public String encode(@NonNull Packet packet) throws CodecException {
        byte[] serialized = this.serialize(packet);
        String encoded = Base93Helper.encode(serialized);
        if(encoded.length() > MTU) throw new MTUExceededException("Device MTU exceeded.");
        return encoded;
    }

    @NonNull
    private byte[] serialize(@NonNull Packet packet) throws CodecException {
        int length = packet.getPayload().length;
        if(length > 255) throw new MTUExceededException("Protocol MTU exceeded.");
        byte[] data = new byte[length + 3];
        data[0] = this.serializeHeader(packet.getType());
        data[1] = (byte)length;
        System.arraycopy(packet.getPayload(), 0, data, 2, packet.getPayload().length);
        data[length + 2] = CRC8Helper.compute(data, length + 2);
        return data;
    }

    private byte serializeHeader(@NonNull PacketType type) {
        if(type.getID() > 127 || type.getID() < 0) throw new IllegalArgumentException("PacketID " + type.getID() + " out of range. Check code for bugs.");
        byte header = (byte)type.getID();
        if(type.getDirection() == PacketDirection.IN) header |= 0x80;
        return header;
    }


    @NonNull
    @Override
    public Packet decode(@NonNull String packet) throws CodecException {
        Packet packetBase = this.deserialize(Base93Helper.decode(packet));
        Constructor<? extends Packet> constructor = packetBase.getType().getPacketConstructor();
        if(constructor == null) {
            Log.d(TAG, new PacketFormatException("Packed is not down-constructable (maybe it's an OUT packet?)", packetBase).getMessage());
            return packetBase;
        }
        try {
            return constructor.newInstance(packetBase);
        }
        catch(IllegalAccessException | InstantiationException e) {
            Log.d(TAG, new PacketFormatException("Exception in reflective instantiation process (maybe it's an OUT packet?)", packetBase).getMessage());
            return packetBase;
        }
        catch (InvocationTargetException e) {
            Log.d(TAG, "Exception in down-construction process. Is the packet corrupted?");
            Throwable cause = e.getCause();
            if(cause != null) {
                String message = cause.getMessage();
                if(message == null) message = "Trace:";
                Log.d(TAG, message, cause);
            }
            return packetBase;
        }
    }

    @NonNull
    private Packet deserialize(@NonNull byte[] data) throws CodecException {
        if(data.length < 3) throw new PacketFormatException("Packet is too small.", data);
        PacketType type = this.deserializeHeader(data[0]);
        if(type == null) throw new PacketFormatException("Packet ID " + (data[0] & 0x7F) + " does not exist.", data);
        int length = Byte.toUnsignedInt(data[1]);
        if(data.length < length + 3) throw new PacketFormatException("Packet is too small and does not contain necessary fields.", data);
        byte crc = CRC8Helper.compute(data, length + 2);
        if(crc != data[length + 2]) throw new PacketFormatException("Packet is corrupted.", data);
        byte[] payload = new byte[length];
        System.arraycopy(data, 2, payload, 0, length);
        return new Packet(type, payload);
    }

    @Nullable
    private PacketType deserializeHeader(byte header) {
        if((header & 0x80) == 0) { // OUT
            return PacketType.OUT.getByID(header & 0x7F);
        }
        else { // IN
            return PacketType.IN.getByID(header & 0x7F);
        }
    }

}
