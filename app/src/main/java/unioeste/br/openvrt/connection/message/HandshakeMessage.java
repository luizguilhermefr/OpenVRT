package unioeste.br.openvrt.connection.message;

import java.util.Arrays;

public class HandshakeMessage extends Message {

    private static HandshakeMessage instance;

    private HandshakeMessage() {
        Arrays.fill(data, (byte) Character.MIN_VALUE);
        opcode = OPCODE.HANDSHAKE;
    }

    public HandshakeMessage getInstance() {
        if (instance == null) {
            instance = new HandshakeMessage();
        }

        return instance;
    }

    @Override
    public byte[] toBytes() {
        byte[] header = header();
        byte[] message = Arrays.copyOf(header, header.length + data.length);
        System.arraycopy(data, 0, message, header.length, data.length);

        return message;
    }
}
