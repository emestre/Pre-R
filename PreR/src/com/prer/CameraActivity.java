package com.prer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
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
        
        // hide the status and action bars before setContentView()
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
        						WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.layout_camera);
        
        // create the Camera and CameraPreview objects
        createCamera();
        
        Log.d(TAG, "camera instance has been created.");
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
    
    @Override
    protected void onPause() {
        super.onPause();
        
        // release the camera immediately on pause event
        releaseCamera();
        
        Log.d(TAG, "camera preview paused, camera has been released.");
    }
    
    /** A safe way to get an instance of the Camera object. */
    private static Camera getCameraInstance() {
        Camera c = null;
        
        try {
        	// attempt to get a Camera instance
            c = Camera.open();
        }
        catch (Exception e){
            // camera is not available (in use or does not exist)
        }
        
        // returns null if camera is unavailable
        return c;
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
        	
        	// create a file for the image to be saved as
            File pictureFile = MediaFileHelper.getOutputMediaFile(
            		  			MediaFileHelper.MEDIA_TYPE_IMAGE);
            
            // code that saves the image to the SD card
            if (pictureFile == null) {
                Log.d(TAG, "Error creating media file, check storage permissions: ");
                return;
            }
            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
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
