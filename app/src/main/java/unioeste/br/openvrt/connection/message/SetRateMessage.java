package unioeste.br.openvrt.connection.message;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;

public class SetRateMessage extends Message {

    private BigDecimal value;

    public SetRateMessage(BigDecimal value) {
        this.value = value;
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
