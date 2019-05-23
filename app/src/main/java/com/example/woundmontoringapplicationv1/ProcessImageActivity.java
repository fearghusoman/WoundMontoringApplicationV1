package com.example.woundmontoringapplicationv1;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.graphics.Palette;
import android.util.Log;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TableLayout;
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
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.woundmontoringapplicationv1.LoginActivity.SHARED_PREFS;

/**
 *
 */
public class ProcessImageActivity extends AppCompatActivity {

    //store our known real-life measurements for the distance between points on a dressing
    //distance between the top two corners of the qr code
    final static double L1 = 7.1;
    //distance between the top right corner of qr code and top left corner of the area to be
    //analysed for colour
    final static double L2 = 2.1;
    //distance between the top two points of the rectangle
    final static double L3 = 15.5;

    ProgressDialog progressDialog;
    static final String checkurl = "http://foman01.lampt.eeecs.qub.ac.uk/woundmonitoring/process_image.php";
    boolean continueWithProcessing = false;

    Button processImgBtn;
    FloatingActionButton floatingActionButton;
    ImageView imageView, imageView2;

    Bitmap bitmap, bitmap2, bitmapForPixelColor;
    BarcodeDetector barcodeDetector;

    View view1, view2, view3, view4, view5, view7, view10, view8, view9, view11, view12;
    View view7QR, view10QR, view8QR, view9QR, view11QR, view12QR;

    TextView textView, textView2, textView3, textView4, textView5, textView6, textView7, textView8, textView9, textView10, textView11, textView12;
    TextView textView7QR, textView8QR, textView9QR, textView10QR, textView11QR, textView12QR;
    TableLayout tableLayout;

    Frame frame;
    SparseArray<Barcode> barcodes;
    Barcode thisBarCode;
    byte[] bytes;
    String fileName;
    Bundle bundle;

    Point[] qrCornerPoints;

    Palette palette;
    int dominantColour, a, r, g, b, r1, g1, b1, a1;

    //variables to hold the calculated four corners of the rectangle to be analysed
    int x1, y1, x2, y2, x3, y3, x4, y4;
    int l1, l2, l3;
    double q;
    Point A1, A2, A3, A4;

