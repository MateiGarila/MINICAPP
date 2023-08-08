package app.uvtracker.sensor.protocol.codec.helper;

import androidx.annotation.NonNull;

// The class implements CRC8_SAE_J1850. Source: http://www.sunshine2k.de/coding/javascript/crc/crc_js.html
public class CRC8Helper {

    public static final byte POLY = (byte)0x1D;
    public static final byte IV   = (byte)0xFF;
    public static final byte XOR  = (byte)0xFF;

    public static byte compute(@NonNull byte[] data) {
        return CRC8Helper.compute(data, data.length);
    }

    public static byte compute(@NonNull byte[] data, int length) {
        if(length > data.length) length = data.length;
        byte crc = IV;
        for(int i = 0; i < length; i++) {
            byte b = data[i];
            crc ^= b;
            for(int j = 0; j < 8; j++) {
                byte t = (byte)(crc << 1);
                if((crc & 0x80) != 0) {
                    t ^= POLY;
                }
                crc = t;
            }
        }
        return (byte)(crc ^ XOR);
    }

}
