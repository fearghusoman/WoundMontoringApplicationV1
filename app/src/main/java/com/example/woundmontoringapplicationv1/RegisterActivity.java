package com.example.woundmontoringapplicationv1;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener{

    EditText fn, ln, a1, a2, town, country, postcode, email, pw, pwCheck, clinicianEmail;

    Button registerBtn, dob;

    TextView textViewDOB;

    Boolean fieldsFilled;

    RequestQueue requestQueue;
    ProgressDialog progressDialog;

    String fnS, lnS, dobS, a1S, a2S, townS, countryS, postcodeS, emailS, pwS, pwCheckS, clinicianEmailS;
    String url = "http://foman01.lampt.eeecs.qub.ac.uk/woundmonitoring/register.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        fn = findViewById(R.id.editFName);
        ln = findViewById(R.id.editLName);
        dob = findViewById(R.id.btnDOB);
        a1 = findViewById(R.id.editAddress1);
        a2 = findViewById(R.id.editAddress2);
        town = findViewById(R.id.editTown);
        country = findViewById(R.id.editCountry);
        postcode = findViewById(R.id.editPCode);
        email = findViewById(R.id.editEmail);
        pw = findViewById(R.id.editPassword2);
        pwCheck = findViewById(R.id.editPassword);
        clinicianEmail = findViewById(R.id.editClinicianEmail);
        textViewDOB = findViewById(R.id.tvDOB);

        dob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment datePicker = new DatePickerFragment();
                datePicker.show(getSupportFragmentManager(), "date picker");
            }
        });

        registerBtn = findViewById(R.id.buttonRegister);

        requestQueue = Volley.newRequestQueue(RegisterActivity.this);
        progressDialog = new ProgressDialog(RegisterActivity.this);

        //when a user clicks register the first thing is to check that all the fields have been
        //filled out
        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.d("FEARGS REGISTER", "REGISTER BUTTON IS CLICKED");
                CheckFieldsAreFilledAndPasswordsMatch();

                if(fieldsFilled){
                    UserRegister();
                }
                else{
                    Log.d("FEARGS REGISTER", "REGISTER ATTEMPTED BUT NOT ALL FIELDS CORRECT");
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

        textViewDOB.setText(currentDateString);
    }

    /**
     *
     */
    public void UserRegister(){
        progressDialog.setMessage("Please wait...");
        progressDialog.show();

        final StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("FEARGS REGISTER", response.toString());
                Log.d("FEARGS REGISTER", "GOT A RESPONSE FROM THE SERVER");
                progressDialog.dismiss();

                if(response.equalsIgnoreCase("Successful Registration")){
                    Log.d("FEARGS REGISTER", "EVERYTHING SHOULD BE PERFECT NOW");
                    Toast.makeText(RegisterActivity.this, "Well done, you've registered a new account!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(intent);
                }
                else if(response.equalsIgnoreCase("Email already exists")){
                    Log.d("FEARGS REGISTER", "WE'VE GOTTEN AS FAR AS THE RESPONSE - AND THE RESPONSE SAYS EMAIL ALREADY IN USE");
                    Toast.makeText(RegisterActivity.this, "Sorry, this email looks like it's already in use!", Toast.LENGTH_SHORT).show();
                }
                else if(response.equalsIgnoreCase("Clinician does not exist")){
                    Log.d("FEARGS REGISTER", "GOTTEN RESPONSE - DR DOESNT EXIST");
                    Toast.makeText(RegisterActivity.this, "Sorry, this clinicisn does nt exist - check email!", Toast.LENGTH_SHORT).show();
                }
                else{
                    Log.d("FEARGS REGISTER", "WE'VE GOTTEN AS FAR AS THE RESPONSE - AND THE RESPONSE RUBBISH");
                    Log.d("FEARGS REGISTER", response);
                    Log.d("FEARGS REGISTER", pwS);
                    Toast.makeText(RegisterActivity.this, response, Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismiss();
                Toast.makeText(RegisterActivity.this, "ERROR " + error.toString() + "ERROR ", Toast.LENGTH_LONG).show();
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                Log.d("FEARGS REGISTER", "STRING REQUEST IS SENDING THE PARAMTERS ALRIGHT");

                Map<String, String> params = new HashMap<String, String>();

                params.put("Email", emailS);
                params.put("Password", pwS);
                params.put("FName", fnS);
                params.put("LName", lnS);
                //params.put("Email", dobS);
                params.put("Add1", a1S);
                params.put("Add2", a2S);
                params.put("Town", townS);
                params.put("Country", countryS);
                params.put("PostCode", postcodeS);
                params.put("Clinician", clinicianEmailS);

                return params;
            }
        };

        stringRequest.setRetryPolicy(new RetryPolicy() {
            @Override
            public int getCurrentTimeout() {
                return 5000;
            }

            @Override
            public int getCurrentRetryCount() {
                return 5000;
            }

            @Override
            public void retry(VolleyError error) throws VolleyError {

            }
        });

        requestQueue.add(stringRequest);
    }


    /**
     * method that returns false to the onclicklistener if any of the fields in the form are empty
     * or if the passwords don't match
     */
    public void CheckFieldsAreFilledAndPasswordsMatch(){

        Log.d("FEARGS REGISTER", "CHECKING IF FIELDS ARE FILLED");

        fnS = fn.getText().toString().trim();
        lnS = ln.getText().toString().trim();
        dobS = dob.getText().toString().trim();
        a1S = a1.getText().toString().trim();
        a2S = a2.getText().toString().trim();
        townS = town.getText().toString().trim();
        countryS = country.getText().toString().trim();
        postcodeS = postcode.getText().toString().trim();
        emailS = email.getText().toString().trim();
        pwS = pw.getText().toString().trim();
        pwCheckS = pwCheck.getText().toString().trim();
        clinicianEmailS = clinicianEmail.getText().toString().trim();

        if(fnS.isEmpty() || lnS.isEmpty() || dobS.isEmpty() || a1S.isEmpty() || a2S.isEmpty() || townS.isEmpty() || countryS.isEmpty()
                || postcodeS.isEmpty() || emailS.isEmpty() || pwS.isEmpty() || pwCheckS.isEmpty() || clinicianEmailS.isEmpty()){

            Log.d("FEARGS REGISTER", "NOT ALL FIELDS ARE FILLED");
            Toast.makeText(RegisterActivity.this, "Please fill in all the fields in the form!", Toast.LENGTH_LONG).show();
            fieldsFilled = false;

        }
        else{
            if(!pwS.equalsIgnoreCase(pwCheckS)){
                Log.d("FEARGS REGISTER", "ALL FIELDS FILLED BUT THE PASSWORDS DON'T MATCH");
                Toast.makeText(RegisterActivity.this, "Passwords do not match", Toast.LENGTH_LONG).show();
                fieldsFilled = false;
            }
            else{
                Log.d("FEARGS REGISTER", "EVERYTHING IS HUNKY DORY SO FAR - FIELDS FILLED & PWS MATCH");
                fieldsFilled = true;
            }
        }

    }
}
