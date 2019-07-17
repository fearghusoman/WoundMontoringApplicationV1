package com.example.woundmontoringapplicationv1.activities.HomeFragments;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.woundmontoringapplicationv1.Adapters.HomeFragmentRecyclerAdapter;
import com.example.woundmontoringapplicationv1.R;
import com.example.woundmontoringapplicationv1.SnapshotItem;
import com.example.woundmontoringapplicationv1.activities.MainActivities.HomeActivity;
import com.example.woundmontoringapplicationv1.activities.MainActivities.RegisterDressingActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Collections;

public class HomeFragment extends Fragment {

    public final static int MY_PERMISSION_REQUEST_CAMERA = 1;

    TextView textView1;

    JsonObjectRequest jsonObjectRequest;

    FirebaseAuth firebaseAuth;

    FirebaseUser firebaseUser;

    Context context;

    SharedPreferences sharedPreferences;

    SharedPreferences.Editor editor;

    JSONObject jsonObject;

    RequestQueue requestQueue;

    String url = "http://foman01.lampt.eeecs.qub.ac.uk/woundmonitoring/most_recent_analysis.php";

    String email;

    private RecyclerView recyclerView;
    private HomeFragmentRecyclerAdapter homeFragmentRecyclerAdapter;
    private ArrayList<SnapshotItem> snapshotItems;

    /**
     *
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        //set-up the variables for the shared preferences
        context = getActivity();
        sharedPreferences = context.getSharedPreferences("APPLICATION_PREFS", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        //use firebase auth to setup the email variable
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser != null) {
            email = firebaseUser.getEmail();
        }

        //set-up the recycler view settings
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.HORIZONTAL));

        //instantiate the recycler item arraylist
        snapshotItems = new ArrayList<>();

        textView1 = view.findViewById(R.id.register_qr);
        textView1.setOnClickListener(new View.OnClickListener() {
            /**
             *
             * @param v
             */
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), RegisterDressingActivity.class);
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
                            Log.d("FEARGS JSON", "JSON: " + jsonObject.getString("EmailVar"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        try {
                            JSONArray jsonArray = response.getJSONArray("Recent_User_Snaps");

                            Log.d("FEARGS TRY", "Made it past getJSONArray");

                            ArrayList<String> strings = new ArrayList<>();
                            int x = 0;

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

                                //also add the id to shared preferences
                                if(strings.contains(qrid)){
                                    //then do nothing
                                }
                                else{
                                    strings.add(qrid);
                                    editor.putString("QRID" + x, qrid);
                                    editor.putInt("X", x);
                                    editor.apply();
                                    x++;
                                }

                            }

                            //order the snapshotItems array list
                            Collections.sort(snapshotItems);

                            ArrayList<SnapshotItem> snapshotItemsNew = new ArrayList<SnapshotItem>();

                            for(int k = 0; k < 5; k++){
                                try {
                                    snapshotItemsNew.add(snapshotItems.get(k));
                                }
                                catch (IndexOutOfBoundsException e){
                                    break;
                                }
                            }

                            homeFragmentRecyclerAdapter = new HomeFragmentRecyclerAdapter(getActivity(), snapshotItemsNew);
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

        requestQueue = Volley.newRequestQueue(getActivity());
        requestQueue.add(jsonObjectRequest);

        //check for camera permission
        if(checkPermission()){
            //continue
            Log.d("FEARGS PERMISSION", "Camera is already allowed");
        }
        else{
            requestPermission();
        }

        return view;
    }

    /**
     * checks whether the camera has been granted permission
     * @return
     */
    private boolean checkPermission(){
        if(ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            return false;
        }
        else{
            return true;
        }
    }

    /**
     * If camera access has not been granted; this method is called to request
     */
    private void requestPermission(){
        ActivityCompat.requestPermissions(getActivity(),
                new String[]{Manifest.permission.CAMERA}, MY_PERMISSION_REQUEST_CAMERA);
        Log.d("FEARGS PERMISSION", "Requesting Camera now");
    }

    /**
     * Checks the request permission result and either continues or re-requests it
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch(requestCode){
            case MY_PERMISSION_REQUEST_CAMERA:

                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(getContext(), "Permission Granted", Toast.LENGTH_SHORT).show();
                }

                else{
                    Toast.makeText(getContext(), "Permission Denied", Toast.LENGTH_SHORT).show();

                }
        }
    }


}
