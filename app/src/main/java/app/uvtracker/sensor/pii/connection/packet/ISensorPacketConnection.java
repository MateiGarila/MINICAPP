package app.uvtracker.sensor.pii.connection.packet;

import androidx.annotation.NonNull;

import app.uvtracker.sensor.pii.connection.shared.IConnectable;
import app.uvtracker.sensor.protocol.packet.base.Packet;

public interface ISensorPacketConnection extends IConnectable {

    // Emits event: PacketReceivedEvent and its 1 subclass
    // Emits event: UnrecognizableMessageReceivedEvent

    boolean write(@NonNull Packet packet);

}
