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
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

/** The activity that will display the camera preview. */
public class CameraActivity extends Activity {

    protected static final String TAG = "CameraActivity";
	private Camera mCamera;
    private CameraPreview mPreview;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera_layout);
        
        Log.d(TAG, "created");
        
        // create the Camera and CameraPreview objects
        createCamera();
    }
    
    private void createCamera() {
    	// create an instance of Camera
        mCamera = getCameraInstance();
        // create our Preview view and set it as the content of our activity
        mPreview = new CameraPreview(this, mCamera);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(mPreview);
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        
        Log.d(TAG, "paused, camera will be released.");
        
        // release the camera immediately on pause event
        releaseCamera();
    }
    
    /** A safe way to get an instance of the Camera object. */
    private static Camera getCameraInstance() {
        Camera c = null;
        
        try {
            c = Camera.open();	// attempt to get a Camera instance
        }
        catch (Exception e){
            // camera is not available (in use or does not exist)
        }
        
        return c; // returns null if camera is unavailable
    }
    
    /** Listener method for the capture button. */
    public void captureClick(View view) {
    	// get an image from the camera
        mCamera.takePicture(null, null, mPicture);
    }
    
    /** Callback to run when a picture has been taken. */
    private PictureCallback mPicture = new PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
        	
        	// create intent to start to ReviewBillActivity
//        	Intent intent = new Intent(getApplicationContext(), ReviewBillActivity.class);
        	
        	// create a file for
            File pictureFile = MediaFileHelper.getOutputMediaFile(
            		  			MediaFileHelper.MEDIA_TYPE_IMAGE);
            // add the file image to the intent to send to ReviewBillActivity
//            intent.putExtra(MediaStore.EXTRA_OUTPUT, pictureFile);
            
            // code that saves the image to the SD card
            if (pictureFile == null){
                Log.d(TAG, "Error creating media file, check storage permissions: ");
                return;
            }

            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();
            } catch (FileNotFoundException e) {
                Log.d(TAG, "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d(TAG, "Error accessing file: " + e.getMessage());
            }
            // end of code that saves the image to the SD card
            
            // start the ReviewBillActivity
//            startActivity(intent);
            
            // restart the preview to get live feed from camera
            mCamera.startPreview();
        }
    };
    
    /** Release the camera so it can be used by other applications. */
    private void releaseCamera(){
        if (mCamera != null){
            mCamera.release();
            mCamera = null;
        }
    }
}
