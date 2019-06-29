package com.example.woundmontoringapplicationv1;

import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.graphics.ColorUtils;
import android.support.v7.app.AppCompatActivity;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class AnalysisSubmissionActivity extends AppCompatActivity {

    boolean success;

    Bundle bundle;

    double deltaEC1, deltaEC2, deltaEC3, deltaEC4;

    String rgbC1, rgbC2, rgbC3, rgbC4;

    String rgbResponseFromServerC1, rgbResponseFromServerC2, rgbResponseFromServerC3, rgbResponseFromServerC4;

    StringRequest stringRequest, stringRequestOriginal;

    JsonObjectRequest jsonObjectRequest;

    JSONObject jsonObject;

    String userEmail, qrInfo, timestamp, c1HashMap, c2HashMap, c3HashMap, c4HashMap;

    String url = "http://foman01.lampt.eeecs.qub.ac.uk/woundmonitoring/insert_analysis.php";

    String urlInitial = "http://foman01.lampt.eeecs.qub.ac.uk/woundmonitoring/initial_analysis_v1.php";

    ProgressDialog progressDialog;

    TextView textView;

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

        textView = findViewById(R.id.textViewSubmitted);
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
                //jsonObject.put("user_email", userEmail);
                //jsonObject.put("qr_info", qrInfo);
                jsonObject.put("user_email", "johndoe@gmail.com");
                jsonObject.put("qr_info", "QR Code Generator 001");
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
                            Log.d("FEARGS VOLLEY", "Got a response from 1st request");
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
            /**------------------STRING REQUEST TO GET ORIGINAL DRESSING ANALYSIS----------------------**/
            /**----------------------------------------------------------------------------------------**/
            stringRequestOriginal = new StringRequest(Request.Method.POST, urlInitial, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {

                    Log.d("FEARGS VOLLEY", "Got a response from 1st request");

                    /**
                    if(response.equalsIgnoreCase("No analysis done")){
                        Log.d("FEARGS VOLLEY", "This is the first image of this dressing");

                        deltaEC1 = 0;
                        deltaEC2 = 0;
                        deltaEC3 = 0;
                        deltaEC4 = 0;
                    }
                    else{
                        Log.d("FEARGS VOLLEY", "This is not the first image of this dressing");

                        //this will need to be changed to interpret a json array return
                        rgbResponseFromServer = response;

                        deltaEC1 = calculateDeltaE(rgbResponseFromServer, rgbC1);
                        deltaEC2 = calculateDeltaE(rgbResponseFromServer, rgbC2);
                        deltaEC3 = calculateDeltaE(rgbResponseFromServer, rgbC3);
                        deltaEC4 = calculateDeltaE(rgbResponseFromServer, rgbC4);
                    }
                     **/

                    //when response received then setup the variables for the next request
                    setupVariables();

                    //now that response has been received and variables have been setup we can add the next request to the queue
                    //requestQueue.add(stringRequest);

                    progressDialog.dismiss();
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                    Log.d("FEARGS VOLLEY", "Got an error response from 1st request");
                    Log.d("FEARGS VOLLEY", "Error: " + error.toString());
                    Toast.makeText(AnalysisSubmissionActivity.this, "ERROR PAL" + error.toString() + "ERROR PAL", Toast.LENGTH_LONG).show();

                    progressDialog.dismiss();

                }
            })

            {
                /**
                 *
                 * @return
                 * @throws AuthFailureError
                 */
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params = new HashMap<String, String>();

                    params.put("user_email", userEmail);
                    params.put("qr_info", qrInfo);

                    return params;                }
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

        // Add the realibility on the connection.
        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(10000, -1, 1.0f));

        //requestQueue.add(stringRequestOriginal);
        requestQueue.add(jsonObjectRequest);
        //requestQueue.add(stringRequest);
        //requestQueue.start();

        Log.d("FEARGS CHECK", "Request Queue Sequence Number: " + requestQueue.getSequenceNumber());
        Log.d("FEARGS CHECK", "Request Queue toString: " + requestQueue.toString());

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
     *
     */
    private void setupVariables(){

        c1HashMap = bundle.getString("Circle1");
        c2HashMap = bundle.getString("Circle2");
        c3HashMap = bundle.getString("Circle3");
        c4HashMap = bundle.getString("Circle4");

        rgbC1 = bundle.getString("rgbC1");
        rgbC2 = bundle.getString("rgbC2");
        rgbC3 = bundle.getString("rgbC3");
        rgbC4 = bundle.getString("rgbC4");

        timestamp = bundle.getString("Timestamp");
    }

    /**
     *
     * @param rgbString
     */
    private double calculateDeltaE(String rgbString, String colorCurrent){

        int[] rgbs = convertStringRGBtoInts(rgbString);
        int[] rgbsCurrent = convertStringRGBtoInts(colorCurrent);

        /**-------------------------------------------------------------------------------------------------------**/
        /**----------------------BEGIN THE CONVERSION TO CIELAB AND DELTA E CALCULATION---------------------------**/
        /**-------------------------------------------------------------------------------------------------------**/
        return euclideanDistanceBetweenLABs(rgbs, rgbsCurrent);

    }

    /**
     *
     * @param rgbString
     * @return
     */
    private int[] convertStringRGBtoInts(String rgbString){
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
     * Response 1 from:
     * https://stackoverflow.com/questions/35841118/how-to-get-error-message-description-using-volley?rq=1
     *
     * To get error response being returned from volley request
     * @param error
     */
    public void parseVolleyError(VolleyError error) {
        try {
            String responseBody = new String(error.networkResponse.data, "utf-8");
            JSONObject data = new JSONObject(responseBody);
            JSONArray errors = data.getJSONArray("errors");
            JSONObject jsonMessage = errors.getJSONObject(0);
            String message = jsonMessage.getString("message");
            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();

        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        catch (UnsupportedEncodingException errorr) {
            errorr.printStackTrace();
        }
    }
}
