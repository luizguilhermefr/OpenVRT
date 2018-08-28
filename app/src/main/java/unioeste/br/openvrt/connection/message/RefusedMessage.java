package unioeste.br.openvrt.connection.message;

public class RefusedMessage extends Message {

    private static RefusedMessage instance;

    private RefusedMessage() {
        //
    }

    public RefusedMessage getInstance() {
        if (instance == null) {
            instance = new RefusedMessage();
        }

        return instance;
    }

    @Override
    protected byte[] data() {
        return emptyData();
    }

    @Override
    protected Opcode opcode() {
        return Opcode.REFUSE_OP;
    }
}
