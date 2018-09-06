package unioeste.br.openvrt.connection.message;

import android.support.annotation.NonNull;
import unioeste.br.openvrt.connection.EndianessUtils;
import unioeste.br.openvrt.connection.OutcomeMessageQueue;
import unioeste.br.openvrt.connection.exception.InvalidMessageException;

import java.util.Arrays;

public abstract class Message {

    private OnMessageResponseListener listener;

    public static final int MSG_LEN = 24;

    public static final int HEADER_LEN = 16;

    public static final int DATA_LEN = 8;

    public static final int SIGNATURE_LEN = 7;

    public static final int VERSION_MAJOR_LEN = 2;

    public static final int VERSION_MINOR_LEN = 2;

    public static final int ID_LEN = 4;

    public static final int OPCODE_LEN = 1;

    private static final String SIGNATURE = "OPENVRT";

    private static final short VERSION_MAJOR = 1;

    private static final short VERSION_MINOR = 0;

    protected char[] signature;

    protected short majorVersion;

    protected short minorVersion;

    protected int id;

    protected char opcode;

    protected char[] data;

    private Message(char[] signature, short major, short minor, int id, char opcode, char[] data) {
        this.signature = signature;
        this.majorVersion = major;
        this.minorVersion = minor;
        this.id = id;
        this.data = data;
    }

    public static Message make(@NonNull byte[] buf) throws InvalidMessageException {
        if (!isValidLength(buf)) {
            throw new InvalidMessageException();
        }

        char[] signature = makeSignatureFromBuffer(buf);
        short major = makeMajorFromBuffer(buf);
        short minor = makeMinorFromBuffer(buf);
        int id = makeIdFromBuffer(buf);
        char opcode = makeOpcodeFromBuffer(buf);
        char[] data = makeDataFromBuffer(buf);

        if (!isValidSignature(signature)) {
            throw new InvalidMessageException();
        }
    }

    private static char[] makeSignatureFromBuffer(@NonNull byte[] buf) {
        byte[] signatureBuf = new byte[SIGNATURE_LEN];
        System.arraycopy(buf, signaturepos(), signatureBuf, signaturepos(), majorpos() - signaturepos());
        return Arrays.toString(signatureBuf).toCharArray();
    }

    private static short makeMajorFromBuffer(@NonNull byte[] buf) {
        byte[] majorBuf = new byte[VERSION_MAJOR_LEN];
        System.arraycopy(buf, majorpos(), majorBuf, majorpos(), minorpos() - majorpos());
        return EndianessUtils.littleEndianBytesToShort(majorBuf);
    }

    private static short makeMinorFromBuffer(@NonNull byte[] buf) {
        byte[] minorBuf = new byte[VERSION_MINOR_LEN];
        System.arraycopy(buf, minorpos(), minorBuf, minorpos(), idpos() - minorpos());
        return EndianessUtils.littleEndianBytesToShort(minorBuf);
    }

    private static int makeIdFromBuffer(@NonNull byte[] buf) {
        byte[] idBuf = new byte[ID_LEN];
        System.arraycopy(buf, idpos(), idBuf, idpos(), opcodepos() - idpos());
        return EndianessUtils.littleEndianBytesToInt(idBuf);
    }

    private static char makeOpcodeFromBuffer(@NonNull byte[] buf) {
        return (char) buf[opcodepos()];
    }

    private static char[] makeDataFromBuffer(@NonNull byte[] buf) {
        byte[] dataBuf = new byte[DATA_LEN];
        System.arraycopy(buf, datapos(), dataBuf, datapos(), datapos() + DATA_LEN - datapos());
        return Arrays.toString(dataBuf).toCharArray();
    }

    private static boolean isValidLength(@NonNull byte[] buf) {
        return buf.length == MSG_LEN;
    }

    private static boolean isValidSignature(@NonNull char[] signature) {
        return Arrays.toString(signature).equals(SIGNATURE);
    }

    protected static int signaturepos() {
        return 0;
    }

    protected static int majorpos() {
        return signaturepos() + SIGNATURE_LEN;
    }

    protected static int minorpos() {
        return majorpos() + VERSION_MAJOR_LEN;
    }

    protected static int idpos() {
        return minorpos() + VERSION_MINOR_LEN;
    }

    protected static int opcodepos() {
        return idpos() + ID_LEN;
    }

    protected static int datapos() {
        return opcodepos() + OPCODE_LEN;
    }

    @NonNull
    protected static Opcode parseOpcodeFromRawMessageResponse(@NonNull byte[] message) {
        int pos = opcodepos();
        String opcodeStr = String.valueOf(message[pos]);
        return Opcode.valueOf(opcodeStr);
    }

    protected static int parseIdFromRawMessageResponse(@NonNull byte[] message) {
        int pos = idpos();
        byte[] rawId = Arrays.copyOfRange(message, pos, pos + ID_LEN - 1);
        return EndianessUtils.littleEndianBytesToInt(rawId);
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
        System.arraycopy(new byte[]{opcode.code}, 0, header, SIGNATURE_LEN + VERSION_MAJOR_LEN + VERSION_MINOR_LEN + ID_LEN, OPCODE_LEN);

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

    public int getId() {
        return EndianessUtils.littleEndianBytesToInt(id());
    }

    public void setResponseListener(OnMessageResponseListener listener) {
        this.listener = listener;
    }

    public void onResponse(OutcomeMessageQueue.MessageResponse response) {
        if (listener != null) {
            listener.onMessageResponse(response);
        }
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

        public final byte code;

        Opcode(final byte code) {
            this.code = code;
        }
    }

    public interface OnMessageResponseListener {
        void onMessageResponse(OutcomeMessageQueue.MessageResponse response);
    }
}
