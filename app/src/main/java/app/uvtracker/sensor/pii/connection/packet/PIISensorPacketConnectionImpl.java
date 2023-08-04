package app.uvtracker.sensor.pii.connection.packet;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

import app.uvtracker.sensor.pii.ISensor;
import app.uvtracker.sensor.pii.connection.bytestream.event.BytesReceivedEvent;
import app.uvtracker.sensor.pii.connection.bytestream.ISensorBytestreamConnection;
import app.uvtracker.sensor.pii.connection.packet.event.PacketReceivedEvent;
import app.uvtracker.sensor.pii.connection.packet.event.UnrecognizableMessageReceivedEvent;
import app.uvtracker.sensor.pii.connection.shared.event.ConnectionStateChangeEvent;
import app.uvtracker.sensor.pii.event.EventHandler;
import app.uvtracker.sensor.pii.event.EventRegistry;
import app.uvtracker.sensor.pii.event.IEventListener;
import app.uvtracker.sensor.protocol.codec.IPacketCodec;
import app.uvtracker.sensor.protocol.codec.exception.CodecException;
import app.uvtracker.sensor.protocol.packet.base.Packet;

public class PIISensorPacketConnectionImpl extends EventRegistry implements ISensorPacketConnection, IEventListener {

    @NonNull
    private static final String TAG = PIISensorPacketConnectionImpl.class.getSimpleName();

    @NonNull
    private final ISensorBytestreamConnection baseConnection;

    @NonNull
    private final LineParser parser;

    public PIISensorPacketConnectionImpl(@NonNull ISensorBytestreamConnection baseConnection) {
        this.baseConnection = baseConnection;
        this.baseConnection.registerListener(this);
        this.parser = new LineParser();
    }

    // Base connection implementation
    @Override
    @NonNull
    public ISensor getSensor() {
        return this.baseConnection.getSensor();
    }

    @Override
    public void reset() {
        this.baseConnection.reset();
    }

    @Override
    public boolean connect() {
        return this.baseConnection.connect();
    }

    @Override
    public boolean disconnect() {
        return this.baseConnection.disconnect();
    }

    // TODO: any better way to propagate these events up?
    @EventHandler
    protected void onConnectionStateChange(@NonNull ConnectionStateChangeEvent event) {
        this.dispatch(event);
    }

    // Specific implementation
    @Override
    public boolean write(@NonNull Packet packet) {
        String encoded;
        try {
            encoded = IPacketCodec.get().encode(packet);
        }
        catch (CodecException e) {
            Log.d(TAG, "Encoding exception:", e);
            return false;
        }
        Log.d(TAG, "Encoded " + packet);
        String message = "#" + encoded + "\r\n";
        return this.baseConnection.write(message.getBytes(StandardCharsets.US_ASCII));
    }

    @EventHandler
    protected void onBytesReceived(@NonNull BytesReceivedEvent event) {
        byte[] bytes = event.getData();
        for(byte b : bytes) {
            String message = parser.accept(b);
            if(message == null) continue;
            if(message.length() > 1 && message.startsWith("#")) {
                try {
                    Log.d(TAG, "Received " + message);
                    Packet decoded = IPacketCodec.get().decode(message.substring(1));
                    Log.d(TAG, "Decoded " + decoded);
                    this.dispatch(PacketReceivedEvent.fromPacket(decoded));
                    continue;
                }
                catch (CodecException e) {
                    Log.d(TAG, "Decoding exception:", e);
                }
            }
            Log.d(TAG, "Received " + message);
            this.dispatch(new UnrecognizableMessageReceivedEvent(message));
        }
    }

}

class LineParser {

    enum State {
        READ_LINE,
        FIND_LINE,
    }

    @NonNull
    private State state;

    @NonNull
    private final ByteArrayOutputStream stream;

    public LineParser() {
        this.state = State.READ_LINE;
        this.stream = new ByteArrayOutputStream(512);
    }

    @Nullable
    public String accept(byte input) {
        switch(this.state) {
            default: {
                this.state = State.READ_LINE;
                this.stream.reset();
                break;
            }
            case READ_LINE: {
                if(input >= ' ' && input <= '~') {
                    this.stream.write(input);
                }
                else {
                    this.state = State.FIND_LINE;
                    return this.fetchMessage();
                }
                break;
            }
            case FIND_LINE: {
                if(input >= ' ' && input <= '~') {
                    this.state = State.READ_LINE;
                    this.stream.write(input);
                }
                break;
            }
        }
        return null;
    }

    @NonNull
    private String fetchMessage() {
        try {
            String message = this.stream.toString(StandardCharsets.US_ASCII.toString());
            this.stream.reset();
            return message;
        } catch (UnsupportedEncodingException e) {
            // This should never happen
            throw new RuntimeException(e);
        }
    }

}
