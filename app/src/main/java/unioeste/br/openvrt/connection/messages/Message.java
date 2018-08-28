package unioeste.br.openvrt.connection.messages;

import android.support.annotation.NonNull;

import java.util.Arrays;

public abstract class Message {

    public static final int MSG_LEN = 24;

    private static final byte[] SIGNATURE = "OPENVRT".getBytes();

    private static final byte[] VERSION = EndianessUtils.shortToBigEndianBytes((short) 1);
    private static final byte[] RESERVED = {
            Character.MIN_VALUE,
            Character.MIN_VALUE,
            Character.MIN_VALUE,
            Character.MIN_VALUE,
            Character.MIN_VALUE
    };
    public final byte[] data = new byte[8];
    private OPCODE opcode;

    protected byte[] header() {
        byte[] header = Arrays.copyOf(SIGNATURE, SIGNATURE.length + VERSION.length + RESERVED.length + opcode.length);
        System.arraycopy(VERSION, 0, header, SIGNATURE.length, VERSION.length);
        System.arraycopy(RESERVED, 0, header, SIGNATURE.length + VERSION.length, RESERVED.length);
        System.arraycopy(opcode.toBytes(), 0, header, SIGNATURE.length + VERSION.length + RESERVED.length, opcode.length);

        return header;
    }

    public abstract byte[] toBytes();

    protected enum OPCODE {
        REFUSE_OP((short) -1),
        ACK_OP((short) 1),
        HANDSHAKE((short) 2),
        VELOCITY_SET((short) 3),
        RATE_SET((short) 4),
        MEASURE_SET((short) 5);

        public final int length = 2;
        private final short code;

        OPCODE(final short code) {
            this.code = code;
        }

        @NonNull
        public final byte[] toBytes() {
            return EndianessUtils.shortToBigEndianBytes(code);
        }
    }
}
