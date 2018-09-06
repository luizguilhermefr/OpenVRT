package unioeste.br.openvrt.connection.message;

import android.support.annotation.NonNull;
import unioeste.br.openvrt.connection.EndianessUtils;
import unioeste.br.openvrt.connection.IdFactory;

import java.util.Arrays;

/**
 * Signal that a message of id x was received and accepted.
 * Opose of "RefusedMessage".
 */
public class AcknowledgedMessage extends Message {

    private int ourId;

    private int acknowledgedId;

    public AcknowledgedMessage(int acknowledgedId) {
        this.ourId = IdFactory.getInstance().next();
        this.acknowledgedId = acknowledgedId;
    }

    public AcknowledgedMessage(int ourId, int acknowledgedId) {
        this.ourId = ourId;
        this.acknowledgedId = acknowledgedId;
    }

    protected static AcknowledgedMessage makeFromRaw(byte[] rawMessage) {
        int id = parseIdFromRawMessageResponse(rawMessage);
        int ackId = parseAcknowledgedIdFromRawMessageResponse(rawMessage);

        return new AcknowledgedMessage(id, ackId);
    }

    private static int parseAcknowledgedIdFromRawMessageResponse(@NonNull byte[] message) {
        int pos = datapos();
        byte[] rawInt = Arrays.copyOfRange(message, pos, pos + DATA_LEN - 1);
        String val = Arrays.toString(rawInt);
        return Integer.valueOf(val);
    }

    public int getAcknowledgedId() {
        return acknowledgedId;
    }

    @Override
    protected byte[] id() {
        return EndianessUtils.intToLittleEndianBytes(ourId);
    }

    @Override
    protected byte[] data() {
        byte[] res = new byte[DATA_LEN];
        byte[] id = EndianessUtils.intToLittleEndianBytes(acknowledgedId);
        Arrays.fill(res, 0, DATA_LEN - ID_LEN - 1, (byte) Character.MIN_VALUE);
        System.arraycopy(id, 0, res, DATA_LEN - ID_LEN, ID_LEN);
        return res;
    }

    @Override
    protected Opcode opcode() {
        return Opcode.ACK_OP;
    }
}
