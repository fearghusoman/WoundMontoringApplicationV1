package com.example.woundmontoringapplicationv1;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.graphics.Palette;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 *
 */
public class ProcessImageActivity extends AppCompatActivity {

    //store our known real-life measurements for the distance between points on a dressing
    //distance between the top two corners of the qr code
    final static int L1 = 5;
    //distance between the top right corner of qr code and top left corner of the area to be
    //analysed for colour
    final static int L2 = 3;
    //distance between the top two points of the rectangle
    final static int L3 = 6;

    Button processImgBtn;
    ImageView imageView, imageView2;
    Bitmap bitmap, bitmap2, bitmapForPixelColor;
    BarcodeDetector barcodeDetector;
    TextView textView, textView2, textView3, textView4, textView5, textView6;
    Frame frame;
    SparseArray<Barcode> barcodes;
    Barcode thisBarCode;
    byte[] bytes;
    String fileName;
    Bundle bundle;

    Point[] qrCornerPoints;

    Palette palette;
    int dominantColour, a, r, g, b, r1, g1, b1;

    //variables to hold the calculated four corners of the rectangle to be analysed
    int x1, y1, x2, y2, x3, y3, x4, y4;
    int l1, l2, l3;
    float q;
    Point A1, A2, A3, A4;

    //Variables for the dressing subsection
    Canvas canvas;
    Bitmap bitmap3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_process_image);

        processImgBtn = (Button) findViewById(R.id.button);
        textView = (TextView)  findViewById(R.id.txtContent) ;
        textView2 = (TextView)  findViewById(R.id.txtContent2) ;
        textView3 = (TextView)  findViewById(R.id.txtContent3) ;
        textView4 = (TextView)  findViewById(R.id.txtContent4) ;
        textView5 = (TextView)  findViewById(R.id.txtContent5) ;
        textView6 = (TextView)  findViewById(R.id.txtContent6) ;

        imageView2 = (ImageView) findViewById(R.id.imgview2);

        processImgBtn.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public void onClick(View v) {

                imageView = (ImageView) findViewById(R.id.imgview);

                bundle = getIntent().getExtras();

                if (bundle != null) {
                    String path = bundle.get("imageName").toString();

                    try {
                        FileInputStream fIS = new FileInputStream(new File(path));

                        //try {
                            //bitmap2 = BitmapFactory.decodeStream(openFileInput(path));
                            bitmap2 = BitmapFactory.decodeStream(fIS);

                            //bitmap2 = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.bandage_mock_up);

                            imageView.setImageBitmap(bitmap2);

                            barcodeDetector = new BarcodeDetector.Builder(getApplicationContext()).setBarcodeFormats(Barcode.QR_CODE).build();

                            if (!barcodeDetector.isOperational()) {
                                textView.setText("Couldn't setup the detector1");
                                return;
                            } else {
                                frame = new Frame.Builder().setBitmap(bitmap2).build();

                                barcodes = barcodeDetector.detect(frame);

                                //.detect return a sparsearray of all barcodes in the frame
                                //since we'll only have one per image, we can take the first value in the array
                                //if it's not empty then:
                                if (barcodes.size() > 0) {
                                    thisBarCode = barcodes.valueAt(0);
                                    textView.setText(thisBarCode.rawValue);

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
                                    calculateOurRectangle(qrCornerPoints);

                                    textView6.setText("A1: (" + A1.x + ", " + A1.y + "), " + "A2: (" + A2.x + ", " + A2.y + "), " +
                                            "A3: (" + A3.x + ", " + A3.y + "), " + "A4: (" + A4.x + ", " + A4.y + ")");

                                    /**
                                     //create the new bitmap to be stored in a new imageview - is the above calculated rectangle
                                     //Rect(left x, top y, right x, bottom y)
                                     //Rect rect = new Rect(A1.x, A1.y, A2.x, A3.y);
                                     Rect rect = new Rect(0, 0, 50, 100);

                                     Bitmap bitmapcircle = getCroppedBitmap(bitmap2);
                                     //mageView2.setImageBitmap(bitmapcircle);
                                     //Rect rect1 = imageView2.getDrawable().getBounds();

                                     //canvas = new Canvas();
                                     //canvas.drawBitmap(bitmap2, rect, rect1, null);
                                     **/
                                }
                                //if the barcode array is empty then:
                                else {
                                    textView.setText("There was no QR code detected in the image. Please try again..");
                                }
                            }
                        //} catch (FileNotFoundException e) {
                        //    e.printStackTrace();
                       // }
                    }
                    catch(FileNotFoundException fe){
                        fe.printStackTrace();
                    }
                }
                else{
                    textView.setText("There was nothing passed from the camera. Please try again..");
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
        l1 = (int) q2.x - q1.x;

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
        //A1.set(x1, y1);
        //A2.set(x2, y2);
        //A3.set(x3, y3);
        //A4.set(x4, y4);
    }

    /**
     *
     * @param bitmap
     * @return
     */
    public Bitmap getCroppedBitmap(Bitmap bitmap) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        // canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
        canvas.drawCircle(bitmap.getWidth() / 2, bitmap.getHeight() / 2,
                bitmap.getWidth() / 2, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        //Bitmap _bmp = Bitmap.createScaledBitmap(output, 60, 60, false);
        //return _bmp;
        return output;
    }
}
