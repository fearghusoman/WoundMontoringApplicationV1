package com.example.woundmontoringapplicationv1.activities.ImageProcessingActivities;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;

import com.example.woundmontoringapplicationv1.R;
import com.example.woundmontoringapplicationv1.activities.MainActivities.RegisterDressingActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;

public class CaptureImageActivity extends AppCompatActivity {

    //setup the class' instance variables
    FloatingActionButton captureButton;

    //Timestamp for the capture
    Timestamp timestamp;

    CameraManager cameraManager;
    CameraDevice.StateCallback stateCallback;
    CameraDevice cameraDevice;

    //variables for the createPreviewSession method
    CaptureRequest.Builder captureRequestBuilder;
    CaptureRequest captureRequest;
    CameraCaptureSession cameraCaptureSession;

    int cameraFacing;
    TextureView textureView;
    TextureView.SurfaceTextureListener surfaceTextureListener;
    Size previewSize;
    String cameraID;
    HandlerThread backgroundThread;
    Handler backgroundHandler;

    //variables for the storage of the captured images
    File galleryFolder;
    String imageLocation;

    //create a bundle to check why the camera is being used
    Bundle bundle;

    /**
     * Method is called when an instance of the class is created
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture_image);

        textureView = findViewById(R.id.texture_view);
        captureButton = findViewById(R.id.takeImage);

        cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        cameraFacing = CameraCharacteristics.LENS_FACING_BACK;

        bundle = getIntent().getExtras();

        /**
         * what happens when the camera is in various states
         */
        stateCallback = new CameraDevice.StateCallback(){
            @Override
            public void onOpened(CameraDevice camera) {
                CaptureImageActivity.this.cameraDevice = camera;
                createPreviewSession();
            }

            @Override
            public void onDisconnected(CameraDevice camera) {
                cameraDevice.close();
                cameraDevice = null;
            }

            @Override
            public void onError(CameraDevice camera, int error) {
                cameraDevice.close();
                cameraDevice = null;
            }
        };

