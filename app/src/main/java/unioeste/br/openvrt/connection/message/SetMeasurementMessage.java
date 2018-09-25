package unioeste.br.openvrt.connection.message;

import android.support.annotation.NonNull;
import unioeste.br.openvrt.connection.IdFactory;
import unioeste.br.openvrt.connection.message.dictionary.Measurement;
import unioeste.br.openvrt.connection.message.dictionary.Opcode;

import java.util.Arrays;

/**
 * Inform to the applier which measurement form should be used.
 */
public class SetMeasurementMessage extends Message {

    private Measurement measurement;

    private SetMeasurementMessage(char[] signature, short major, short minor, int id, Measurement measurement) {
        super(signature, major, minor, id);
        this.measurement = measurement;
        makeDataFromMeasurement();
    }

    @NonNull
    public static SetMeasurementMessage newInstance(@NonNull Measurement measurement) {
        int id = IdFactory.getInstance().next();
        return new SetMeasurementMessage(SIGNATURE.toCharArray(), VERSION_MAJOR, VERSION_MINOR, id, measurement);
    }

    private void makeDataFromMeasurement() {
        String measureStr = measurement.toString();
        data = new char[DATA_LEN];
        Arrays.fill(data, 0, DATA_LEN - measureStr.length() - 1, Character.MIN_VALUE);
        System.arraycopy(measureStr.toCharArray(), 0, data, DATA_LEN - measureStr.length(), measureStr.length());
    }

    public Measurement getMeasurement() {
        return measurement;
    }

    @Override
    protected Opcode opcode() {
        return Opcode.MEASURE_SET;
    }
}
