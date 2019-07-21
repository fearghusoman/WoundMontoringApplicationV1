package com.example.woundmontoringapplicationv1.activities.RegisterActivities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.woundmontoringapplicationv1.R;
import com.example.woundmontoringapplicationv1.activities.CalendarFragments.DatePickerFragment;

import java.text.DateFormat;
import java.util.Calendar;

public class PersonalDetailsActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    private Boolean fieldsFilled;

    private Button continueBtn, datePicker;

    private EditText fName, lName;

    private TextView dateOfBirth;

    private String fnS, lnS, dob;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_details);

        fName = findViewById(R.id.editText);
        lName = findViewById(R.id.editText2);
        continueBtn = findViewById(R.id.button3);
        datePicker = findViewById(R.id.btnDOB);
        dateOfBirth = findViewById(R.id.tvDOB);

        datePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment datePicker = new DatePickerFragment();
                datePicker.show(getSupportFragmentManager(), "date picker");
            }
        });

        continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                fieldsFilled = checkFieldsFilled();

                if(fieldsFilled){

                    //pass the data to the underlying activity - use intent
                    Intent intent = new Intent(getApplicationContext(), AddressDetailsActivity.class);
                    intent.putExtra("FirstName", fnS);
                    intent.putExtra("LastName", lnS);
                    intent.putExtra("DateOfBirth", dob);
                    startActivity(intent);

                }
            }
        });

    }

    /**
     *
     * @param view
     * @param year
     * @param month
     * @param dayOfMonth
     */
    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, month);
        c.set(Calendar.DAY_OF_MONTH, dayOfMonth);

        String currentDateString = DateFormat.getDateInstance(DateFormat.FULL).format(c.getTime());
        String alternativeDateString = DateFormat.getDateInstance(DateFormat.SHORT).format(c.getTime());
        String alternativeDateString2;

        if(c.get(Calendar.MONTH) < 10){
            if(c.get(Calendar.DAY_OF_MONTH) < 10){
                alternativeDateString2 = c.get(Calendar.YEAR) + "-0" + c.get(Calendar.MONTH) + "-0" + c.get(Calendar.DAY_OF_MONTH);
            }
            else{
                alternativeDateString2 = c.get(Calendar.YEAR) + "-0" + c.get(Calendar.MONTH) + "-" + c.get(Calendar.DAY_OF_MONTH);
            }

        }
        else{
            if(c.get(Calendar.DAY_OF_MONTH) < 10){
                alternativeDateString2 = c.get(Calendar.YEAR) + "-" + c.get(Calendar.MONTH) + "-0" + c.get(Calendar.DAY_OF_MONTH);
            }
            else{
                alternativeDateString2 = c.get(Calendar.YEAR) + "-" + c.get(Calendar.MONTH) + "-" + c.get(Calendar.DAY_OF_MONTH);
            }
        }

        Log.d("FEARGS TIME", alternativeDateString);
        Log.d("FEARGS TIME", alternativeDateString2);

        dateOfBirth.setText(alternativeDateString2);
    }

    /**
     *
     * @return
     */
    private boolean checkFieldsFilled(){
        boolean filled;

        fnS = fName.getText().toString().trim();
        lnS = lName.getText().toString().trim();
        dob = dateOfBirth.getText().toString().trim();

        if(fnS.isEmpty() || lnS.isEmpty() || dob.equalsIgnoreCase("Date of Birth")){
            filled = false;
            Toast.makeText(getApplicationContext(), "Please fill in all the fields in the form!", Toast.LENGTH_LONG).show();
        }
        else{
            filled = true;
        }

        return filled;
    }
}
