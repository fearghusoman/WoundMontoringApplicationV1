package com.example.woundmontoringapplicationv1.Adapters;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.woundmontoringapplicationv1.R;
import com.example.woundmontoringapplicationv1.SnapshotItem;

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
        String deltaEC1 = currentItem.getDeltaEC1();
        String deltaEC2 = currentItem.getDeltaEC2();
        String deltaEC3 = currentItem.getDeltaEC3();
        String deltaEC4 = currentItem.getDeltaEC4();
        String warning = currentItem.getWarning();

        homeFragmentViewHolder.qrInfoTextView.setText(qrInfo);
        homeFragmentViewHolder.timestampTextView.setText(timestamp);
        homeFragmentViewHolder.detlaeTextView.setText("O2: " + deltaEC1 + ", H2O: " + deltaEC2 +
                                                            ", CO2: " + deltaEC3 + ", RNH2: " + deltaEC4);
        homeFragmentViewHolder.warningTextView.setText(warning);

        switch(warning){
            case "OK":
                homeFragmentViewHolder.warningTextView.setTextColor(Color.GREEN);
                break;
            case "AMBER":
                homeFragmentViewHolder.warningTextView.setTextColor(Color.YELLOW);
                break;
            case "RED":
                homeFragmentViewHolder.warningTextView.setTextColor(Color.RED);
                break;
        }

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
        public TextView detlaeTextView;
        public TextView warningTextView;

        /**
         *
         * @param itemView
         */
        public HomeFragmentViewHolder(@NonNull View itemView) {
            super(itemView);
            qrInfoTextView = itemView.findViewById(R.id.textViewQR);
            timestampTextView = itemView.findViewById(R.id.textViewTime);
            detlaeTextView = itemView.findViewById(R.id.textViewDeltaE);
            warningTextView = itemView.findViewById(R.id.textViewWarningLevel);
        }
    }
}
