package com.example.woundmontoringapplicationv1;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.DateFormat;
import java.util.Calendar;

/**
 *
 */
public class RemindersActivity extends AppCompatActivity implements TimePickerDialog.OnTimeSetListener{

    TextView textView, textView2, textView3, textViewEmpty;
    View view2, view3, view4;
    Button button1, button2, button3;
    String timeText;
    FloatingActionButton floatingActionButton, floatingActionButtonBack;
    int numberOfAlarmsSet = 0;

    /**
     * onCreate method is called when the activity is first created within
     * the application
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminders);

        textView = findViewById(R.id.textTime);
        textView2 = findViewById(R.id.textTime1);
        textView3 = findViewById(R.id.textTime2);
        textViewEmpty = findViewById(R.id.textViewNoAlarms);

        view2 = findViewById(R.id.view2);
        view3 = findViewById(R.id.view3);
        view4 = findViewById(R.id.view4);

        button1 = findViewById(R.id.cancelReminder);
        button2 = findViewById(R.id.cancelReminder1);
        button3 = findViewById(R.id.cancelReminder2);

        floatingActionButton = findViewById(R.id.addNewReminder);
        floatingActionButtonBack = findViewById(R.id.backToMenu);

        floatingActionButtonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                intent.putExtra("message", "yourdatafragment");
                startActivity(intent);
            }
        });

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(numberOfAlarmsSet >= 0 && numberOfAlarmsSet < 3){
                    DialogFragment timePicker = new TimePickerFragment();
                    timePicker.show(getSupportFragmentManager(), "time picker");
                }
                else{
                    Toast.makeText(RemindersActivity.this, "The maximum alarms allowed is 3!", Toast.LENGTH_LONG);
                }

            }
        });

        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelAlarm(textView, button1, view2);
            }
        });

        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelAlarm(textView2, button2, view3);
            }
        });

        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelAlarm(textView3, button3, view4);
            }
        });

        if(savedInstanceState != null){
            timeText = savedInstanceState.getString("setTime1");
            textView.setText(timeText);
        }
    }

    /**
     *
     * @param outState
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString("setTime1", timeText);
    }

    /**
     * Activity  implements the TimePickerDialog.OnTimesetListener and so must override the
     * onTimeSet method.
     * We get the chosen time from the dialog and using this time pass it to the startAlarm
     * and updateTimeText methods
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

        updateTimeText(c);
        startAlarm(c);
    }

    /**
     * update the view's text with the time chosen from the time picker
     * dialog
     * @param c
     */
    private void updateTimeText(Calendar c) {
        timeText = "";
        timeText += DateFormat.getTimeInstance(DateFormat.SHORT).format(c.getTime());

        switch(numberOfAlarmsSet){
            case 0:
                textView.setText(timeText);
                break;
            case 1:
                textView2.setText(timeText);
                break;
            case 2:
                textView3.setText(timeText);
                break;
        }

        showAlarm();
    }

    /**
     * once the alarm has been set we can show it on screen and cancel the other text view
     */
    private void showAlarm(){
        switch(numberOfAlarmsSet){
            case 0:
                textView.setVisibility(View.VISIBLE);
                button1.setVisibility(View.VISIBLE);
                view2.setVisibility(View.VISIBLE);
                textViewEmpty.setVisibility(View.GONE);
                break;
            case 1:
                textView2.setVisibility(View.VISIBLE);
                button2.setVisibility(View.VISIBLE);
                view3.setVisibility(View.VISIBLE);
                break;
            case 2:
                textView3.setVisibility(View.VISIBLE);
                button3.setVisibility(View.VISIBLE);
                view4.setVisibility(View.VISIBLE);
                break;
        }

        numberOfAlarmsSet += 1;
    }

    /**
     * we take the chosen time from the dialog and pass it through an intent
     * to an instance of the alarmmanager class using the alertreceiver class
     * @param c
     */
    private void startAlarm(Calendar c) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlertReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 1, intent, 0);

        if (c.before(Calendar.getInstance())) {
            c.add(Calendar.DATE, 1);
        }

        alarmManager.setExact(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), pendingIntent);
    }

    /**
     * this method is called when the cancelalarm button is clicked by the user
     * it cancels the alarmmanager intent and updates the text view
     */
    private void cancelAlarm(TextView tv, Button btn, View v) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlertReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 1, intent, 0);

        alarmManager.cancel(pendingIntent);
        tv.setVisibility(View.GONE);
        btn.setVisibility(View.GONE);
        v.setVisibility(View.GONE);
        numberOfAlarmsSet -= 1;

        if(numberOfAlarmsSet == 0){
            textViewEmpty.setText("There are no reminders set.");
            textViewEmpty.setVisibility(View.VISIBLE);
        }
    }
}
