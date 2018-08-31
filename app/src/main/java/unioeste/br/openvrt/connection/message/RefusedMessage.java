package unioeste.br.openvrt.connection.message;

/**
 * Signal that a message of id x was received and refused by the receiver.
 */
public class RefusedMessage extends AcknowledgedMessage {

    public RefusedMessage(int refusedId) {
        super(refusedId);
    }

    public RefusedMessage(int ourId, int refusedId) {
        super(ourId, refusedId);
    }

    @Override
    protected Opcode opcode() {
        return Opcode.REFUSE_OP;
    }
}
