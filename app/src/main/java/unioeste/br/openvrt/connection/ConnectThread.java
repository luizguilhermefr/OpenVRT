package unioeste.br.openvrt.connection;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import unioeste.br.openvrt.connection.exception.HandshakeException;
import unioeste.br.openvrt.connection.message.HandshakeMessage;
import unioeste.br.openvrt.connection.message.Message;

import java.io.IOException;
import java.util.UUID;

public class ConnectThread extends Thread {

    private OnConnectingListener connectingListener;

    private OnConnectedListener connectedListener;

    private OnCannotConnectListener cannotConnectListener;

    private UUID uuid;

    private BluetoothDevice device;

    private ConnectedThread connectedThread;

    private boolean handshaked = false;

    public ConnectThread(BluetoothDevice device, UUID uuid) {
        this.device = device;
        this.uuid = uuid;
    }

    private void onConnecting() {
        if (connectingListener != null) {
            connectingListener.onConnecting();
        }
    }

    private void onCannotConnect() {
        if (cannotConnectListener != null) {
            cannotConnectListener.onCannotConnect();
        }
    }

    private void onConnected() {
        if (connectedListener != null) {
            connectedListener.onConnected(connectedThread);
        }
    }

    public void setOnConnectingListener(OnConnectingListener connectingListener) {
        this.connectingListener = connectingListener;
    }

    public void setOnConnectedListener(OnConnectedListener connectedListener) {
        this.connectedListener = connectedListener;
    }

    public void setOnCannotConnectListener(OnCannotConnectListener cannotConnectListener) {
        this.cannotConnectListener = cannotConnectListener;
    }

    private void handshakeSleep() {
        try {
            final int HANDSHAKE_TIMEOUT = 1000;
            Thread.sleep(HANDSHAKE_TIMEOUT);
        } catch (InterruptedException e) {
            //
        }
    }

    private synchronized void handshake() throws HandshakeException {
        bindToHandshake();
        connectedThread.write(new HandshakeMessage());
        handshakeSleep();
        unbindToHandshake();
        if (!handshaked) {
            throw new HandshakeException();
        }
    }

    private synchronized void handshakeListener(Message message) {
        if (message instanceof HandshakeMessage) {
            handshaked = true;
            interrupt();
        }
    }

    private void bindToHandshake() {
        connectedThread.setOnMessageReceivedListener(this::handshakeListener);
    }

    private void unbindToHandshake() {
        connectedThread.setOnMessageReceivedListener(null);
    }

    @Override
    public void run() {
        onConnecting();
        try {
            BluetoothSocket socket = device.createRfcommSocketToServiceRecord(uuid);
            socket.connect();
            connectedThread = ConnectedThread.getInstance();
            connectedThread.setConnection(socket);
            connectedThread.start();
            handshake();
            onConnected();
        } catch (HandshakeException e) {
            e.printStackTrace();
            connectedThread.cancel();
            onCannotConnect();
        } catch (IOException e) {
            e.printStackTrace();
            onCannotConnect();
        }
    }

    public interface OnConnectingListener {
        void onConnecting();
    }

    public interface OnCannotConnectListener {
        void onCannotConnect();
    }

    public interface OnConnectedListener {
        void onConnected(ConnectedThread connectedThread);
    }
}
