package unioeste.br.openvrt.connection.message;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import unioeste.br.openvrt.connection.EndianessUtils;
import unioeste.br.openvrt.connection.exception.InvalidMessageException;

import java.util.Arrays;

public abstract class Message {

    public static final int MSG_LEN = 24;

    public static final int HEADER_LEN = 16;

    public static final int DATA_LEN = 8;

    public static final int SIGNATURE_LEN = 7;

    public static final int VERSION_MAJOR_LEN = 2;

    public static final int VERSION_MINOR_LEN = 2;

    public static final int ID_LEN = 4;

    public static final int OPCODE_LEN = 1;

    private static final byte[] SIGNATURE = "OPENVRT".getBytes();

    private static final byte[] VERSION_MAJOR = {
            (byte) 0x00,
            (byte) 0x01,
    };

    private static final byte[] VERSION_MINOR = {
            (byte) 0x00,
            (byte) 0x00,
    };

    @NonNull
    private static Opcode parseOpcodeFromRawMessageResponse(@NonNull byte[] message) {
        int pos = SIGNATURE_LEN + VERSION_MAJOR_LEN + VERSION_MINOR_LEN + ID_LEN;
        String opcodeStr = String.valueOf(message[pos]);
        return Opcode.valueOf(opcodeStr);
    }

    private static int parseIdFromRawMessageResponse(@NonNull byte[] message) {
        int pos = SIGNATURE_LEN + VERSION_MAJOR_LEN + VERSION_MINOR_LEN;
        byte[] rawId = Arrays.copyOfRange(message, pos, pos + ID_LEN - 1);
        return EndianessUtils.littleEndianBytesToInt(rawId);
    }

    private static boolean isValid(@NonNull byte[] content) {
        if (content.length != MSG_LEN) {
            return false;
        }

        // Check signature equals
        for (int i = 0; i < SIGNATURE_LEN; i++) {
            if (content[i] != SIGNATURE[i]) {
                return false;
            }
        }

        // Check version equals
        for (int i = SIGNATURE_LEN; i < SIGNATURE_LEN + VERSION_MAJOR_LEN; i++) {
            if (content[i] != VERSION_MAJOR[i - SIGNATURE_LEN]) {
                return false;
            }
        }

        try {
            Opcode opcode = parseOpcodeFromRawMessageResponse(content);
        } catch (IllegalArgumentException e) {
            return false;
        }


        return true;
    }

    @Nullable
    public static Message makeFromResponse(byte[] content) throws InvalidMessageException {
        if (!isValid(content)) {
            throw new InvalidMessageException();
        }

        switch (parseOpcodeFromRawMessageResponse(content)) {
            case ACK_OP:
                return AcknowledgedMessage.makeFromRaw(content);
            case REFUSE_OP:
                return RefusedMessage.makeFromRaw(content);
            case HANDSHAKE:
                return HandshakeMessage.makeFromRaw(content);
        }

        return null;
    }

    private byte[] header() {
        Opcode opcode = opcode();
        byte[] id = id();
        byte[] header = Arrays.copyOf(SIGNATURE, HEADER_LEN);
        // Add major
        System.arraycopy(VERSION_MAJOR, 0, header, SIGNATURE_LEN, VERSION_MAJOR_LEN);
        // Add minor
        System.arraycopy(VERSION_MINOR, 0, header, SIGNATURE_LEN + VERSION_MAJOR_LEN, VERSION_MINOR_LEN);
        // Add id
        System.arraycopy(id, 0, header, SIGNATURE_LEN + VERSION_MAJOR_LEN + VERSION_MINOR_LEN, ID_LEN);
        // Add opcode
        System.arraycopy(opcode.toString().getBytes(), 0, header, SIGNATURE_LEN + VERSION_MAJOR_LEN + VERSION_MINOR_LEN + ID_LEN, OPCODE_LEN);

        return header;
    }

    byte[] emptyData() {
        byte[] data = new byte[DATA_LEN];
        Arrays.fill(data, (byte) Character.MIN_VALUE);

        return data;
    }

    public byte[] toBytes() {
        byte[] header = header();
        byte[] data = data();
        byte[] message = Arrays.copyOf(header, MSG_LEN);
        System.arraycopy(data, 0, message, HEADER_LEN, DATA_LEN);

        return message;
    }

    protected abstract byte[] id();

    protected abstract byte[] data();

    protected abstract Opcode opcode();

    protected enum Opcode {
        REFUSE_OP((byte) 0x00),
        ACK_OP((byte) 0x01),
        HANDSHAKE((byte) 0x02),
        RATE_SET((byte) 0x03),
        MEASURE_SET((byte) 0x04);

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
