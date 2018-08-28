package unioeste.br.openvrt.connection.message;

public class HandshakeMessage extends Message {

    private static HandshakeMessage instance;

    private HandshakeMessage() {
        //
    }

    public static HandshakeMessage getInstance() {
        if (instance == null) {
            instance = new HandshakeMessage();
        }

        return instance;
    }

    @Override
    protected byte[] data() {
        return emptyData();
    }

    @Override
    protected Opcode opcode() {
        return Opcode.HANDSHAKE;
    }
}
