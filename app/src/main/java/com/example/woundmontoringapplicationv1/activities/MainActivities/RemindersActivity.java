package com.example.woundmontoringapplicationv1.activities.MainActivities;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.woundmontoringapplicationv1.Adapters.RemindersRecyclerAdapter;
import com.example.woundmontoringapplicationv1.AlertReceiver;
import com.example.woundmontoringapplicationv1.DressingReminderItem;
import com.example.woundmontoringapplicationv1.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * This activity uses a Volley request to check with the MySQL database the current warning level
 * of a wound, and whether or not the alarm needs to be updated.
 * It does so by calling the RemindersRecyclerAdapter for each returned dressing.
 */
public class RemindersActivity extends AppCompatActivity{

    private RecyclerView recyclerView;
    private RemindersRecyclerAdapter remindersRecyclerAdapter;
    private ArrayList<DressingReminderItem> dressingItems;

    FloatingActionButton floatingActionButtonBack;

    FirebaseAuth firebaseAuth;

    FirebaseUser firebaseUser;

    String email;

    JSONObject jsonObject;

    JsonObjectRequest jsonObjectRequest;

    RequestQueue requestQueue;

    SharedPreferences sharedPreferences;

    String url = "http://foman01.lampt.eeecs.qub.ac.uk/woundmonitoring/registered_dressing.php";

    /**
     * onCreate method is called when the activity is first created within
     * the application
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminders);

        //use firebase auth to setup the email variable
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        email = firebaseUser.getEmail();

        dressingItems = new ArrayList<>();
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.HORIZONTAL));

        sharedPreferences = getApplicationContext().getSharedPreferences("APPLICATION_PREFS", Context.MODE_PRIVATE);
        String testSharedPrefs = sharedPreferences.getString("QRID0", "Default initiated");

        Log.d("FEARGS SHARED", testSharedPrefs);

        floatingActionButtonBack = findViewById(R.id.backToMenu);

        floatingActionButtonBack.setOnClickListener(new View.OnClickListener() {
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
                            JSONArray jsonArray = response.getJSONArray("Users_Registered_Dressings");

                            Log.d("FEARGS TRY", "Made it past getJSONArray");

                            for(int i = 0; i < jsonArray.length(); i++){

                                JSONObject jsonObject = jsonArray.getJSONObject(i);

                                String qrid = jsonObject.getString("QRID");
                                String qrinfo = jsonObject.getString("QRInformation");
                                String location = jsonObject.getString("WoundLocation");
                                String timestamp = jsonObject.getString("Timestamp");
                                String currentWarningLevel = jsonObject.getString("CurrentWarningLevel");
                                int alarmNeedsUpdating = jsonObject.getInt("AlarmNeedsUpdating");

                                Log.d("FEARG FORLOOP", i + ": " + qrid + ", " + qrinfo + " " + location + " " + timestamp);

                                dressingItems.add(new DressingReminderItem(qrid, qrinfo, location, timestamp, currentWarningLevel, alarmNeedsUpdating));
                            }

                            remindersRecyclerAdapter = new RemindersRecyclerAdapter(getApplicationContext(), dressingItems);
                            recyclerView.setAdapter(remindersRecyclerAdapter);

                            //dressingItems array has now been fully populated with the response objects
                            //we will now call the methods for adding alerts to the dressings

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
