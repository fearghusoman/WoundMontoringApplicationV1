package com.example.woundmontoringapplicationv1.activities.ImageProcessingActivities;

import android.app.ProgressDialog;
import android.content.Intent;

import com.example.woundmontoringapplicationv1.CIELab;
import com.example.woundmontoringapplicationv1.R;
import com.example.woundmontoringapplicationv1.activities.MainActivities.HomeActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.core.graphics.ColorUtils;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 * This activity receives the image analysis data from ProcessImageActivity
 * It sends a request to the server, where it gets the RGB analysis from the
 * the dressing's initial analysis. Using this it calculates the delta e value
 * for the current analysis.
 * The Delta E values are used to give the user feedback on the state of their wound.
 */
public class AnalysisSubmissionActivity extends AppCompatActivity {

    //static variable for the delta e warning levels
    public final static int DELTAE_THRESHOLD_GREEN = 10;
    public final static int DELTAE_THRESHOLD_AMBER = 30;

    boolean success;

    Bundle bundle;

    //delta e values for each circle are stored as doubles
    double deltaEC1, deltaEC2, deltaEC3, deltaEC4;

    String rgbC1, rgbC2, rgbC3, rgbC4;

    //the rgb values for the initial analysis are received as strings from the server
    String rgbResponseFromServerC1, rgbResponseFromServerC2, rgbResponseFromServerC3, rgbResponseFromServerC4;

    StringRequest stringRequest;

    JsonObjectRequest jsonObjectRequest;

    JSONObject jsonObject;

    String userEmail, qrInfo, timestamp;

    //url for the volley request that inserts the new analysis to the server
    String url = "http://foman01.lampt.eeecs.qub.ac.uk/woundmonitoring/insert_analysis.php";

    //url for the first volley request
    String urlInitial = "http://foman01.lampt.eeecs.qub.ac.uk/woundmonitoring/initial_analysis_v1.php";

    ProgressDialog progressDialog;

    TextView textView, textViewCO2, textViewO2, textViewH20, textViewRNH2, textViewDeltaE1, textViewDeltaE2, textViewDeltaE3, textViewDeltaE4;

    FloatingActionButton floatingActionButton;

