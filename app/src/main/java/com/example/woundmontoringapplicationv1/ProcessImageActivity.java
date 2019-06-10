package com.example.woundmontoringapplicationv1;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorSpace;
import android.graphics.Matrix;
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
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.woundmontoringapplicationv1.LoginActivity.SHARED_PREFS;

/**
 *
 */
public class ProcessImageActivity extends AppCompatActivity {

    //store our known real-life measurements for the distance between points on a dressing
    //distance between the top two corners of the qr code in cm
    //final static double L1 = 7.1;
    final static double L1 = 5.0;
    //distance between the top right corner of qr code and top left corner of the area to be
    //analysed for colour in cm
    final static double L2 = 2.1;
    //distance between the top two points of the rectangle in cm
    //final static double L3 = 15.5;
    final static double L3 = 15.9;

    //Measurements for the circles
    //distance from edge of rect to centre of circle1 in cm
    final static double LCirc1 = 2.0;
    //distance from centre of circle1 to centre of circle2 in cm
    final static double LCirc2 = 4.0;
    //distance from the centre of circle2 to centre of circle3 in cm
    final static double LCirc3 = 3.6;
    //distance from the centre of circle3 to centre of circle4 in cm
    final static double LCirc4 = 4.1;

    //the real-world radius of the 4 circles in cm
    final static double radius1 = 1.7 / 2;
    final static double radius2 = 1.6 / 2;
    final static double radius3 = 1.5 / 2;
    final static double radius4 = 1.5 / 2;

    ProgressDialog progressDialog;
    static final String checkurl = "http://foman01.lampt.eeecs.qub.ac.uk/woundmonitoring/process_image.php";
    boolean continueWithProcessing = false;

    String timestamp = "";
    String path = "";

    Button processImgBtn, submitAnalysisBtn, opencvBtn;
    FloatingActionButton floatingActionButton;
    ImageView imageView, imageView2, imageView3;

    Bitmap bitmap, bitmap2, bitmapForPixelColor;
    BarcodeDetector barcodeDetector;

    View view1, view2, view3, view4, view5, view7, view10, view8, view9, view11, view12;
    View view7QR, view10QR, view8QR, view9QR, view11QR, view12QR;
    View viewCAL1, viewBetweenBitmaps;

    TextView textView, textView2, textView3, textView4, textView5, textView6, textView7, textView8, textView9, textView10, textView11, textView12;
    TextView textView7QR, textView8QR, textView9QR, textView10QR, textView11QR, textView12QR;
    TextView textViewCAL1;
    TableLayout tableLayout;

    Frame frame, frame1;
    SparseArray<Barcode> barcodes;
    Barcode thisBarCode;
    Bundle bundle;

    Point[] qrCornerPoints, qrCornerPoints1;
    Rect rect;

    //CIE LAB conversion
    ColorSpace.Connector connector, connector2;
    float[] labDeltaE;
    int[][] updatedPixelValues;

    Palette palette;
    int dominantColour, a, r, g, b, r1, g1, b1, a1;

    //variables to hold the calculated four corners of the rectangle to be analysed
    int x1, y1, x2, y2, x3, y3, x4, y4;
    int l1, l2, l3;

    double slope;

    //coordinates for circles
    int circ1x, circ1y, circ2x, circ2y, circ3x, circ3y, circ4x, circ4y;
    Point circ1, circ2, circ3, circ4;

    double q;
    Point A1, A2, A3, A4;

    //integers for the reg, green, blue rgb values
    int redR = 255, redG = 0, redB = 0, greenR = 0, greenG = 128, greenB = 0, blueR = 0, blueG = 0, blueB = 255;
    boolean redPresent = false, greenPresent = false, bluePresent = false;

