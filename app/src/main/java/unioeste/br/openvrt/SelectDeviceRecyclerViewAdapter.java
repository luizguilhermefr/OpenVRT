package unioeste.br.openvrt;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class SelectDeviceRecyclerViewAdapter extends RecyclerView.Adapter<SelectDeviceRecyclerViewAdapter.ViewHolder> {

    private final ArrayList<String> mValues;

    private final SelectDeviceFragment.DeviceListFragmentInteractionListener mListener;

    SelectDeviceRecyclerViewAdapter(ArrayList<String> items, SelectDeviceFragment.DeviceListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_device_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);

        holder.mNameView.setText(mValues.get(position));

        holder.mView.setOnClickListener(v -> {
            if (mListener != null) {
                mListener.onDeviceListFragmentInteraction(holder.mItem);
            }
        });
    }

    public void add(String item) {
        mValues.add(item);
    }

    public void clear() {
        mValues.clear();
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        final View mView;

        final TextView mNameView;

        String mItem;

        ViewHolder(View view) {
            super(view);
            mView = view;
            mNameView = view.findViewById(R.id.device_list_item);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mNameView.getText() + "'";
        }
    }
}
