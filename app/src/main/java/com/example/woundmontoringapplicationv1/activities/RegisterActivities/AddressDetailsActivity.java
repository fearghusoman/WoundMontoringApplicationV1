package com.example.woundmontoringapplicationv1.activities.RegisterActivities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.woundmontoringapplicationv1.R;

public class AddressDetailsActivity extends AppCompatActivity {

    private EditText a1, a2, town, country, postcode;

    private Button continueBtn;

    private String a1S, a2S, townS, countryS, postcodeS, fName, lName, dateOfBirth;

    private boolean fieldsFilled;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address_details);

        a1 =  findViewById(R.id.editText);
        a2 =  findViewById(R.id.editText2);
        town =  findViewById(R.id.editText3);
        country =  findViewById(R.id.editText4);
        postcode = findViewById(R.id.editText5);

        continueBtn = findViewById(R.id.button3);

        //get data from last intent
        Bundle bundle = getIntent().getExtras();

        if(bundle != null){
            setPersonalDetails(bundle.getString("FirstName"), bundle.getString("LastName"), bundle.getString("DateOfBirth"));

            continueBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    fieldsFilled = checkFieldsFilled();

                    if(fieldsFilled){

                        //intent to next activity
                        Intent intent = new Intent(getApplicationContext(), AccountDetailsActivity.class);
                        intent.putExtra("Address1", a1S);
                        intent.putExtra("Address2", a2S);
                        intent.putExtra("Town", townS);
                        intent.putExtra("Country", countryS);
                        intent.putExtra("Postcode", postcodeS);
                        intent.putExtra("FirstName", fName);
                        intent.putExtra("LastName", lName);
                        intent.putExtra("DateOfBirth", dateOfBirth);
                        startActivity(intent);

                    }
                }
            });
        }



    }

    /**
     *
     * @return
     */
    private boolean checkFieldsFilled(){
        boolean filled;

        a1S = a1.getText().toString().trim();
        a2S = a2.getText().toString().trim();
        townS = town.getText().toString().trim();
        countryS = country.getText().toString().trim();
        postcodeS = postcode.getText().toString().trim();

        if(a1S.isEmpty() || a2S.isEmpty() || townS.isEmpty() || countryS.isEmpty() || postcodeS.isEmpty()){
            filled = false;
            Toast.makeText(getApplicationContext(), "Please fill in all the fields in the form!", Toast.LENGTH_LONG).show();
        }
        else{
            filled = true;
        }

        return filled;
    }

    /**
     *
     * @param fn
     * @param ln
     * @param dob
     */
    public void setPersonalDetails(String fn, String ln, String dob){
        fName = fn;
        lName = ln;
        dateOfBirth = dob;
    }
}
