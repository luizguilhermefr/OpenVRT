package unioeste.br.openvrt.connection.message.dictionary;

public enum Measurement {
    KG_HA("KG_HA"), L_HA("L_HA");

    public final int length = 2;

    public final String code;

    Measurement(final String code) {
        this.code = code;
    }
}
