package unioeste.br.openvrt.connection.message;

import android.support.annotation.NonNull;
import unioeste.br.openvrt.connection.IdFactory;
import unioeste.br.openvrt.connection.message.dictionary.Opcode;

import java.util.Locale;

/**
 * Inform to the applier what flow rate should be used.
 */
public class SetRateMessage extends Message {

    private float rate;

    private SetRateMessage(char[] signature, short major, short minor, int id, float rate) {
        super(signature, major, minor, id);
        this.rate = rate;
        makeDataFromRate();
    }

    @NonNull
    public static SetRateMessage newInstance(float rate) {
        int id = IdFactory.getInstance().next();
        return new SetRateMessage(SIGNATURE.toCharArray(), VERSION_MAJOR, VERSION_MINOR, id, rate);
    }

    private void makeDataFromRate() {
        data = String.format(Locale.ENGLISH, "%09.2f", rate).replace(".", "").toCharArray();
    }


    @Override
    protected Opcode opcode() {
        return Opcode.RATE_SET;
    }
}
