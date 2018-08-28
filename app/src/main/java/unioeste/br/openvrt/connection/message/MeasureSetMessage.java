package unioeste.br.openvrt.connection.message;

public class MeasureSetMessage extends Message {

    private static MeasureSetMessage instance;

    private MeasureSetMessage() {
        //
    }

    public static MeasureSetMessage getInstance() {
        if (instance == null) {
            instance = new MeasureSetMessage();
        }

        return instance;
    }

    @Override
    protected byte[] data() {
        return emptyData();
    }

    @Override
    protected Opcode opcode() {
        return Opcode.MEASURE_SET;
    }
}
