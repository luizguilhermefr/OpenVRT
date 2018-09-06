package unioeste.br.openvrt.connection.message.factory;

import android.support.annotation.NonNull;
import unioeste.br.openvrt.connection.EndianessUtils;
import unioeste.br.openvrt.connection.exception.InvalidMessageException;
import unioeste.br.openvrt.connection.exception.UnexpectedMessageException;
import unioeste.br.openvrt.connection.message.AcknowledgedMessage;
import unioeste.br.openvrt.connection.message.Message;
import unioeste.br.openvrt.connection.message.RefusedMessage;
import unioeste.br.openvrt.connection.message.dictionary.Opcode;

import java.util.Arrays;

public class MessageFactory {

    private MessageFactory() {
        //
    }

    public static Message make(@NonNull byte[] buf) throws InvalidMessageException, UnexpectedMessageException {
        if (!isValidLength(buf)) {
            throw new InvalidMessageException();
        }

        char[] signature = makeSignatureFromBuffer(buf);
        short major = makeMajorFromBuffer(buf);
        short minor = makeMinorFromBuffer(buf);
        int id = makeIdFromBuffer(buf);
        byte opcode = makeOpcodeFromBuffer(buf);
        char[] data = makeDataFromBuffer(buf);

        if (!isValidSignature(signature) || !isValidVersion(major) || !isValidOpcode(opcode)) {
            throw new InvalidMessageException();
        }

        switch (Opcode.valueOf(String.valueOf(opcode))) {
            case ACK_OP:
                return new AcknowledgedMessage(signature, major, minor, id, data);
            case REFUSE_OP:
                return new RefusedMessage(signature, major, minor, id, data);
        }

        throw new UnexpectedMessageException();
    }

    private static char[] makeSignatureFromBuffer(@NonNull byte[] buf) {
        byte[] signatureBuf = new byte[Message.SIGNATURE_LEN];
        System.arraycopy(buf, Message.signaturepos(), signatureBuf, 0, Message.SIGNATURE_LEN);
        return Arrays.toString(signatureBuf).toCharArray();
    }

    private static short makeMajorFromBuffer(@NonNull byte[] buf) {
        byte[] majorBuf = new byte[Message.VERSION_MAJOR_LEN];
        System.arraycopy(buf, Message.majorpos(), majorBuf, 0, Message.VERSION_MAJOR_LEN);
        return EndianessUtils.littleEndianBytesToShort(majorBuf);
    }

    private static short makeMinorFromBuffer(@NonNull byte[] buf) {
        byte[] minorBuf = new byte[Message.VERSION_MINOR_LEN];
        System.arraycopy(buf, Message.minorpos(), minorBuf, 0, Message.VERSION_MINOR_LEN);
        return EndianessUtils.littleEndianBytesToShort(minorBuf);
    }

    private static int makeIdFromBuffer(@NonNull byte[] buf) {
        byte[] idBuf = new byte[Message.ID_LEN];
        System.arraycopy(buf, Message.idpos(), idBuf, 0, Message.ID_LEN);
        return EndianessUtils.littleEndianBytesToInt(idBuf);
    }

    private static byte makeOpcodeFromBuffer(@NonNull byte[] buf) {
        return buf[Message.opcodepos()];
    }

    private static char[] makeDataFromBuffer(@NonNull byte[] buf) {
        byte[] dataBuf = new byte[Message.DATA_LEN];
        System.arraycopy(buf, Message.datapos(), dataBuf, 0, Message.DATA_LEN);
        return Arrays.toString(dataBuf).toCharArray();
    }

    private static boolean isValidLength(@NonNull byte[] buf) {
        return buf.length == Message.MSG_LEN;
    }

    private static boolean isValidSignature(@NonNull char[] signature) {
        return Arrays.toString(signature).equals(Message.SIGNATURE);
    }

    private static boolean isValidVersion(short major) {
        return major == Message.VERSION_MAJOR;
    }

    private static boolean isValidOpcode(byte opcode) {
        for (Opcode c : Opcode.values()) {
            if (c.code == opcode) {
                return true;
            }
        }

        return false;
    }
}
