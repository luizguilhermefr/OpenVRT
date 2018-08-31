package unioeste.br.openvrt.connection.message;

import unioeste.br.openvrt.connection.EndianessUtils;
import unioeste.br.openvrt.connection.IdFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;

/**
 * Inform to the applier what flow rate should be used.
 */
public class SetRateMessage extends Message {

    private BigDecimal value;

    private int ourId;

    public SetRateMessage(BigDecimal value) {
        this.ourId = IdFactory.getInstance().next();
        this.value = value;
    }

    public SetRateMessage(int ourId, BigDecimal value) {
        this.ourId = ourId;
        this.value = value;
    }

    @Override
    protected byte[] id() {
        return EndianessUtils.intToLittleEndianBytes(ourId);
    }

    @Override
    protected byte[] data() {
        DecimalFormat df = new DecimalFormat("000000.00");
        df.setRoundingMode(RoundingMode.DOWN);
        return df.format(value).replace(".", "").getBytes();
    }

    @Override
    protected Opcode opcode() {
        return Opcode.RATE_SET;
    }
}
