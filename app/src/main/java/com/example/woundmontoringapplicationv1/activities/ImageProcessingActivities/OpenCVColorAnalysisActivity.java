package com.example.woundmontoringapplicationv1.activities.ImageProcessingActivities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.woundmontoringapplicationv1.R;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;

public class OpenCVColorAnalysisActivity extends AppCompatActivity {

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

    double Q = 0;

    //Variables with regards to QR Codes
    Frame frame;
    SparseArray<Barcode> barcodeSparseArray;
    Barcode barcode;
    BarcodeDetector barcodeDetector;

    //point array to hold the corner points of the QR Code
    Point[] qrCornerPoints;
    Rect rect;
    Point centreC1, centreC2, centreC3, centreC4;

    TextView textViewRED, textViewGREEN, textViewBLUE, textViewYELLOW, textViewDISTANCE, tvC1, tvC2, tvC3, tvC4;
    ImageView imageViewRED, imageViewGREEN, imageViewBLUE, imageViewYELLOW, imageViewDISTANCE, imageViewC1, imageViewC2, imageViewC3, imageViewC4;

    //an array of colors to store the primary colors we allow in our analysis
    //Color[] colors = {Color.valueOf(Color.WHITE), Color.valueOf(Color.RED), Color.valueOf(Color.GREEN), Color.valueOf(Color.BLUE), Color.valueOf(Color.YELLOW)};
    int[] colorsInt = {Color.WHITE, Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW, Color.BLACK};

    Bitmap bitmap, rotatedBitmap;
    Bitmap circle1, circle2, circle3, circle4;

    Bundle bundle;

    Scalar scalarREDLower = new Scalar(120,50, 50);
    Scalar scalarREDUpper = new Scalar(180, 255, 255);

    Scalar scalarGREENLower = new Scalar(50,50, 50);
    Scalar scalarGREENUpper = new Scalar(70, 255, 255);

    Scalar scalarBLUELower = new Scalar(0,50, 50);
    Scalar scalarBLUEUpper = new Scalar(60, 255, 255);

    Scalar scalarYELLOWLower = new Scalar(60,50, 50);
    Scalar scalarYELLOWUpper = new Scalar(120, 255, 255);

