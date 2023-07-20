package app.uvtracker.sensor.protocol.packet.type;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.Collectors;

import app.uvtracker.sensor.protocol.packet.base.Packet;
import app.uvtracker.sensor.protocol.packet.in.PacketInButtonInteract;
import app.uvtracker.sensor.protocol.packet.in.PacketInDebug;
import app.uvtracker.sensor.protocol.packet.in.PacketInKeepAlive;
import app.uvtracker.sensor.protocol.packet.in.PacketInNewSample;
import app.uvtracker.sensor.protocol.packet.out.PacketOutBuzz;
import app.uvtracker.sensor.protocol.packet.out.PacketOutFlashLED;
import app.uvtracker.sensor.protocol.packet.out.PacketOutKeepAlive;

/*
    Packet ID range conventions:
        0x00 to 0x1F: System packets
        0x20 to 0x3F: Data transfer & request packets
        0x40 to 0x5F: HMI packets
        0x60 to 0x6F: Reserved
        0x70 to 0x7F: Debug & tracing
 */
public interface PacketType {

    enum IN implements PacketType {

        // Debug
        DEBUG           (0x7F, PacketInDebug.class),
        // System
        KEEP_ALIVE      (0x00, PacketInKeepAlive.class),
        // Data transfer
        NEW_SAMPLE(0x20, PacketInNewSample.class),
        // HMI
        BUTTON_INTERACT (0x40, PacketInButtonInteract.class),

        ;

        private final int packetID;

        @NonNull
        private final Class<? extends Packet> clazz;

        IN(int packetID, @NonNull Class<? extends Packet> clazz) {
            this.packetID = packetID;
            this.clazz = clazz;
        }

        @Override
        public int getID() {
            return this.packetID;
        }

        @Override
        @NonNull
        public PacketDirection getDirection() {
            return PacketDirection.IN;
        }

        @Override
        @NonNull
        public Class<? extends Packet> getPacketClass() {
            return this.clazz;
        }

        @Override
        @NonNull
        public String toString() {
            return "PacketIn" + Arrays.stream(super.toString().split("_")).map((s) -> s.substring(0,1).toUpperCase() + s.substring(1).toLowerCase()).collect(Collectors.joining());
        }

        private static final HashMap<Integer, IN> map = new HashMap<>();

        static {
            for(IN e : IN.values()) map.put(e.packetID, e);
        }

        @Nullable
        public static PacketType getByID(int packetID) {
            return IN.map.get(packetID);
        }

    }

    enum OUT implements PacketType {

        // System
        KEEP_ALIVE      (0x00, PacketOutKeepAlive.class),
        // HMI
        BUZZ            (0x41, PacketOutBuzz.class),
        FLASH_LED       (0x42, PacketOutFlashLED.class),

        ;

        private final int packetID;

        @NonNull
        private final Class<? extends Packet> clazz;

        OUT(int packetID, @NonNull Class<? extends Packet> clazz) {
            this.packetID = packetID;
            this.clazz = clazz;
        }

        @Override
        public int getID() {
            return this.packetID;
        }

        @Override
        @NonNull
        public PacketDirection getDirection() {
            return PacketDirection.OUT;
        }

        @Override
        @NonNull
        public Class<? extends Packet> getPacketClass() {
            return this.clazz;
        }

        @Override
        @NonNull
        public String toString() {
            return "PacketOut" + Arrays.stream(super.toString().split("_")).map((s) -> s.substring(0,1).toUpperCase() + s.substring(1).toLowerCase()).collect(Collectors.joining());
        }

        private static final HashMap<Integer, OUT> map = new HashMap<>();

        static {
            for(OUT e : OUT.values()) map.put(e.packetID, e);
        }

        @Nullable
        public static PacketType getByID(int packetID) {
            return OUT.map.get(packetID);
        }

    }


    int getID();

    @NonNull
    PacketDirection getDirection();

    @NonNull
    Class<? extends Packet> getPacketClass();

    @Nullable
    default Constructor<? extends Packet> getPacketConstructor() {
        try {
            return this.getPacketClass().getConstructor(Packet.class);
        } catch (NoSuchMethodException | SecurityException e) {
            return null;
        }
    }

    @Override
    @NonNull
    String toString();

}
