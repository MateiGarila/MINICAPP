package app.uvtracker.sensor.protocol.codec.helper;

import androidx.annotation.NonNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

public class Base93Helper {

    public static int encodeSymbol(int symbol) {
        if(symbol < 0 || symbol > 93) throw new IllegalArgumentException("Unable to encode value " + symbol);
        symbol += '!';
        if(symbol >= '+') symbol++;
        return symbol;
    }

    public static int decodeSymbol(int symbol) {
        if(symbol < ' ' || symbol > '~' || symbol == '+')
            throw new IllegalArgumentException("Unable to decode '" + (char)symbol + "' (" + symbol + ")");
        if(symbol > '+') symbol--;
        return symbol - '!';
    }

    public static int estimateEncodedSize(int byteArrayLength) {
        int blocks = byteArrayLength / 4 + (byteArrayLength % 4 == 0 ? 0 : 1);
        return blocks * 5;
    }

    public static int estimateDecodedSize(int stringLength) {
        int blocks = stringLength / 5;
        return blocks * 4;
    }

    // Encode 4 bytes from input stream to 5 characters in output buffer
    // Returns whether the block is the last block
    public static boolean blockEncode(@NonNull ByteArrayInputStream istream, @NonNull ByteArrayOutputStream ostream) {
        if(istream.available() <= 0) return true;
        long bigint = 0;
        for(int i = 0; i < 4; i++) {
            long data = istream.read();
            if(data < 0) data = 0;
            bigint = (bigint >> 8) + (data << 24);
        }
        for(int i = 0; i < 5; i++) {
            ostream.write(Base93Helper.encodeSymbol((int)(bigint % 93L)));
            bigint /= 93L;
        }
        return istream.available() <= 0;
    }

    // Decode 5 characters from input stream to 4 bytes in output buffer
    // Returns whether the block is the last block
    public static boolean blockDecode(@NonNull ByteArrayInputStream istream, @NonNull ByteArrayOutputStream ostream) {
        long bigint = 0;
        long weight = 1;
        for(int i = 0; i < 5; i++) {
            int data = istream.read();
            if(data < 0) return true;
            bigint += Base93Helper.decodeSymbol(data) * weight;
            weight *= 93L;
        }
        for(int i = 0; i < 4; i++) {
            ostream.write((int)(bigint & 0xFF));
            bigint >>= 8;
        }
        return false;
    }

    @NonNull
    public static String encode(@NonNull byte[] input) {
        ByteArrayInputStream istream = new ByteArrayInputStream(input);
        ByteArrayOutputStream ostream = new ByteArrayOutputStream(Base93Helper.estimateEncodedSize(input.length));
        //noinspection StatementWithEmptyBody
        while(!Base93Helper.blockEncode(istream, ostream));
        try {
            return ostream.toString(StandardCharsets.US_ASCII.toString());
        } catch (UnsupportedEncodingException e) {
            // This should never happen
            throw new RuntimeException(e);
        }

    }

    @NonNull
    public static byte[] decode(@NonNull String input) {
        ByteArrayInputStream istream;
        istream = new ByteArrayInputStream(input.getBytes(StandardCharsets.US_ASCII));
        ByteArrayOutputStream ostream = new ByteArrayOutputStream(Base93Helper.estimateDecodedSize(istream.available()));
        //noinspection StatementWithEmptyBody
        while(!Base93Helper.blockDecode(istream, ostream));
        return ostream.toByteArray();
    }

}
