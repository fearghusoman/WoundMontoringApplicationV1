package com.example.woundmontoringapplicationv1;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

public class ProcessAndAnalyseImageActivity extends AppCompatActivity {

    private static final String TAG = "FEARGS CHECK";

    //store our known real-life measurements for the distance between points on a dressing
    //distance between the top two corners of the qr code in cm
    //final static double L1 = 7.1;
    final static double L1 = 5.2;
    //distance between the top right corner of qr code and top left corner of the area to be
    //analysed for colour in cm
    final static double L2 = 2.2;
    //distance between the top two points of the rectangle in cm
    //final static double L3 = 15.5;
    final static double L3 = 16.2;

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

    //proportional value
    double Q = 0;

    //an array of colors to store the primary colors we allow in our analysis
    int[] colorsInt = {Color.WHITE, Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW, Color.BLACK};

    //Bundle for intent extras passed from last activity
    Bundle bundle;

    //QR Information String
    String qrInfoHolder;

    //Variables with regards to QR Codes
    Frame frame;
    SparseArray<Barcode> barcodeSparseArray;
    Barcode barcode;
    BarcodeDetector barcodeDetector;

    //point array to hold the corner points of the QR Code
    Point[] qrCornerPoints, qrCornerPoints1;
    Rect rect;

    //points for the centre coordinates of the 4 circles
    Point centreC1, centreC2, centreC3, centreC4;

    //hash maps for the colour results of each circle
    HashMap<String, Boolean> c1HashMap, c2HashMap, c3HashMap, c4HashMap;

    ProgressDialog progressDialog;
    static final String checkurl = "http://foman01.lampt.eeecs.qub.ac.uk/woundmonitoring/process_image.php";

    //empty strings to hold the timestamp and path to image variables
    String timestamp = "";
    String path = "";
    String LoggedInEmail;

    //buttons for processing image & submitting
    Button processImgBtn, submitAnalysisBtn;
    FloatingActionButton floatingActionButton;

    //ImageViews for the colour analysis
    ImageView imageView, imageViewC1, imageViewC2, imageViewC3, imageViewC4, imageViewDISTANCE;

    //double slope
    double slope;

    //Original bitmap, rotated bitmap, and the four circle bitmaps
    Bitmap bitmap, rotatedBitmap;
    Bitmap circle1, circle2, circle3, circle4;;

    TextView tvC1, tvC2, tvC3, tvC4;
    TextView textView;

