package com.example.woundmontoringapplicationv1.activities.MainActivities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.woundmontoringapplicationv1.R;
import com.example.woundmontoringapplicationv1.activities.ImageProcessingActivities.AnalysisSubmissionActivity;
import com.example.woundmontoringapplicationv1.activities.RegisterActivities.PersonalDetailsActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class PersonalDetailsDataActivity extends AppCompatActivity {

    JsonObjectRequest jsonObjectRequest;

    JSONObject jsonObject;

    RequestQueue requestQueue;

    String url = "http://foman01.lampt.eeecs.qub.ac.uk/woundmonitoring/personaldetails.php";

    String userEmail;

    FirebaseAuth firebaseAuth;

    FirebaseUser firebaseUser;

    String fName, lName, email, dateOfBirth, address1, address2, town, country, postcode, clinicianEmail;

    TextView fNamet, lNamet, emailt, dateOfBirtht, address1t, address2t, townt, countryt, postcodet, clinicianEmailt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_details_data);

        fNamet = findViewById(R.id.fname);
        lNamet = findViewById(R.id.lastname);
        emailt = findViewById(R.id.email);
        dateOfBirtht = findViewById(R.id.dob);
        address1t = findViewById(R.id.a1);
        address2t = findViewById(R.id.a2);
        townt = findViewById(R.id.town);
        countryt = findViewById(R.id.country);
        postcodet = findViewById(R.id.pc);
        clinicianEmailt = findViewById(R.id.clinician);

        //use firebase auth to setup the email variable
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        userEmail = firebaseUser.getEmail();

        //setup the jsonobject
        jsonObject = new JSONObject();
        try{
            jsonObject.put("user_email", userEmail);
        }
        catch(Exception e){
            e.printStackTrace();
        }

        Log.d("FEARGS FB EMAiL", userEmail);

        jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, jsonObject,
                new Response.Listener<JSONObject>() {
                    /**
                     *
                     * @param response
                     */
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray jsonArray = response.getJSONArray("PersonalDetails");

                            Log.d("FEARGS PD VOLLEY", "Made it past getJSONArray");

                            for (int i = 0; i < 1; i++) {

                                JSONObject jsonObject = jsonArray.getJSONObject(i);

                                fName = jsonObject.getString("FirstName");
                                lName = jsonObject.getString("LastName");
                                email = jsonObject.getString("Email");
                                dateOfBirth = jsonObject.getString("DateOfBirth");
                                address1 = jsonObject.getString("AddressLine1");
                                address2 = jsonObject.getString("AddressLine2");
                                town = jsonObject.getString("TownCity");
                                country = jsonObject.getString("Country");
                                postcode = jsonObject.getString("Postcode");
                                clinicianEmail = jsonObject.getString("ClinicianEmail");

                                setupViews(fName, lName, email, dateOfBirth, address1, address2, town, country, postcode, clinicianEmail);
                            }
                        }
                        catch(JSONException e){
                            e.printStackTrace();
                            Log.d("FEARGS JSON ARRAY ERR", "Nothing gotten from the jsonarray");
                        }
                    }
                },new Response.ErrorListener() {
            /**
             *
             * @param error
             */
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("FEARGS VOLLEY CHECK", "ERROR RESPONSE: " + error.toString());
            }
        })
        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();

                params.put("user_email", userEmail);

                return params;
            }
        };

        requestQueue = Volley.newRequestQueue(PersonalDetailsDataActivity.this);
        requestQueue.add(jsonObjectRequest);
    }

    /**
     *
     * @param f
     * @param l
     * @param e
     * @param dob
     * @param a1
     * @param a2
     * @param town
     * @param country
     * @param pc
     * @param clinician
     */
    private void setupViews(String f, String l, String e, String dob, String a1, String a2, String town, String country, String pc, String clinician){

        fNamet.setText(f);
        lNamet.setText(l);
        emailt.setText(e);
        dateOfBirtht.setText(dob);
        address1t.setText(a1);
        address2t.setText(a2);
        townt.setText(town);
        countryt.setText(country);
        postcodet.setText(pc);
        clinicianEmailt.setText(clinician);

    }
}