    String LoggedInEmail, qrInfoHolder;
    RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_process_image);

        processImgBtn = findViewById(R.id.button);
        floatingActionButton = findViewById(R.id.backToTakeImage);

        tableLayout = findViewById(R.id.main_table);

        textView = findViewById(R.id.txtContent) ;
        textView2 = findViewById(R.id.txtContent2) ;
        textView3 = findViewById(R.id.txtContent3) ;
        textView4 = findViewById(R.id.txtContent4) ;
        textView5 = findViewById(R.id.txtContent5) ;
        textView6 = findViewById(R.id.txtContent6) ;
        textView7 = findViewById(R.id.txtContent7) ;
        textView8 = findViewById(R.id.txtContent8) ;
        textView9 = findViewById(R.id.txtContent9) ;
        textView10 = findViewById(R.id.txtContent10) ;
        textView11 = findViewById(R.id.txtContent11) ;
        textView12 = findViewById(R.id.txtContent12) ;

        textView7QR = findViewById(R.id.txtContent7QR) ;
        textView8QR = findViewById(R.id.txtContent8QR) ;
        textView9QR = findViewById(R.id.txtContent9QR) ;
        textView10QR = findViewById(R.id.txtContent10QR) ;
        textView11QR = findViewById(R.id.txtContent11QR) ;
        textView12QR = findViewById(R.id.txtContent12QR) ;

        view1 =  findViewById(R.id.view1);
        view2 =  findViewById(R.id.view2);
        view3 =  findViewById(R.id.view3);
        view4 =  findViewById(R.id.view4);
        view5 =  findViewById(R.id.view5);

        view7 =  findViewById(R.id.view7);
        view8 =  findViewById(R.id.view8);
        view9 =  findViewById(R.id.view9);
        view10 =  findViewById(R.id.view10);
        view11 =  findViewById(R.id.view11);
        view12 =  findViewById(R.id.view12);

        view7QR =  findViewById(R.id.view7QR);
        view8QR =  findViewById(R.id.view8QR);
        view9QR =  findViewById(R.id.view9QR);
        view10QR =  findViewById(R.id.view10QR);
        view11QR =  findViewById(R.id.view11QR);
        view12QR =  findViewById(R.id.view12QR);

        imageView2 = findViewById(R.id.imgview2);
        imageView = findViewById(R.id.imgview);

        requestQueue = Volley.newRequestQueue(ProcessImageActivity.this);
        progressDialog = new ProgressDialog(ProcessImageActivity.this);

        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        //LoggedInEmail = sharedPreferences.getString("email", "");
        LoggedInEmail = "johndoe@gmail.com";

        bundle = getIntent().getExtras();

        if (bundle != null) {
            String path = bundle.get("imageName").toString();

            try {
                FileInputStream fIS = new FileInputStream(new File(path));

                bitmap2 = BitmapFactory.decodeStream(fIS);
                imageView.setImageBitmap(bitmap2);
            }
            catch(FileNotFoundException e){
                e.printStackTrace();
            }
        }
        else{
            textView.setText("There was nothing passed from the camera. Please try again..");
        }

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), CaptureImageActivity.class);
                intent.putExtra("CALLING_ACTIVITY", "ProcessNewImage");
                startActivity(intent);
            }
        });

        processImgBtn.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public void onClick(View v) {

                barcodeDetector = new BarcodeDetector.Builder(getApplicationContext()).setBarcodeFormats(Barcode.QR_CODE).build();

                if (!barcodeDetector.isOperational()) {
                    textView.setText("Couldn't setup the detector1");
                    return;
                } else {
                    frame = new Frame.Builder().setBitmap(bitmap2).build();

                    barcodes = barcodeDetector.detect(frame);

                    if (barcodes.size() > 0) {
                        thisBarCode = barcodes.valueAt(0);
                        qrInfoHolder = thisBarCode.rawValue;

                        //check the qr is registered - if it is then the below method will call the analysis method
                        checkQRRegisteredWithCurrentUser();
                    }
                    else {
                        textView.setText("There was no QR code detected in the image. Please try again..");
                    }
                }
            }


        });
    }

    /**
     * return an array of coordinates for the four corners of the qr code identified in the image
     * the first point in the upper-left corner and then moves clockwise
     * @param barcodeDetector
     * @param frame
     * @return
     */
    private Point[] getQRCoordinates(BarcodeDetector barcodeDetector, Frame frame){
        SparseArray<Barcode> barcodeSparseArray;

        barcodeSparseArray = barcodeDetector.detect(frame);
        Barcode b = barcodeSparseArray.valueAt(0);
        Point[] qrCornerPoints = b.cornerPoints;

        return qrCornerPoints;
    }

    /**
     *
     * @param qrCornerPoints
     */
    private void calculateOurRectangle(Point[] qrCornerPoints){
        //let's store the four corners of our qr code in variables in order to do some analysis
        Point q1 = qrCornerPoints[0];
        Point q2 = qrCornerPoints[1];
        Point q3 = qrCornerPoints[2];
        Point q4 = qrCornerPoints[3];

        //distance between a1 and b1;
        l1 = q2.x - q1.x;

        //proportional value
        q = l1 / L1;

        //distance between qr code and desired rectangle
        l2 = (int) (L2 * q);
        l3 = (int) (L3 * q);

        //now calculate the coordinates
        x1 = q1.x + l1 + l2;
        y1 = q1.y;

        x2 = x1 + l3;
        y2 = y1;

        x3 = x2;
        y3 = q3.y;

        x4 = x1;
        y4 = y3;

        //add the coordinates to the point variables
        A1 = new Point(x1, y1);
        A2 = new Point(x2, y2);
        A3 = new Point(x3, y3);
        A4 = new Point(x4, y4);
    }

    /**
     *
     * @param palette
     */
    private void getRGBValuesFromPaletteInt(int palette){
        a1 = Color.alpha(palette);
        r1 = Color.red(palette);
        g1 = Color.green(palette);
        b1 = Color.blue(palette);
    }

    /**
     *
     * @param imageView
     * @param rect
     * @param bitmap
     */
    private void drawRectOnImage(ImageView imageView, Rect rect, Bitmap bitmap){

        Bitmap myBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(myBitmap);

        Paint paint = new Paint();
        paint.setColor(Color.rgb(0, 0, 0));
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(10);

        canvas.drawRect(rect, paint);

        imageView.setImageBitmap(myBitmap);
    }

    /**
     *
     * @return
     */
    private void checkQRRegisteredWithCurrentUser(){StringRequest stringRequest = new StringRequest(Request.Method.POST, checkurl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {


                if (response.equalsIgnoreCase("QR Code is registered with this user")) {
                    continueWithProcessing = true;
                    Log.d("FEARGS CHECK", "The QR is registered with the right user");
                } else if (response.equalsIgnoreCase("QR Code is not yet registered with this user")) {
                    continueWithProcessing = false;
                    Log.d("FEARGS CHECK", "The QR is NOT registered with the right user");
                    Toast.makeText(ProcessImageActivity.this, "You must first register this dressing before you can begin analysis!", Toast.LENGTH_LONG).show();
                } else {
                    continueWithProcessing = false;
                    Log.d("FEARGS CHECK", "PURE SHITE");
                    Toast.makeText(ProcessImageActivity.this, "Something has gone wrong!", Toast.LENGTH_LONG).show();
                }

                //once response has been received we'll call the analysis method
                carryOutColourAnalysis();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismiss();
                Toast.makeText(ProcessImageActivity.this, "Response Error: " + error.toString(), Toast.LENGTH_LONG).show();
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                Map<String, String> params = new HashMap<String, String>();

                //adding all the values from the response and input to the map
                //keys are fields from database
                params.put("qr_info", qrInfoHolder);
                params.put("user_email", LoggedInEmail);

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

        requestQueue.add(stringRequest);
    }

    private void carryOutColourAnalysis(){
        if(continueWithProcessing == true){
            Log.d("FEARGS CHECK", "Value of QR Registered should be true: " + continueWithProcessing);
            textView.setText(qrInfoHolder);

            tableLayout.setVisibility(View.VISIBLE);
            textView2.setVisibility(View.VISIBLE);
            textView3.setVisibility(View.VISIBLE);
            textView4.setVisibility(View.VISIBLE);
            textView5.setVisibility(View.VISIBLE);
            textView6.setVisibility(View.VISIBLE);
            view1.setVisibility(View.VISIBLE);
            view2.setVisibility(View.VISIBLE);
            view3.setVisibility(View.VISIBLE);
            view4.setVisibility(View.VISIBLE);
            view5.setVisibility(View.VISIBLE);

            palette = Palette.from(bitmap2).generate();
            dominantColour = palette.getDominantColor(0);

            a = Color.alpha(dominantColour);
            r = Color.red(dominantColour);
            g = Color.green(dominantColour);
            b = Color.blue(dominantColour);

            textView2.setText("The dominant packed RGB value for the image is: " + dominantColour + ", RGB is (" + r + ", " + g + ", " + b + ")");
            textView2.setBackgroundColor(Color.rgb(r, g, b));

            //set up the touch listener for the image view
            imageView.setDrawingCacheEnabled(true);
            imageView.buildDrawingCache(true);

            //decide what happens the the imageview is touched on the screen
            imageView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {

                    if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
                        bitmapForPixelColor = imageView.getDrawingCache();

                        int pixel = bitmapForPixelColor.getPixel((int) event.getX(), (int) event.getY());

                        r1 = Color.red(pixel);
                        g1 = Color.green(pixel);
                        b1 = Color.blue(pixel);

                        textView3.setBackgroundColor(Color.rgb(r1, g1, b1));
                        textView3.setText("Current Pixel Selection - R: " + r1 + ", G: " + g1 + ", B: " + b1);

                        //we'll get the views layout in pixels
                        //and then convert to density pixels
                        float density = getResources().getDisplayMetrics().density;
                        int top = v.getTop();
                        int left = v.getLeft();

                        int topDP = (int) (top / density);
                        int leftDP = (int) (left / density);

                        //let's get the top left corner of the rectangle that we want to do our colour analysis on
                        int xcoordinate = (int) event.getX() - leftDP;
                        int ycoordinate = (int) event.getY() - topDP;

                        textView4.setText("Current Coordinates: (" + xcoordinate + ", " + ycoordinate + ")");
                    }

                    return true;
                }
            });

            //now, let's begin analysing the geometric properties of our qr code
            //within our image
            qrCornerPoints = getQRCoordinates(barcodeDetector, frame);
            String corners = "";

            for (int i = 0; i < 4; i++) {
                corners = corners + i + ": (" + qrCornerPoints[i].x + ", " + qrCornerPoints[i].y + ")  ";
            }
            textView5.setText(corners);

            //let's set up our rectangle
            //this method takes the qr codes coordinates as input and applies geomtric
            //arithmetic to the corners to calculate the location of our desired
            //rectangle for analusys
            calculateOurRectangle(qrCornerPoints);

            textView6.setText("A1: (" + A1.x + ", " + A1.y + "), " + "A2: (" + A2.x + ", " + A2.y + "), " +
                    "A3: (" + A3.x + ", " + A3.y + "), " + "A4: (" + A4.x + ", " + A4.y + ")");

            //use the Palette API to extract the colours from the QR code - these can
            //potentially be used to calibrate the colours in the rest of the picture
            Palette paletteQR = Palette.from(bitmap2).setRegion(qrCornerPoints[0].x, qrCornerPoints[0].y, qrCornerPoints[1].x, qrCornerPoints[2].y).generate();
            int dominantColourQR = paletteQR.getDominantColor(0);
            int colourLightVibrantQR = paletteQR.getLightVibrantColor(dominantColourQR);
            int colourVibrantQR = paletteQR.getVibrantColor(dominantColourQR);
            int colourDarkVibrantQR = paletteQR.getDarkVibrantColor(dominantColourQR);
            int colourLightMutedQR = paletteQR.getLightMutedColor(dominantColourQR);
            int colourMutedQR = paletteQR.getMutedColor(dominantColourQR);
            int colourDarkMutedQR = paletteQR.getDarkMutedColor(dominantColourQR);

            getRGBValuesFromPaletteInt(colourLightVibrantQR);
            textView7QR.setText("QR: The lightvibrant packed RGB value for the subset of the image is: " + colourLightVibrantQR + ", RGB is (" + r1 + ", " + g1 + ", " + b1 + ")");
            //textView7.setBackgroundColor(Color.rgb(r1, g1, b1));
            view7QR.setBackgroundColor(Color.rgb(r1, g1, b1));

            getRGBValuesFromPaletteInt(colourLightMutedQR);
            textView8QR.setText("QR: The lightmuted packed RGB value for the subset of the image is: " + colourLightMutedQR+ ", RGB is (" + r1 + ", " + g1 + ", " + b1 + ")");
            //textView8.setBackgroundColor(Color.rgb(r1, g1, b1));
            view8QR.setBackgroundColor(Color.rgb(r1, g1, b1));

            getRGBValuesFromPaletteInt(colourVibrantQR);
            textView9QR.setText("QR: The vibrant packed RGB value for the subset of the image is: " + colourVibrantQR + ", RGB is (" + r1 + ", " + g1 + ", " + b1 + ")");
            //textView9.setBackgroundColor(Color.rgb(r1, g1, b1));
            view9QR.setBackgroundColor(Color.rgb(r1, g1, b1));

            getRGBValuesFromPaletteInt(colourDarkMutedQR);
            textView10QR.setText("QR: The darkmuted packed RGB value for the subset of the image is: " + colourDarkMutedQR + ", RGB is (" + r1 + ", " + g1 + ", " + b1 + ")");
            //textView10.setBackgroundColor(Color.rgb(r1, g1, b1));
            view10QR.setBackgroundColor(Color.rgb(r1, g1, b1));

            getRGBValuesFromPaletteInt(colourMutedQR);
            textView11QR.setText("QR: The muted packed RGB value for the subset of the image is: " + colourMutedQR + ", RGB is (" + r1 + ", " + g1 + ", " + b1 + ")");
            //textView11.setBackgroundColor(Color.rgb(r1, g1, b1));
            view11.setBackgroundColor(Color.rgb(r1, g1, b1));

            getRGBValuesFromPaletteInt(colourDarkVibrantQR);
            textView12QR.setText("QR: The darkvibrant packed RGB value for the subset of the image is: " + colourDarkVibrantQR + ", RGB is (" + r1 + ", " + g1 + ", " + b1 + ")");
            //textView12.setBackgroundColor(Color.rgb(r1, g1, b1));
            view12QR.setBackgroundColor(Color.rgb(r1, g1, b1));


            //use the Palette API to set the are of the bitmap to analyse
            Palette palette1 = Palette.from(bitmap2).setRegion(A1.x, A1.y, A2.x, A3.y).generate();
            int dominantColour1 = palette1.getDominantColor(0);
            int colourLightVibrant = palette1.getLightVibrantColor(dominantColour1);
            int colourVibrant = palette1.getVibrantColor(dominantColour1);
            int colourDarkVibrant = palette1.getDarkVibrantColor(dominantColour1);
            int colourLightMuted = palette1.getLightMutedColor(dominantColour1);
            int colourMuted = palette1.getMutedColor(dominantColour1);
            int colourDarkMuted = palette1.getDarkMutedColor(dominantColour1);

            try {
                List<Palette.Swatch> list = palette1.getSwatches();
                Palette.Swatch swatch = palette1.getDarkMutedSwatch();
                //textView6.setBackgroundColor(swatch.getRgb());

            } catch (NullPointerException e) {
                e.printStackTrace();
            }

            getRGBValuesFromPaletteInt(colourLightVibrant);
            textView7.setText("The lightvibrant packed RGB value for the subset of the image is: " + colourLightVibrant + ", RGB is (" + r1 + ", " + g1 + ", " + b1 + ")");
            //textView7.setBackgroundColor(Color.rgb(r1, g1, b1));
            view7.setBackgroundColor(Color.rgb(r1, g1, b1));

            getRGBValuesFromPaletteInt(colourLightMuted);
            textView8.setText("The lightmuted packed RGB value for the subset of the image is: " + colourLightMuted + ", RGB is (" + r1 + ", " + g1 + ", " + b1 + ")");
            //textView8.setBackgroundColor(Color.rgb(r1, g1, b1));
            view8.setBackgroundColor(Color.rgb(r1, g1, b1));

            getRGBValuesFromPaletteInt(colourVibrant);
            textView9.setText("The vibrant packed RGB value for the subset of the image is: " + colourVibrant + ", RGB is (" + r1 + ", " + g1 + ", " + b1 + ")");
            //textView9.setBackgroundColor(Color.rgb(r1, g1, b1));
            view9.setBackgroundColor(Color.rgb(r1, g1, b1));

            getRGBValuesFromPaletteInt(colourDarkMuted);
            textView10.setText("The darkmuted packed RGB value for the subset of the image is: " + colourDarkMuted + ", RGB is (" + r1 + ", " + g1 + ", " + b1 + ")");
            //textView10.setBackgroundColor(Color.rgb(r1, g1, b1));
            view10.setBackgroundColor(Color.rgb(r1, g1, b1));

            getRGBValuesFromPaletteInt(colourMuted);
            textView11.setText("The muted packed RGB value for the subset of the image is: " + colourMuted + ", RGB is (" + r1 + ", " + g1 + ", " + b1 + ")");
            //textView11.setBackgroundColor(Color.rgb(r1, g1, b1));
            view11.setBackgroundColor(Color.rgb(r1, g1, b1));

            getRGBValuesFromPaletteInt(colourDarkVibrant);
            textView12.setText("The darkvibrant packed RGB value for the subset of the image is: " + colourDarkVibrant + ", RGB is (" + r1 + ", " + g1 + ", " + b1 + ")");
            //textView12.setBackgroundColor(Color.rgb(r1, g1, b1));
            view12.setBackgroundColor(Color.rgb(r1, g1, b1));

            //now let's try to create new imageview with the original image
            //with a rectangle drawn over the desired area
            Rect rect = new Rect(A1.x, A1.y, A2.x, A3.y);
            drawRectOnImage(imageView2, rect, bitmap2);
        }
        else{
            textView.setText("Make sure you've registered the QR Code..");
            Log.d("FEARGS CHECK", "Value of QR Registered: " + continueWithProcessing);
        }

    }
}
