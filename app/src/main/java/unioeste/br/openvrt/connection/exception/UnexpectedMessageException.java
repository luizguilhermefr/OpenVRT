package unioeste.br.openvrt.connection.exception;

public class UnexpectedMessageException extends Exception {

    private final int messageId;

    public UnexpectedMessageException(int messageId) {
        super("This message is valid but should not be received, only sent.");
        this.messageId = messageId;
    }

    public int getMessageId() {
        return messageId;
    }
}