    //Volley variables
    RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_process_and_analyse_image);

        LoggedInEmail = "johndoe@gmail.com";

        requestQueue = Volley.newRequestQueue(ProcessAndAnalyseImageActivity.this);
        progressDialog = new ProgressDialog(ProcessAndAnalyseImageActivity.this);

        //textView that holds QR Info
        textView = findViewById(R.id.txtContent);

        //buttons for processing and submitting
        processImgBtn = findViewById(R.id.button);
        submitAnalysisBtn = findViewById(R.id.buttonSubmit);

        progressDialog = new ProgressDialog(ProcessAndAnalyseImageActivity.this);

        //imageView for holding original bitmap image
        imageView = findViewById(R.id.imgview);

        //back floating button
        floatingActionButton = findViewById(R.id.backToTakeImage);

        //text views that will show each circles hashmap
        tvC1 = findViewById(R.id.textViewC1hm);
        tvC2 = findViewById(R.id.textViewC2hm);
        tvC3 = findViewById(R.id.textViewC3hm);
        tvC4 = findViewById(R.id.textViewC4hm);

        //imageviews that will show each circle converted to nearest primary colours
        imageViewDISTANCE = findViewById(R.id.imageViewDISTANCE);
        imageViewC1 = findViewById(R.id.imageViewC1);
        imageViewC2 = findViewById(R.id.imageViewC2);
        imageViewC3 = findViewById(R.id.imageViewC3);
        imageViewC4 = findViewById(R.id.imageViewC4);

        bundle = getIntent().getExtras();

        //if a bundle of information has been passed then continue with analysis
        if (bundle != null) {
            path = bundle.get("imageName").toString();
            timestamp = bundle.get("timestamp").toString();

            try {
                FileInputStream fIS = new FileInputStream(new File(path));

                bitmap = BitmapFactory.decodeStream(fIS);
                imageView.setImageBitmap(bitmap);
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

        //Once the image is passed we can click this button to begin the analysis
        processImgBtn.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public void onClick(View v) {

                progressDialog.setMessage("Please wait...");
                progressDialog.show();

                barcodeDetector = new BarcodeDetector.Builder(getApplicationContext()).setBarcodeFormats(Barcode.QR_CODE).build();

                //if the barcode detector has been setup correctly then continue with analysis
                if (!barcodeDetector.isOperational()) {
                    textView.setText("Couldn't setup the detector1");
                    return;
                } else {
                    frame = new Frame.Builder().setBitmap(bitmap).build();

                    barcodeSparseArray = barcodeDetector.detect(frame);

                    //if there is at least one barcode present in the image
                    if (barcodeSparseArray.size() > 0) {
                        barcode = barcodeSparseArray.valueAt(0);
                        qrInfoHolder = barcode.rawValue;

                        //check the qr is registered with the current user
                        checkQRRegisteredWithCurrentUser();
                    }
                    //if there is no barcode present in the image
                    else {
                        textView.setText("There was no QR code detected in the image. Please try again..");
                        progressDialog.dismiss();
                    }
                }
            }


        });
    }

    /**
     * This method sends a Volley StringRequest to check whether the QR Code in the captured image has been
     * registered with the logged in user.
     * If it is then the boolean continueWithProcessing is set to true
     * SInce we have to wait for onResponse from the server, all further method are called from within onResponse
     **/
    private void checkQRRegisteredWithCurrentUser(){

        StringRequest stringRequest = new StringRequest(Request.Method.POST, checkurl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                Boolean continueWithProcessing1 = false;

                if (response.equalsIgnoreCase("QR Code is registered with this user")) {
                    continueWithProcessing1 = true;
                    Log.d("FEARGS CHECK", "The QR is registered with the right user");
                }

                else if (response.equalsIgnoreCase("QR Code is not yet registered with this user")) {
                    Log.d("FEARGS CHECK", "The QR is NOT registered with the right user");
                    Toast.makeText(ProcessAndAnalyseImageActivity.this, "You must first register this dressing before you can begin analysis!", Toast.LENGTH_LONG).show();
                }

                else {
                    Log.d("FEARGS CHECK", "PURE SHITE");
                    Toast.makeText(ProcessAndAnalyseImageActivity.this, "Something has gone wrong!", Toast.LENGTH_LONG).show();
                }

                //if the qr code is registered with our user then continue analysis
                if(continueWithProcessing1 == true){
                    //call next method
                    //call the QR corner points method
                    qrCornerPoints = getQRCornerPoints(barcode);

                    //calculate the slope of the qr code using original qrcornerpoints
                    slope = getSlopeOfRectangle(qrCornerPoints);

                    //call the method to calculate the proportional value Q
                    Q = calculateProportion(qrCornerPoints);

                    //call the method to get the coordinates of the rectangle to draw
                    rect = getRectangleOnImage(qrCornerPoints, Q);

                    //rotate the bitmap using orig bitmap, slope, and rect - set it to an image view
                    rotatedBitmap = createNewBitmapRotateAndClosestColorConversion(bitmap, rect, slope, imageViewDISTANCE);

                    frame = new Frame.Builder().setBitmap(rotatedBitmap).build();
                    barcodeSparseArray = barcodeDetector.detect(frame);

                    //if there is at least one barcode present in the image
                    if (barcodeSparseArray.size() > 0) {
                        barcode = barcodeSparseArray.valueAt(0);
                        qrInfoHolder = barcode.rawValue;

                        //override value of corner points to new corner points of rotated bitmap
                        qrCornerPoints = getQRCornerPoints(barcode);

                        //call the method to calculate the proportional value Q
                        Q = calculateProportion(qrCornerPoints);

                        //call the method to get the coordinates of the rectangle to draw
                        rect = getRectangleOnImage(qrCornerPoints, Q);

                        //rotate the bitmap using orig bitmap, slope, and rect - set it to an image view
                        rotatedBitmap = createNewBitmapRotateAndClosestColorConversion(bitmap, rect, slope, imageViewDISTANCE);


                    }
                    //if there is no barcode present in the image
                    else {
                        textView.setText("There was no QR code detected in the image. Please try again..");
                        progressDialog.dismiss();
                    }


                    //call the method to calculate the centres of the 4 circles to draw - calculated with
                    //relation to the rect
                    getCirclesOnImage(rect, Q);

                    //method to draw the first circle as its own bitmap
                    getBitmapClippedCirclePath(rotatedBitmap, centreC1,radius1 * Q, imageViewC1);
                    getBitmapClippedCirclePath(rotatedBitmap, centreC2,radius2 * Q, imageViewC2);
                    getBitmapClippedCirclePath(rotatedBitmap, centreC3,radius3 * Q, imageViewC3);
                    getBitmapClippedCirclePath(rotatedBitmap, centreC4,radius4 * Q, imageViewC4);

                    BitmapDrawable bitmapDrawableC1 = (BitmapDrawable) imageViewC1.getDrawable();
                    BitmapDrawable bitmapDrawableC2 = (BitmapDrawable) imageViewC2.getDrawable();
                    BitmapDrawable bitmapDrawableC3 = (BitmapDrawable) imageViewC3.getDrawable();
                    BitmapDrawable bitmapDrawableC4 = (BitmapDrawable) imageViewC4.getDrawable();

                    circle1 = bitmapDrawableC1.getBitmap();
                    circle2 = bitmapDrawableC2.getBitmap();
                    circle3 = bitmapDrawableC3.getBitmap();
                    circle4 = bitmapDrawableC4.getBitmap();

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

                }
            }
        },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressDialog.dismiss();
                        Toast.makeText(ProcessAndAnalyseImageActivity.this, "Response Error: " + error.toString(), Toast.LENGTH_LONG).show();
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
     * takes two integer colours as parameters and calculates the Euclidean distance between them
     * Note: this is not the correct/most accurate way to calculate the distance between colours
     * use CIELAB delta E
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
     * takes an integer color and the array of possible colors as parameters
     * and uses the euclideansitance formula to find the color's closest match from
     * the array
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
     *  takes the original bitmap, the rect for analysis, the slope
     *  rotates the bitmap by the angle defined by the slope, converts the pixels in the rectangle to their
     *  closest desired color and outputs to the passed imageview
     * @param bitmap
     * @param rect
     * @param slope
     */
    private Bitmap createNewBitmapRotateAndClosestColorConversion(Bitmap bitmap, Rect rect, double slope, ImageView imageView){
        Matrix rotationMatrix = new Matrix();
        rotationMatrix.postRotate((float) (Math.abs(slope)));

        Bitmap newBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), rotationMatrix, false);

        int c;

        for(int i = rect.left; i <= rect.right; i++){
            for(int j = rect.top; j <= rect.bottom; j++) {
                c = checkClosestColorINRGBSpace(newBitmap.getPixel(i, j), colorsInt);
                newBitmap.setPixel(i, j, c);
            }
        }

        imageView.setImageBitmap(newBitmap);
        return newBitmap;
    }

    /**
     * returns an array of points that represent the corner points of the barcode
     * @param barcode
     * @return
     */
    private Point[] getQRCornerPoints(Barcode barcode){
        return barcode.cornerPoints;
    }

    /**
     * uses the barcode from the captured image to calculate the proportional value
     * to be applied to all measurements
     * @param qrCornerPoints
     * @return
     */
    private double calculateProportion(Point[] qrCornerPoints){
        double prop = (qrCornerPoints[1].x - qrCornerPoints[0].x) / L1;

        return prop;
    }

    /**
     * returns the rectangle that represents the area of the dressing to be analysed
     * @param qrCornerPoints
     * @return
     */
    private Rect getRectangleOnImage(Point[] qrCornerPoints, double prop){
        Rect rect = new Rect((int) (qrCornerPoints[1].x + (L2 * prop)),
                qrCornerPoints[1].y,
                (int) (qrCornerPoints[1].x + (L2 * prop) + (L3 * prop)),
                qrCornerPoints[2].y);

        return rect;
    }

    /**
     * calculates the centre points of each of the circles for analysis on the image
     * @param rect
     */
    private void getCirclesOnImage(Rect rect, double prop){

        centreC1 = new Point((int) (rect.left + (LCirc1 * prop)), rect.top + ((rect.bottom - rect.top) / 2));
        centreC2 = new Point((int) (centreC1.x + (LCirc2 * prop)), rect.top + ((rect.bottom - rect.top) / 2));
        centreC3 = new Point((int) (centreC2.x + (LCirc3 * prop)), rect.top + ((rect.bottom - rect.top) / 2));
        centreC4 = new Point((int) (centreC3.x + (LCirc4 * prop)), rect.top + ((rect.bottom - rect.top) / 2));

    }

    /**
     * Uses centre of circle and radius to extract the circle from the original rotated bitmap and put into
     * a new image view - can then be used for analysis
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
     * Pass the bitmap with one of the circles in it, and returns a hash map containing
     * all the desired colors present in the circle
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

}
