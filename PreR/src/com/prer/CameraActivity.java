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
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.FrameLayout;
import android.widget.ImageButton;

/** The activity that will display the camera preview. */
public class CameraActivity extends Activity {

    private static final String TAG = "CameraActivity";
    protected static final String KEY_IMAGE_PATH = "image_path";
    private static final String HELP_KEY = "help preference";
    
	private Camera mCamera;
	private boolean isFocusing = false;
    private CameraPreview mPreview;
    
    private FrameLayout mPreviewFrame;
    private ImageButton mCaptureButton;
    private AlertDialog mHelpDialog;
    
    private boolean mShowHelp;
    private SharedPreferences mHelpPreference;
    private Editor mEditor;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // hide the status bar and action bar before setContentView()
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
        						WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // link this activity to the camera layout file
        this.setContentView(R.layout.activity_camera);
        
        mHelpPreference = this.getSharedPreferences(HELP_KEY, MODE_PRIVATE);
        mShowHelp = mHelpPreference.getBoolean("help", true);
        mEditor = mHelpPreference.edit();
        
        initPreview();
        initLayout();
        
        if (mShowHelp)
        	mHelpDialog.show();
        
        // open the camera in onResume() so it can be properly released and re-opened
    }
    
    private void initPreview() {
    	// create our Preview object
        mPreview = new CameraPreview(this);
        mPreviewFrame = (FrameLayout) findViewById(R.id.camera_preview);
        mPreviewFrame.setOnTouchListener(new View.OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					handleTouchToFocus();
				}
                return true;
			}
        });
    }
    
    private void initLayout() {
    	View checkBoxView = View.inflate(this, R.layout.help_dialog, null);
    	CheckBox checkBox = (CheckBox) checkBoxView.findViewById(R.id.help_checkbox);
    	checkBox.setChecked(!mShowHelp);
    	checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				mShowHelp = !isChecked;
				mEditor.clear();
				mEditor.putBoolean("help", !isChecked);
				mEditor.commit();
			}
    		
    	});
    	if (Build.VERSION.SDK_INT == Build.VERSION_CODES.GINGERBREAD_MR1)
    		checkBox.setTextColor(Color.WHITE);
    	
    	// build the help dialog window
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Picture Tips");
        builder.setMessage("Touch to focus.\nPlace the bill flat on a dark surface for best results.\n" +
        				   "PreR will not store or use any personal information visible on your bill, " +
        				   "however please cover any sensitive information you wish not to be seen.");
        builder.setNeutralButton("OK", new DialogInterface.OnClickListener() {
        	public void onClick(DialogInterface dialog,int id) {
        		dialog.dismiss();
        	}
		});
        builder.setView(checkBoxView);
        // create the help window dialog
        mHelpDialog = builder.create();
        
        // set the event listener for the capture button after we opened the camera
        mCaptureButton = (ImageButton) findViewById(R.id.camera_capture_button);
        mCaptureButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				mCaptureButton.setEnabled(false);
				
				// get an image from the camera
		        mCamera.takePicture(null, null, mPicture);
		        Log.d(TAG, "picture taken");
			}
        	
        });
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        
        mCamera = getCameraInstance();
        mPreview.setCamera(mCamera);
        Log.d(TAG, "new camera instance has been created");
        mPreviewFrame.addView(mPreview);
        
        mCaptureButton.setEnabled(true);
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        
        // release the camera immediately on pause event so it can be used by other apps
        if (mCamera != null){
        	mCamera.setPreviewCallback(null);
            mCamera.release();
            mCamera = null;
            mPreviewFrame.removeView(mPreview);
            
            Log.d(TAG, "camera preview paused, camera has been released");
        }
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
        	String path = getApplicationContext().getCacheDir().getPath() + 
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
    
    private void handleTouchToFocus() {
    	if (mCamera != null && !isFocusing) {

    		isFocusing = true;
    		// focus the camera, no callback
    		mCamera.autoFocus(new Camera.AutoFocusCallback() {
    			@Override
    			public void onAutoFocus(boolean success, Camera camera) {
    				// no longer attempting to auto focus
    				isFocusing = false;
    			}
    		});
    	}
    }
}
