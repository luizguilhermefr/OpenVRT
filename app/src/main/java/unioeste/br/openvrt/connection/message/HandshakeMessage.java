package unioeste.br.openvrt.connection.message;

import unioeste.br.openvrt.connection.EndianessUtils;
import unioeste.br.openvrt.connection.IdFactory;

/**
 * Signal that the device wants to start a communication.
 * Has no data content.
 */
public class HandshakeMessage extends Message {

    private int ourId;

    public HandshakeMessage(int ourId) {
        this.ourId = ourId;
    }

    public HandshakeMessage() {
        this.ourId = IdFactory.getInstance().next();
    }

    public static HandshakeMessage makeFromRaw(byte[] rawMessage) {
        return null;
    }

    @Override
    protected byte[] id() {
        return EndianessUtils.intToLittleEndianBytes(ourId);
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
