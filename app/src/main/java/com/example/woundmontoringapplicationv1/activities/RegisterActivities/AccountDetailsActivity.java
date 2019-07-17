package com.example.woundmontoringapplicationv1.activities.RegisterActivities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
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
import com.example.woundmontoringapplicationv1.R;
import com.example.woundmontoringapplicationv1.activities.LoginAndRegisterActivities.LoginActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.util.HashMap;
import java.util.Map;

public class AccountDetailsActivity extends AppCompatActivity {

    EditText email, password, passwordCheck, clinicianEmail;

    boolean fieldsFilledAndPasswordMatch;

    private String emailS, passwordS, passwordCheckS, clinicianS, fnS, lnS, a1S, a2S, townS, countryS, postcodeS;

    Button registerBtn;

    ProgressDialog progressDialog;

    FirebaseAuth firebaseAuth;

    RequestQueue requestQueue;

    String url = "http://foman01.lampt.eeecs.qub.ac.uk/woundmonitoring/register.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_details);

        email = findViewById(R.id.editText);
        password = findViewById(R.id.editText2);
        passwordCheck = findViewById(R.id.editText3);
        clinicianEmail = findViewById(R.id.editText4);
        registerBtn = findViewById(R.id.button3);
        requestQueue = Volley.newRequestQueue(AccountDetailsActivity.this);
        progressDialog = new ProgressDialog(AccountDetailsActivity.this);

        firebaseAuth = FirebaseAuth.getInstance();
        Bundle bundle = getIntent().getExtras();

        if(bundle != null){
            setPersonalAndAddressDetails(bundle.getString("FirstName"), bundle.getString("LastName"), bundle.getString("FirstName"),
                    bundle.getString("Address1"), bundle.getString("Address2"), bundle.getString("Town"),
                    bundle.getString("Country"), bundle.getString("Postcode"));

            registerBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    fieldsFilledAndPasswordMatch = checkFieldsFilled();

                    if(fieldsFilledAndPasswordMatch){

                        userRegister();

                    }
                }
            });
        }
    }

    /**
     * @return
     */
    private boolean checkFieldsFilled(){
        boolean filled;

        emailS = email.getText().toString().trim();
        passwordS = password.getText().toString().trim();
        passwordCheckS = passwordCheck.getText().toString().trim();
        clinicianS = clinicianEmail.getText().toString().trim();

        if(emailS.isEmpty() || passwordS.isEmpty() || passwordCheckS.isEmpty() || clinicianS.isEmpty()){
            filled = false;
            Toast.makeText(getApplicationContext(), "Please fill in all the fields in the form!", Toast.LENGTH_LONG).show();
        }
        else{
            if(passwordS.equals(passwordCheckS)){
                filled = true;
            }
            else {
                filled = false;
                Toast.makeText(getApplicationContext(), "Passwords don't match!", Toast.LENGTH_LONG).show();
            }
        }

        return filled;
    }

    /**
     *
     */
    public void userRegister(){
        progressDialog.setMessage("Please wait...");
        progressDialog.show();

        final StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                progressDialog.dismiss();

                if(response.equalsIgnoreCase("Successful Registration")){
                    //at this point also create a firebase user
                    firebaseAuth.createUserWithEmailAndPassword(emailS, passwordS).addOnCompleteListener(AccountDetailsActivity.this,
                            new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if(task.isSuccessful()){
                                        Toast.makeText(getApplicationContext(), "Well done, you've registered a new account!", Toast.LENGTH_SHORT).show();

                                        //we don't want the user to actually be automatically signed in
                                        firebaseAuth.signOut();

                                        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                                        intent.putExtra("IntentFrom", "Registration");
                                        startActivity(intent);
                                    }
                                    else{
                                        Toast.makeText(getApplicationContext(), "Something went wrong with the Firebase Registration!", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });


                }
                else if(response.equalsIgnoreCase("Email already exists")){
                    Log.d("FEARGS REGISTER", "WE'VE GOTTEN AS FAR AS THE RESPONSE - AND THE RESPONSE SAYS EMAIL ALREADY IN USE");
                    Toast.makeText(getApplicationContext(), "Sorry, this email looks like it's already in use!", Toast.LENGTH_SHORT).show();
                }
                else if(response.equalsIgnoreCase("Clinician does not exist")){
                    Log.d("FEARGS REGISTER", "GOTTEN RESPONSE - DR DOESNT EXIST");
                    Toast.makeText(getApplicationContext(), "Sorry, this clinicisn does nt exist - check email!", Toast.LENGTH_SHORT).show();
                }
                else{
                    Log.d("FEARGS REGISTER", "WE'VE GOTTEN AS FAR AS THE RESPONSE - AND THE RESPONSE RUBBISH");
                    Log.d("FEARGS REGISTER", response);
                    Log.d("FEARGS REGISTER", "pw: " + passwordS);
                    Log.d("FEARGS REGISTER", "email: " + emailS);
                    Log.d("FEARGS REGISTER", "fn: " + fnS);
                    Log.d("FEARGS REGISTER", "ln: " + lnS);
                    Log.d("FEARGS REGISTER", "a1: " + a1S);
                    Log.d("FEARGS REGISTER", "a2: " + a2S);
                    Log.d("FEARGS REGISTER", "town: " + townS);
                    Log.d("FEARGS REGISTER", "country: " + countryS);
                    Log.d("FEARGS REGISTER", "pc: " + postcodeS);
                    Log.d("FEARGS REGISTER", "clinician: " + clinicianS);


                    Toast.makeText(getApplicationContext(), response, Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismiss();
                Toast.makeText(getApplicationContext(), "ERROR " + error.toString() + "ERROR ", Toast.LENGTH_LONG).show();
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                Log.d("FEARGS REGISTER", "STRING REQUEST IS SENDING THE PARAMTERS ALRIGHT");

                Map<String, String> params = new HashMap<String, String>();

                params.put("Email", emailS);
                params.put("Password", passwordS);
                params.put("FName", fnS);
                params.put("LName", lnS);
                //params.put("Email", dobS);
                params.put("Add1", a1S);
                params.put("Add2", a2S);
                params.put("Town", townS);
                params.put("Country", countryS);
                params.put("PostCode", postcodeS);
                params.put("Clinician", clinicianS);

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
     *
     * @param firstName
     * @param lastName
     * @param dateOfBirth
     * @param add1
     * @param add2
     * @param town
     * @param country
     * @param postcode
     */
    public void setPersonalAndAddressDetails(String firstName,String lastName,String dateOfBirth,
                                             String add1,String add2,String town,String country,String postcode){
        fnS = firstName;
        lnS = lastName;
        a1S = add1;
        a2S = add2;
        townS = town;
        countryS = country;
        postcodeS = postcode;
        Log.d("FEARG", dateOfBirth);

    }
}
