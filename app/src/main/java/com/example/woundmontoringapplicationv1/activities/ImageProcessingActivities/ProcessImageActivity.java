package com.example.woundmontoringapplicationv1.activities.ImageProcessingActivities;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;

import com.example.woundmontoringapplicationv1.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint2f;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * The activity which takes the image from the CaptureImageActivity
 * and processes it. QR Code is extracted, image is transformed and rotated
 * and colour analysis is performed on the four circular regions of the dressing
 */
public class ProcessImageActivity extends AppCompatActivity {
    /**-----------------------------------------------------------------------**/
    /**----------------------STATIC VARIABLES --------------------------------**/
    /**-----------------------------------------------------------------------**/
     //store our known real-life measurements for the distance between points on a dressing
    //distance between the top two corners of the qr code in cm
    final static double L1 = 5.12;
    //distance between the top right corner of qr code and top left corner of the area to be
    //analysed for colour in cm
    final static double L2 = 2.3;
    //distance between the top two points of the rectangle in cm
    final static double L3 = 16.2;
    //length of the side of qr corner square
    final static double L_QR = 1.4;

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

    //url that checks that the dressing has been registered with the user
    static final String checkurl = "http://foman01.lampt.eeecs.qub.ac.uk/woundmonitoring/process_image.php";

    /**-----------------------------------------------------------------------**/
    /**---------------------INSTANCE VARiABLES -------------------------------**/
    /**-----------------------------------------------------------------------**/
    //visions api barcode vars
    Barcode thisBarCode;
    BarcodeDetector barcodeDetector;
    Bitmap bitmap;
    //boolean, if set to true by the checkregisteredwithuser method, then analysis is performed
    boolean continueWithProcessing = false;
    //bundle for data passed from the CaptureImageActivity
    Bundle bundle;
    Button processImgBtn, submitAnalysisBtn;
    double slope;
    double q;
    //firebase vars
    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;
    FloatingActionButton floatingActionButton;
    Frame frame, frameRotated;
    ImageView imageView2;
    //variables to hold the calculated four corners of the rectangle to be analysed
    int x1, y1, x2, y2, x3, y3, x4, y4, orientation, l1, l2, l3;
    int[] overallRGBC1, overallRGBC2, overallRGBC3, overallRGBC4;
    Point[] qrCornerPoints, qrCornerPointsRotated;
    Point centreC1, centreC2, centreC3, centreC4;
    Point A1, A2, A3, A4;
    ProgressDialog progressDialog;
    Rect rect;
    RequestQueue requestQueue;
    SparseArray<Barcode> barcodes;
    String LoggedInEmail, qrInfoHolder, timestamp = "", path = "", overallRGBC1String, overallRGBC2String, overallRGBC3String, overallRGBC4String;
    TextView textView;

    /**-----------------------------------------------------------------------**/
    /**---------------------BASE LOADER CALLBACK -----------------------------**/
    /**-----------------------------------------------------------------------**/
    //ensures the correct setup of the opencv library
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch(status){
                case LoaderCallbackInterface.SUCCESS:{
                    Log.i("FEARG", "OpenCV loaded successfully");
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };


    /**-----------------------------------------------------------------------**/
    /**---------------------ONCREATE METHOD ----------------------------------**/
    /**-----------------------------------------------------------------------**/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_process_image);

        //initialise the opencv loader
        OpenCVLoader.initDebug();

        processImgBtn = findViewById(R.id.button);
        submitAnalysisBtn = findViewById(R.id.buttonSubmit);
        floatingActionButton = findViewById(R.id.backToTakeImage);

        progressDialog = new ProgressDialog(ProcessImageActivity.this);

        textView = findViewById(R.id.txtContent) ;
        imageView2 = findViewById(R.id.imgview2);

        requestQueue = Volley.newRequestQueue(ProcessImageActivity.this);
        progressDialog = new ProgressDialog(ProcessImageActivity.this);

        //use firebase auth to setup the email variable
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        LoggedInEmail = firebaseUser.getEmail();

        bundle = getIntent().getExtras();

