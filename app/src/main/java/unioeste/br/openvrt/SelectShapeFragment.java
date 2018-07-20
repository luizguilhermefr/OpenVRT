package unioeste.br.openvrt;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
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
import unioeste.br.openvrt.file.PrescriptionMapFinder;
import unioeste.br.openvrt.file.PrescriptionMapFinderCallback;

import java.io.File;
import java.util.ArrayList;

public class SelectShapeFragment extends Fragment {

    private static final int PERMISSION_READ_EXTERNAL_DIR = 1;

    private ShapeListFragmentInteractionListener mListener;

    private SelectShapeRecyclerViewAdapter mAdapter;

    private SwipeRefreshLayout swiper = null;

    private Thread shapeFinderThread = null;

    public SelectShapeFragment() {
        //
    }

    @NonNull
    static SelectShapeFragment newInstance() {
        return new SelectShapeFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_shape_list, container, false);
        Context context = view.getContext();
        mAdapter = new SelectShapeRecyclerViewAdapter(new ArrayList<>(), mListener);
        RecyclerView recyclerView = view.findViewById(R.id.shape_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(mAdapter);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        makeSwiper();
        scanOrAskPermissions();
    }

    private void scanOrAskPermissions() {
        if (hasPermissionToReadFiles()) {
            scanForFiles();
        } else {
            askPermissionToReadFiles();
        }
    }

    private void makeSwiper() {
        swiper = getView().findViewById(R.id.shape_list_swiper);
        swiper.setOnRefreshListener(this::scanOrAskPermissions);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mListener = (ShapeListFragmentInteractionListener) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        killShapeFinderThread();
        mListener = null;
    }

    private void killShapeFinderThread() {
        if (shapeFinderThread != null) {
            if (shapeFinderThread.isAlive() && !shapeFinderThread.isInterrupted()) {
                shapeFinderThread.interrupt();
            }
            shapeFinderThread = null;
        }
    }

    @NonNull
    private Boolean hasPermissionToReadFiles() {
        return ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private void askPermissionToReadFiles() {
        ActivityCompat.requestPermissions(getActivity(), new String[]{
                Manifest.permission.READ_EXTERNAL_STORAGE
        }, PERMISSION_READ_EXTERNAL_DIR);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_READ_EXTERNAL_DIR:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    scanForFiles();
                }
                break;
        }
    }

    private void scanForFiles() {
        File startingPoint = Environment.getExternalStorageDirectory();
        PrescriptionMapFinder shapeFinder = new PrescriptionMapFinder(startingPoint, new PrescriptionMapFinderCallback() {
            @Override
            public void onSearchStarted() {
                mAdapter.clear();
                getActivity().runOnUiThread(() -> {
                    mAdapter.notifyDataSetChanged();
                    swiper.setRefreshing(true);
                });
            }

            @Override
            public void onSearchEnded() {
                getActivity().runOnUiThread(() -> {
                    swiper.setRefreshing(false);
                });
            }

            @Override
            public void onShapeDiscovered(String file) {
                mAdapter.add(file);
                getActivity().runOnUiThread(() -> mAdapter.notifyDataSetChanged());
            }
        });
        shapeFinderThread = new Thread(shapeFinder);
        shapeFinderThread.start();
    }

    public interface ShapeListFragmentInteractionListener {
        void onShapeListFragmentInteraction(String item);
    }
}
