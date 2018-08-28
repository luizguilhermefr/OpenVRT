package unioeste.br.openvrt.connection.message;

import android.support.annotation.NonNull;

import java.util.Arrays;

public class SetMeasurementMessage extends Message {

    private Measurement measurement;

    public SetMeasurementMessage(Measurement measurement) {
        this.measurement = measurement;
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
