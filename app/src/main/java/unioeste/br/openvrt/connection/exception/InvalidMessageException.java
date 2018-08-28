package unioeste.br.openvrt.connection.exception;

public class InvalidMessageException extends Exception {
    public InvalidMessageException() {
        super("A message received is not valid.");
    }
}