        //if data has been passed from the camera
        if (bundle != null) {
            path = bundle.get("imageName").toString();
            timestamp = bundle.get("timestamp").toString();

            try {
                //create a bitmap from the file's byte stream
                FileInputStream fIS = new FileInputStream(new File(path));
                bitmap = BitmapFactory.decodeStream(fIS);
                imageView2.setImageBitmap(bitmap);
            }
            catch(FileNotFoundException e){
                e.printStackTrace();
            }
        }
        else{
            textView.setText("There was nothing passed from the camera. Please try again..");
        }

        /**
         * click to go back
         */
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), CaptureImageActivity.class);
                intent.putExtra("CALLING_ACTIVITY", "ProcessNewImage");
                startActivity(intent);
            }
        });

        /**-----------------------------------------------------------------------**/
        /**----------------------BEGIN IMAGE PROCESSING---------------------------**/
        /**-----------------------------------------------------------------------**/
        processImgBtn.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public void onClick(View v) {

                progressDialog.setMessage("Please wait...");
                progressDialog.show();

                //setup the barcode detector
                barcodeDetector = new BarcodeDetector.Builder(getApplicationContext()).setBarcodeFormats(Barcode.QR_CODE).build();

                if (!barcodeDetector.isOperational()) {
                    textView.setText("Couldn't setup the detector1");
                    return;
                } else {
                    //if it is setup then create a frame of the image bitmap
                    frame = new Frame.Builder().setBitmap(bitmap).build();

                    barcodes = barcodeDetector.detect(frame);

                    //if a barcode has been detected
                    if (barcodes.size() > 0) {
                        thisBarCode = barcodes.valueAt(0);
                        qrInfoHolder = thisBarCode.rawValue;

                        /**-----------------------------------------------------------------------**/
                        /**-------------------THE ANALYSIS IS STARTED HERE------------------------**/
                        /**-----------------------------------------------------------------------**/
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
     * When onResume is called, the OpenCV loader checks whether openCv has been loaded
     * into the project correctly
     */
    @Override
    public void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d("FEARG", "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d("FEARG", "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
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
        Point[] qrCornerPoints1 = b.cornerPoints;

        return qrCornerPoints1;
    }

    /**
     * Method takes the top two corners of the QR code and
     * calculates the slope between them
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
     * Method rotates the bitmap using the slope that has been
     * calculated; this creates a horizontal bitmap
     * with relation to the QR code
     * @param bitmap
     * @param slope
     */
    private Bitmap rotateBitmap(Bitmap bitmap, double slope){

        Matrix rotationMatrix = new Matrix();
        rotationMatrix.postRotate((float) (Math.abs(slope)));

        Bitmap rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), rotationMatrix, false);

        return rotated;
    }

    /**
     * Method begins calling the orientation methods
     * It uses the bitmap and the QR code coordinates to
     * orientate the bitmap
     * @param bitmap
     * @param points
     * @return
     */
    private Bitmap orientateBitmap(Bitmap bitmap, Point[] points){
        Bitmap bitmap1;

        orientation = getOrientationOfBitmap(bitmap, points);
        Log.d("FEARGS ORIENTATION", "orientation degrees: " + orientation);

        Matrix rotationMatrix = new Matrix();
        rotationMatrix.postRotate((float) (Math.abs(orientation)));

        bitmap1 = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), rotationMatrix, false);

        return bitmap1;
    }

    /**
     * Method gets the results of the setUpArrayOfPoints method
     * for each corner of the QR code. If the result is true for a corner
     * then this means it is the Alignment Marker; which should be in the bottom
     * right corner for the correct orientation
     * @param bitmap
     * @param points
     * @return
     */
    private int getOrientationOfBitmap(Bitmap bitmap, Point[] points){
        int ORIENTATION = 0;

        boolean corner1 = setupArrayOfPoints(0, points[0], bitmap);
        boolean corner2 = setupArrayOfPoints(1, points[1], bitmap);
        boolean corner3 = setupArrayOfPoints(2, points[2], bitmap);
        boolean corner4 = setupArrayOfPoints(3, points[3], bitmap);

        String orientationCorner = getOrientationCorner(corner1, corner2, corner3, corner4);

        switch (orientationCorner){
            case "topLeft":
                ORIENTATION = 180;
                break;
            case "topRight":
                ORIENTATION = 90;
                break;
            case "bottomRight":
                ORIENTATION = 0;
                break;
            case "bottomLeft":
                ORIENTATION = 270;
                break;
        }

        return ORIENTATION;
    }

    /**
     * Pass in one of the qr corners, come one pixel in from the corner, then using the actual real
     * world length of the side of the squares, check that a line drawn on both directions passes
     * only black pixels
     * @param corner
     * @return
     */
    private boolean setupArrayOfPoints(int corner, Point point, Bitmap bitmap){

        float radius = 1;

        Bitmap bitmap1 = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(bitmap1);

        Paint paint = new Paint();
        paint.setColor(Color.BLUE);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setStrokeWidth(15);

        ArrayList<Point> points = new ArrayList<>();

        boolean orientationCorner;

        int signX = 1, signY = 1;

        if(corner == 0){
            signX = 1;
            signY = 1;
        }
        else if(corner == 1){
            signX = -1;
            signY = 1;
        }
        else if(corner == 2){
            signX = -1;
            signY = -1;
        }
        else if(corner == 3){
            signX = 1;
            signY = -1;
        }

        points.add(new Point(point.x + (3 * signX), point.y + (1 * signY)));
        points.add(new Point(point.x + (3 * signX), point.y + ((int) (L_QR * q * .1) * signY)));
        points.add(new Point(point.x + (3 * signX), point.y + ((int) (L_QR * q * .2) * signY)));
        points.add(new Point(point.x + (3 * signX), point.y + ((int) (L_QR * q * .3) * signY)));
        points.add(new Point(point.x + (3 * signX), point.y + ((int) (L_QR * q * .4) * signY)));
        points.add(new Point(point.x + (3 * signX), point.y + ((int) (L_QR * q * .5) * signY)));
        points.add(new Point(point.x + (3 * signX), point.y + ((int) (L_QR * q * .6) * signY)));
        points.add(new Point(point.x + (3 * signX), point.y + ((int) (L_QR * q * .7) * signY)));

        points.add(new Point(point.x + (1 * signX), point.y + (1 * signY)));
        points.add(new Point(point.x + ((int) (L_QR * q * .1) * signX), point.y + (3 * signY)));
        points.add(new Point(point.x + ((int) (L_QR * q * .2) * signX), point.y + (3 * signY)));
        points.add(new Point(point.x + ((int) (L_QR * q * .3) * signX), point.y + (3 * signY)));
        points.add(new Point(point.x + ((int) (L_QR * q * .4) * signX), point.y + (3 * signY)));
        points.add(new Point(point.x + ((int) (L_QR * q * .5) * signX), point.y + (3 * signY)));
        points.add(new Point(point.x + ((int) (L_QR * q * .6) * signX), point.y + (3 * signY)));
        points.add(new Point(point.x + ((int) (L_QR * q * .7) * signX), point.y + (3 * signY)));

        for(Point p : points){
            canvas.drawCircle((float) p.x, (float) p.y, radius, paint);
        }

        Log.d("FEARGS CORNER", "WE ARE NOW CHECKING CORNER " + corner);

        boolean dataCorner = closerToBlack(points, bitmap);
        if(dataCorner){
            orientationCorner = false;
        }
        else{
            orientationCorner = true;
        }

        return orientationCorner;
    }

    /**
     * Takes the array of points from a QR corner. Calls the method that
     * checks whether each pixels corner os closer to black or white. If it
     * is closer to white for any pixel then false is returned, as it must then not be
     * a Position Marker.
     * @param points
     * @return
     */
    private boolean closerToBlack(ArrayList<Point> points, Bitmap bitmap){
        boolean closerToBlack = true;

        for(int i = 0; i < points.size(); i++){
            closerToBlack = checkWhetherPixelCloserToBlack(bitmap, points.get(i));
            Log.d("FEARGS CHECK", "Checking the point: (" + points.get(i).x + ", " + points.get(i).y + ")");
            if(closerToBlack == false){
                break;
            }
        }
        return closerToBlack;
    }

    /**
     * Checks whether a pixel in the bitmap is closer to
     * black or white.
     * @param bitmap
     * @param point
     * @return
     */
    private boolean checkWhetherPixelCloserToBlack(Bitmap bitmap, Point point){
        boolean closerToBlack;

        int colorToCheck = bitmap.getPixel(point.x,point.y);

        int r = Color.red(colorToCheck);
        int g = Color.green(colorToCheck);
        int b = Color.blue(colorToCheck);

        int white = 765;

        if((r + g + b) > (white / 2)){
            closerToBlack = false;
        }
        else{
            closerToBlack = true;
        }

        return closerToBlack;
    }

    /**
     * Creates the orientation corner string; if a corner's value is true
     * then this means it is the orientation corner.
     * @param c1
     * @param c2
     * @param c3
     * @param c4
     * @return
     */
    private String getOrientationCorner(boolean c1, boolean c2, boolean c3, boolean c4){
        String orientationCorner = "";

        Log.d("FEARGS ORIENTATION", "C1: " + c1);
        Log.d("FEARGS ORIENTATION", "C2: " + c2);
        Log.d("FEARGS ORIENTATION", "C3: " + c3);
        Log.d("FEARGS ORIENTATION", "C4: " + c4);

        if(c1){ orientationCorner = "topLeft";
        }
        else if(c2){ orientationCorner = "topRight";
        }
        else if(c3){ orientationCorner = "bottomRight";
        }
        else if(c4){ orientationCorner = "bottomLeft";
        }

        return orientationCorner;
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

        //add the coordinates to the point variables
        A1 = new Point(x1, y1);
        A2 = new Point(x2, y2);
        A3 = new Point(x3, y3);
        A4 = new Point(x4, y4);
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

        imageView.setImageBitmap(myBitmap);
    }

    /**
     * This method sends a Volley StringRequest to check whether the QR Code in the captured image has been
     * registered with the logged in user.
     * If it is then the boolean continueWithProcessing is set to true - this is checked in the next method
     * (carryOutColourAnalysis)
     *
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

                /**-------------------------------------------------------------------------**/
                /**----------------------CARRY OUT COLOR ANALYSIS---------------------------**/
                /**-------------------------------------------------------------------------**/
                //once response has been received we'll call the analysis method
                carryOutColourAnalysis();
                progressDialog.dismiss();

                //add an on click listener to the submit analysis button
                submitAnalysisBtn.setVisibility(View.VISIBLE);
                submitAnalysisBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getApplicationContext(), AnalysisSubmissionActivity.class);
                        intent.putExtra("QRInfo", qrInfoHolder);
                        intent.putExtra("UserEmail", LoggedInEmail);
                        intent.putExtra("Timestamp", timestamp);

                        intent.putExtra("rgbC1", overallRGBC1String);
                        intent.putExtra("rgbC2", overallRGBC2String);
                        intent.putExtra("rgbC3", overallRGBC3String);
                        intent.putExtra("rgbC4", overallRGBC4String);

                        startActivity(intent);
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
        if(continueWithProcessing){
            textView.setText(qrInfoHolder);
            qrCornerPoints = getQRCoordinates(barcodeDetector, frame);
            //let's set up our rectangle this method takes the qr codes coordinates as input and applies geometric
            //arithmetic to the corners to calculate the location of our desired rectangle for analysis
            calculateOurRectangle(qrCornerPoints);

            /**-----------------------------------------------------------------------**/
            /**-----------------------------------------------------------------------**/
            /**---------------------------ROTATION -----------------------------------**/
            /**-----------------------------------------------------------------------**/
            //calculate the slope of the qrcode in the original bitmap
            slope = getSlopeOfRectangle(qrCornerPoints);
            Bitmap rotatedBitmap = rotateBitmap(bitmap, slope);

            frameRotated = new Frame.Builder().setBitmap(rotatedBitmap).build();
            qrCornerPointsRotated = getQRCoordinates(barcodeDetector, frameRotated);

            /**---------------------------------------------------------------------------------**/
            /**---------------------------------------------------------------------------------**/
            /**---------------------WARP PERSPECTIVE TRANSFORMATION ----------------------------**/
            /**---------------------------------------------------------------------------------**/
            org.opencv.core.Point[] srcPointsForWarp = getQRCornerPointsWarp(qrCornerPointsRotated);
            org.opencv.core.Point[] referencePointsForWarp = getReferencePointsWarp(qrCornerPointsRotated);
            Bitmap warpedBitmap = performWarpPerspective(rotatedBitmap, srcPointsForWarp, referencePointsForWarp);
            Frame frameWarpPerspective = new Frame.Builder().setBitmap(warpedBitmap).build();
            Point[] qrCornerPointsWarpPerspective = getQRCoordinates(barcodeDetector, frameWarpPerspective);

            /**-----------------------------------------------------------------------**/
            /**-----------------------------------------------------------------------**/
            /**---------------------AFFINE TRANSFORMATION ----------------------------**/
            /**-----------------------------------------------------------------------**/
            org.opencv.core.Point[] sourcePoints = getQRCornerPoints(qrCornerPointsWarpPerspective);
            org.opencv.core.Point[] referencePoints = getReferencePoints(qrCornerPointsWarpPerspective);
            Bitmap affineTransformedBitmap = performAffineTransformation(warpedBitmap, sourcePoints, referencePoints);
            Frame frameAffine = new Frame.Builder().setBitmap(affineTransformedBitmap).build();
            Point[] qrCornerPointsAffine = getQRCoordinates(barcodeDetector, frameAffine);

            /**-----------------------------------------------------------------------**/
            /**-----------------------------------------------------------------------**/
            /**-----------------------ORIENTATION ------------------------------------**/
            /**-----------------------------------------------------------------------**/
            Bitmap rotatedAndOrientatedBitmap = orientateBitmap(affineTransformedBitmap, qrCornerPointsAffine);
            Frame frame2 = new Frame.Builder().setBitmap(rotatedAndOrientatedBitmap).build();
            Point[] qrCornerPoints2 = getQRCoordinates(barcodeDetector, frame2);

            /**-------------------------------------------------------------------------------------**/
            /**-------------------------------------------------------------------------------------**/
            /**-----------------------RECTANGLE SUPERIMPOSITION ------------------------------------**/
            /**-------------------------------------------------------------------------------------**/
            calculateOurRectangle(qrCornerPoints2);
            rect = new Rect(A1.x, A1.y, A2.x, A3.y);
            drawRectOnImage(imageView2, rect, rotatedAndOrientatedBitmap);

            /**-------------------------------------------------------------------------------------**/
            /**-------------------------------------------------------------------------------------**/
            /**-----------------------DRAWING EACH CIRCLE'S BITMAP ---------------------------------**/
            /**-------------------------------------------------------------------------------------**/
            //call the method to calculate the centres of the 4 circles to draw - calculated with relation to the rect
            getCirclesOnImage(rect, q);

            //call the averageRGBValues method for the circles on the bitmap
            overallRGBC1 = getAverageRGBValueFromBitmap(rotatedAndOrientatedBitmap, centreC1);
            overallRGBC2 = getAverageRGBValueFromBitmap(rotatedAndOrientatedBitmap, centreC2);
            overallRGBC3 = getAverageRGBValueFromBitmap(rotatedAndOrientatedBitmap, centreC3);
            overallRGBC4 = getAverageRGBValueFromBitmap(rotatedAndOrientatedBitmap, centreC4);

            /**---------------------------------------------------------------------------------**/
            /**---------------------------------------------------------------------------------**/
            /**-------------------AVERAGE RGB VALUES OF EACH CIRCLE ----------------------------**/
            /**---------------------------------------------------------------------------------**/
            overallRGBC1String = convertIntArrayRGBToString(overallRGBC1);
            overallRGBC2String = convertIntArrayRGBToString(overallRGBC2);
            overallRGBC3String = convertIntArrayRGBToString(overallRGBC3);
            overallRGBC4String = convertIntArrayRGBToString(overallRGBC4);
        }
        else{
            textView.setText("Make sure you've registered the QR Code..");
            Log.d("FEARGS CHECK", "Value of QR Registered: " + continueWithProcessing);
        }
    }


    /**
     * Method uses the proportional value calculated and the rectangle calculated
     * and creates the centre points of the 4 circles for analysis from it.
     * @param rect
     */
    private void getCirclesOnImage(Rect rect, double prop){
        centreC1 = new Point((int) (rect.left + (LCirc1 * prop)), rect.top + ((rect.bottom - rect.top) / 2));
        centreC2 = new Point((int) (centreC1.x + (LCirc2 * prop)), rect.top + ((rect.bottom - rect.top) / 2));
        centreC3 = new Point((int) (centreC2.x + (LCirc3 * prop)), rect.top + ((rect.bottom - rect.top) / 2));
        centreC4 = new Point((int) (centreC3.x + (LCirc4 * prop)), rect.top + ((rect.bottom - rect.top) / 2));
    }


    /**
     * This method takes in the qr corner points from the original image. It uses the top left corner
     * and the real world measurements for the QR code to calculate where the other corners 'should' be.
     * These corners are then used as the reference points for the affine transformation.
     * @param qrCornerPoints
     * @return
     */
    private org.opencv.core.Point[] getReferencePoints(Point[] qrCornerPoints){
        org.opencv.core.Point[] referencePoints = new org.opencv.core.Point[3];

        referencePoints[0] = new org.opencv.core.Point(qrCornerPoints[0].x, qrCornerPoints[0].y);
        referencePoints[1] = new org.opencv.core.Point((int) qrCornerPoints[0].x + (L1 * q), qrCornerPoints[0].y);
        referencePoints[2] = new org.opencv.core.Point(qrCornerPoints[0].x, (int) qrCornerPoints[0].y + (L1 * q));

        return referencePoints;
    }

    /**
     * Takes barcode as parameter. Extracts the top left, top right, bottom left corners of the barcode and
     * stores them in an array. These points will be the source points used for the Affine transformation
     * @param cornerPoints
     * @return
     */
    private org.opencv.core.Point[] getQRCornerPoints(Point[] cornerPoints){
        org.opencv.core.Point[] srcPoints = new org.opencv.core.Point[3];

        srcPoints[0] = new org.opencv.core.Point(cornerPoints[0].x, cornerPoints[0].y);
        srcPoints[1] = new org.opencv.core.Point(cornerPoints[1].x, cornerPoints[1].y);
        srcPoints[2] = new org.opencv.core.Point(cornerPoints[3].x, cornerPoints[3].y);

        return srcPoints;
    }

    /**
     * Pass the original bitmap (perhaps first rotated) and the source points, reference points, and the imageview where
     * you want to display the result.
     * @param bitmap
     * @param srcPoints
     * @param referencePoints
     */
    private Bitmap performAffineTransformation(Bitmap bitmap, org.opencv.core.Point[] srcPoints, org.opencv.core.Point[] referencePoints){
        Mat mat = new Mat();

        Bitmap bitmap1 = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        Utils.bitmapToMat(bitmap1, mat);

        //using the two point arrays do the transformation
        Mat warpMat = Imgproc.getAffineTransform(new MatOfPoint2f(srcPoints), new MatOfPoint2f(referencePoints));
        Mat warpDst = Mat.zeros(mat.rows(), mat.cols(), mat.type());

        Imgproc.warpAffine(mat, warpDst, warpMat, warpDst.size());

        //create new bitmap from our mat and then set it to the passed imageView
        Bitmap bmp = Bitmap.createBitmap(warpDst.cols(), warpDst.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(warpDst, bmp);

        return bmp;
    }

    /**
     * This method takes in the qr corner points from the original image. It uses the top left corner
     * and the real world measurements for the QR code to calculate where the other corners 'should' be.
     * These corners are then used as the reference points for the warp perspective transformation.
     * @param qrCornerPoints
     * @return
     */
    private org.opencv.core.Point[] getReferencePointsWarp(Point[] qrCornerPoints){
        org.opencv.core.Point[] referencePoints = new org.opencv.core.Point[4];

        referencePoints[0] = new org.opencv.core.Point(qrCornerPoints[0].x, qrCornerPoints[0].y);
        referencePoints[1] = new org.opencv.core.Point(qrCornerPoints[0].x + (L1 * q), qrCornerPoints[0].y);
        referencePoints[2] = new org.opencv.core.Point(qrCornerPoints[0].x + (L1 * q), qrCornerPoints[0].y + (L1 * q));
        referencePoints[3] = new org.opencv.core.Point(qrCornerPoints[0].x,qrCornerPoints[0].y + (L1 * q));

        return referencePoints;
    }

    /**
     *
     * @param cornerPoints
     * @return
     */
    private org.opencv.core.Point[] getQRCornerPointsWarp(Point[] cornerPoints){
        org.opencv.core.Point[] srcPoints = new org.opencv.core.Point[4];

        srcPoints[0] = new org.opencv.core.Point(cornerPoints[0].x, cornerPoints[0].y);
        srcPoints[1] = new org.opencv.core.Point(cornerPoints[1].x, cornerPoints[1].y);
        srcPoints[2] = new org.opencv.core.Point(cornerPoints[2].x, cornerPoints[2].y);
        srcPoints[3] = new org.opencv.core.Point(cornerPoints[3].x, cornerPoints[3].y);

        return srcPoints;
    }

    /**
     * Pass the affined bitmap and the source points, reference points, and the imageview where
     * you want to display the result.
     * @param bitmap
     * @param srcPoints
     * @param referencePoints
     * @return
     */
    private Bitmap performWarpPerspective(Bitmap bitmap, org.opencv.core.Point[] srcPoints, org.opencv.core.Point[] referencePoints){
        Mat mat = new Mat();

        Bitmap bitmap1 = bitmap.copy(Bitmap.Config.ARGB_8888, true);

        Utils.bitmapToMat(bitmap1, mat);

        MatOfPoint2f source = new MatOfPoint2f(srcPoints[0], srcPoints[1], srcPoints[2], srcPoints[3]);
        MatOfPoint2f dst = new MatOfPoint2f(referencePoints[0], referencePoints[1], referencePoints[2], referencePoints[3]);

        Mat warpMat = Imgproc.getPerspectiveTransform(source, dst);
        Mat destMat = new Mat();

        Imgproc.warpPerspective(mat, destMat, warpMat, mat.size());

        //create new bitmap from our mat and then set it to the passed imageView
        Bitmap bmp = Bitmap.createBitmap(destMat.cols(), destMat.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(destMat, bmp);

        return bmp;
    }

    /**
     * Creates a square around the passed point in the bitmap, iterates through
     * all pixels in the bitmap and returns the average RGB value for them.
     * @param bitmap
     */
    public int[] getAverageRGBValueFromBitmap(Bitmap bitmap, Point centre){
        int[] rgb = new int[3];
        int totalR = 0, totalG = 0, totalB = 0;
        int count = 0;

        int startPointx = centre.x - 5;
        int startPointy = centre.y - 5;
        int endPointx = centre.x + 5;
        int endPointy = centre.y + 5;

        for(int i = startPointx; i < endPointx; i++){
            for(int j = startPointy; j < endPointy; j++){
                if(Color.red(bitmap.getPixel(i, j)) > 0 && Color.green(bitmap.getPixel(i, j)) > 0 && Color.blue(bitmap.getPixel(i, j)) > 0){
                    totalR += Color.red(bitmap.getPixel(i, j));
                    totalG += Color.green(bitmap.getPixel(i, j));
                    totalB += Color.blue(bitmap.getPixel(i, j));
                    count += 1;
                }
            }
        }

        try {
            rgb[0] = totalR / count;
            rgb[1] = totalG / count;
            rgb[2] = totalB / count;
        }
        catch(ArithmeticException e){
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "Please retake the image", Toast.LENGTH_LONG).show();
        }

        return rgb;
    }

    /**
     * Converts an integer array of RGB components to an intelligible string
     * @param rgb
     * @return
     */
    public String convertIntArrayRGBToString(int[] rgb){
        int r = rgb[0];
        int g = rgb[1];
        int b = rgb[2];
        Log.d("FEARGS RGB STRING", "(" + r + ", " + g + ", " + b + ")");

        return "(" + r + ", " + g + ", " + b + ")";
    }


}
