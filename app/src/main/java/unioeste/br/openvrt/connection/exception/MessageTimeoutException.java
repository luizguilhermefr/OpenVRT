package unioeste.br.openvrt.connection.exception;

public class MessageTimeoutException extends Exception {
    public MessageTimeoutException() {
        super("Message did not receive acknowledgement in time.");
    }
}
