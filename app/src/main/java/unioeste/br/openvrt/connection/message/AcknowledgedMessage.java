package unioeste.br.openvrt.connection.message;

public class AcknowledgedMessage extends Message {

    private static AcknowledgedMessage instance;

    private AcknowledgedMessage() {
        //
    }

    public AcknowledgedMessage getInstance() {
        if (instance == null) {
            instance = new AcknowledgedMessage();
        }

        return instance;
    }

    @Override
    protected byte[] data() {
        return emptyData();
    }

    @Override
    protected Opcode opcode() {
        return Opcode.ACK_OP;
    }
}