    String LoggedInEmail, qrInfoHolder;
    RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_process_image);

        processImgBtn = findViewById(R.id.button);
        submitAnalysisBtn = findViewById(R.id.buttonSubmit);
        floatingActionButton = findViewById(R.id.backToTakeImage);
        opencvBtn = findViewById(R.id.buttonOPENCVANALYSIS);

        progressDialog = new ProgressDialog(ProcessImageActivity.this);

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

        textViewCAL1 = findViewById(R.id.txtContentCAL1) ;

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

        viewCAL1 = findViewById(R.id.viewCAL1);
        viewBetweenBitmaps = findViewById(R.id.viewBETWEENBITMAPS);

        imageView2 = findViewById(R.id.imgview2);
        imageView = findViewById(R.id.imgview);

        requestQueue = Volley.newRequestQueue(ProcessImageActivity.this);
        progressDialog = new ProgressDialog(ProcessImageActivity.this);

        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        //LoggedInEmail = sharedPreferences.getString("email", "");
        LoggedInEmail = "johndoe@gmail.com";

        connector = ColorSpace.connect(ColorSpace.get(ColorSpace.Named.SRGB), ColorSpace.get(ColorSpace.Named.CIE_LAB));
        connector2 = ColorSpace.connect(ColorSpace.get(ColorSpace.Named.CIE_LAB), ColorSpace.get(ColorSpace.Named.SRGB));

        bundle = getIntent().getExtras();

        if (bundle != null) {
            path = bundle.get("imageName").toString();
            timestamp = bundle.get("timestamp").toString();

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

                progressDialog.setMessage("Please wait...");
                progressDialog.show();

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
                        progressDialog.dismiss();
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
     * @param cornerPoints
     * @return
     */
    private double getSlopeOfRectangle(Point[] cornerPoints){
        double m = 0;

        Point a = cornerPoints[0];
        Point b = cornerPoints[1];

        double numerator = b.y - a.y;
        double denominator = b.x - a.x;

        m = Math.toDegrees(Math.atan2(numerator, denominator));
        Log.d("FEARGS SLOPE", "Slope: " + m);

        return m;
    }

    /**
     *
     * @param bitmap
     * @param slope
     */
    private Bitmap rotateBitmap(Bitmap bitmap, double slope){
        Matrix rotationMatrix = new Matrix();
        rotationMatrix.postRotate((float) (Math.abs(slope)));


        Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), rotationMatrix, false);

        //imageView.setImageBitmap(rotatedBitmap);
        return rotatedBitmap;
    }

    /**
     * Pass the coordinates of the QR codes' corner points and using geometry calculate
     * the coordinates of the corners of the rectangle that covers the area of the dressing above the
     * wound.
     * The real world values for distance between points on the dressing, in cm, are set at the top of the
     * class and used to calculate the proportional distances between points in the image.
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

        //slope of the qr code
        double m = (q2.y - q1.y) / (q2.x - q1.x);

        //proportional value
        q = l1 / L1;

        //distance between qr code and desired rectangle
        l2 = (int) (L2 * q);
        l3 = (int) (L3 * q);

        //calculate new coordinates using slope, point on the line, and our distances
        x1 = (int) (q2.x + (l2 * (Math.sqrt(1 / (1 + Math.pow(m, 2))))));
        y1 = (int) (q2.y + ((m * l2) * (Math.sqrt(1 / (1 + Math.pow(m, 2))))));

        x2 = (int) (x1 + (l3 * (Math.sqrt(1 / (1 + Math.pow(m, 2))))));
        y2 = (int) (y1 + ((m * l3) * (Math.sqrt(1 / (1 + Math.pow(m, 2))))));

        x3 = x2;
        y3 = y2 + l1;

        x4 = x1;
        y4 = y1 + l1;

        /**
        //now calculate the coordinates
        x1 = q1.x + l1 + l2;
        y1 = q1.y;

        x2 = x1 + l3;
        y2 = y1;

        x3 = x2;
        y3 = q3.y;

        x4 = x1;
        y4 = y3;
        **/

        //add the coordinates to the point variables
        A1 = new Point(x1, y1);
        A2 = new Point(x2, y2);
        A3 = new Point(x3, y3);
        A4 = new Point(x4, y4);
    }

    /**
     *
     * @param rect
     */
    private void calculateOurCircles(Rect rect, double proportion){
        circ1y = rect.top + ((rect.bottom - rect.top) / 2);
        circ2y = circ1y;
        circ3y = circ1y;
        circ4y = circ1y;

        circ1x = (int) (rect.left + (LCirc1 * proportion));
        circ2x = (int) (circ1x + (LCirc2 * proportion));
        circ3x = (int) (circ2x + (LCirc3 * proportion));
        circ4x = (int) (circ3x + (LCirc4 * proportion));

        circ1 = new Point(circ1x, circ1y);
        circ2 = new Point(circ2x, circ2y);
        circ3 = new Point(circ3x, circ3y);
        circ4 = new Point(circ4x, circ4y);
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
     * The method receives an imageView, rectangle, and bitmap as parameters. The imageview is set
     * to the passed bitmap, and draws a black rectangle over the bitmap using the coordinates passed
     * by the rectangle parameter.
     * The aim here is to draw a rectangle a closely as possible to the area of interest on the
     * dressing.
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

        float angle = (float) (Math.toDegrees(Math.atan2(x2 - x1, y2 - y1)));
        //canvas.rotate(angle, rect.left, rect.top);
        canvas.drawRect(rect, paint);

        //try to draw the circles
        canvas.drawCircle(circ1.x, circ1.y, (float) (radius1 * q), paint);
        canvas.drawCircle(circ2.x, circ2.y, (float) (radius2 * q), paint);
        canvas.drawCircle(circ3.x, circ3.y, (float) (radius3 * q), paint);
        canvas.drawCircle(circ4.x, circ4.y, (float) (radius4 * q), paint);

        imageView.setImageBitmap(myBitmap);
    }

    /**
     * This method sends a Volley StringRequest to check whether the QR Code in the captured image has been
     * registered with the logged in user.
     * If it is then the boolean continueWithProcessing is set to true - this is checked in the next method
     * (carryOutColourAnalysis)
     * The methods carryOutColourAnalysis, calculateCalibration, checkBitmapForRGBValues are all called from within here
     **/
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

                //and we'll call the method that attempts the calibration process
                //calculateCalibration(qrCornerPoints);

                //call the other calibration method
                //labDeltaE = calculateDistanceFromWhiteUsingLAB(qrCornerPoints, bitmap2);
                //updatedPixelValues = correctErrorDueToLightingConditions(labDeltaE, bitmap2);

                //create the new, updated bitmap
                //createCorrectedBitmap(updatedPixelValues);

                //check the bitmap for red, green, blue
                checkBitmapForRGBValues(bitmap2);

                progressDialog.dismiss();

                //add an on click listener to the submit analysis button
                submitAnalysisBtn.setVisibility(View.VISIBLE);
                submitAnalysisBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getApplicationContext(), AnalysisSubmissionActivity.class);
                        intent.putExtra("QRInfo", qrInfoHolder);
                        intent.putExtra("DominantColour", "(" + r + ", " + g + ", " + b + ")");
                        intent.putExtra("RedPresent", redPresent);
                        intent.putExtra("GreenPresent", greenPresent);
                        intent.putExtra("BluePresent", bluePresent);
                        intent.putExtra("UserEmail", LoggedInEmail);
                        intent.putExtra("Timestamp", timestamp);
                        startActivity(intent);
                    }
                });

                opencvBtn.setVisibility(View.VISIBLE);
                opencvBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent openIntent = new Intent(getApplicationContext(), OpenCVColorAnalysisActivity.class);
                        openIntent.putExtra("imageName", path);
                        openIntent.putExtra("rectangleForAnalysis", rect);
                        openIntent.putExtra("slope", slope);
                        startActivity(openIntent);
                    }
                });
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

    /**
     * This method checks whether the boolean continueWithProcessing is true or not.
     * If true, then the views to do with the analysis are all set to visible.
     * The following analyses are done:
     *  - the dominant colour is extracted from the whole image
     *  - an on touch listener is set for the image so that user can point to a part of the image
     *    and see what the exact RGB values for the pixel are
     *  - the Palette API extracts the palette for the subset of the image over wound
     *  - the Palette API extracts the palette for the subset of the image for QR Code
     *  - the coordinates of the subset of the image above the wound is calculated and a black rectangle
     *    is drawn over the calculated area
     */
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

            //calculate the slope of the qrcode in the original bitmap
            slope = getSlopeOfRectangle(qrCornerPoints);
            Bitmap rotatedBitmap = rotateBitmap(bitmap2, slope);

            //now let's try to create new imageview with the original image
            //with a rectangle drawn over the desired area
            frame1 = new Frame.Builder().setBitmap(rotatedBitmap).build();
            qrCornerPoints1 = getQRCoordinates(barcodeDetector, frame1);
            calculateOurRectangle(qrCornerPoints1);
            rect = new Rect(A1.x, A1.y, A2.x, A3.y);
            calculateOurCircles(rect, q);

            drawRectOnImage(imageView2, rect, rotatedBitmap);
        }
        else{
            textView.setText("Make sure you've registered the QR Code..");
            Log.d("FEARGS CHECK", "Value of QR Registered: " + continueWithProcessing);
        }

    }

    /**
     * This method takes an array of points as a parameter - these points are the corner points
     * of the image's QR Code.
     * The idea here is that we draw a diagonal line from the top left corner to the bottom right corner,
     * every pixel along this straight line should be one of two colours: black or white
     * At the moment the method finds the pixel closest to white and the pixel closest to black.
     * A better alternative could be to take every pixel in the QR Code and assign them to one of two groups:
     * closer to black, or closer to white. Then (using an appropriate colour space) we could get the average
     * distance of each cluster from their desired colour and use this as the delta error due to lighting
     * conditions - check the validity of this method with Darryl.
     * @param qrCornerPoints
     */
    private void calculateCalibration(Point[] qrCornerPoints){
        //first two corner points are the top left and top right corners
        //if we go along the diagonal we should get pixels with only two different values: black and white
        //let's iterate along this line and store every integer value that appears at least once
        Point topLeft = qrCornerPoints[0];
        Point bottomRight = qrCornerPoints[2];
        Log.d("FEARGS LINE", "The top left corner is (" + topLeft.x + ", " + topLeft.y + ")");
        Log.d("FEARGS LINE", "The bottom right corner is (" + bottomRight.x + ", " + bottomRight.y + ")");

        ArrayList<Integer> coloursInQRCode = new ArrayList<>();

        double slope = (bottomRight.y - topLeft.y) / (bottomRight.x - topLeft.x);
        double yIntercept = (-bottomRight.y + (slope*bottomRight.x)) / slope;

        Log.d("FEARGS LINE", "The eqaution of the line is y = " + slope + "x + " + yIntercept);

        for(int i = topLeft.x; i <= bottomRight.x; i++){
            double j = (slope * i) - yIntercept;

            int pixel = bitmap2.getPixel((int) i, (int) j);

            coloursInQRCode.add(pixel);
        }

        Collections.sort(coloursInQRCode);
        int size = coloursInQRCode.size();

        int r1lowest = Color.red(coloursInQRCode.get(0));
        int g1lowest = Color.green(coloursInQRCode.get(0));
        int b1lowest = Color.blue(coloursInQRCode.get(0));
        int checkLowestCOvnersion = coloursInQRCode.get(0) & 0xFFFFFF;

        int r1highest = Color.red(coloursInQRCode.get(size - 1));
        int g1highest = Color.green(coloursInQRCode.get(size - 1));
        int b1highest = Color.blue(coloursInQRCode.get(size - 1));
        int checkHighestCOvnersion = coloursInQRCode.get(size - 1) & 0xFFFFFF;

        textViewCAL1.setText("The blackest colour on the line is: " + coloursInQRCode.get(0) + "(" + checkLowestCOvnersion + ")" + "(" + r1lowest + ", " + g1lowest + ", " + b1lowest + ")." + "The whitest colour on the line is: "+ coloursInQRCode.get(size - 1) + "(" + checkHighestCOvnersion + ")" + "(" + r1highest + ", " + g1highest + ", " + b1highest + ")");
        viewCAL1.setBackgroundColor(Color.rgb(r1lowest, g1lowest, b1lowest));
        textViewCAL1.setBackgroundColor(Color.rgb(r1highest, g1highest, b1highest));
    }

    /**
     * takes original bitmap and cornerpoints of the QR code as parameters
     * iterates through all pixels along the top of the qr code and identifies the point with the highest
     * sum of rgb values: logic behind this is that we want the point as close to (255, 255, 255) as possible
     * as this gives us our white point
     * converts to CIELAB and uses Euclidean distance formula to find the distance to 'pure' white
     * returns this distance in the form of a double (float array)
     * @param points, bitmap
     */
    private float[] calculateDistanceFromWhiteUsingLAB(Point[] points, Bitmap bitmap){

        Point topLeft = points[0];
        Point topRight = points[1];

        Point pixelForCalibration = new Point();

        int maxRGB = 0;

        for(int x = topLeft.x; x < topRight.x; x++){
            Log.d("FEARGS TOP LINE", x + ": (" + Color.red(bitmap.getPixel(x, topLeft.y)) + ", " + Color.green(bitmap.getPixel(x, topLeft.y)) + ", " + Color.blue(bitmap.getPixel(x, topLeft.y)) + ")");

            int rgbCombined = Color.red(bitmap.getPixel(x, topLeft.y)) + Color.green(bitmap.getPixel(x, topLeft.y)) + Color.blue(bitmap.getPixel(x, topLeft.y));

            if(rgbCombined > maxRGB){
                maxRGB = rgbCombined;
                pixelForCalibration.set(x, topLeft.y);
            }
        }

        Log.d("FEARGS EUCLIDEAN", "the pixel chosen: " + pixelForCalibration.x + ", " + pixelForCalibration.y);
        Log.d("FEARGS EUCLIDEAN", "the pixel chosen(RGB): " + Color.red(bitmap.getPixel(pixelForCalibration.x, pixelForCalibration.y)) + ", " + Color.green(bitmap.getPixel(pixelForCalibration.x, pixelForCalibration.y)) + ", " + Color.blue(bitmap.getPixel(pixelForCalibration.x, pixelForCalibration.y)));

        float[] cie_lab = connector.transform(Color.red(bitmap.getPixel(pixelForCalibration.x, pixelForCalibration.y)), Color.green(bitmap.getPixel(pixelForCalibration.x, pixelForCalibration.y)), Color.blue(bitmap.getPixel(pixelForCalibration.x, pixelForCalibration.y)));

        //euclidean distance is used for LAB color distance measurement
        float[] whiteLAB = connector.transform(255, 255, 255);
        double distanceBetween = Math.sqrt((cie_lab[0] * whiteLAB[0]) + (cie_lab[1] * whiteLAB[1]) + (cie_lab[2] * whiteLAB[2]));

        Log.d("FEARGS LAB", "Our shite white: " + cie_lab[0] + "." + cie_lab[1] + "." + cie_lab[2]);
        Log.d("FEARGS LAB", "Pure angelic white: " + whiteLAB[0] + "." + whiteLAB[1] + "." + whiteLAB[2]);

        float lDifference = whiteLAB[0] - cie_lab[0];
        float aDifference = whiteLAB[1] - cie_lab[1];
        float bDifference = whiteLAB[2] - cie_lab[2];

        float[] deltaValues = {lDifference, aDifference, bDifference};

        Log.d("FEARGS EUCLIDEAN", "Distance according to Euclid: " + distanceBetween);
        Log.d("FEARGS LAB", "Difference between the whites: " + deltaValues[0] + "." + deltaValues[1] + "." + deltaValues[2]);
        return deltaValues;
    }

    /**
     *
     * @param deltas
     * @param bitmap
     * @return
     */
    private int[][] correctErrorDueToLightingConditions(float[] deltas, Bitmap bitmap){
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        int[][] newPixelValues = new int[width][height];

        for(int i = 0; i < width; i++){
            for(int j = 0; j < height; j++){
                newPixelValues[i][j] = correctPixelValue(bitmap.getPixel(i, j), deltas);
            }
        }

        return newPixelValues;
    }

    /**
     *
     * @param pixel
     * @param deltas
     * @return
     */
    private int correctPixelValue(int pixel, float[] deltas){
        int pixelCorrected;

        float[] labPixelValues = connector.transform(Color.red(pixel), Color.green(pixel), Color.blue(pixel));
        float[] correctedLabPixelValues = {labPixelValues[0] + deltas[0], labPixelValues[1] + deltas[1], labPixelValues[2] + deltas[2]};

        float[] pixelsCorrected = connector2.transform(correctedLabPixelValues);
        float rgb = 65536 * pixelsCorrected[0] * 256 * pixelsCorrected[1] * pixelsCorrected[2];

        pixelCorrected = Math.round(rgb);

        return pixelCorrected;
    }


    /**
     * This method takes our original bitmap (created from the captured image) and creates a new
     * bitmap based on the subsection that we identified as being the area for analysis.
     * The method uses a nested for loop to iterate through all pixels in the new bitmap and for
     * each pixel checks whether its color is exactly equal to red, green, or blue.
     * It is unlikely that this will ever be the case so this method is mainly for roughwork
     * @param bitmap
     */
    private void checkBitmapForRGBValues(Bitmap bitmap){
        Bitmap bitmapForAnalysis = Bitmap.createBitmap(bitmap, A1.x, A1.y, (A2.x - A1.x), (A4.y - A1.y));

        for(int i = 0; i < bitmapForAnalysis.getWidth(); i++){
            for(int j = 0; j < bitmapForAnalysis.getHeight(); j++){

                //Log.d("FEARGS BITMAP LOOP", i + " " + j);
                int check = bitmapForAnalysis.getPixel(i, j);

                if(Color.red(check) == redR && Color.green(check) == redG && Color.blue(check) == redB){
                    redPresent = true;
                }
                else if(Color.red(check) == greenR && Color.green(check) == greenG && Color.blue(check) == greenB){
                    greenPresent = true;
                }
                else if(Color.red(check) == blueR && Color.green(check) == blueG && Color.blue(check) == blueB){
                    bluePresent = true;
                }
            }
        }
    }

}
