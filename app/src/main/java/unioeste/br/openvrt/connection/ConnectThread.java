package unioeste.br.openvrt.connection;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import unioeste.br.openvrt.connection.message.HandshakeMessage;

import java.io.IOException;
import java.util.UUID;

public class ConnectThread extends Thread {

    private OnConnectingListener connectingListener;

    private OnConnectedListener connectedListener;

    private OnCannotConnectListener cannotConnectListener;

    private UUID uuid;

    private BluetoothDevice device;

    private ConnectedThread connectedThread;

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

    private void handshake() {
        HandshakeMessage handshakeMessage = new HandshakeMessage();
        handshakeMessage.setResponseListener(response -> {
            switch (response) {
                case ACK_TIMEOUT:
                case ACK_NEGATIVE:
                    connectedThread.cancel();
                    onCannotConnect();
                case ACK_POSITIVE:
                    onConnected();
            }
        });
        connectedThread.write(handshakeMessage);
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
