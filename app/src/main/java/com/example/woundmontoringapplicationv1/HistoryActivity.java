package com.example.woundmontoringapplicationv1;

import android.content.Intent;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.app.AppCompatActivity;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class HistoryActivity extends AppCompatActivity {

    JsonObjectRequest jsonObjectRequest;

    JSONObject jsonObject;

    RequestQueue requestQueue;

    String url = "http://foman01.lampt.eeecs.qub.ac.uk/woundmonitoring/most_recent_analysis.php";

    String email = "johndoe@gmail.com";

    private RecyclerView recyclerView;
    private HomeFragmentRecyclerAdapter homeFragmentRecyclerAdapter;
    private ArrayList<SnapshotItem> snapshotItems;

    FloatingActionButton floatingActionButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

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
                                String timestamp = jsonObject.getString("Timestamp");

                                Log.d("FEARG FORLOOP", i + ": " + qrid + ", " + timestamp);

                                snapshotItems.add(new SnapshotItem(qrid, timestamp));
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
