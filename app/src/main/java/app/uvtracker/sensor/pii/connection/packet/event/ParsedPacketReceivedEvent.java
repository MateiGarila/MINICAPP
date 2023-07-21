package app.uvtracker.sensor.pii.connection.packet.event;

import androidx.annotation.NonNull;

import app.uvtracker.sensor.protocol.packet.base.Packet;

public class ParsedPacketReceivedEvent extends PacketReceivedEvent {

    public ParsedPacketReceivedEvent(@NonNull Packet packet) {
        super(packet);
    }

}
