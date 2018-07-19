package unioeste.br.openvrt;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class EmptyStateFragment extends Fragment {

    public EmptyStateFragment() {
        // Required empty public constructor
    }


    public static EmptyStateFragment newInstance() {
        return new EmptyStateFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_empty_state, container, false);
    }
}
