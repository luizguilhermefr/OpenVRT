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

    protected static RefusedMessage makeFromRaw(byte[] rawMessage) {
        AcknowledgedMessage msg = AcknowledgedMessage.makeFromRaw(rawMessage);

        return (RefusedMessage) msg;
    }

    @Override
    protected Opcode opcode() {
        return Opcode.REFUSE_OP;
    }
}
