package app.uvtracker.sensor.protocol.packet.in;

import androidx.annotation.NonNull;

import app.uvtracker.data.type.Record;
import app.uvtracker.sensor.protocol.codec.exception.PacketFormatException;
import app.uvtracker.sensor.protocol.packet.base.Packet;
import app.uvtracker.sensor.protocol.packet.base.PacketIn;
import app.uvtracker.sensor.protocol.util.Packing;

public class PacketInNewSample extends PacketIn {

    private final int remoteTimestamp;

    @NonNull
    private final Record record;

    public PacketInNewSample(Packet packetBase) throws PacketFormatException {
        super(packetBase);
        PacketFormatException.requireLength(packetBase, 7);
        this.remoteTimestamp = Packing.unpack4(this.payload, 0) * 60 + Packing.unpack1(this.payload, 4);
        this.record = Record.decompress(this.payload[6], this.payload[5]);
    }

    public int getRemoteTimestamp() {
        return this.remoteTimestamp;
    }

    @NonNull
    public Record getRecord() {
        return this.record;
    }

    @Override
    @NonNull
    public String toString() {
        int day = 0;
        int hour = 0;
        int minute = 0;
        int second = this.remoteTimestamp;
        if(second >= 60) {
            minute = second / 60;
            second = second % 60;
        }
        if(minute >= 60) {
            hour = minute / 60;
            minute = minute % 60;
        }
        if(hour >= 24) {
            day = hour / 24;
            hour = hour % 24;
        }
        StringBuilder sb = new StringBuilder();
        boolean flag = false;
        if(day > 0) {
            flag = true;
            sb.append(day).append("d");
        }
        if(flag || hour > 0) {
            flag = true;
            sb.append(hour).append("h");
        }
        if(flag || minute > 0) {
            sb.append(minute).append("m");
        }
        sb.append(second).append("s");
        return this.type + String.format("{%1$s,%2$s}", sb, this.record);
    }

}
