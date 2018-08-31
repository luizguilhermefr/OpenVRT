package unioeste.br.openvrt.connection;

import android.support.annotation.NonNull;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class EndianessUtils {

    public static final int INT_LEN = 4;

    @NonNull
    public static byte[] intToLittleEndianBytes(int n) {
        ByteBuffer bb = ByteBuffer.allocate(INT_LEN);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.putInt(n);
        return bb.array();
    }

    public static int littleEndianBytesToInt(byte[] buf) {
        return ByteBuffer.wrap(buf).order(ByteOrder.LITTLE_ENDIAN).getInt();
    }
}
