package com.example.woundmontoringapplicationv1.Adapters;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.woundmontoringapplicationv1.DressingItem;
import com.example.woundmontoringapplicationv1.R;

import java.util.ArrayList;

/**
 * This adapter presents the registered dressing data to the user; with each
 * dressing in its own section of the recycler view.
 */
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
        registeredDressingViewHolder.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "You're deleting the dressing", Toast.LENGTH_LONG).show();
                
            }
        });
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
        public TextView qrInfoTextView__;

        public TextView qridTextView;
        public TextView qridTextView__;

        public TextView locationTextView;
        public TextView locationTextView__;

        public Button deleteButton;

        /**
         *
         * @param itemView
         */
        public RegisteredDressingViewHolder(@NonNull View itemView) {
            super(itemView);
            qrInfoTextView = itemView.findViewById(R.id.textViewQRInfo);
            qrInfoTextView__ = itemView.findViewById(R.id.textViewQRInfo__);

            qridTextView = itemView.findViewById(R.id.textViewQRID);
            qridTextView__ = itemView.findViewById(R.id.textView__);

            locationTextView = itemView.findViewById(R.id.textViewLocation);
            locationTextView__ = itemView.findViewById(R.id.textViewLocation__);

            deleteButton = itemView.findViewById(R.id.buttonDeleteDressing);
        }
    }
}
