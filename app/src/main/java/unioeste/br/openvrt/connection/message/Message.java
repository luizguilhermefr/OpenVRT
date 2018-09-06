package unioeste.br.openvrt.connection.message;

import unioeste.br.openvrt.connection.EndianessUtils;
import unioeste.br.openvrt.connection.message.dictionary.MessageResponse;
import unioeste.br.openvrt.connection.message.dictionary.Opcode;

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

    public static final String SIGNATURE = "OPENVRT";

    public static final short VERSION_MAJOR = 1;

    public static final short VERSION_MINOR = 0;

    protected char[] signature;

    protected short majorVersion;

    protected short minorVersion;

    protected int id;

    protected char[] data;

    Message(char[] signature, short major, short minor, int id) {
        this.signature = signature;
        this.majorVersion = major;
        this.minorVersion = minor;
        this.id = id;
    }

    public static int signaturepos() {
        return 0;
    }

    public static int majorpos() {
        return signaturepos() + SIGNATURE_LEN;
    }

    public static int minorpos() {
        return majorpos() + VERSION_MAJOR_LEN;
    }

    public static int idpos() {
        return minorpos() + VERSION_MINOR_LEN;
    }

    public static int opcodepos() {
        return idpos() + ID_LEN;
    }

    public static int datapos() {
        return opcodepos() + OPCODE_LEN;
    }

    private byte[] buildHeaderBytes() {
        byte[] signatureBytes = SIGNATURE.getBytes();
        byte[] majorBytes = EndianessUtils.shortToLittleEndianBytes(majorVersion);
        byte[] minorBytes = EndianessUtils.shortToLittleEndianBytes(minorVersion);
        byte[] idBytes = EndianessUtils.intToLittleEndianBytes(id);
        byte opcodeByte = opcode().code;
        byte[] header = new byte[HEADER_LEN];
        System.arraycopy(signatureBytes, 0, header, signaturepos(), SIGNATURE_LEN);
        System.arraycopy(majorBytes, 0, header, majorpos(), VERSION_MAJOR_LEN);
        System.arraycopy(minorBytes, 0, header, minorpos(), VERSION_MINOR_LEN);
        System.arraycopy(idBytes, 0, header, idpos(), ID_LEN);
        System.arraycopy(new byte[]{opcodeByte}, 0, header, opcodepos(), OPCODE_LEN);

        return header;
    }

    char[] emptyData() {
        char[] data = new char[DATA_LEN];
        Arrays.fill(data, Character.MIN_VALUE);

        return data;
    }

    public byte[] toBytes() {
        byte[] headerBytes = buildHeaderBytes();
        byte[] dataBytes = String.valueOf(data).getBytes();
        byte[] msg = new byte[MSG_LEN];
        System.arraycopy(headerBytes, 0, msg, 0, HEADER_LEN);
        System.arraycopy(dataBytes, 0, msg, HEADER_LEN, DATA_LEN);

        return msg;
    }

    public int getId() {
        return id;
    }

    public void setResponseListener(OnMessageResponseListener listener) {
        this.listener = listener;
    }

    public void onResponse(MessageResponse response) {
        if (listener != null) {
            listener.onMessageResponse(response);
        }
    }

    abstract Opcode opcode();

    public interface OnMessageResponseListener {
        void onMessageResponse(MessageResponse response);
    }
}
