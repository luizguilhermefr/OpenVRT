package unioeste.br.openvrt.connection.message;

import android.support.annotation.NonNull;
import unioeste.br.openvrt.connection.IdFactory;
import unioeste.br.openvrt.connection.message.dictionary.Opcode;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;

/**
 * Inform to the applier what flow rate should be used.
 */
public class SetRateMessage extends Message {

    private BigDecimal rate;

    public SetRateMessage(char[] signature, short major, short minor, int id, BigDecimal rate) {
        super(signature, major, minor, id);
        this.rate = rate;
        makeDataFromRate();
    }

    @NonNull
    public static SetRateMessage newInstance(BigDecimal rate) {
        int id = IdFactory.getInstance().next();
        return new SetRateMessage(SIGNATURE.toCharArray(), VERSION_MAJOR, VERSION_MINOR, id, rate);
    }

    private void makeDataFromRate() {
        DecimalFormat df = new DecimalFormat("000000.00");
        df.setRoundingMode(RoundingMode.DOWN);
        data = df.format(rate).replace(".", "").toCharArray();
    }


    @Override
    protected Opcode opcode() {
        return Opcode.RATE_SET;
    }
}
