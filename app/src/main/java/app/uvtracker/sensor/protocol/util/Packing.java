package app.uvtracker.sensor.protocol.util;

public class Packing {

    public static void pack1(byte[] buffer, int start, int value) {
        buffer[start] = (byte)value;
    }

    public static void pack2(byte[] buffer, int start, int value) {
        buffer[start    ] = (byte)(value >> 8);
        buffer[start + 1] = (byte)(value);
    }

    public static void pack3(byte[] buffer, int start, int value) {
        buffer[start    ] = (byte)(value >> 16);
        buffer[start + 1] = (byte)(value >> 8);
        buffer[start + 2] = (byte)(value);
    }

    public static void pack4(byte[] buffer, int start, int value) {
        buffer[start    ] = (byte)(value >> 24);
        buffer[start + 1] = (byte)(value >> 16);
        buffer[start + 2] = (byte)(value >> 8);
        buffer[start + 3] = (byte)(value);
    }

    public static int unpack1(byte[] buffer, int start) {
        return Byte.toUnsignedInt(buffer[start]);
    }

    public static int unpack2(byte[] buffer, int start) {
        return    (Byte.toUnsignedInt(buffer[start    ]) << 8)
                + (Byte.toUnsignedInt(buffer[start + 1]));
    }

    public static int unpack3(byte[] buffer, int start) {
        return    (Byte.toUnsignedInt(buffer[start    ]) << 16)
                + (Byte.toUnsignedInt(buffer[start + 1]) << 8)
                + (Byte.toUnsignedInt(buffer[start + 2]));
    }

    public static int unpack4(byte[] buffer, int start) {
        return    (Byte.toUnsignedInt(buffer[start    ]) << 24)
                + (Byte.toUnsignedInt(buffer[start + 1]) << 16)
                + (Byte.toUnsignedInt(buffer[start + 2]) << 8)
                + (Byte.toUnsignedInt(buffer[start + 3]));
    }

}
