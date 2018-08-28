package unioeste.br.openvrt.connection;

import android.bluetooth.BluetoothSocket;
import android.support.annotation.NonNull;
import unioeste.br.openvrt.connection.exception.InvalidMessageException;
import unioeste.br.openvrt.connection.message.Message;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ConnectedThread extends Thread {

    private static ConnectedThread instance;

    private OnMessageReceivedListener messageReceivedListener;

    private OnSocketReceiveErrorListener socketReceiveErrorListener;

    private OnSocketSendErrorListener socketSendErrorListener;

    private InputStream istream;

    private OutputStream ostream;

    private BluetoothSocket socket;

    private boolean shouldDie = false;

    private ConnectedThread() {
        //
    }

    public static ConnectedThread getInstance() {
        if (instance == null) {
            instance = new ConnectedThread();
        }

        return instance;
    }

    public void setConnection(@NonNull BluetoothSocket socket) throws IOException {
        this.socket = socket;
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
        return istream.available() >= Message.MSG_LEN;
    }

    private void onMessageReceived(byte[] buffer) {
        if (messageReceivedListener != null) {
            try {
                Message message = Message.make(buffer);
                messageReceivedListener.onMessageReceived(message);
            } catch (InvalidMessageException e) {
                e.printStackTrace();
            }
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

    public void write(Message message) {
        try {
            ostream.write(message.toBytes());
        } catch (IOException e) {
            onSocketSendError();
        }
    }

    private void bury() {
        try {
            istream.close();
            ostream.close();
            socket.close();
        } catch (IOException ignored) {
            //
        }
    }

    @Override
    public void run() {
        if (istream == null || ostream == null) {
            throw new IllegalArgumentException("Must call setConnection() before start.");
        }
        byte[] buffer = new byte[Message.MSG_LEN];
        int bytesRead;
        while (!shouldDie) {
            try {
                if (messageReady()) {
                    bytesRead = istream.read(buffer, 0, Message.MSG_LEN);
                    if (bytesRead > 0) {
                        onMessageReceived(buffer);
                    } else {
                        // TODO: Cannot read?
                    }
                }
            } catch (IOException e) {
                onSocketReceiveError();
            }
        }
        bury();
    }

    public interface OnMessageReceivedListener {
        void onMessageReceived(Message message);
    }

    public interface OnSocketReceiveErrorListener {
        void onSocketReceiveError();
    }

    public interface OnSocketSendErrorListener {
        void onSocketSendError();
    }
}
