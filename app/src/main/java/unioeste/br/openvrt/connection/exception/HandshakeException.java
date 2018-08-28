package unioeste.br.openvrt.connection.exception;

public class HandshakeException extends Exception {
    public HandshakeException() {
        super("The device did not accept the handshake.");
    }
}
