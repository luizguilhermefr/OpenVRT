package unioeste.br.openvrt;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import unioeste.br.openvrt.connection.ConnectThread;
import unioeste.br.openvrt.connection.ConnectedThread;

public class SelectDeviceActivity extends AppCompatActivity implements SelectDeviceFragment.DeviceListFragmentInteractionListener {

    private SelectDeviceFragment selectDeviceFragment;

    private BluetoothDevice selectedDevice;

    private ConnectedThread connectedThread;

    private Snackbar snackbar;

    private String mapLocation;

    private void toSettingsActivity() {
        Intent intent = new Intent(this, ApplicationSettingsActivity.class);
        intent.putExtra("map", mapLocation);
        startActivity(intent);
    }

    private void onSelectDevice(@NonNull BluetoothDevice device) {
        selectedDevice = device;
        runOnUiThread(() -> selectDeviceFragment.lockList());
        connect();
    }

    private void onConnecting() {
        runOnUiThread(() -> {
            snackbar.setText(getString(R.string.connecting, selectedDevice.getName()));
            snackbar.setAction("", v -> {
                // No action
            });
            snackbar.show();
        });
    }

    private void onConnected(ConnectedThread connectedThread) {
        this.connectedThread = connectedThread;
        this.runOnUiThread(() -> {
            snackbar.dismiss();
            selectDeviceFragment.unlockList();
        });
        toSettingsActivity();
    }

    private void onConnectionError() {
        runOnUiThread(() -> {
            selectDeviceFragment.unlockList();
            snackbar.setText(getString(R.string.error_connecting, selectedDevice.getName()));
            snackbar.setAction(getString(R.string.retry), v -> onSelectDevice(selectedDevice));
            snackbar.show();
        });
    }

    private void connect() {
        ConnectThread connectThread = new ConnectThread(selectedDevice);
        connectThread.setOnConnectingListener(this::onConnecting);
        connectThread.setOnConnectedListener(this::onConnected);
        connectThread.setOnCannotConnectListener(this::onConnectionError);
        connectThread.start();
    }

    private void makeSnackbar() {
        snackbar = Snackbar.make(findViewById(R.id.device_fragment_container), "", Snackbar.LENGTH_INDEFINITE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (connectedThread != null) {
            connectedThread.cancel();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        mapLocation = intent.getStringExtra("map");
        setContentView(R.layout.activity_select_device);
        selectDeviceFragment = SelectDeviceFragment.newInstance();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.device_fragment_container, selectDeviceFragment);
        fragmentTransaction.commit();
        makeSnackbar();
    }

    @Override
    public void onDeviceListFragmentInteraction(BluetoothDevice device) {
        onSelectDevice(device);
    }
}
