package unioeste.br.openvrt.connection.message;

import android.support.annotation.NonNull;
import unioeste.br.openvrt.connection.exception.InvalidMessageException;

import java.util.Arrays;

public abstract class Message {

    public static final int MSG_LEN = 24;

    public static final int HEADER_LEN = 16;

    public static final int DATA_LEN = 8;

    private static final byte[] SIGNATURE = "OPENVRT".getBytes();

    private static final byte[] VERSION_MAJOR = {
            (byte) 0x00,
            (byte) 0x01,
    };

    private static final byte[] VERSION_MINOR = {
            (byte) 0x00,
            (byte) 0x00,
    };

    private static final byte[] RESERVED = {
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00
    };

    @NonNull
    private static Opcode parseOpcodeFromRawMessage(@NonNull byte[] message) {
        String opcodeStr = String.valueOf(message[SIGNATURE.length + VERSION_MAJOR.length + VERSION_MINOR.length + RESERVED.length]);
        return Opcode.valueOf(opcodeStr);
    }

    private static boolean isValid(@NonNull byte[] content) {
        if (content.length != MSG_LEN) {
            return false;
        }

        for (int i = 0; i < SIGNATURE.length; i++) {
            if (content[i] != SIGNATURE[i]) {
                return false;
            }
        }

        for (int i = SIGNATURE.length; i < SIGNATURE.length + VERSION_MAJOR.length; i++) {
            if (content[i] != VERSION_MAJOR[i]) {
                return false;
            }
        }

        try {
            Opcode opcode = parseOpcodeFromRawMessage(content);
        } catch (IllegalArgumentException e) {
            return false;
        }


        return true;
    }

    public static Message make(byte[] content) throws InvalidMessageException {
        if (!isValid(content)) {
            throw new InvalidMessageException();
        }

        switch (parseOpcodeFromRawMessage(content)) {
            case ACK_OP:
                return AcknowledgedMessage.getInstance();
            case REFUSE_OP:
                return RefusedMessage.getInstance();
            case HANDSHAKE:
                return HandshakeMessage.getInstance();
        }

        return null;
    }

    private byte[] header() {
        Opcode opcode = opcode();
        byte[] header = Arrays.copyOf(SIGNATURE, HEADER_LEN);
        // Add major
        System.arraycopy(VERSION_MAJOR, 0, header, SIGNATURE.length, VERSION_MAJOR.length);
        // Add minor
        System.arraycopy(VERSION_MINOR, 0, header, SIGNATURE.length + VERSION_MAJOR.length, VERSION_MINOR.length);
        // Add reserved
        System.arraycopy(RESERVED, 0, header, SIGNATURE.length + VERSION_MAJOR.length + VERSION_MINOR.length, RESERVED.length);
        // Add opcode
        System.arraycopy(opcode.toString().getBytes(), 0, header, SIGNATURE.length + VERSION_MAJOR.length + VERSION_MINOR.length + RESERVED.length, opcode.toString().length());

        return header;
    }

    byte[] emptyData() {
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
        REFUSE_OP((byte) 0x00),
        ACK_OP((byte) 0x01),
        HANDSHAKE((byte) 0x02),
        RATE_SET((byte) 0x03),
        MEASURE_SET((byte) 0x04);

        public static final int length = 1;
        private final byte code;

        Opcode(final byte code) {
            this.code = code;
        }

        @NonNull
        @Override
        public String toString() {
            return String.valueOf(code);
        }
    }
}
