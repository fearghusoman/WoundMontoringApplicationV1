package com.example.woundmontoringapplicationv1;

import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class AnalysisSubmissionActivity extends AppCompatActivity {

    boolean success;

    Bundle bundle;

    StringRequest stringRequest;

    String userEmail, qrInfo, timestamp, c1HashMap, c2HashMap, c3HashMap, c4HashMap;

    String url = "http://foman01.lampt.eeecs.qub.ac.uk/woundmonitoring/insert_analysis.php";

    ProgressDialog progressDialog;

    TextView textView;

    FloatingActionButton floatingActionButton;

    //Create a Volley (REST) RequestQueue
    RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analysis_submission);

        textView = findViewById(R.id.textViewSubmitted);
        floatingActionButton = findViewById(R.id.buttonHome);

        progressDialog = new ProgressDialog(AnalysisSubmissionActivity.this);

        bundle = getIntent().getExtras();

        if(bundle != null){
            userEmail = bundle.getString("UserEmail");

            qrInfo = bundle.getString("QRInfo");

            c1HashMap = bundle.getString("Circle1");
            c2HashMap = bundle.getString("Circle2");
            c3HashMap = bundle.getString("Circle3");
            c4HashMap = bundle.getString("Circle4");

            timestamp = bundle.getString("Timestamp");

            progressDialog.setMessage("Please wait...");
            progressDialog.show();

            stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.d("FEARGS CHECK", response);

                    if(response.substring(0, 10).equalsIgnoreCase("successful")){
                        success = true;
                    }
                    else{
                        success = false;
                    }

                    progressDialog.dismiss();
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d("FEARGS CHECK", "ERROR: " + error.toString());
                    progressDialog.dismiss();
                }
            })
            {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params = new HashMap<String, String>();

                    params.put("user_email", userEmail);
                    params.put("qr_info", qrInfo);
                    params.put("c1", c1HashMap);
                    params.put("c2", c2HashMap);
                    params.put("c3", c3HashMap);
                    params.put("c4", c4HashMap);
                    params.put("Timestamp", timestamp);

                    return params;
                }
            };

        }
        else{
            Log.d("FEARGS CHECK", "There was nothing passed from process to submit");
            textView.setText("No data submitted to the server.");
        }

        //create the volley request queue
        requestQueue = Volley.newRequestQueue(AnalysisSubmissionActivity.this);
        requestQueue.add(stringRequest);

        if(!success){
            textView.setText("Something went wrong with the submission. Please try again later.");
        }
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                startActivity(intent);
            }
        });
    }
}
