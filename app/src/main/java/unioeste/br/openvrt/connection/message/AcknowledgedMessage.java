package unioeste.br.openvrt.connection.message;

import android.support.annotation.NonNull;
import unioeste.br.openvrt.connection.IdFactory;
import unioeste.br.openvrt.connection.message.dictionary.Opcode;

import java.util.Locale;

/**
 * Signal that a message of id x was received and accepted.
 * Opose of "RefusedMessage".
 */
public class AcknowledgedMessage extends Message {

    private int acknowledgedId;

    AcknowledgedMessage(char[] signature, short major, short minor, int id, int acknowledgedId) {
        super(signature, major, minor, id);
        this.acknowledgedId = acknowledgedId;
        makeDataFromAcknowledgedId();
    }

    public AcknowledgedMessage(char[] signature, short major, short minor, int id, char[] data) {
        super(signature, major, minor, id);
        this.data = data;
        makeAcknowledgedIdFromData();
    }

    @NonNull
    public static AcknowledgedMessage newInstance(int acknowledgedId) {
        int id = IdFactory.getInstance().next();
        return new AcknowledgedMessage(SIGNATURE.toCharArray(), VERSION_MAJOR, VERSION_MINOR, id, acknowledgedId);
    }

    private void makeAcknowledgedIdFromData() {
        String val = new String(data);
        acknowledgedId = Integer.valueOf(val);
    }

    private void makeDataFromAcknowledgedId() {
        data = new char[DATA_LEN];
        data = String.format(Locale.ENGLISH, "%08d", acknowledgedId).toCharArray();
    }

    public int getAcknowledgedId() {
        return acknowledgedId;
    }

    @Override
    protected Opcode opcode() {
        return Opcode.ACK_OP;
    }
}
