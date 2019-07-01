package com.example.woundmontoringapplicationv1;

import android.app.TimePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.widget.TimePicker;

import java.util.ArrayList;
import java.util.Calendar;

/**
 *
 */
public class RemindersActivityV2 extends AppCompatActivity implements TimePickerDialog.OnTimeSetListener{

    Context context;

    SharedPreferences sharedPreferences;

    RecyclerView recyclerView;

    ArrayList<DressingItem> dressingItems;

    RemindersRecyclerAdapter remindersRecyclerAdapter;

    /**
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminders_v2);

        //set-up the variables for the shared preferences
        context = getApplicationContext();
        sharedPreferences = context.getSharedPreferences("APPLICATION_PREFS", Context.MODE_PRIVATE);

        //assign the recycler view to a variable
        recyclerView = findViewById(R.id.recycler_viewReminders);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.HORIZONTAL));


        //instantiate the recycler item array list
        dressingItems = new ArrayList<>();

        //get the number of dressings for this user from the shared preferences
        int num = sharedPreferences.getInt("X", 0) + 1;
        Log.d("FEARGS DRESSNUM", "There are " + num + " dressings registered with this user in shared preferences");

        for(int i = 0; i < num; i++){
            String qrid = sharedPreferences.getString("QRID" + i, "");
            dressingItems.add(new DressingItem(qrid));
        }

        remindersRecyclerAdapter = new RemindersRecyclerAdapter(getApplicationContext(), dressingItems);
        recyclerView.setAdapter(remindersRecyclerAdapter);
    }

    /**
     * Activity  implements the TimePickerDialog.OnTimesetListener and so must override the
     * onTimeSet method.
     * @param view
     * @param hourOfDay
     * @param minute
     */
    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, hourOfDay);
        c.set(Calendar.MINUTE, minute);
        c.set(Calendar.SECOND, 0);

    }
}
