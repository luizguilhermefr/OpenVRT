package unioeste.br.openvrt.connection.messages;

import unioeste.br.openvrt.connection.messages.exception.InvalidMessageException;

public class MessageFactory {

    public static final int PACKET_SIZE = 255;

    private static boolean isValid(byte[] content) {
        return false;
    }

    public static Message make(byte[] content) throws InvalidMessageException {
        if (!isValid(content)) {
            throw new InvalidMessageException();
        }
        return null;
    }
}
