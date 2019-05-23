package com.example.woundmontoringapplicationv1;

import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    EditText fn, ln, dob, a1, a2, town, country, postcode, email, pw, pwCheck;
    Button registerBtn;

    Boolean fieldsFilled;

    RequestQueue requestQueue;
    ProgressDialog progressDialog;

    String fnS, lnS, dobS, a1S, a2S, townS, countryS, postcodeS, emailS, pwS, pwCheckS;
    String url = "http://foman01.lampt.eeecs.qub.ac.uk/woundmonitoring/register.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        fn = findViewById(R.id.editFName);
        ln = findViewById(R.id.editLName);
        dob = findViewById(R.id.editDOB);
        a1 = findViewById(R.id.editAddress1);
        a2 = findViewById(R.id.editAddress2);
        town = findViewById(R.id.editTown);
        country = findViewById(R.id.editCountry);
        postcode = findViewById(R.id.editPCode);
        email = findViewById(R.id.editEmail);
        pw = findViewById(R.id.editPassword2);
        pwCheck = findViewById(R.id.editPassword);

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
     */
    public void UserRegister(){
        progressDialog.setMessage("Please wait...");
        progressDialog.show();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("FEARGS REGISTER", response.toString());
                Log.d("FEARGS REGISTER", "GOT A RESPONSE FROM THE SERVER");
                progressDialog.dismiss();

                if(response.equalsIgnoreCase("Successful Registration")){
                    Log.d("FEARGS REGISTER", "EVERYTHING SHOULD BE PERFECT NOW");
                    Toast.makeText(RegisterActivity.this, "Well done, you've registered a new account!", Toast.LENGTH_LONG);
                }
                else if(response.equalsIgnoreCase("Email already exists")){
                    Log.d("FEARGS REGISTER", "WE'VE GOTTEN AS FAR AS THE RESPONSE - AND THE RESPONSE SAYS EMAIL ALREADY IN USE");
                    Toast.makeText(RegisterActivity.this, "Sorry, this email looks like it's already in use!", Toast.LENGTH_SHORT).show();
                }
                else{
                    Log.d("FEARGS REGISTER", "WE'VE GOTTEN AS FAR AS THE RESPONSE - AND THE RESPONSE RUBBISH");
                    Log.d("FEARGS REGISTER", response.toString());
                    Log.d("FEARGS REGISTER", pwS);
                    Toast.makeText(RegisterActivity.this, response.toString(), Toast.LENGTH_SHORT).show();
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

        if(fnS.isEmpty() || lnS.isEmpty() || dobS.isEmpty() || a1S.isEmpty() || a2S.isEmpty() || townS.isEmpty() || countryS.isEmpty()
                || postcodeS.isEmpty() || emailS.isEmpty() || pwS.isEmpty() || pwCheckS.isEmpty()){

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
