package com.example.woundmontoringapplicationv1;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class HomeFragmentRecyclerAdapter extends RecyclerView.Adapter<HomeFragmentRecyclerAdapter.HomeFragmentViewHolder> {

    private Context context;
    private ArrayList<SnapshotItem> snapshotItems;

    /**
     * default constructor
     * @param context
     * @param snapshotItems
     */
    public HomeFragmentRecyclerAdapter(Context context, ArrayList<SnapshotItem> snapshotItems){
        this.context = context;
        this.snapshotItems = snapshotItems;
    }

    /**
     *
     * @param viewGroup
     * @param i
     * @return
     */
    @NonNull
    @Override
    public HomeFragmentViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(context).inflate(R.layout.recycler_item, viewGroup, false);
        return new HomeFragmentViewHolder(v);
    }

    /**
     * this is where we take the current item in the arraylist and use its attributes
     * to setup the view
     * @param homeFragmentViewHolder
     * @param i
     */
    @Override
    public void onBindViewHolder(@NonNull HomeFragmentViewHolder homeFragmentViewHolder, int i) {
        SnapshotItem currentItem = snapshotItems.get(i);

        String qrInfo = currentItem.getQRInfo();
        String timestamp = currentItem.getTimestamp();

        homeFragmentViewHolder.qrInfoTextView.setText(qrInfo);
        homeFragmentViewHolder.timestampTextView.setText(timestamp);
    }

    /**
     * returns the amount of items in the arraylist - this is how many recycler views we will have
     * @return
     */
    @Override
    public int getItemCount() {
        return snapshotItems.size();
    }

    /**
     *
     */
    public class HomeFragmentViewHolder extends RecyclerView.ViewHolder{

        public TextView qrInfoTextView;
        public TextView timestampTextView;

        /**
         *
         * @param itemView
         */
        public HomeFragmentViewHolder(@NonNull View itemView) {
            super(itemView);
            qrInfoTextView = itemView.findViewById(R.id.textViewQR);
            timestampTextView = itemView.findViewById(R.id.textViewTime);
        }
    }
}
