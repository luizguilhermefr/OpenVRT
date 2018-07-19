package unioeste.br.openvrt;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

public class SelectShapeFragment extends Fragment {

    private ShapeListFragmentInteractionListener mListener;

    public SelectShapeFragment() {
        //
    }

    @NonNull
    static SelectShapeFragment newInstance(ArrayList<String> files) {
        SelectShapeFragment selectShapeFragment = new SelectShapeFragment();
        Bundle args = new Bundle();
        args.putStringArrayList("files", files);
        selectShapeFragment.setArguments(args);

        return selectShapeFragment;
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
        Bundle args = getArguments();
        ArrayList<String> files = args != null ? args.getStringArrayList("files") : new ArrayList<>();
        RecyclerView recyclerView = (RecyclerView) view;
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(new SelectShapeRecyclerViewAdapter(files, mListener));

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mListener = (ShapeListFragmentInteractionListener) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface ShapeListFragmentInteractionListener {
        void onShapeListFragmentInteraction(String item);
    }
}
