package com.example.woundmontoringapplicationv1;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.support.v4.graphics.ColorUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

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

public class OpenCVColorAnalysisActivity extends AppCompatActivity {

    private static final String TAG = "FEARGS CHECK";

    TextView textViewRED, textViewGREEN, textViewBLUE, textViewYELLOW, textViewDISTANCE;
    ImageView imageViewRED, imageViewGREEN, imageViewBLUE, imageViewYELLOW, imageViewDISTANCE;

    //an array of colors to store the primary colors we allow in our analysis
    Color[] colors = {Color.valueOf(Color.WHITE), Color.valueOf(Color.RED), Color.valueOf(Color.GREEN), Color.valueOf(Color.BLUE), Color.valueOf(Color.YELLOW)};
    int[] colorsInt = {Color.WHITE, Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW, Color.BLACK};

    Bitmap bitmap;

    Bundle bundle;

    Scalar scalarREDLower = new Scalar(120,50, 50);
    Scalar scalarREDUpper = new Scalar(180, 255, 255);

    Scalar scalarGREENLower = new Scalar(50,50, 50);
    Scalar scalarGREENUpper = new Scalar(70, 255, 255);

    Scalar scalarBLUELower = new Scalar(0,50, 50);
    Scalar scalarBLUEUpper = new Scalar(60, 255, 255);

    Scalar scalarYELLOWLower = new Scalar(60,50, 50);
    Scalar scalarYELLOWUpper = new Scalar(120, 255, 255);

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

        imageViewRED = findViewById(R.id.imageViewRED);
        imageViewGREEN = findViewById(R.id.imageViewGREEN);
        imageViewBLUE = findViewById(R.id.imageViewBLUE);
        imageViewYELLOW = findViewById(R.id.imageViewYELLOW);
        imageViewDISTANCE = findViewById(R.id.imageViewDISTANCE);

        bundle = getIntent().getExtras();

        if (bundle != null) {
            String path = bundle.get("imageName").toString();
            Rect rect = (Rect) bundle.get("rectangleForAnalysis");
            double slope = (double) bundle.get("slope");
            Log.d(TAG, "Bundle: " + path);
            Log.d(TAG, "RECT: " + rect.left + " " + rect.top);

            try {
                FileInputStream fIS = new FileInputStream(new File(path));
                bitmap = BitmapFactory.decodeStream(fIS);

                processImage(bitmap, imageViewRED, scalarREDLower, scalarREDUpper);
                processImage(bitmap, imageViewGREEN, scalarGREENLower, scalarGREENUpper);
                processImage(bitmap, imageViewBLUE, scalarBLUELower, scalarBLUEUpper);
                processImage(bitmap, imageViewYELLOW, scalarYELLOWLower, scalarYELLOWUpper);

                createNewBitmap(bitmap, rect, slope);
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
    private double[] convertRGBtoLAB(int[] rgb){
        int r  = rgb[0];
        int g  = rgb[1];
        int b  = rgb[2];

        double[] lab = new double[3];

        ColorUtils.colorToLAB();
        //D65 / 2 degrees

        return lab;
    }
     **/

    /**
     *
     * @param desiredColors
     * @param color
     * @return
     */
    private int rgbToLAB(int[] desiredColors, int color){
        int closestPrimary = 0;
        double distance = Double.MAX_VALUE;
        double[] ourLAB = new double[3];

        ColorUtils.RGBToLAB(Color.red(color), Color.green(color), Color.blue(color), ourLAB);

        for(int c : desiredColors){
            double[] desiredLAB = new double[3];

            ColorUtils.RGBToLAB(Color.red(c), Color.green(c), Color.blue(c), desiredLAB);

            if(ColorUtils.distanceEuclidean(ourLAB, desiredLAB) < distance){
                closestPrimary = c;
                distance = ColorUtils.distanceEuclidean(ourLAB, desiredLAB);
            }
        }

        return closestPrimary;
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
    private void createNewBitmap(Bitmap bitmap, Rect rect, double slope){
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
    }
}
