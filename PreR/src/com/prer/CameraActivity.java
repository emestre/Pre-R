package com.prer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

/** The activity that will display the camera preview. */
public class CameraActivity extends Activity {

    private static final String TAG = "CameraActivity";
	private Camera mCamera;
    private CameraPreview mPreview;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // hide the status bar and action bar before setContentView(), fullscreen mode for camera
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
        						WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // link this activity to the corresponding XML file
        setContentView(R.layout.layout_camera);
        
        // create the Camera and CameraPreview objects, called in onResume()
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        
        createCamera();
        
        Log.d(TAG, "new camera instance has been created");
    } 
    
    private void createCamera() {
    	// create an instance of Camera
        mCamera = getCameraInstance();
        
        // get the default preview size for the camera
        Camera.Parameters params = mCamera.getParameters();
        Camera.Size previewSize = params.getPreviewSize();
        
        // set layout parameters to the width and height of camera preview
        // width and height are switched for portrait mode
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
        									previewSize.height, previewSize.width);
        
        // create our Preview view and set it as the content of our activity
        mPreview = new CameraPreview(this, mCamera);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        // applying layout parameters to the camera preview layout
        preview.setLayoutParams(lp);
        preview.addView(mPreview);
    }
    
    /** A safe way to get an instance of the Camera object. */
    private static Camera getCameraInstance() {
        Camera c = null;
        
        try {
        	// attempt to get a Camera instance
            c = Camera.open();
        }
        catch (Exception e) {
            // camera is not available (in use or does not exist)
        	Log.d(TAG, "camera object could no be obtained: in use or does not exist");
        }
        
        // returns null if camera is unavailable
        return c;
    }
    
    /** Listener method for the capture button. */
    public void captureClick(View view) {
    	// get an image from the camera
        mCamera.takePicture(null, null, mPicture);
        
        Log.d(TAG, "picture taken");
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        
        // release the camera immediately on pause event so it can be used by other apps
        if (mCamera != null){
        	mCamera.setPreviewCallback(null);
            mPreview.getHolder().removeCallback(mPreview);
            mCamera.release();
            mCamera = null;
        }
        
        Log.d(TAG, "camera preview paused, camera has been released");
    }
    
    /** Callback to run when a picture has been taken. */
    private PictureCallback mPicture = new PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
        	
        	// create a file for the image to be saved as
            File billImage = MediaFileHelper.getOutputMediaFile(
            		  			MediaFileHelper.MEDIA_TYPE_IMAGE);
            
            // code that saves the image to the SD card
            if (billImage == null) {
                Log.d(TAG, "Error creating media file, check storage permissions: ");
                return;
            }
            try {
                FileOutputStream fos = new FileOutputStream(billImage);
                fos.write(data);
                fos.close();
            }
            catch (FileNotFoundException e) {
                Log.d(TAG, "File not found: " + e.getMessage());
            }
            catch (IOException e) {
                Log.d(TAG, "Error accessing file: " + e.getMessage());
            }
            // end of code that saves the image to the SD card
            
            Intent intent = new Intent(getApplicationContext(), ReviewBillActivity.class);
            // add the file path to the intent and it send to ReviewBillActivity
            intent.putExtra("path", billImage.getAbsolutePath());
	        // start the ReviewBillActivity
            startActivity(intent);
        }
    };
}
