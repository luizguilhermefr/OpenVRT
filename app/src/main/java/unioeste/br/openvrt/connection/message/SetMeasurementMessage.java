package unioeste.br.openvrt.connection.message;

public class SetMeasurementMessage extends Message {

    @Override
    protected byte[] data() {
        return new byte[0];
    }

    @Override
    protected Opcode opcode() {
        return Opcode.MEASURE_SET;
    }
}
