package com.example.woundmontoringapplicationv1;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    //EditText objects
    EditText Email, Password;

    //Button object
    Button LoginButton;

    //Create a Volley (REST) RequestQueue
    RequestQueue requestQueue;

    //Strings to hold the values entered
    String EmailHolder, PasswordHolder;

    ProgressDialog progressDialog;

    //Storing URL in a String variable
    String hhtpUrl = "http://foman01.lampt.eeecs.qub.ac.uk/woundmonitoring/login.php";

    Boolean checkEditText, userLoggedIn;

    String responseUserChecker;

    //textView object for signup
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //assign IDs to the objects created
        Email = (EditText) findViewById(R.id.plain_text_input_email);
        Password = (EditText) findViewById(R.id.input_password);
        LoginButton = (Button) findViewById(R.id.btn_login);
        textView = (TextView) findViewById(R.id.link_signup);

        requestQueue = Volley.newRequestQueue(LoginActivity.this);
        progressDialog = new ProgressDialog(LoginActivity.this);

        LoginButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                CheckLoginFieldsAreNotEmpty();

                if(checkEditText){ //if there are no empty fields then call the userlogin method
                    UserLogin();
                }
                else{ //if there are empty fields then popup error message
                    Toast.makeText(LoginActivity.this, "Please Fill In All Form Fields!", Toast.LENGTH_LONG).show();
                }
            }
        });

        //if textview is clicked then the register activity is started
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent registerIntent = new Intent(getApplicationContext(), RegisterActivity.class);
                startActivity(registerIntent);
            }
        });
    }

    /**
     * method that processes the request and response to and from the api
     * will either log the user in or not
     */
    public void UserLogin(){
        //show progress dialogue at user registration time
        progressDialog.setMessage("Please wait...");
        progressDialog.show();


        //create a string request with the POST method - Stringrequest comes from volley library
        StringRequest stringRequest = new StringRequest(Request.Method.POST, hhtpUrl,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String ServerResponse) {

                        Log.d("Debug", ServerResponse.toString());
                        //hide the progress dialogue after all tasks completed
                        progressDialog.dismiss();

                        //make sure server response matches what we want
                        //for a data match
                        //if(ServerResponse.equalsIgnoreCase(responseUserChecker)){
                        if(ServerResponse.equalsIgnoreCase("login successful")){
                            //if the responses match then shw the toast
                            Toast.makeText(LoginActivity.this, "Logged In Succesfully", Toast.LENGTH_LONG).show();

                            //finish the login activity - amend this to actually do something else
                            //if the user has logged in successfully then set the value of userLoggedIn to true
                            userLoggedIn = true;
                            Log.d("FEARGS LOGIN", "This is the log in val: " + userLoggedIn);

                            if(userLoggedIn == true){ //build an intent to link the home activity
                                Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                                startActivity(intent);
                            }
                            else{ //otherwise end the whole shenanigans
                                finish();
                            }

                        } else {
                            Toast.makeText(LoginActivity.this, "Incorrect login details", Toast.LENGTH_LONG).show();
                            Log.d("FEARGS LOGIN", ServerResponse.toString());
                            userLoggedIn = false;
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        progressDialog.dismiss();
                        Toast.makeText(LoginActivity.this, "ERROR PAL" + volleyError.toString() + "ERROR PAL", Toast.LENGTH_LONG).show();
                    }
                }
        ){
            @Override
            protected Map<String, String> getParams(){

                Map<String, String> params = new HashMap<String, String>();

                //adding all the values from the response and input to the map
                //keys are fields from database
                params.put("Email", EmailHolder);
                params.put("Password", PasswordHolder);

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

        //creating requestQueue - already done beforehand, elsewhere within the activity
        RequestQueue requestQueue = Volley.newRequestQueue(LoginActivity.this);

        //add the StringRequest into the requestQueue
        requestQueue.add(stringRequest);
        //requestQueue.add(jsonObjectRequest);
    }

    /**
     * Check that neither of the text fields have been left empty.
     * Also assign the checker string that will he used to check whether the entered credentials
     * are correct or incorrect.
     */
    public void CheckLoginFieldsAreNotEmpty(){
        //get values from edittext fields
        EmailHolder = Email.getText().toString().trim();
        PasswordHolder = Password.getText().toString().trim();

        responseUserChecker = "{\"user\":{\"email\":\"" + EmailHolder + "\",\"password\":\"" + PasswordHolder + "\"}}";

        //check whether either is empty
        if(EmailHolder.isEmpty() || PasswordHolder.isEmpty()){
            checkEditText = false;
        }
        else{
            checkEditText = true;
        }
    }
}
