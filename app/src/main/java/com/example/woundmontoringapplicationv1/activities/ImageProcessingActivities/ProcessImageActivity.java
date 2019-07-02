package com.example.woundmontoringapplicationv1.activities.ImageProcessingActivities;

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
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;

import com.example.woundmontoringapplicationv1.Dressing;
import com.example.woundmontoringapplicationv1.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.palette.graphics.Palette;
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

import static com.example.woundmontoringapplicationv1.activities.LoginAndRegisterActivities.LoginActivity.SHARED_PREFS;

/**
 *
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

    static final String checkurl = "http://foman01.lampt.eeecs.qub.ac.uk/woundmonitoring/process_image.php";

    /**-----------------------------------------------------------------------**/
    /**---------------------INSTANCE VARiABLES -------------------------------**/
    /**-----------------------------------------------------------------------**/
    Barcode thisBarCode;
    BarcodeDetector barcodeDetector;

    Bitmap bitmap;
    Bitmap circle1, circle2, circle3, circle4;

    boolean continueWithProcessing = false;

    Bundle bundle;

    Button processImgBtn, submitAnalysisBtn, opencvBtn;

    //CIE LAB conversion
    ColorSpace.Connector connector, connector2;

    double slope;
    double q;

    Dressing dressing;

    FloatingActionButton floatingActionButton;
    Frame frame, frameRotated;

    //hash maps for the colour results of each circle
    HashMap<String, Boolean> c1HashMap, c2HashMap, c3HashMap, c4HashMap;

    ImageView imageViewOriginal, imageView2, imgviewDrawCirclesOnCorners1, imgviewDrawCirclesOnCorners2, imgviewDrawCirclesOnCorners3, imgviewDrawCirclesOnCorners4;
    ImageView imageViewC1, imageViewC2, imageViewC3, imageViewC4, imgviewWarpedAndAffineTransformation, imgviewWarpedTransformation;

    int orientation;
    //variables to hold the calculated four corners of the rectangle to be analysed
    int x1, y1, x2, y2, x3, y3, x4, y4;
    int l1, l2, l3;
    int a, r, g, b;
    int circ1x, circ1y, circ2x, circ2y, circ3x, circ3y, circ4x, circ4y;
    int circle1Color = Color.BLUE, circle2Color = Color.RED, circle3Color = Color.GREEN, circle4Color = Color.YELLOW;
    int overallRGBC1, overallRGBC2, overallRGBC3, overallRGBC4;

    int[] colorsInt = {Color.WHITE, Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW};
    int[] colorsByCircle = {circle1Color, circle2Color, circle3Color, circle4Color};

    Point[] qrCornerPoints, qrCornerPointsRotated;
    Point centreC1, centreC2, centreC3, centreC4;
    Point A1, A2, A3, A4;
    //coordinates for circles
    Point circ1, circ2, circ3, circ4;

    ProgressDialog progressDialog;

    Rect rect;
    RequestQueue requestQueue;

    SparseArray<Barcode> barcodes;

    String timestamp = "";
    String path = "";
    String LoggedInEmail, qrInfoHolder;
    String overallRGBC1String, overallRGBC2String, overallRGBC3String, overallRGBC4String;

    TextView textView, textView5, textView6;
    TextView tvC1, tvC2, tvC3, tvC4;

    View view3, view4, view5;

    /**-----------------------------------------------------------------------**/
    /**---------------------BASE LOADER CALLBACK -----------------------------**/
    /**-----------------------------------------------------------------------**/

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

        OpenCVLoader.initDebug();

        //create an instance of our dressing
        dressing = new Dressing(L1,L2, L3, L_QR, LCirc1, LCirc2, LCirc3, LCirc4, radius1, radius2, radius3, radius4);

        processImgBtn = findViewById(R.id.button);
        submitAnalysisBtn = findViewById(R.id.buttonSubmit);
        floatingActionButton = findViewById(R.id.backToTakeImage);
        opencvBtn = findViewById(R.id.buttonOPENCVANALYSIS);

        progressDialog = new ProgressDialog(ProcessImageActivity.this);

        textView = findViewById(R.id.txtContent) ;
        textView5 = findViewById(R.id.txtContent5) ;
        textView6 = findViewById(R.id.txtContent6) ;

        //text view for the colors in the four circles
        tvC1 = findViewById(R.id.textViewC1hm);
        tvC2 = findViewById(R.id.textViewC2hm);
        tvC3 = findViewById(R.id.textViewC3hm);
        tvC4 = findViewById(R.id.textViewC4hm);

        view3 =  findViewById(R.id.view3);
        view4 =  findViewById(R.id.view4);
        view5 =  findViewById(R.id.view5);

        //image views for the subsets that are the 4 circles
        imageViewC1 = findViewById(R.id.imageViewC1);
        imageViewC2 = findViewById(R.id.imageViewC2);
        imageViewC3 = findViewById(R.id.imageViewC3);
        imageViewC4 = findViewById(R.id.imageViewC4);

        imageView2 = findViewById(R.id.imgview2);
        imageViewOriginal = findViewById(R.id.imgViewOriginal);
        imgviewDrawCirclesOnCorners1 = findViewById(R.id.imgviewDrawCirclesOnCorners1);
        imgviewDrawCirclesOnCorners2 = findViewById(R.id.imgviewDrawCirclesOnCorners2);
        imgviewDrawCirclesOnCorners3 = findViewById(R.id.imgviewDrawCirclesOnCorners3);
        imgviewDrawCirclesOnCorners4 = findViewById(R.id.imgviewDrawCirclesOnCorners4);
        imgviewWarpedAndAffineTransformation = findViewById(R.id.imgviewWarpedAndAffineTransformation);;
        imgviewWarpedTransformation = findViewById(R.id.imgviewAffineTransformation);;

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

                bitmap = BitmapFactory.decodeStream(fIS);
                Log.d("FEARGS BITMAP", "Bitmap:" + bitmap.getHeight() + " " + bitmap.getWidth());
                imageViewOriginal.setImageBitmap(bitmap);
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

                barcodeDetector = new BarcodeDetector.Builder(getApplicationContext()).setBarcodeFormats(Barcode.QR_CODE).build();

                if (!barcodeDetector.isOperational()) {
                    textView.setText("Couldn't setup the detector1");
                    return;
                } else {
                    frame = new Frame.Builder().setBitmap(bitmap).build();

                    barcodes = barcodeDetector.detect(frame);

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
    public void onResume()
    {
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
    private Bitmap rotateBitmap(Bitmap bitmap, double slope, Barcode barcode){

        Matrix rotationMatrix = new Matrix();
        rotationMatrix.postRotate((float) (Math.abs(slope)));

        Bitmap rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), rotationMatrix, false);

        return rotated;
    }

    /**
     *
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
     *
     * @param bitmap
     * @param points
     * @return
     */
    private int getOrientationOfBitmap(Bitmap bitmap, Point[] points){
        int ORIENTATION = 0;

        boolean corner1 = setupArrayOfPoints(0, points[0], bitmap, imgviewDrawCirclesOnCorners1);
        boolean corner2 = setupArrayOfPoints(1, points[1], bitmap, imgviewDrawCirclesOnCorners2);
        boolean corner3 = setupArrayOfPoints(2, points[2], bitmap, imgviewDrawCirclesOnCorners3);
        boolean corner4 = setupArrayOfPoints(3, points[3], bitmap, imgviewDrawCirclesOnCorners4);

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
     * world length of the side of the squares, check that a line drawn on both direcitons passes
     * only black pixels
     * @param corner
     * @return
     */
    private boolean setupArrayOfPoints(int corner, Point point, Bitmap bitmap, ImageView imageView){

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

        imageView.setImageBitmap(bitmap1);

        Log.d("FEARGS CORNER", "WE ARE NOW CHECKING CORNER " + corner);

        boolean dataCorner = closerToBlack(points, bitmap);

        if(dataCorner == true){
            orientationCorner = false;
        }
        else{
            orientationCorner = true;
        }

        return orientationCorner;
    }

    /**
     *
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
     *
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
     *
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

                //check the bitmap for red, green, blue
                //checkBitmapForRGBValues(bitmap);

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

                        intent.putExtra("Circle1", c1HashMap.toString());
                        intent.putExtra("Circle2", c2HashMap.toString());
                        intent.putExtra("Circle3", c3HashMap.toString());
                        intent.putExtra("Circle4", c4HashMap.toString());

                        intent.putExtra("rgbC1", overallRGBC1String);
                        intent.putExtra("rgbC2", overallRGBC2String);
                        intent.putExtra("rgbC3", overallRGBC3String);
                        intent.putExtra("rgbC4", overallRGBC4String);

                        startActivity(intent);
                    }
                });

                /**
                opencvBtn.setVisibility(View.VISIBLE);
                opencvBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent openIntent = new Intent(getApplicationContext(), DressingCirclesColouranalysisActivity.class);
                        openIntent.putExtra("imageName", path);
                        openIntent.putExtra("rectangleForAnalysis", rect);
                        openIntent.putExtra("slope", slope);
                        openIntent.putExtra("orientation", orientation);
                        startActivity(openIntent);
                    }
                });
                 **/
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

            textView5.setVisibility(View.VISIBLE);
            textView6.setVisibility(View.VISIBLE);
            view3.setVisibility(View.VISIBLE);
            view4.setVisibility(View.VISIBLE);
            view5.setVisibility(View.VISIBLE);

            //now, let's begin analysing the geometric properties of our qr code
            //within our image
            qrCornerPoints = getQRCoordinates(barcodeDetector, frame);
            String corners = "Original QR Code coordinates: ";

            for (int i = 0; i < 4; i++) {
                corners = corners + i + ": (" + qrCornerPoints[i].x + ", " + qrCornerPoints[i].y + ")  ";
            }
            textView5.setText(corners);

            //let's set up our rectangle this method takes the qr codes coordinates as input and applies geometric
            //arithmetic to the corners to calculate the location of our desired rectangle for analysis
            calculateOurRectangle(qrCornerPoints);

            textView6.setText("Original Rectangle coordinates: A1: (" + A1.x + ", " + A1.y + "), " + "A2: (" + A2.x + ", " + A2.y + "), " +
                    "A3: (" + A3.x + ", " + A3.y + "), " + "A4: (" + A4.x + ", " + A4.y + ")");

            /**-----------------------------------------------------------------------**/
            /**-----------------------------------------------------------------------**/
            /**---------------------------ROTATION -----------------------------------**/
            /**-----------------------------------------------------------------------**/
            //calculate the slope of the qrcode in the original bitmap
            slope = getSlopeOfRectangle(qrCornerPoints);
            Bitmap rotatedBitmap = rotateBitmap(bitmap, slope, thisBarCode);

            frameRotated = new Frame.Builder().setBitmap(rotatedBitmap).build();
            qrCornerPointsRotated = getQRCoordinates(barcodeDetector, frameRotated);

            /**---------------------------------------------------------------------------------**/
            /**---------------------------------------------------------------------------------**/
            /**---------------------WARP PERSPECTIVE TRANSFORMATION ----------------------------**/
            /**---------------------------------------------------------------------------------**/
            org.opencv.core.Point[] srcPointsForWarp = getQRCornerPointsWarp(qrCornerPointsRotated);
            org.opencv.core.Point[] referencePointsForWarp = getReferencePointsWarp(qrCornerPointsRotated);
            Bitmap warpedBitmap = performWarpPerspective(rotatedBitmap, srcPointsForWarp, referencePointsForWarp, imgviewWarpedTransformation);

            Frame frameWarpPerspective = new Frame.Builder().setBitmap(warpedBitmap).build();
            Point[] qrCornerPointsWarpPerspective = getQRCoordinates(barcodeDetector, frameWarpPerspective);

            /**-----------------------------------------------------------------------**/
            /**-----------------------------------------------------------------------**/
            /**---------------------AFFINE TRANSFORMATION ----------------------------**/
            /**-----------------------------------------------------------------------**/
            org.opencv.core.Point[] sourcePoints = getQRCornerPoints(qrCornerPointsWarpPerspective);
            org.opencv.core.Point[] referencePoints = getReferencePoints(qrCornerPointsWarpPerspective);
            Bitmap affineTransformedBitmap = performAffineTransformation(warpedBitmap, sourcePoints, referencePoints, imgviewWarpedAndAffineTransformation);

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
            Log.d("FEARGS RECT", "RECT: left x:" + rect.left + ", top y:" + rect.top + ", right x: " + rect.right + ", bottom y: " + rect.bottom);
            calculateOurCircles(rect, q);

            drawRectOnImage(imageView2, rect, rotatedAndOrientatedBitmap);

            /**-------------------------------------------------------------------------------------**/
            /**-------------------------------------------------------------------------------------**/
            /**---------------------PIXEL CONVERSION TO NEAREST PRIMARY COLOR ----------------------**/
            /**-------------------------------------------------------------------------------------**/
            Bitmap rotatedAndOrientatedAndConvertedBitmap = createNewBitmapRotateAndClosestColorConversion(rotatedAndOrientatedBitmap, rect, slope, orientation);

            /**-------------------------------------------------------------------------------------**/
            /**-------------------------------------------------------------------------------------**/
            /**-----------------------DRAWING EACH CIRCLE'S BITMAP ---------------------------------**/
            /**-------------------------------------------------------------------------------------**/
            //call the method to calculate the centres of the 4 circles to draw - calculated with relation to the rect
            getCirclesOnImage(rect, q);

            //method to draw the first circle as its own bitmap
            getBitmapClippedCirclePath(rotatedAndOrientatedAndConvertedBitmap, centreC1,radius1 * q, imageViewC1);
            getBitmapClippedCirclePath(rotatedAndOrientatedAndConvertedBitmap, centreC2,radius2 * q, imageViewC2);
            getBitmapClippedCirclePath(rotatedAndOrientatedAndConvertedBitmap, centreC3,radius3 * q, imageViewC3);
            getBitmapClippedCirclePath(rotatedAndOrientatedAndConvertedBitmap, centreC4,radius4 * q, imageViewC4);

            BitmapDrawable bitmapDrawableC1 = (BitmapDrawable) imageViewC1.getDrawable();
            BitmapDrawable bitmapDrawableC2 = (BitmapDrawable) imageViewC2.getDrawable();
            BitmapDrawable bitmapDrawableC3 = (BitmapDrawable) imageViewC3.getDrawable();
            BitmapDrawable bitmapDrawableC4 = (BitmapDrawable) imageViewC4.getDrawable();

            circle1 = bitmapDrawableC1.getBitmap();
            circle2 = bitmapDrawableC2.getBitmap();
            circle3 = bitmapDrawableC3.getBitmap();
            circle4 = bitmapDrawableC4.getBitmap();

            /**-------------------------------------------------------------------------------------**/
            /**-------------------------------------------------------------------------------------**/
            /**-------------------PRIMARY COLOR ANALYSIS OF EACH CIRCLE ----------------------------**/
            /**-------------------------------------------------------------------------------------**/
            c1HashMap = getColoursInCircle(circle1);
            c2HashMap = getColoursInCircle(circle2);
            c3HashMap = getColoursInCircle(circle3);
            c4HashMap = getColoursInCircle(circle4);

            Log.d("FEARGS HASH", "C1: " + c1HashMap.toString());
            Log.d("FEARGS HASH", "C2: " + c2HashMap.toString());
            Log.d("FEARGS HASH", "C3: " + c3HashMap.toString());
            Log.d("FEARGS HASH", "C4: " + c4HashMap.toString());

            tvC1.setText(c1HashMap.toString());
            tvC2.setText(c2HashMap.toString());
            tvC3.setText(c3HashMap.toString());
            tvC4.setText(c4HashMap.toString());

            /**---------------------------------------------------------------------------------**/
            /**---------------------------------------------------------------------------------**/
            /**-------------------AVERAGE RGB VALUES OF EACH CIRCLE ----------------------------**/
            /**---------------------------------------------------------------------------------**/
            //overallRGBC1 = getOverallRGBFromCircle(circle1);
            //overallRGBC2 = getOverallRGBFromCircle(circle2);
            //overallRGBC3 = getOverallRGBFromCircle(circle3);
            //overallRGBC4 = getOverallRGBFromCircle(circle4);

            overallRGBC1 = getDominantColourFromCircle(circle1);
            overallRGBC2 = getDominantColourFromCircle(circle2);
            overallRGBC3 = getDominantColourFromCircle(circle3);
            overallRGBC4 = getDominantColourFromCircle(circle4);

            overallRGBC1String = convertIntRGBToString(overallRGBC1);
            overallRGBC2String = convertIntRGBToString(overallRGBC2);
            overallRGBC3String = convertIntRGBToString(overallRGBC3);
            overallRGBC4String = convertIntRGBToString(overallRGBC4);

        }
        else{
            textView.setText("Make sure you've registered the QR Code..");
            Log.d("FEARGS CHECK", "Value of QR Registered: " + continueWithProcessing);
        }
    }

    /**
     *
     * @param rgb
     * @return
     */
    private String convertIntRGBToString(int rgb){
        int r = Color.red(rgb);
        int g = Color.green(rgb);
        int b = Color.blue(rgb);

        return "(" + r + ", " + g + ", " + b + ")";
    }

    /**
     * @param color
     * @param colorCheck
     * @return
     */
    private double euclideanDistanceBetweenRGBs(int color, int colorCheck){
        double distance;

        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);

        int rX = Color.red(colorCheck);
        int gX = Color.green(colorCheck);
        int bX = Color.blue(colorCheck);

        distance = Math.sqrt(Math.pow((r - rX), 2) + Math.pow((g - gX), 2) + Math.pow((b - bX), 2));

        return distance;
    }

    /**
     * Check closest color to each pixel using Euclidean distance within the RGB color space
     * @param color
     * @param colorsInt
     * @return
     */
    private int checkClosestColorINRGBSpace(int color, int[] colorsInt){
        int closestColor = Color.WHITE;
        double distance = 10000000000.0;

        for(int i : colorsInt){
            if(euclideanDistanceBetweenRGBs(color, i) < distance){
                closestColor = i;
                distance = euclideanDistanceBetweenRGBs(color, i);
            }
        }
        return closestColor;
    }

    /**
     *
     * @param bitmap
     * @param rect
     * @param slope
     */
    private Bitmap createNewBitmapRotateAndClosestColorConversion(Bitmap bitmap, Rect rect, double slope, int orientation){
        //Matrix rotationMatrix = new Matrix();
        //rotationMatrix.postRotate((float) (Math.abs(slope) + orientation));

        //Bitmap newBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), rotationMatrix, false);
        Bitmap newBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);

        int c;

        //the try-catch here is in case the rotation has not been done properly
        try{
            for(int i = rect.left; i <= rect.right; i++){
                for(int j = rect.top; j <= rect.bottom; j++) {
                    //Log.d("FEARGS LOOP", "createNEwBitmapRotate: (" + i + ", " + j + ")");
                    //use Euclidean within RGB color space
                    c = checkClosestColorINRGBSpace(newBitmap.getPixel(i, j), colorsInt);

                    //use Delta E distance in CIELab color space
                    //c = checkClosestColorInCIELABSpace(newBitmap.getPixel(i, j), colorsInt);

                    newBitmap.setPixel(i, j, c);
                }
            }
        }
        catch(IllegalArgumentException e){
            e.printStackTrace();
            Log.d("FEARGS CATCH", "The color check for the rectangle of the rotate bitmap is going off the edge of the bitmap.");
            Toast.makeText(ProcessImageActivity.this, "The image is not clear enough for precise analysis..", Toast.LENGTH_LONG).show();
        }

        return newBitmap;
    }

    /**
     *
     * @param rect
     */
    private void getCirclesOnImage(Rect rect, double prop){

        centreC1 = new Point((int) (rect.left + (LCirc1 * prop)), rect.top + ((rect.bottom - rect.top) / 2));
        centreC2 = new Point((int) (centreC1.x + (LCirc2 * prop)), rect.top + ((rect.bottom - rect.top) / 2));
        centreC3 = new Point((int) (centreC2.x + (LCirc3 * prop)), rect.top + ((rect.bottom - rect.top) / 2));
        centreC4 = new Point((int) (centreC3.x + (LCirc4 * prop)), rect.top + ((rect.bottom - rect.top) / 2));

    }

    /**
     *
     * @param bitmap
     * @param radius
     * @param imageView
     */
    private void getBitmapClippedCirclePath(Bitmap bitmap, Point centreOfCircle, double radius, ImageView imageView){

        Bitmap circleBitmap;
        circleBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);

        Path path = new Path();
        path.addCircle(
                (float) (centreOfCircle.x),
                (float) (centreOfCircle.y),
                (float) (radius),
                Path.Direction.CCW
        );

        Canvas canvas = new Canvas(circleBitmap);
        canvas.clipPath(path);
        canvas.drawBitmap(bitmap, 0, 0, null);
        imageView.setImageBitmap(circleBitmap);

    }

    /**
     *
     * @param bitmap
     * @return
     */
    private HashMap<String, Boolean> getColoursInCircle(Bitmap bitmap){

        HashMap<String, Boolean> stringBooleanHashMap = new HashMap<>();

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        int redCount = 0;
        int greenCount = 0;;
        int blueCount = 0;
        int yellowCount = 0;

        for(int i = 0; i < width; i++){
            for(int j = 0; j < height; j++){

                switch(bitmap.getPixel(i, j)){

                    case Color.RED:
                        redCount += 1;

                        if(redCount >= 50){
                            if(!stringBooleanHashMap.containsKey("RED")){
                                stringBooleanHashMap.put("RED", true);
                            }
                        }
                        break;
                    case Color.GREEN:
                        greenCount += 1;

                        if(greenCount >= 50) {
                            if (!stringBooleanHashMap.containsKey("GREEN")) {
                                stringBooleanHashMap.put("GREEN", true);
                            }
                        }
                        break;
                    case Color.BLUE:
                        blueCount += 1;

                        if(blueCount >= 50) {
                            if (!stringBooleanHashMap.containsKey("BLUE")) {
                                stringBooleanHashMap.put("BLUE", true);
                            }
                        }
                        break;
                    case Color.YELLOW:
                        yellowCount += 1;

                        if(yellowCount >= 50) {
                            if (!stringBooleanHashMap.containsKey("YELLOW")) {
                                stringBooleanHashMap.put("YELLOW", true);
                            }
                        }
                        break;

                }
            }
        }
        return stringBooleanHashMap;
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
     * @param imageView
     */
    private Bitmap performAffineTransformation(Bitmap bitmap, org.opencv.core.Point[] srcPoints, org.opencv.core.Point[] referencePoints, ImageView imageView){
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
        imageView.setImageBitmap(bmp);

        return bmp;
    }

    /**
     *
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
     *
     * @param bitmap
     * @param srcPoints
     * @param referencePoints
     * @param imageView
     * @return
     */
    private Bitmap performWarpPerspective(Bitmap bitmap, org.opencv.core.Point[] srcPoints, org.opencv.core.Point[] referencePoints, ImageView imageView){
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
        imageView.setImageBitmap(bmp);

        return bmp;
    }

    /**
     *
     * @param bitmap
     * @return
     */
    private int getOverallRGBFromCircle(Bitmap bitmap){
        int overallRGB;

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        int rgb;
        int r = 0, g = 0, b = 0, count = 0;

        for(int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {

                rgb = bitmap.getPixel(i, j);

                if(Color.red(rgb) > 0 && Color.blue(rgb) > 0 && Color.green(rgb) > 0){
                    Log.d("FEARGS R", "" + Color.red(rgb));
                    Log.d("FEARGS G", "" + Color.green(rgb));
                    Log.d("FEARGS B", "" + Color.blue(rgb));
                }

                r += Color.red(rgb);
                g += Color.green(rgb);
                b += Color.blue(rgb);

                count += 1;

            }
        }

        Log.d("FEARGS RGB", "R1: " + r);
        Log.d("FEARGS RGB", "G1: " + g);
        Log.d("FEARGS RGB", "B1: " + b);

        Log.d("FEARGS RGB", "COUNT: " + count);

        r = r / count;
        g = g / count;
        b = b / count;

        Log.d("FEARGS RGB", "R2: " + r);
        Log.d("FEARGS RGB", "G2: " + g);
        Log.d("FEARGS RGB", "B2: " + b);

        overallRGB = r;
        overallRGB = (overallRGB << 8) + g;
        overallRGB = (overallRGB << 8) + b;

        Log.d("FEARGS RGB", "OverallRGB: " + overallRGB);

        return overallRGB;
    }

    /**
     *
     * @param bitmap
     * @return
     */
    private int getDominantColourFromCircle(Bitmap bitmap){
        int dominantColor = 0;

        Palette palette = Palette.from(bitmap).generate();

        dominantColor = palette.getDominantColor(0);

        Log.d("FEARG DOMINANT", "Dominant Color: " + dominantColor);

        return dominantColor;
    }
}
