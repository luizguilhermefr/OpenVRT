package unioeste.br.openvrt.connection.message;

import android.support.annotation.NonNull;
import unioeste.br.openvrt.connection.IdFactory;
import unioeste.br.openvrt.connection.message.dictionary.Opcode;

/**
 * Signal that the device wants to start a communication.
 * Has no data content.
 */
public class HandshakeMessage extends Message {

    private HandshakeMessage(char[] signature, short major, short minor, int id) {
        super(signature, major, minor, id);
        this.data = emptyData();
    }

    @NonNull
    public static HandshakeMessage newInstance() {
        int id = IdFactory.getInstance().next();
        return new HandshakeMessage(SIGNATURE.toCharArray(), VERSION_MAJOR, VERSION_MINOR, id);
    }

    @Override
    protected Opcode opcode() {
        return Opcode.HANDSHAKE;
    }
}
