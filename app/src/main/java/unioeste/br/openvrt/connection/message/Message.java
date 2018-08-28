package unioeste.br.openvrt.connection.message;

import android.support.annotation.NonNull;
import unioeste.br.openvrt.connection.EndianessUtils;
import unioeste.br.openvrt.connection.exception.InvalidMessageException;

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

    final byte[] data = new byte[8];

    OPCODE opcode;

    private static boolean isValid(byte[] content) {
        return false;
    }

    public static Message make(byte[] content) throws InvalidMessageException {
        if (!isValid(content)) {
            throw new InvalidMessageException();
        }
        return null;
    }

    byte[] header() {
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
