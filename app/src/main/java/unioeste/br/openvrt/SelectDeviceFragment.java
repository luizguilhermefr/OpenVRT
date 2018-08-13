package unioeste.br.openvrt;

import android.content.Context;
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

    private Thread shapeFinderThread = null;

    public SelectDeviceFragment() {
        //
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
        //
    }

    private void scanForDevices() {
        //
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
}
