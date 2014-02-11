package com.prer;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

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
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

/** The activity that will display the camera preview. */
public class CameraActivity extends Activity {

    private static final String TAG = "CameraActivity";
    protected static final String KEY_IMAGE_PATH = "image_path";
	private Camera mCamera;
    private CameraPreview mPreview;
    
    private PopupWindow mPopup;

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
        
        // create the help pop-up
        TextView popupText = new TextView(this);
        popupText.setText("My sample popup message.\nWill display hints to user.");
        LinearLayout popupLayout = new LinearLayout(this);
        popupLayout.setOrientation(LinearLayout.VERTICAL);
        
        // open the camera in onResume() so it can be properly released and re-opened
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        
        mCamera = getCameraInstance();
        mPreview.setCamera(mCamera);
        
        Log.d(TAG, "new camera instance has been created");
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
    
    public void helpButtonClick(View view) {
    	
    }
    
    /** Listener method for the capture button. */
    public void captureClick(View view) {
    	// get an image from the camera
        mCamera.takePicture(null, null, mPicture);
        Log.d(TAG, "picture taken");
    }
    
    /** Callback to run when a picture has been taken. */
    private PictureCallback mPicture = new PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
        	
//        	// create a file for the image to be saved as
//            File billImage = MediaFileHelper.getOutputMediaFile(
//            		  			MediaFileHelper.MEDIA_TYPE_IMAGE);
//            
//            // code that saves the image to the SD card
//            if (billImage == null) {
//                Log.d(TAG, "Error creating media file, check storage permissions: ");
//                return;
//            }
//            
//            try {
//                FileOutputStream fos = new FileOutputStream(billImage);
//                fos.write(data);
//                fos.close();
//            }
//            catch (FileNotFoundException e) {
//                Log.d(TAG, "File not found: " + e.getMessage());
//            }
//            catch (IOException e) {
//                Log.d(TAG, "Error accessing file: " + e.getMessage());
//            }
//            // end of code that saves the image to the SD card
//            
//            Intent intent = new Intent(getApplicationContext(), ReviewBillActivity.class);
//            // add the file path to the intent and it send to ReviewBillActivity
//            intent.putExtra("path", billImage.getAbsolutePath());
//	        // start the ReviewBillActivity
//            startActivity(intent);
        	
        	// get the time stamp for the image name
        	String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        	// get the path to our app's internal storage directory and append image name
        	String path = getApplicationContext().getFilesDir().getPath() + 
        				  File.separator + "IMG_" + timeStamp + ".jpg";
        	
        	// save the image to our app's internal storage
        	try {
        		FileOutputStream fos = new FileOutputStream(path);

        		fos.write(data);
        		fos.close();
        	}
        	catch (Exception e) {
        		// error occurred during writing
        		Log.d(TAG, "Error writing image to internal storage\n" + e.getMessage());
        	}

        	Intent intent = new Intent(getApplicationContext(), ReviewBillActivity.class);
        	// add the file path to the intent and it send to ReviewBillActivity
        	intent.putExtra(KEY_IMAGE_PATH, path);
        	// start the ReviewBillActivity
        	startActivity(intent);
        }
    };
}