    //hash maps for the colour results of each circle
    HashMap<String, Boolean> c1HashMap, c2HashMap, c3HashMap, c4HashMap;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch(status){
                case LoaderCallbackInterface.SUCCESS:{
                    Log.i(TAG, "OpenCV loaded successfully");
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_cvcolor_analysis);

        OpenCVLoader.initDebug();

        textViewRED = findViewById(R.id.textViewRED);
        textViewGREEN = findViewById(R.id.textViewGREEN);
        textViewBLUE = findViewById(R.id.textViewBLUE);
        textViewYELLOW = findViewById(R.id.textViewYELLOW);
        textViewDISTANCE = findViewById(R.id.textViewDISTANCE);

        tvC1 = findViewById(R.id.textViewC1hm);
        tvC2 = findViewById(R.id.textViewC2hm);
        tvC3 = findViewById(R.id.textViewC3hm);
        tvC4 = findViewById(R.id.textViewC4hm);

        imageViewRED = findViewById(R.id.imageViewRED);
        imageViewGREEN = findViewById(R.id.imageViewGREEN);
        imageViewBLUE = findViewById(R.id.imageViewBLUE);
        imageViewYELLOW = findViewById(R.id.imageViewYELLOW);
        imageViewDISTANCE = findViewById(R.id.imageViewDISTANCE);
        imageViewC1 = findViewById(R.id.imageViewC1);
        imageViewC2 = findViewById(R.id.imageViewC2);
        imageViewC3 = findViewById(R.id.imageViewC3);
        imageViewC4 = findViewById(R.id.imageViewC4);

        bundle = getIntent().getExtras();

        if (bundle != null) {
            String path = bundle.get("imageName").toString();
            Rect rect1 = (Rect) bundle.get("rectangleForAnalysis");
            double slope = (double) bundle.get("slope");
            Log.d(TAG, "Bundle: " + path);
            Log.d(TAG, "RECT: " + rect1.left + " " + rect1.top);

            try {
                FileInputStream fIS = new FileInputStream(new File(path));
                bitmap = BitmapFactory.decodeStream(fIS);

                processImage(bitmap, imageViewRED, scalarREDLower, scalarREDUpper);
                processImage(bitmap, imageViewGREEN, scalarGREENLower, scalarGREENUpper);
                processImage(bitmap, imageViewBLUE, scalarBLUELower, scalarBLUEUpper);
                processImage(bitmap, imageViewYELLOW, scalarYELLOWLower, scalarYELLOWUpper);

                rotatedBitmap = createNewBitmapRotateAndClosestColorConversion(bitmap, rect1, slope);

                barcodeDetector = new BarcodeDetector.Builder(getApplicationContext()).setBarcodeFormats(Barcode.QR_CODE).build();

                if (!barcodeDetector.isOperational()) {
                    return;
                }
                //if the barcode detector has been set up correctly
                else {
                    frame = new Frame.Builder().setBitmap(rotatedBitmap).build();

                    barcodeSparseArray = barcodeDetector.detect(frame);

                    //if there is a barcode present in the frame then analyse it
                    if (barcodeSparseArray.size() > 0) {
                        barcode = barcodeSparseArray.valueAt(0);

                        //call the QR corner points method
                        qrCornerPoints = getQRCornerPoints(barcode);

                        //call the method to calculate the proportional value Q
                        Q = calculateProportion(qrCornerPoints);

                        //call the method to get the coordinates of the rectangle to draw
                        rect = getRectangleOnImage(qrCornerPoints, Q);

                        //call the method to calculate the centres of the 4 circles to draw - calculated with
                        //relation to the rect
                        getCirclesOnImage(rect, Q);

                        //now draw the rect and circles on the canvas and set on the second imageView
                        //drawCanvasOnImageBitmap(imageView1, rect, centreC1, centreC2, centreC3, centreC4, rotatedBitmap, Q);

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
            }
            catch(FileNotFoundException e){
                e.printStackTrace();
            }
        }

    }

    /**
     *
     * @param bmp
     * @param imageView
     * @param lower
     * @param upper
     */
    private void processImage(Bitmap bmp, ImageView imageView, Scalar lower, Scalar upper){

        //Reading the image
        Mat image = new Mat();
        Bitmap bmp32 = bmp.copy(Bitmap.Config.ARGB_8888, true);
        Utils.bitmapToMat(bmp32, image);

        Mat oImg = detectColor(image, lower, upper);

        //converting image from mat to bitmap to display it in the imageview
        Bitmap bm = Bitmap.createBitmap(oImg.cols(), oImg.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(oImg, bm);
        imageView.setImageBitmap(bm);

    }

    /**
     *
     * @param sourceImage
     * @param lower
     * @param upper
     * @return
     */
    private Mat detectColor(Mat sourceImage, Scalar lower, Scalar upper){

        Mat blurImg = new Mat();
        Mat hsvImg = new Mat();
        Mat colorRange = new Mat();

        //blur the image to filter small noises
        Imgproc.GaussianBlur(sourceImage, blurImg, new Size(5,5), 0);

        //converting blurred image from rgb to hsv
        Imgproc.cvtColor(blurImg, hsvImg, Imgproc.COLOR_BGR2HSV);

        Core.inRange(hsvImg, lower, upper, colorRange);

        return colorRange;
    }

    /**
     *
     */
    @Override
    public void onResume()
    {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    /**
     *
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
     *
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
    private Bitmap createNewBitmapRotateAndClosestColorConversion(Bitmap bitmap, Rect rect, double slope){
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

        imageViewDISTANCE.setImageBitmap(newBitmap);
        return newBitmap;
    }

    /**
     *
     * @param barcode
     * @return
     */
    private Point[] getQRCornerPoints(Barcode barcode){
        return barcode.cornerPoints;
    }

    /**
     *
     * @param qrCornerPoints
     * @return
     */
    private double calculateProportion(Point[] qrCornerPoints){
        double prop = (qrCornerPoints[1].x - qrCornerPoints[0].x) / L1;

        return prop;
    }

    /**
     *
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
}