        /**
         *
         */
        surfaceTextureListener = new TextureView.SurfaceTextureListener(){
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                setUpCamera();
                openCamera();
            }
            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {}
            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                return false;
            }
            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {}
        };

        /**
         * what happens when the user clicks capture image
         */
        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //get current timestamp
                Date date= new Date();
                long time = date.getTime();
                timestamp = new Timestamp(time);

                //first, we'll create the gallery if it doesn't yet exist
                imageLocation = createImageGallery();

                lock();
                FileOutputStream outputPhoto = null;

                try{

                    //galleryFolder is the file variable instantiated in the createImageGallery method
                    File imageFile = createImageFile(galleryFolder);
                    outputPhoto = new FileOutputStream(imageFile);
                    Log.d("FEARGS CHECK", "try to save worked");

                    //this compresses the bitmap within the textureView and writes the compressed version
                    //to the output stream 'outputPhoto'
                    textureView.getBitmap().compress(Bitmap.CompressFormat.PNG, 100, outputPhoto);

                    if (bundle != null) {
                        String reason = bundle.get("CALLING_ACTIVITY").toString();

                        if(reason.equalsIgnoreCase("RegisterDressing")){
                            Intent imageIntent = new Intent(getApplicationContext(), RegisterDressingActivity.class);
                            imageIntent.putExtra("imageName", imageFile.getAbsoluteFile());
                            Log.d("FEARGS CHECK", imageFile.getAbsolutePath());
                            setResult(Activity.RESULT_OK, imageIntent);
                            finish();

                        }
                        else if(reason.equalsIgnoreCase("ProcessNewImage")){
                            //create the intent that goes to the next activity & pass it the path to the captured image
                            Intent imageIntent = new Intent(getApplicationContext(), ProcessImageActivity.class);
                            imageIntent.putExtra("imageName", imageFile.getAbsoluteFile());
                            imageIntent.putExtra("timestamp", timestamp);
                            Log.d("FEARGS CHECK", imageFile.getAbsolutePath());
                            startActivity(imageIntent);
                        }
                        else{
                            Log.d("FEARGS CHECK", "Intent extras are not being processed correctly.");
                        }
                    }

                }
                catch(Exception e){
                    e.printStackTrace();
                    Log.d("FEARGS CHECK", "try to save did not work: " + e);
                }
                finally {
                    //call the unlock method
                    unlock();

                    try{
                        if(outputPhoto != null){
                            outputPhoto.close();
                        }
                    }
                    catch(IOException e){
                        e.printStackTrace();
                    }
                }
            }
        });


    }

    /**
     * onResume is invoked when the activity enters the resumed state
     * this is the state in which the app interacts with the user
     * app stays in this state until something happens to take focus
     * away from the app
     */
    @Override
    protected void onResume() {
        super.onResume();

        openBackgroundThread();

        if(textureView.isAvailable()){
            setUpCamera();
            openCamera();
        }
        else{
            textureView.setSurfaceTextureListener(surfaceTextureListener);
        }
    }

    /**
     *
     */
    private void setUpCamera(){
        try{

            for(String cameraID : cameraManager.getCameraIdList()){

                CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraID);

                if(cameraCharacteristics.get(CameraCharacteristics.LENS_FACING) == cameraFacing){
                    StreamConfigurationMap streamConfigurationMap = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

                    previewSize = streamConfigurationMap.getOutputSizes(SurfaceTexture.class)[0];
                    this.cameraID = cameraID;
                }

            }

        }
        catch(CameraAccessException e){
            e.printStackTrace();
        }
    }

    /**
     * few things involved: can't overload the main thread too much; backgroundhandler
     * also, need some listener indicating when camera is opened and when it failed to do so
     */
    private void openCamera(){
        try{

            if(ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){
                cameraManager.openCamera(cameraID, stateCallback, backgroundHandler);
            }

        }
        catch(CameraAccessException e){
            e.printStackTrace();
        }
    }

    /**
     * creating a background thread
     */
    private void openBackgroundThread(){
        backgroundThread = new HandlerThread("camera_background_thread");
        backgroundThread.start();
        backgroundHandler = new Handler(backgroundThread.getLooper());
    }

    /**
     * close memory leaks when the application is stopped
     */
    @Override
    protected void onStop() {
        super.onStop();
    }

    /**
     *
     */
    private void createPreviewSession(){

        try{

            SurfaceTexture surfaceTexture = textureView.getSurfaceTexture();
            surfaceTexture.setDefaultBufferSize(previewSize.getWidth(), previewSize.getHeight());

            Surface previewSurface = new Surface(surfaceTexture);

            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(previewSurface);

            cameraDevice.createCaptureSession(Collections.singletonList(previewSurface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(CameraCaptureSession session) {

                    if(cameraDevice == null){
                        return;
                    }

                    try{
                        captureRequest = captureRequestBuilder.build();

                        CaptureImageActivity.this.cameraCaptureSession = session;
                        CaptureImageActivity.this.cameraCaptureSession.setRepeatingRequest(captureRequest, null, backgroundHandler);

                    }
                    catch(CameraAccessException e){
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession session) {

                }
            }, backgroundHandler);

        }
        catch(CameraAccessException e){
            e.printStackTrace();
        }
    }

    /**
     * creating a folder for the captured images on the local device
     */
    private String createImageGallery(){
        File storageDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        galleryFolder = new File(storageDirectory, "Wound Monitoring App");
        Log.d("FEARGS CHECK", "Location: " + storageDirectory.toString());
        Log.d("FEARGS CHECK", "Location Deeper: " + galleryFolder.getAbsolutePath());

        if(!galleryFolder.exists()){
            boolean wasCreated = galleryFolder.mkdirs();
            Log.d("FEARGS CHECK", "wasCreated value: " + wasCreated);

            if(!wasCreated){
                Log.e("FEARG GALLERY","Failed to create the directory.");
            }
        }
        else{
            Log.d("FEARGS CHECK", "The location already exists!!!");
        }

        return galleryFolder.getAbsolutePath();
    }

    /**
     * method for creating an image file
     * @param galleryFolder
     * @return
     * @throws IOException
     */
    private File createImageFile(File galleryFolder) throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "image_" + timeStamp + "_";

        return File.createTempFile(imageFileName, ".jpg", galleryFolder);
    }

    /**
     * need to lock preview for short time after photo was taken
     */
    private void lock(){
        try{
            cameraCaptureSession.capture(captureRequestBuilder.build(), null, backgroundHandler);
        }
        catch(CameraAccessException e){
            e.printStackTrace();
        }
    }

    /**
     * then need to unlock it via the setRepeatingRequest method
     */
    private void unlock(){
        try{
            cameraCaptureSession.setRepeatingRequest(captureRequestBuilder.build(), null, backgroundHandler);
        }
        catch(CameraAccessException e){
            e.printStackTrace();
        }
    }
}
