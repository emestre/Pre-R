/*
 * PreR Android Application
 * 
 * Home screen activity source file. Displays two buttons to the user:
 * 		- search for procedure cost
 * 		- take picture of bill and upload
 * 
*/

package com.prer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;

public class HomeScreenActivity extends Activity {

	private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
	private Uri fileUri;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_homescreen);
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.home_screen, menu);
//        return true;
//    }
    
    /** Start the search activity. */
    public void startSearch(View view) {
    	Intent intent = new Intent(this, SearchActivity.class);
		startActivity(intent);
    }
    
    /** Start the custom camera activity. */
    public void startCameraAPI(View view) {
    	// check if the device has a camera before starting the activity
    	if (checkCameraHardware(getApplicationContext())) {
    		Intent intent = new Intent(this, CameraActivity.class);
    		startActivity(intent);
    	}
    }
    
    /** Check if this device has a camera. */
    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            // this device has a camera
            return true;
        } 
        else {
            // no camera on this device
            return false;
        }
    }
    
    
    
    // TAKING PICTURE WITH CAMERA USING INTENT CODE ***************************
    
    // uses an intent to open the default camera
    public void startCamera(View view) {
        // create Intent to take a picture and return control to the calling application
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        fileUri = MediaFileHelper.getOutputMediaFileUri(
        		   MediaFileHelper.MEDIA_TYPE_IMAGE); 		// create a file to save the image
        
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); 	// set the image file name

        // start the image capture Intent
        startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
    }
    
    // runs when we return from the Camera intent
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // Image captured and saved to fileUri specified in the Intent
            	// data is null, picture is stored in Uri
            } else if (resultCode == RESULT_CANCELED) {
                // User cancelled the image capture
            } else {
                // Image capture failed, advise user
            }
        }
    }
    
    // END OF TAKING PICTURE WITH CAMERA USING INTENT CODE ********************
}
