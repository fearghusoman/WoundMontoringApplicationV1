package com.example.woundmontoringapplicationv1;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class RegisteredDressingRecyclerAdapter extends RecyclerView.Adapter<RegisteredDressingRecyclerAdapter.RegisteredDressingViewHolder> {

    private Context context;
    private ArrayList<DressingItem> dressingItems;

    /**
     *
     * @param context
     * @param dressingItems
     */
    public RegisteredDressingRecyclerAdapter(Context context, ArrayList<DressingItem> dressingItems){
        this.context = context;
        this.dressingItems = dressingItems;
    }

    /**
     *
     * @param viewGroup
     * @param i
     * @return
     */
    @NonNull
    @Override
    public RegisteredDressingRecyclerAdapter.RegisteredDressingViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(context).inflate(R.layout.recycler_item_reg_dressings, viewGroup, false);
        return new RegisteredDressingRecyclerAdapter.RegisteredDressingViewHolder(v);
    }

    /**
     *
     * @param registeredDressingViewHolder
     * @param i
     */
    @Override
    public void onBindViewHolder(@NonNull RegisteredDressingRecyclerAdapter.RegisteredDressingViewHolder registeredDressingViewHolder, int i) {
        DressingItem currentItem = dressingItems.get(i);

        String qrid = currentItem.getQRID();
        String qrInfo = currentItem.getQRInfo();
        String location = currentItem.getLocation();

        registeredDressingViewHolder.qridTextView.setText(qrid);
        registeredDressingViewHolder.qrInfoTextView.setText(qrInfo);
        registeredDressingViewHolder.locationTextView.setText(location);
    }

    /**
     * returns the amount of items in the arraylist - this is how many recycler views we will have
     * @return
     */
    @Override
    public int getItemCount() {
        return dressingItems.size();
    }

    /**
     *
     */
    public class RegisteredDressingViewHolder extends RecyclerView.ViewHolder{

        public TextView qrInfoTextView;
        public TextView qridTextView;
        public TextView locationTextView;

        /**
         *
         * @param itemView
         */
        public RegisteredDressingViewHolder(@NonNull View itemView) {
            super(itemView);
            qrInfoTextView = itemView.findViewById(R.id.textViewQRInfo);
            qridTextView = itemView.findViewById(R.id.textViewQRID);
            locationTextView = itemView.findViewById(R.id.textViewLocation);
        }
    }
}
