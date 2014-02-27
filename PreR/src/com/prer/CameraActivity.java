package com.prer;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;

/** The activity that will display the camera preview. */
public class CameraActivity extends Activity {

    private static final String TAG = "CameraActivity";
    protected static final String KEY_IMAGE_PATH = "image_path";
    
	private Camera mCamera;
    private CameraPreview mPreview;
    private AlertDialog mHelpDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // hide the status bar and action bar before setContentView()
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
        						WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // link this activity to the camera layout file
        this.setContentView(R.layout.layout_camera);
        
        // create our Preview object
        mPreview = new CameraPreview(this);
        // set the preview object as the view of the FrameLayout
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(mPreview);
        
        // build the help dialog window
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Picture Tips");
        builder.setMessage("Place the bill flat on a dark surface for best results.\n" +
        				   "PreR will not store or use any personal information visible on your bill, " +
        				   "however please cover any sensitive information you wish not to be seen.");
        builder.setNeutralButton("OK", new DialogInterface.OnClickListener() {
        	public void onClick(DialogInterface dialog,int id) {
        		dialog.dismiss();
        	}
		});
        // create the help window dialog
        mHelpDialog = builder.create();
        
        // open the camera in onResume() so it can be properly released and re-opened
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        
        mCamera = getCameraInstance();
        mPreview.setCamera(mCamera);
        Log.d(TAG, "new camera instance has been created");
        
        // set the event listener for the capture button after we opened the camera
        ImageButton capture = (ImageButton) findViewById(R.id.camera_capture_button);
        capture.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				// get an image from the camera
		        mCamera.takePicture(null, null, mPicture);
		        Log.d(TAG, "picture taken");
			}
        	
        });
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        
        // release the camera immediately on pause event so it can be used by other apps
        if (mCamera != null){
        	mCamera.setPreviewCallback(null);
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
    
	/** Listener method for the help button. Displays a dialog with tips for taking a picture */
    public void helpButtonClick(View view) {
    	mHelpDialog.show();
    }
    
    /** Callback to run when a picture has been taken. */
    private PictureCallback mPicture = new PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
        	
        	// get the time stamp for the image name
        	String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        	// get the path to our app's internal storage directory and append image name
        	String path = getApplicationContext().getFilesDir().getPath() + 
        				  File.separator + "IMG_" + timeStamp + ".jpg";
        	
        	// save the image to the app's internal storage directory
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
