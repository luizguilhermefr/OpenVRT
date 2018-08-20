package unioeste.br.openvrt.connection;

import android.bluetooth.BluetoothSocket;
import android.support.annotation.NonNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ConnectedThread extends Thread {

    private static int MSG_LEN = 255;

    private OnMessageReceivedListener messageReceivedListener;

    private OnSocketReceiveErrorListener socketReceiveErrorListener;

    private OnSocketSendErrorListener socketSendErrorListener;

    private InputStream istream;

    private OutputStream ostream;

    private boolean shouldDie = false;

    public ConnectedThread(@NonNull BluetoothSocket socket) throws IOException {
        this.istream = socket.getInputStream();
        this.ostream = socket.getOutputStream();
    }

    public void setOnMessageReceivedListener(OnMessageReceivedListener messageReceivedListener) {
        this.messageReceivedListener = messageReceivedListener;
    }

    public void setOnSocketErrorListener(OnSocketReceiveErrorListener socketErrorListener) {
        this.socketReceiveErrorListener = socketErrorListener;
    }

    public void setOnSocketSendErrorListener(OnSocketSendErrorListener socketSendErrorListener) {
        this.socketSendErrorListener = socketSendErrorListener;
    }

    private boolean messageReady() throws IOException {
        return istream.available() >= MSG_LEN;
    }

    private void onMessageReceived(String message) {
        if (messageReceivedListener != null) {
            messageReceivedListener.onMessageReceived(message);
        }
    }

    private void onSocketReceiveError() {
        if (socketReceiveErrorListener != null) {
            socketReceiveErrorListener.onSocketReceiveError();
        }
    }

    private void onSocketSendError() {
        if (socketSendErrorListener != null) {
            socketSendErrorListener.onSocketSendError();
        }
    }

    public void cancel() {
        shouldDie = true;
    }

    public void write(byte[] message) {
        try {
            ostream.write(message);
        } catch (IOException e) {
            onSocketSendError();
        }
    }

    @Override
    public void run() {
        byte[] buffer = new byte[MSG_LEN]; // TODO: Replace magic number with const
        int bytesRead;
        while (!shouldDie) {
            try {
                if (messageReady()) {
                    bytesRead = istream.read(buffer, 0, MSG_LEN);
                    if (bytesRead > 0) {
                        onMessageReceived(new String(buffer, 0, bytesRead));
                    } else {
                        // TODO: Cannot read all bytes?
                    }
                }
            } catch (IOException e) {
                onSocketReceiveError();
            }
        }
    }

    public interface OnMessageReceivedListener {
        void onMessageReceived(String message);
    }

    public interface OnSocketReceiveErrorListener {
        void onSocketReceiveError();
    }

    public interface OnSocketSendErrorListener {
        void onSocketSendError();
    }
}
