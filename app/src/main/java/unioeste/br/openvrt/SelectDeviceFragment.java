package unioeste.br.openvrt;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Objects;

public class SelectDeviceFragment extends Fragment {

    private static final int PERMISSION_ACCESS_FINE_LOCATION = 21;

    private static final int BT_ENABLE_REQUEST = 41;

    private DeviceListFragmentInteractionListener mListener;

    private SelectDeviceRecyclerViewAdapter mAdapter;

    private SwipeRefreshLayout swiper = null;

    private BluetoothAdapter bluetoothAdapter;

    private BroadcastReceiver broadcastReceiver;

    public SelectDeviceFragment() {
        //
    }

    @NonNull
    static SelectDeviceFragment newInstance() {
        return new SelectDeviceFragment();
    }

    private void makeSwiper() {
        swiper = Objects.requireNonNull(getView()).findViewById(R.id.device_list_swiper);
        swiper.setOnRefreshListener(this::askPermissionsToUseGpsOrPrepareBluetooth);
    }

    private void scanForDevices() {
        onDiscoveryStarted();
        boolean discovering = bluetoothAdapter.startDiscovery();
        if (!discovering) {
            // TODO: Discovery error.
        }
    }

    private void onDeviceFound(@NonNull BluetoothDevice device) {
        mAdapter.add(device.getName());
        Objects.requireNonNull(getActivity()).runOnUiThread(() -> mAdapter.notifyDataSetChanged());
    }

    private void onDiscoveryStarted() {
        mAdapter.clear();
        Objects.requireNonNull(getActivity()).runOnUiThread(() -> {
            mAdapter.notifyDataSetChanged();
            swiper.setRefreshing(true);
        });
    }

    private void onDiscoveryFinished() {
        Objects.requireNonNull(getActivity()).runOnUiThread(() -> swiper.setRefreshing(false));
    }

    private void initiateBroadcastReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        broadcastReceiver = new DeviceDiscoveryBroadcastReceiver();
        Objects.requireNonNull(getContext()).registerReceiver(broadcastReceiver, filter);
    }

    private void prepareBluetoothAndScanForDevices() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            // TODO: Error! Device doesn't has bluetooth!!!
        } else {
            if (bluetoothAdapter.isEnabled()) {
                scanForDevices();
            } else {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, BT_ENABLE_REQUEST);
            }
        }
    }

    private void askPermissionsToUseGpsOrPrepareBluetooth() {
        if (hasPermissionToUseGps()) {
            prepareBluetoothAndScanForDevices();
        } else {
            askPermissionToUseGps();
        }
    }

    @NonNull
    private Boolean hasPermissionToUseGps() {
        return ContextCompat.checkSelfPermission(Objects.requireNonNull(getContext()),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void askPermissionToUseGps() {
        ActivityCompat.requestPermissions(Objects.requireNonNull(getActivity()), new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION
        }, PERMISSION_ACCESS_FINE_LOCATION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_ACCESS_FINE_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    prepareBluetoothAndScanForDevices();
                }
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case BT_ENABLE_REQUEST:
                if (resultCode == Activity.RESULT_OK) {
                    scanForDevices();
                }
                break;
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mListener = (DeviceListFragmentInteractionListener) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Objects.requireNonNull(getContext()).unregisterReceiver(broadcastReceiver);
        bluetoothAdapter.cancelDiscovery();
        mListener = null;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_device_list, container, false);
        Context context = view.getContext();
        mAdapter = new SelectDeviceRecyclerViewAdapter(new ArrayList<>(), mListener);
        RecyclerView recyclerView = view.findViewById(R.id.device_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(mAdapter);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        makeSwiper();
        initiateBroadcastReceiver();
        askPermissionsToUseGpsOrPrepareBluetooth();
    }

    public interface DeviceListFragmentInteractionListener {
        void onDeviceListFragmentInteraction(String item);
    }

    private class DeviceDiscoveryBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null) {
                if (action.equals(BluetoothDevice.ACTION_FOUND)) {
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    onDeviceFound(device);
                } else if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)) {
                    onDiscoveryFinished();
                }
            }
        }
    }
}
