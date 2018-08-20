package unioeste.br.openvrt.connection;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.util.UUID;

public class ConnectThread extends Thread {

    private OnConnectingListener connectingListener;

    private OnConnectedListener connectedListener;

    private OnCannotConnectListener cannotConnectListener;

    private UUID uuid;

    private BluetoothDevice pairedDevice;

    public ConnectThread(BluetoothDevice pairedDevice, UUID uuid) {
        this.pairedDevice = pairedDevice;
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

    private void onConnected(BluetoothSocket socket) {
        if (connectedListener != null) {
            connectedListener.onConnected(socket);
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

    @Override
    public void run() {
        onConnecting();
        try {
            BluetoothSocket socket = pairedDevice.createRfcommSocketToServiceRecord(uuid);
            onConnected(socket);
        } catch (IOException e) {
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
        void onConnected(BluetoothSocket socket);
    }
}
