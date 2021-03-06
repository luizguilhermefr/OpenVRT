package unioeste.br.openvrt;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import unioeste.br.openvrt.file.PrescriptionMapFinder;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

public class SelectShapeFragment extends Fragment {

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

    private void makeSwiper() {
        swiper = Objects.requireNonNull(getView()).findViewById(R.id.shape_list_swiper);
        swiper.setOnRefreshListener(this::scanForFiles);
    }

    private void killShapeFinderThread() {
        if (shapeFinderThread != null) {
            if (shapeFinderThread.isAlive() && !shapeFinderThread.isInterrupted()) {
                shapeFinderThread.interrupt();
            }
            shapeFinderThread = null;
        }
    }

    private void onSearchStarted() {
        mAdapter.clear();
        Objects.requireNonNull(getActivity()).runOnUiThread(() -> {
            mAdapter.notifyDataSetChanged();
            swiper.setRefreshing(true);
        });
    }

    private void onSearchEnded() {
        Objects.requireNonNull(getActivity()).runOnUiThread(() -> swiper.setRefreshing(false));
    }

    private void onShapeDiscovered(String file) {
        mAdapter.add(file);
        Objects.requireNonNull(getActivity()).runOnUiThread(() -> mAdapter.notifyDataSetChanged());
    }

    private void scanForFiles() {
        File startingPoint = Environment.getExternalStorageDirectory();
        PrescriptionMapFinder shapeFinder = new PrescriptionMapFinder(startingPoint);
        shapeFinder.setOnSearchStartedListener(this::onSearchStarted);
        shapeFinder.setOnSearchEndedListener(this::onSearchEnded);
        shapeFinder.setOnShapeDiscoveredListener(this::onShapeDiscovered);
        shapeFinderThread = new Thread(shapeFinder);
        shapeFinderThread.start();
        // TODO: Alert when no maps found?
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
        scanForFiles();
    }

    public interface ShapeListFragmentInteractionListener {
        void onShapeListFragmentInteraction(String item);
    }
}
