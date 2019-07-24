package com.example.woundmontoringapplicationv1.Adapters;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.woundmontoringapplicationv1.AlertReceiver;
import com.example.woundmontoringapplicationv1.DressingReminderItem;
import com.example.woundmontoringapplicationv1.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * A RecyclerAdapter that sets up the notifications, and their frequencies, for each dressing registered
 * with the user. It then adapts the data into a format that is suitable for the user of the application,
 * to be viewed in the Reminders Activity.
 */
public class    RemindersRecyclerAdapter extends RecyclerView.Adapter<RemindersRecyclerAdapter.RemindersViewHolder> {

    private Context context;
    private ArrayList<DressingReminderItem> dressingItems;

    //static variables for the reminder frequencies depending on threat levels
    private int NO_ALERT_REMINDER_FREQUENCY  = 24;
    private int AMBER_ALERT_REMINDER_FREQUENCY  = 8;
    private int RED_ALERT_REMINDER_FREQUENCY  = 4;

    /**
     *
     * @param context
     * @param dressingItems
     */
    public RemindersRecyclerAdapter(Context context, ArrayList<DressingReminderItem> dressingItems){
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
        DressingReminderItem currentItem = dressingItems.get(i);

        String qrid = currentItem.getQRID();
        String qrinfo = currentItem.getQRInfo();
        String location = currentItem.getLocation();
        String timestamp = currentItem.getTimestamp();
        String currentWarningLevel = currentItem.getCurrentWarningLevel();
        int alarmNeedsUpdating = currentItem.getAlarmNeedsUpdating();

        Log.d("FEARGS REMINDER", qrid + ", " + location + ", " + timestamp + ", " + currentWarningLevel + ", " + alarmNeedsUpdating);

        remindersViewHolder.qridTextView.setText("Dressing: " + qrinfo);
        remindersViewHolder.locationTextView.setText("Location: " + location);
        remindersViewHolder.timestampTextView.setText("First snapshot at: " + timestamp);

        //setup the alarm
        if(alarmNeedsUpdating == 1){

            if(!timestamp.equalsIgnoreCase("null")){
                setupAlarm(timestamp, qrid, location, currentWarningLevel, remindersViewHolder);
            }

            updateAlarmNeedsUpdating(qrid);

        }
        else{
            SharedPreferences sharedPreferences = context.getSharedPreferences("APPLICATION_PREFS", Context.MODE_PRIVATE);
            remindersViewHolder.alarm1View.setText(sharedPreferences.getString("Alarm" + qrid, "This dressing has not been analysed yet."));
        }
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
        public TextView timestampTextView;
        public TextView locationTextView;
        public TextView alarm1View, alarm2View, alarm3View;
        /**
         *
         * @param itemView
         */
        public RemindersViewHolder(@NonNull View itemView) {
            super(itemView);
            qridTextView = itemView.findViewById(R.id.qridTextView);
            timestampTextView = itemView.findViewById(R.id.textViewTimestamp);
            locationTextView = itemView.findViewById(R.id.textViewLocation);
            alarm1View = itemView.findViewById(R.id.alarmView1);
            alarm2View = itemView.findViewById(R.id.alarmView2);
            alarm3View = itemView.findViewById(R.id.alarmView3);
        }
    }

    /**
     *
     * @param timestamp
     * @param qrid
     * @param location
     * @param currentWarningLevel
     * @param remindersViewHolder
     */
    public void setupAlarm(String timestamp, String qrid, String location, String currentWarningLevel, @NonNull RemindersRecyclerAdapter.RemindersViewHolder remindersViewHolder){
        String[] partsOfTimestamp = timestamp.split(" ");
        Log.d("FEARGS TIMESTAMP", "" + timestamp);

        String time = partsOfTimestamp[1];

        int hour = Integer.parseInt(time.substring(0, 2));
        int minute = Integer.parseInt(time.substring(3, 5));
        Log.d("FEARGS PARSE HOUR", "" + hour);

        SharedPreferences sharedPreferences;
        sharedPreferences = context.getSharedPreferences("APPLICATION_PREFS", Context.MODE_PRIVATE);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);

