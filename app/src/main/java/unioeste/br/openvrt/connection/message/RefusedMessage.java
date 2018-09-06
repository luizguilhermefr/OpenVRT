package unioeste.br.openvrt.connection.message;

import android.support.annotation.NonNull;
import unioeste.br.openvrt.connection.IdFactory;
import unioeste.br.openvrt.connection.message.dictionary.Opcode;

/**
 * Signal that a message of id x was received and refused by the receiver.
 */
public class RefusedMessage extends AcknowledgedMessage {

    public RefusedMessage(char[] signature, short major, short minor, int id, int acknowledgedId) {
        super(signature, major, minor, id, acknowledgedId);
    }

    public RefusedMessage(char[] signature, short major, short minor, int id, char[] data) {
        super(signature, major, minor, id, data);
    }

    @NonNull
    public static AcknowledgedMessage newInstance(int acknowledgedId) {
        int id = IdFactory.getInstance().next();
        return new RefusedMessage(SIGNATURE.toCharArray(), VERSION_MAJOR, VERSION_MINOR, id, acknowledgedId);
    }

    @Override
    protected Opcode opcode() {
        return Opcode.REFUSE_OP;
    }
}
