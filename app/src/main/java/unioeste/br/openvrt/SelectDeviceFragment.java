package unioeste.br.openvrt;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Objects;

public class SelectDeviceFragment extends Fragment {

    private DeviceListFragmentInteractionListener mListener;

    private SelectDeviceRecyclerViewAdapter mAdapter;

    private SwipeRefreshLayout swiper = null;

    private BluetoothAdapter bluetoothAdapter;

    private BroadcastReceiver broadcastReceiver;

    public SelectDeviceFragment() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        broadcastReceiver = new DeviceBroadcastReceiver();
    }

    @NonNull
    static SelectDeviceFragment newInstance() {
        return new SelectDeviceFragment();
    }

    private void makeSwiper() {
        swiper = Objects.requireNonNull(getView()).findViewById(R.id.device_list_swiper);
        swiper.setOnRefreshListener(this::scanForDevices);
    }

    private void killDeviceFinderThread() {
        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }
        Objects.requireNonNull(getContext()).unregisterReceiver(broadcastReceiver);
    }

    private void scanForDevices() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        bluetoothAdapter.startDiscovery();
        Objects.requireNonNull(getContext()).registerReceiver(broadcastReceiver, filter);
    }

    private void onDeviceFound(@NonNull BluetoothDevice device) {
        mAdapter.add(device.getName());
        Objects.requireNonNull(getActivity()).runOnUiThread(() -> mAdapter.notifyDataSetChanged());
    }

    private void onDiscoveryFinished() {
        Objects.requireNonNull(getActivity()).runOnUiThread(() -> swiper.setRefreshing(false));
        killDeviceFinderThread();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mListener = (DeviceListFragmentInteractionListener) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        killDeviceFinderThread();
        mListener = null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        scanForDevices();
    }

    public interface DeviceListFragmentInteractionListener {
        void onDeviceListFragmentInteraction(String item);
    }

    private class DeviceBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Objects.equals(action, BluetoothDevice.ACTION_FOUND)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                onDeviceFound(device);
            } else if (Objects.equals(action, BluetoothAdapter.ACTION_DISCOVERY_FINISHED)) {
                onDiscoveryFinished();
            }
        }
    }
}
