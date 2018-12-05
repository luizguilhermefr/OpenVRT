package unioeste.br.openvrt.connection.message.dictionary;

public enum Opcode {
    REFUSE_OP((byte) 0x0),
    ACK_OP((byte) 0x1),
    HANDSHAKE((byte) 0x2),
    RATE_SET((byte) 0x3),
    MEASURE_SET((byte) 0x4),
    WORK_WIDTH_SET((byte) 0x5);

    public final byte code;

    public static Opcode valueOf(byte code) {
        for (Opcode opcode : Opcode.values()) {
            if (opcode.code == code) {
                return opcode;
            }
        }
        return null;
    }

    Opcode(final byte code) {
        this.code = code;
    }
}
