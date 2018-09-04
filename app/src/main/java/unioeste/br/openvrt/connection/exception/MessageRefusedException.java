package unioeste.br.openvrt.connection.exception;

public class MessageRefusedException extends Exception {
    public MessageRefusedException() {
        super("Message was refused by the receiver.");
    }
}
