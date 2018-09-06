package unioeste.br.openvrt.connection.exception;

public class UnexpectedMessageException extends Exception {
    public UnexpectedMessageException() {
        super("This message is valid but should not be received, only sent.");
    }
}
