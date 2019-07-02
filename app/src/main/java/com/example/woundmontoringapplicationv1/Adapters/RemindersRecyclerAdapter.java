package com.example.woundmontoringapplicationv1.Adapters;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.woundmontoringapplicationv1.DressingItem;
import com.example.woundmontoringapplicationv1.R;

import java.util.ArrayList;

/**
 *
 */
public class RemindersRecyclerAdapter extends RecyclerView.Adapter<RemindersRecyclerAdapter.RemindersViewHolder> {

    private Context context;
    private ArrayList<DressingItem> dressingItems;

    /**
     *
     * @param context
     * @param dressingItems
     */
    public RemindersRecyclerAdapter(Context context, ArrayList<DressingItem> dressingItems){
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
    public RemindersRecyclerAdapter.RemindersViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(context).inflate(R.layout.recycler_item_reminder, viewGroup, false);
        return new RemindersRecyclerAdapter.RemindersViewHolder(v);
    }

    /**
     *
     * @param remindersViewHolder
     * @param i
     */
    @Override
    public void onBindViewHolder(@NonNull RemindersRecyclerAdapter.RemindersViewHolder remindersViewHolder, int i) {
        DressingItem currentItem = dressingItems.get(i);

        String qrid = currentItem.getQRID();

        remindersViewHolder.qridTextView.setText(qrid);
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
    public class RemindersViewHolder extends RecyclerView.ViewHolder{

        public TextView qridTextView;
        /**
         *
         * @param itemView
         */
        public RemindersViewHolder(@NonNull View itemView) {
            super(itemView);
            qridTextView = itemView.findViewById(R.id.textViewQRID);
        }
    }
}