    //Create a Volley (REST) RequestQueue
    RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analysis_submission);

        //show the progress dialog  - will get closed when the analysis is finished
        progressDialog = new ProgressDialog(AnalysisSubmissionActivity.this);
        progressDialog.setMessage("Please wait...");
        progressDialog.show();

        //setup all the activity's view
        textView = findViewById(R.id.textViewSubmitted);
        textViewCO2 = findViewById(R.id.textViewFeedback1);
        textViewO2 = findViewById(R.id.textViewFeedback2);
        textViewH20 = findViewById(R.id.textViewFeedback3);
        textViewRNH2 = findViewById(R.id.textViewFeedback4);
        textViewDeltaE1 = findViewById(R.id.textViewDeltaE1);
        textViewDeltaE2 = findViewById(R.id.textViewDeltaE2);
        textViewDeltaE3 = findViewById(R.id.textViewDeltaE3);
        textViewDeltaE4 = findViewById(R.id.textViewDeltaE4);
        floatingActionButton = findViewById(R.id.buttonHome);

        bundle = getIntent().getExtras();

        //if data has in fact been passed from the process image activity
        if(bundle != null){

            //get the four rgb strings passed from the process image activity
            rgbC1 = bundle.getString("rgbC1");
            rgbC2 = bundle.getString("rgbC2");
            rgbC3 = bundle.getString("rgbC3");
            rgbC4 = bundle.getString("rgbC4");

            //also get the user email and qr info
            userEmail = bundle.getString("UserEmail");
            qrInfo = bundle.getString("QRInfo");

            //create the json object
            jsonObject = new JSONObject();
            try{
                jsonObject.put("user_email", userEmail);
                jsonObject.put("qr_info", qrInfo);
            }
            catch(Exception e){
                e.printStackTrace();
            }

            Log.d("FEARGS BUNDLE", "Bundle received; rgbs for circles: " + rgbC1 + " " + rgbC2 + " " + rgbC3 + " " + rgbC4);
            Log.d("FEARGS JSONOBJECT",  qrInfo + " " + userEmail);

            try {
                Log.d("FEARGS JSONOBJECT", jsonObject.getString("qr_info") + " " + jsonObject.getString("user_email"));
            } catch (JSONException e) {
                e.printStackTrace();
            }


            /**--------------------------------------------------------------------------------------------**/
            /**------------------JSONOBJECT REQUEST TO GET ORIGINAL DRESSING ANALYSIS----------------------**/
            /**--------------------------------------------------------------------------------------------**/
            jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, urlInitial, jsonObject,
                    new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject response) {
                            Log.d("FEARGS VOLLEY",  "Got a response from 1st request");
                            Log.d("FEARGS VOLLEY CHECK", response.toString());

                            try {
                                JSONArray jsonArray = response.getJSONArray("InitialRGBAnalysis");

                                Log.d("FEARGS VOLLEY TRY", "Made it past getJSONArray");

                                for(int i = 0; i < jsonArray.length(); i++) {

                                    JSONObject jsonObject = jsonArray.getJSONObject(i);

                                    rgbResponseFromServerC1 = jsonObject.getString("C1_Analysis_RGB");
                                    rgbResponseFromServerC2 = jsonObject.getString("C2_Analysis_RGB");
                                    rgbResponseFromServerC3 = jsonObject.getString("C3_Analysis_RGB");
                                    rgbResponseFromServerC4 = jsonObject.getString("C4_Analysis_RGB");

                                    deltaEC1 = calculateDeltaE(rgbResponseFromServerC1, rgbC1);
                                    deltaEC2 = calculateDeltaE(rgbResponseFromServerC2, rgbC2);
                                    deltaEC3 = calculateDeltaE(rgbResponseFromServerC3, rgbC3);
                                    deltaEC4 = calculateDeltaE(rgbResponseFromServerC4, rgbC4);

                                }

                                setupVariables();

                                //now add the next request to the queue
                                requestQueue.add(stringRequest);

                                progressDialog.dismiss();

                            }
                            catch (JSONException e) {
                                Log.d("FEARGS VOLLEY TRY ERROR", e.toString());

                                progressDialog.dismiss();

                            }
                        }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.d("FEARGS VOLLEY CHECK", "ERROR RESPONSE: " + error.toString());

                                progressDialog.dismiss();

                            }
                        }){
                @Override
                protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {

                    if(response.data == null || response.data.length == 0){
                        return Response.success(new JSONObject(), HttpHeaderParser.parseCacheHeaders(response));

                    }
                    else{
                        return super.parseNetworkResponse(response);
                    }
                }
            };

            /**----------------------------------------------------------------------------------------**/
            /**----------------------STRING REQUEST TO SUBMIT IMAGE ANALYSIS---------------------------**/
            /**----------------------------------------------------------------------------------------**/
            stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.d("FEARGS VOLLEY", "Got a response from 2nd request: " + response);
                    Log.d("FEARGS VOLLEY", response.substring(0, 10));

                    if(response.substring(0, 10).equalsIgnoreCase("Successful")){
                        success = true;
                    }
                    else{
                        success = false;
                    }

                    //set the text views of the activity to give the user instantaneous feedback
                    getFeedback(deltaEC1, textViewCO2, textViewDeltaE1);
                    getFeedback(deltaEC2, textViewO2, textViewDeltaE2);
                    getFeedback(deltaEC3, textViewH20, textViewDeltaE3);
                    getFeedback(deltaEC4, textViewRNH2, textViewDeltaE4);

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
                    params.put("Timestamp", timestamp);

                    params.put("rgbC1", rgbC1);
                    params.put("rgbC2", rgbC2);
                    params.put("rgbC3", rgbC3);
                    params.put("rgbC4", rgbC4);

                    params.put("DeltaEC1", "" + deltaEC1);
                    params.put("DeltaEC2", "" + deltaEC2);
                    params.put("DeltaEC3", "" + deltaEC3);
                    params.put("DeltaEC4", "" + deltaEC4);

                    return params;
                }
            };

        }
        else{
            Log.d("FEARGS CHECK", "There was nothing passed from process to submit");
            textView.setText("No data submitted to the server.");
        }


        //create the volley request queue and add only the first request - the other is added onResponse
        requestQueue = Volley.newRequestQueue(AnalysisSubmissionActivity.this);
        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(10000, -1, 1.0f));
        requestQueue.add(jsonObjectRequest);

        //click the floating action button to return to the home screen
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                startActivity(intent);
            }
        });
    }

    /**
     * Get the RGB values from the bundle received from the processImageActivity
     * passed as a string
     */
    private void setupVariables(){

        rgbC1 = bundle.getString("rgbC1");
        rgbC2 = bundle.getString("rgbC2");
        rgbC3 = bundle.getString("rgbC3");
        rgbC4 = bundle.getString("rgbC4");

        timestamp = bundle.getString("Timestamp");
    }

    /**
     * Method takes the original colour and the current analysis colour
     * as RGB Strings and converts them to lab space
     * before performing the delta e calculation
     * @param rgbString
     */
    private double calculateDeltaE(String rgbString, String colorCurrent){
        double deltaE;

        //convert the strings to int arrrays
        int[] rgbs = convertStringRGBtoInts(rgbString);
        int[] rgbsCurrent = convertStringRGBtoInts(colorCurrent);

        //call the euclidean distance method
        deltaE = euclideanDistanceBetweenLABs(rgbs, rgbsCurrent);
        return deltaE;
    }

    /**
     * Takes the RGB values in the String format in which the server stores them
     * and then converts the string to an integer array
     * @param rgbString
     * @return
     */
    public int[] convertStringRGBtoInts(String rgbString){
        String withoutBraces = rgbString.replace("(", "");
        withoutBraces = withoutBraces.replace(")", "");

        String[] rgbInts = withoutBraces.split(", ");

        int[] rgbs = new int[3];

        for(int i = 0; i < 3; i++){
            rgbs[i] = Integer.parseInt(rgbInts[i]);
        }

        return rgbs;
    }

    /**
     * Create an instance of the class CIELab
     * then use the ColorUtils class' method called distanceEuclidean; this calculates the
     * delta E difference between two LAB colors
     * @param colorInitial
     * @param colorCurrent
     * @return
     */
    private double euclideanDistanceBetweenLABs(int[] colorInitial, int[] colorCurrent){
        CIELab cieLab = new CIELab();

        int r = colorInitial[0];
        int g = colorInitial[1];
        int b = colorInitial[2];

        double[] colorInitialLAB = cieLab.rgbToLab(r, g, b);

        int rX = colorCurrent[0];
        int gX = colorCurrent[1];
        int bX = colorCurrent[2];

        double[] colorCurrentLAB = cieLab.rgbToLab(rX,gX, bX);

        return ColorUtils.distanceEuclidean(colorInitialLAB, colorCurrentLAB);
    }

    /**
     * Sets up the text views, to show the user feedback on the delta e values
     * received from the analysis
     * @param deltaE
     * @param textView
     */
    private void getFeedback(double deltaE, TextView textView, TextView textViewD){
        if(deltaE >= DELTAE_THRESHOLD_AMBER){
            textView.setText("RED ALERT");
            textView.setTextColor(Color.RED);
        }
        else if(deltaE >= DELTAE_THRESHOLD_GREEN){
            textView.setText("AMBER ALERT");
            textView.setTextColor(Color.YELLOW);
        }
        else{
            textView.setTextColor(Color.GREEN);
        }

        textViewD.setText("" + deltaE);
    }
}
