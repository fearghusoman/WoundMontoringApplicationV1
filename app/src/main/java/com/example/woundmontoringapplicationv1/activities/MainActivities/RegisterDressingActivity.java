package com.example.woundmontoringapplicationv1.activities.MainActivities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
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
import com.example.woundmontoringapplicationv1.activities.ImageProcessingActivities.CaptureImageActivity;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

import static com.example.woundmontoringapplicationv1.activities.LoginAndRegisterActivities.LoginActivity.SHARED_PREFS;

/**
 * This activity involves a user registering their dressing.
 * It takes the image from the CaptureImage activity and allows the user to select
 * the body part to assign it to.
 */
public class RegisterDressingActivity extends AppCompatActivity {

    public static final int REQUEST_CODE_CAPTURED_IMAGE = 001;
    static final String registerurl = "http://foman01.lampt.eeecs.qub.ac.uk/woundmonitoring/register_dressing.php";

    Bundle bundle;
    Bitmap bitmap;
    ImageView imageView;
    TextView textView1, textView2;
    BarcodeDetector barcodeDetector;
    Barcode dressingBarcode;
    Frame frame;
    SparseArray<Barcode> barcodes;
    Spinner spinner;
    ArrayAdapter<CharSequence> adapter;
    Button btn;
    ProgressDialog progressDialog;
    String qrInfoHolder, locationHolder, loggedInEmail;
    FloatingActionButton floatingActionButton;
    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;

    /**
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_dressing);

        //use firebase auth to setup the email variable
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        loggedInEmail = firebaseUser.getEmail();

        imageView = findViewById(R.id.dressing_image);
        textView1 = findViewById(R.id.textView);
        textView2 = findViewById(R.id.textViewSpinner);
        spinner = findViewById(R.id.myspinner);
        btn = findViewById(R.id.button_register_dressing);
        floatingActionButton = findViewById(R.id.backToTakeImage);

        progressDialog = new ProgressDialog(RegisterDressingActivity.this);

        Intent intent = new Intent(getApplicationContext(), CaptureImageActivity.class);
        intent.putExtra("CALLING_ACTIVITY", "RegisterDressing");
        startActivityForResult(intent, REQUEST_CODE_CAPTURED_IMAGE);

    }

    /**
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), CaptureImageActivity.class);
                intent.putExtra("CALLING_ACTIVITY", "RegisterDressing");
                startActivityForResult(intent, REQUEST_CODE_CAPTURED_IMAGE);
            }
        });

        Log.d("FEARGS CHECK", "A result has been returned from he Capture Activity");

        if(requestCode == REQUEST_CODE_CAPTURED_IMAGE){
            if(resultCode == RESULT_OK){
                Log.d("FEARGS CHECK", "The right result code was received from the capture activity.");

                //now we can start processing the qr code and add it to our database
                //we want to: (1) display the image (2) display the data (3) allow user to specify location
                bundle = data.getExtras();

                if(bundle != null){
                    String path = bundle.get("imageName").toString();

                    //try to create input steam; file may not exist, which will  throw an exception
                    try {
                        FileInputStream fIS = new FileInputStream(new File(path));
                        bitmap = BitmapFactory.decodeStream(fIS);
                        barcodeDetector = new BarcodeDetector.Builder(getApplicationContext()).setBarcodeFormats(Barcode.QR_CODE).build();

                        if (!barcodeDetector.isOperational()) {
                            textView1.setText("Couldn't setup the detector1");
                            return;
                        }
                        else {
                            frame = new Frame.Builder().setBitmap(bitmap).build();
                            barcodes = barcodeDetector.detect(frame);

                            if (barcodes.size() > 0) {
                                dressingBarcode = barcodes.valueAt(0);
                                textView1.setText(dressingBarcode.rawValue);
                                imageView.setImageBitmap(bitmap);

                                spinner.setVisibility(View.VISIBLE);
                                btn.setVisibility(View.VISIBLE);
                                textView2.setVisibility(View.VISIBLE);

                                adapter = ArrayAdapter.createFromResource(this, R.array.wound_locations, R.layout.support_simple_spinner_dropdown_item);
                                adapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);

                                spinner.setAdapter(adapter);

                                btn.setOnClickListener(new View.OnClickListener() {
                                    /**
                                     *
                                     * @param v
                                     */
                                    @Override
                                    public void onClick(View v) {
                                        Toast.makeText(RegisterDressingActivity.this,"Registering dressing...", Toast.LENGTH_SHORT);
                                        qrInfoHolder = textView1.getText().toString().trim();
                                        locationHolder = spinner.getSelectedItem().toString();
                                        RegisterDressing();
                                    }
                                });

                            }
                            else{
                                textView1.setText("Sorry, but there was no QR code present in the image.");
                                spinner.setVisibility(View.INVISIBLE);
                                btn.setVisibility(View.INVISIBLE);
                                textView2.setVisibility(View.INVISIBLE);
                            }
                        }

                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }

                }
                else {
                    Log.d("FEARGS CHECK", "An empty bundle was received in the response.");
                }

            }
        }
        else{
            Log.d("FEARGS CHECK", "A bad result was received from the capture activity.");
        }
    }

    /**
     * Method sets up the volley stringrequest; passes the user's information to the server
     * and adds it to database. if succesful a message will be presented and user will
     * be redirected to the home page
     */
    private void RegisterDressing(){
        progressDialog.setMessage("Please wait...");
        progressDialog.show();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, registerurl,

                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        progressDialog.dismiss();

                        if(response.equalsIgnoreCase("Successful")){
                            Log.d("FEARGS CHECK","The server returned successful");
                            Log.d("FEARGS CHECK", response);
                            Toast.makeText(RegisterDressingActivity.this, "You have succesfully registered your dressing", Toast.LENGTH_LONG).show();

                            Intent intentHome = new Intent(getApplicationContext(), HomeActivity.class);
                            startActivity(intentHome);
                        }
                        else if(response.equalsIgnoreCase("QR Code already registered")){
                            Log.d("FEARGS CHECK","The server says the qr codes already exists");
                            Toast.makeText(RegisterDressingActivity.this, "This dressing has already been registered!", Toast.LENGTH_LONG).show();
                        }
                        else{
                            Toast.makeText(RegisterDressingActivity.this, "Something went wrong...", Toast.LENGTH_LONG).show();
                            Log.d("FEARGS CHECK","The server didn't return successful");
                            Log.d("FEARGS CHECK", response);
                        }
                    }
                }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            progressDialog.dismiss();
                            Toast.makeText(RegisterDressingActivity.this, "Response Error: " + error.toString(), Toast.LENGTH_LONG).show();
                        }
                    }
                    ) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                Map<String, String> params = new HashMap<String, String>();

                //adding all the values from the response and input to the map
                //keys are fields from database
                params.put("qr_info", qrInfoHolder);
                params.put("location", locationHolder);
                params.put("user_email", loggedInEmail);

                return params;
            }
        };

        //set a retry policay in case the server is taking a while to respond
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

        RequestQueue requestQueue = Volley.newRequestQueue(RegisterDressingActivity.this);
        requestQueue.add(stringRequest);
    }

}
