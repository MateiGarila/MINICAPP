package app.uvtracker.sensor.protocol.packet.in;

import androidx.annotation.NonNull;

import app.uvtracker.sensor.protocol.codec.exception.PacketFormatException;
import app.uvtracker.sensor.protocol.packet.base.Packet;
import app.uvtracker.sensor.protocol.packet.base.PacketIn;
import app.uvtracker.sensor.protocol.util.Packing;

public class PacketInButtonInteract extends PacketIn {

    public enum Button {
        FIRST,
        SECOND,
    }

    public enum Action {
        PRESSED,
        RELEASED,
    }

    @NonNull
    private final Button button;

    @NonNull
    private final Action action;

    public PacketInButtonInteract(Packet packetBase) throws PacketFormatException {
        super(packetBase);
        PacketFormatException.requireLength(packetBase, 1);
        int data = Packing.unpack1(this.payload, 0);
        int buttonCode = data & 0x0F;
        int actionCode = (data & 0xF0) >> 4;
        switch(buttonCode) {
            case 0: this.button = Button.FIRST; break;
            case 1: this.button = Button.SECOND; break;
            default: throw new PacketFormatException("Undefined button number " + buttonCode, packetBase);
        }
        switch(actionCode) {
            case 0: this.action = Action.RELEASED; break;
            case 1: this.action = Action.PRESSED; break;
            default: throw new PacketFormatException("Undefined action code " + actionCode, packetBase);
        }
    }

    @NonNull
    public Button getButton() {
        return this.button;
    }

    @NonNull
    public Action getAction() {
        return this.action;
    }

    @Override
    @NonNull
    public String toString() {
        return this.type + "{" + this.button.toString().toLowerCase() + " button " + this.action.toString().toLowerCase() + "}";
    }

}