        String alarmMessage;

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(context, AlertReceiver.class);
        //send message to the receiver
        intent.putExtra("QRID", qrid);
        intent.putExtra("Location", location);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, Integer.parseInt(qrid), intent, PendingIntent.FLAG_CANCEL_CURRENT);

        if(currentWarningLevel.equalsIgnoreCase("OK")){

            alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), 1000 * 60 * 60 * NO_ALERT_REMINDER_FREQUENCY, pendingIntent);

            alarmMessage = "Since your dressing is not on alert, you will receive a notification every day at " + hour + ":" + minute + ".";

            SharedPreferences.Editor editor = sharedPreferences.edit();

            editor.putString("Alarm" + qrid, alarmMessage);

            //important: **must apply to the Editor instance
            editor.apply();

            remindersViewHolder.alarm1View.setText(sharedPreferences.getString("Alarm" + qrid, ""));
            //remindersViewHolder.alarm1View.setText(alarmManager.getNextAlarmClock().describeContents());
        }

        //every 8 hours for amber level
        else if(currentWarningLevel.equalsIgnoreCase("AMBER")){
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), 1000 * 60 * 60 * AMBER_ALERT_REMINDER_FREQUENCY, pendingIntent);

            alarmMessage = "Since your dressing is on Amber alert, you are due to monitor your wound every " + AMBER_ALERT_REMINDER_FREQUENCY + " hours, starting at " + hour + ":00.";

            SharedPreferences.Editor editor = sharedPreferences.edit();

            editor.putString("Alarm" + qrid, alarmMessage);

            //important: **must apply to the Editor instance
            editor.apply();

            remindersViewHolder.alarm1View.setText(sharedPreferences.getString("Alarm" + qrid, ""));
            //remindersViewHolder.alarm2View.setText("Your next alarm is at: " + alarmManager.getNextAlarmClock().describeContents());
        }

        //every 4 hours for red level
        else if(currentWarningLevel.equalsIgnoreCase("RED")){
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), 1000 * 60 * 60 * RED_ALERT_REMINDER_FREQUENCY, pendingIntent);

            alarmMessage = "Since your dressing is on Red alert, you are due to monitor your wound every " + RED_ALERT_REMINDER_FREQUENCY + " hours, starting at " + hour + ":00.";

            SharedPreferences.Editor editor = sharedPreferences.edit();

            editor.putString("Alarm" + qrid, alarmMessage);

            //important: **must apply to the Editor instance
            editor.apply();

            remindersViewHolder.alarm1View.setText(sharedPreferences.getString("Alarm" + qrid, ""));
            //remindersViewHolder.alarm2View.setText("Your next alarm is at: " + alarmManager.getNextAlarmClock().describeContents());
        }

        else{
            Log.d("FEARGS ALARM", "Warning level invalid:" + currentWarningLevel);
        }

    }

    private void updateAlarmNeedsUpdating(final String qr){
        StringRequest stringRequest;
        RequestQueue requestQueue;
        String url = "http://foman01.lampt.eeecs.qub.ac.uk/woundmonitoring/updatealarmstatus.php";

        stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (response.equalsIgnoreCase("Success")) {
                    Log.d("FEARGS REM UPDATE REQ", "Successfully updated the qr's alarm status");
                } else {
                    Log.d("FEARGS REM UPDATE REQ", "Unsuccessful update to the qr's alarm status: " + response);

                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("FEARGS REM UPDATE REQ", "Error: " + error.toString());
            }
        }){
            @Override
            protected Map<String, String> getParams(){

                Map<String, String> params = new HashMap<String, String>();

                //adding all the values from the response and input to the map
                //keys are fields from database
                params.put("QRID", qr);

                return params;
            }
        };

        requestQueue = Volley.newRequestQueue(context);
        requestQueue.add(stringRequest);
    }
}
