/*
 * PreR Android Application
 * 
 * Home screen activity source file. Displays two buttons to the user:
 * 		- search for procedure cost
 * 		- take picture of bill and upload
 * 
*/

package com.prer;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;

public class HomeScreenActivity extends SherlockActivity {
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_homescreen);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getSupportMenuInflater().inflate(R.menu.menu_homescreen, menu);
        return true;
    }
    
    /** Start the search activity. */
    public void startSearch(View view) {
    	Intent intent = new Intent(this, SearchActivity.class);
		startActivity(intent);
    }
    
    /** Start the custom camera activity. */
    public void startCamera(View view) {
    	// check if the device has a camera before starting the activity
    	if (checkCameraHardware(getApplicationContext())) {
    		Intent intent = new Intent(this, CameraActivity.class);
    		startActivity(intent);
    	}
    	else {
    		// device has no camera
    		// TODO - notify user somehow
    	}
    }
    
    /** Check if this device has a camera. */
    private boolean checkCameraHardware(Context context) {
    	
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA))
            return true;	// this device has a camera
        else
            return false;	// no camera on this device
    }
}
