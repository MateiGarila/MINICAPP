package app.uvtracker.sensor;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;

import java.util.UUID;

@SuppressWarnings("CanBeFinal")
public class BLEOptions {

    public static boolean TRACE_ENABLED = true;

    @SuppressWarnings("CanBeFinal")
    public static class Scanner {
        public static boolean RESTRICTED = true;
        public static UUID FILTER_UUID = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e");
    }

    @SuppressWarnings("CanBeFinal")
    public static class Device {
        public static int MTU_REQUIRED = 128;
        public static UUID SERVICE = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e");
        public static class Serial {
            @SuppressWarnings("CanBeFinal")
            public static class Read {
                public static UUID ENDPOINT = UUID.fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9e");
                public static int PROPERTY = BluetoothGattCharacteristic.PROPERTY_NOTIFY;
                public static UUID DESCRIPTOR = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
                public static byte[] DESCRIPTOR_VAL = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE;
            }
            @SuppressWarnings("CanBeFinal")
            public static class Write {
                public static UUID ENDPOINT = UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e");
                public static int PROPERTY = BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE;
                public static int WRITE_TYPE = BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE;
                @SuppressWarnings("CanBeFinal")
                public static class Buffer {
                    public static int CAPACITY = 4096;
                    public static int SPEED_BPS = 950;
                    public static int STICKY_DLY = 5;
                    public static int MIN_DLY = 1;
                }
            }
        }
    }

    @SuppressWarnings("CanBeFinal")
    public static class Connection {
        public static int CONNECTION_TIMEOUT = 8000;
        public static int CONNECTION_GRACE_PERIOD = 500;
        public static int DISCONNECTION_TIMEOUT = 2000;
    }

    @SuppressWarnings("CanBeFinal")
    public static class Sync {
        public static int SYNC_TIMEOUT = 2000;
    }

}
