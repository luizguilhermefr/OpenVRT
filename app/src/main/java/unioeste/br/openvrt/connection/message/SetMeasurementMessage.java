package unioeste.br.openvrt.connection.message;

import android.support.annotation.NonNull;
import unioeste.br.openvrt.connection.EndianessUtils;
import unioeste.br.openvrt.connection.IdFactory;

import java.util.Arrays;

/**
 * Inform to the applier which measurement form should be used.
 */
public class SetMeasurementMessage extends Message {

    private Measurement measurement;

    private int ourId;

    public SetMeasurementMessage(Measurement measurement) {
        this.ourId = IdFactory.getInstance().next();
        this.measurement = measurement;
    }

    public SetMeasurementMessage(int ourId, Measurement measurement) {
        this.ourId = ourId;
        this.measurement = measurement;
    }

    @Override
    protected byte[] id() {
        return EndianessUtils.intToLittleEndianBytes(ourId);
    }

    @Override
    protected byte[] data() {
        byte[] strBytes = measurement.toBytes();
        byte[] res = new byte[DATA_LEN];
        Arrays.fill(res, 0, DATA_LEN - strBytes.length - 1, (byte) Character.MIN_VALUE);
        System.arraycopy(strBytes, 0, res, DATA_LEN - strBytes.length, strBytes.length);
        return res;
    }

    public enum Measurement {
        KG_HA("KG_HA"), L_HA("L_HA");

        public final int length = 2;

        private final String code;

        Measurement(final String code) {
            this.code = code;
        }

        @NonNull
        public final byte[] toBytes() {
            return code.getBytes();
        }
    }

    @Override
    protected Opcode opcode() {
        return Opcode.MEASURE_SET;
    }
}
