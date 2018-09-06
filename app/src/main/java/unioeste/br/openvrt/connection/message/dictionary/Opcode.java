package unioeste.br.openvrt.connection.message.dictionary;

public enum Opcode {
    REFUSE_OP((byte) 0x00),
    ACK_OP((byte) 0x01),
    HANDSHAKE((byte) 0x02),
    RATE_SET((byte) 0x03),
    MEASURE_SET((byte) 0x04);

    public final byte code;

    Opcode(final byte code) {
        this.code = code;
    }
}
