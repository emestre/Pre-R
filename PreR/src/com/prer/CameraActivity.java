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

/** The activity that will display the camera preview. */
public class CameraActivity extends Activity {

    private static final String TAG = "CameraActivity";
	private Camera mCamera;
    private CameraPreview mPreview;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // hide the status bar and action bar before setContentView()
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
        						WindowManager.LayoutParams.FLAG_FULLSCREEN);

        
        // link this activity to the camera preview XML file
        this.setContentView(R.layout.layout_camera);
        
        // create our Preview object
        mPreview = new CameraPreview(this);
        // set the preview object as the view of the FrameLayout
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(mPreview);
                
        // open the camera in onResume() so it can be properly released and re-opened
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        
        mCamera = getCameraInstance();
        mPreview.setCamera(mCamera);
//        createCamera();
        
        Log.d(TAG, "new camera instance has been created");
    } 
    
//	private void createCamera() {
//    	// create an instance of Camera
//        mCamera = getCameraInstance();
//        
//        // get the default preview size for the camera
////        Camera.Parameters params = mCamera.getParameters();
////        Camera.Size previewSize = params.getPreviewSize();
////        
////        Display display = getWindowManager().getDefaultDisplay();
////        int displayWidth = 0, displayHeight = 0;
////        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB_MR2) {
////        	Point displaySize = new Point();
////        	display.getSize(displaySize);
////            displayWidth = displaySize.x;
////            displayHeight = displaySize.y;
////        }
////        else {
////        	displayWidth = display.getWidth();  // deprecated
////        	displayHeight = display.getHeight();
////        }
//        
//        // get screen density scale to convert pixels to density independent pixels
////        DisplayMetrics metrics = new DisplayMetrics();
////        getWindowManager().getDefaultDisplay().getMetrics(metrics);
////        float density = metrics.density;
//////        int heightDP = (int) Math.ceil(previewSize.height * density);
////        int widthDP = (int) Math.ceil(previewSize.width * density);
////        int heightDP = (widthDP * displayHeight) / displayWidth;
////        
////        Log.d(TAG, "density scaled: " + density);
////        
////        Log.d(TAG, "camera height in pixels: " + previewSize.height);
////        Log.d(TAG, "camera height in DP: " + heightDP);
////        Log.d(TAG, "camera width in pixels: " + previewSize.width);
////        Log.d(TAG, "camera width in DP: " + widthDP);
////        
////        
////        Log.d(TAG, "default screen size:");
////        Log.d(TAG, "screen height: " + displayHeight);
////        Log.d(TAG, "screen width: " + displayWidth);
////        Log.d(TAG, "");
////        
////        Log.d(TAG, "supported preview sizes:");
////        List<Camera.Size> list = params.getSupportedPreviewSizes();
////        Camera.Size size;
////        for (int i = 0; i < list.size(); i++) {
////        	size = list.get(i);
////        	Log.d(TAG, "height:" + size.height);
////        	Log.d(TAG, "width: " + size.width);
////        }
//        
//        // create our Preview view and set it as the content of our activity
//        mPreview = new CameraPreview(this, mCamera);
//        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
//        preview.addView(mPreview);
//    }
    
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
//            mPreview.getHolder().removeCallback(mPreview);
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
