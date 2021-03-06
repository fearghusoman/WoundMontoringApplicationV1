package com.example.woundmontoringapplicationv1.activities.MainActivities;

import android.content.Context;
import android.content.Intent;

import com.example.woundmontoringapplicationv1.Adapters.HomeFragmentRecyclerAdapter;
import com.example.woundmontoringapplicationv1.R;
import com.example.woundmontoringapplicationv1.SnapshotItem;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static com.example.woundmontoringapplicationv1.activities.LoginAndRegisterActivities.LoginActivity.TEXT;

/**
 * This activity displays a list of the user's analyses that they have performed.
 * It uses the HomeFragmentRecyclerAdapter to adapt the data to the view
 */
public class HistoryActivity extends AppCompatActivity {

    JsonObjectRequest jsonObjectRequest;

    JSONObject jsonObject;

    RequestQueue requestQueue;

    FirebaseAuth firebaseAuth;

    FirebaseUser firebaseUser;

    String url = "http://foman01.lampt.eeecs.qub.ac.uk/woundmonitoring/most_recent_analysis.php";

    String email;

    private RecyclerView recyclerView;
    private HomeFragmentRecyclerAdapter homeFragmentRecyclerAdapter;
    private ArrayList<SnapshotItem> snapshotItems;

    FloatingActionButton floatingActionButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        //use firebase auth to setup the email variable
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        email = firebaseUser.getEmail();

        floatingActionButton = findViewById(R.id.backToMenu);
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.HORIZONTAL));

        snapshotItems = new ArrayList<>();

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                intent.putExtra("message", "yourdatafragment");
                startActivity(intent);
            }
        });

        jsonObject = new JSONObject();
        try{
            jsonObject.put("EmailVar", email);
        }
        catch(Exception e){
            e.printStackTrace();
        }

        jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, jsonObject,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("FEARGS CHECK", response.toString());

                        try {
                            JSONArray jsonArray = response.getJSONArray("Recent_User_Snaps");

                            Log.d("FEARGS TRY", "Made it past getJSONArray");

                            for(int i = 0; i < jsonArray.length(); i++){

                                JSONObject jsonObject = jsonArray.getJSONObject(i);

                                String qrid = jsonObject.getString("QRID");
                                String qrinfo = jsonObject.getString("QRInformation");
                                String timestamp = jsonObject.getString("Timestamp");
                                String deltaEC1 = jsonObject.getString("DeltaE_C1");
                                String deltaEC2 = jsonObject.getString("DeltaE_C2");
                                String deltaEC3 = jsonObject.getString("DeltaE_C3");
                                String deltaEC4 = jsonObject.getString("DeltaE_C4");
                                String warning = jsonObject.getString("WarningLevel");

                                snapshotItems.add(new SnapshotItem(qrinfo, timestamp, deltaEC1, deltaEC2,
                                        deltaEC3, deltaEC4, warning));
                            }

                            homeFragmentRecyclerAdapter = new HomeFragmentRecyclerAdapter(getApplicationContext(), snapshotItems);
                            recyclerView.setAdapter(homeFragmentRecyclerAdapter);

                        } catch (JSONException e) {
                            Log.d("FEARG TRY ERROR", e.toString());
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("FEARGS CHECK", "ERROR RESPONSE: " + error.toString());
            }
        });

        requestQueue = Volley.newRequestQueue(getApplicationContext());
        requestQueue.add(jsonObjectRequest);
    }
}
