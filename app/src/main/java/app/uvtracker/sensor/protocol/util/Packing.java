package app.uvtracker.sensor.protocol.util;

import androidx.annotation.NonNull;

import app.uvtracker.data.optical.OpticalRecord;

public class Packing {

    public static void pack1(@NonNull byte[] buffer, int start, int value) {
        buffer[start] = (byte)value;
    }

    public static void pack2(@NonNull byte[] buffer, int start, int value) {
        buffer[start    ] = (byte)(value >> 8);
        buffer[start + 1] = (byte)(value);
    }

    public static void pack3(@NonNull byte[] buffer, int start, int value) {
        buffer[start    ] = (byte)(value >> 16);
        buffer[start + 1] = (byte)(value >> 8);
        buffer[start + 2] = (byte)(value);
    }

    public static void pack4(@NonNull byte[] buffer, int start, int value) {
        buffer[start    ] = (byte)(value >> 24);
        buffer[start + 1] = (byte)(value >> 16);
        buffer[start + 2] = (byte)(value >> 8);
        buffer[start + 3] = (byte)(value);
    }

    public static int unpack1(@NonNull byte[] buffer, int start) {
        return Byte.toUnsignedInt(buffer[start]);
    }

    public static int unpack2(@NonNull byte[] buffer, int start) {
        return    (Byte.toUnsignedInt(buffer[start    ]) << 8)
                + (Byte.toUnsignedInt(buffer[start + 1]));
    }

    public static int unpack3(@NonNull byte[] buffer, int start) {
        return    (Byte.toUnsignedInt(buffer[start    ]) << 16)
                + (Byte.toUnsignedInt(buffer[start + 1]) << 8)
                + (Byte.toUnsignedInt(buffer[start + 2]));
    }

    public static int unpack4(@NonNull byte[] buffer, int start) {
        return    (Byte.toUnsignedInt(buffer[start    ]) << 24)
                + (Byte.toUnsignedInt(buffer[start + 1]) << 16)
                + (Byte.toUnsignedInt(buffer[start + 2]) << 8)
                + (Byte.toUnsignedInt(buffer[start + 3]));
    }

    @NonNull
    public static OpticalRecord unpackOpticalRecord(@NonNull byte[] buffer, int start) {
        byte uvRaw = buffer[start + 1];
        byte visRaw = buffer[start];
        if(uvRaw == -1 && visRaw == -1) return new OpticalRecord();
        return new OpticalRecord(
                (float)decompress8(uvRaw) / 10.9375f,
                (float)decompress44(visRaw) * 2.4f
        );
    }

    private static int decompress44(byte input0) {
        int input = Byte.toUnsignedInt(input0);
        int dig = (input & 0xF0) >> 4;
        int exp = (input & 0x0F);
        return dig << exp;
    }

    private static int decompress8(byte input0) {
        return Byte.toUnsignedInt(input0);
    }

}
