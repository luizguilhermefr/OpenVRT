package unioeste.br.openvrt;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.File;
import java.util.List;

public class MyShapeRecyclerViewAdapter extends RecyclerView.Adapter<MyShapeRecyclerViewAdapter.ViewHolder> {

    private final List<File> mValues;

    private final ShapeFragment.ShapeListFragmentInteractionListener mListener;

    MyShapeRecyclerViewAdapter(List<File> items, ShapeFragment.ShapeListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_shape, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mPathView.setText(mValues.get(position).getAbsolutePath());
        holder.mNameView.setText(mValues.get(position).getName());
        holder.mView.setOnClickListener(v -> {
            if (mListener != null) {
                mListener.onShapeListFragmentInteraction(holder.mItem);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        final View mView;

        final TextView mPathView;

        final TextView mNameView;

        File mItem;

        ViewHolder(View view) {
            super(view);
            mView = view;
            mPathView = view.findViewById(R.id.item_number);
            mNameView = view.findViewById(R.id.content);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mNameView.getText() + "'";
        }
    }
}
