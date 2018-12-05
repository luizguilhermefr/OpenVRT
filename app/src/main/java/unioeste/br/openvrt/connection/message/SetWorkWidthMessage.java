package unioeste.br.openvrt.connection.message;

import android.support.annotation.NonNull;
import unioeste.br.openvrt.connection.IdFactory;
import unioeste.br.openvrt.connection.message.dictionary.Opcode;

import java.util.Locale;

/**
 * Inform to the applier which work width should be used.
 */
public class SetWorkWidthMessage extends Message {

    private float rate;

    private SetWorkWidthMessage(char[] signature, short major, short minor, int id, float rate) {
        super(signature, major, minor, id);
        this.rate = rate;
        makeDataFromRate();
    }

    @NonNull
    public static SetWorkWidthMessage newInstance(float width) {
        int id = IdFactory.getInstance().next();
        return new SetWorkWidthMessage(SIGNATURE.toCharArray(), VERSION_MAJOR, VERSION_MINOR, id, width);
    }

    private void makeDataFromRate() {
        data = String.format(Locale.ENGLISH, "%09.2f", rate).replace(".", "").toCharArray();
    }


    @Override
    protected Opcode opcode() {
        return Opcode.WORK_WIDTH_SET;
    }
}
