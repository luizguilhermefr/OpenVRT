package unioeste.br.openvrt.connection.message;

import android.support.annotation.NonNull;
import unioeste.br.openvrt.connection.EndianessUtils;
import unioeste.br.openvrt.connection.exception.InvalidMessageException;

import java.util.Arrays;

public abstract class Message {

    public static final int MSG_LEN = 24;

    public static final int DATA_LEN = 8;

    private static final byte[] SIGNATURE = "OPENVRT".getBytes();

    private static final byte[] VERSION = EndianessUtils.shortToBigEndianBytes((short) 1);

    private static final byte[] RESERVED = {
            Character.MIN_VALUE,
            Character.MIN_VALUE,
            Character.MIN_VALUE,
            Character.MIN_VALUE,
            Character.MIN_VALUE
    };

    private static boolean isValid(byte[] content) {
        return false;
    }

    public static Message make(byte[] content) throws InvalidMessageException {
        if (!isValid(content)) {
            throw new InvalidMessageException();
        }
        return null;
    }

    private byte[] header() {
        Opcode opcode = opcode();
        byte[] header = Arrays.copyOf(SIGNATURE, SIGNATURE.length + VERSION.length + RESERVED.length + opcode.length);
        System.arraycopy(VERSION, 0, header, SIGNATURE.length, VERSION.length);
        System.arraycopy(RESERVED, 0, header, SIGNATURE.length + VERSION.length, RESERVED.length);
        System.arraycopy(opcode.toBytes(), 0, header, SIGNATURE.length + VERSION.length + RESERVED.length, opcode.length);

        return header;
    }

    protected byte[] emptyData() {
        byte[] data = new byte[DATA_LEN];
        Arrays.fill(data, (byte) Character.MIN_VALUE);

        return data;
    }

    public byte[] toBytes() {
        byte[] data = data();
        byte[] header = header();
        byte[] message = Arrays.copyOf(header, header.length + data.length);
        System.arraycopy(data, 0, message, header.length, data.length);

        return message;
    }

    protected abstract byte[] data();

    protected abstract Opcode opcode();

    protected enum Opcode {
        REFUSE_OP((short) -1),
        ACK_OP((short) 1),
        HANDSHAKE((short) 2),
        RATE_SET((short) 3),
        MEASURE_SET((short) 4);

        public final int length = 2;

        private final short code;

        Opcode(final short code) {
            this.code = code;
        }

        @NonNull
        public final byte[] toBytes() {
            return EndianessUtils.shortToBigEndianBytes(code);
        }
    }
}
