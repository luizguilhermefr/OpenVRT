package unioeste.br.openvrt.connection;

import android.support.annotation.NonNull;

public class EndianessUtils {

    @NonNull
    public static byte[] shortToBigEndianBytes(short val) {
        return new byte[]{
                (byte) ((val >> 8) & 0xFF),
                (byte) (val & 0xFF)
        };
    }

}
