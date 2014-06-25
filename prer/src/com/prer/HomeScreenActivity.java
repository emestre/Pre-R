/*
 * PreR Android Application
 * 
 * Home screen activity source file. Displays two buttons to the user:
 * 		- search for procedure cost
 * 		- take picture of bill and upload
 * 
*/

package com.prer;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class HomeScreenActivity extends SherlockActivity {
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homescreen);
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
    		Toast.makeText(this, "Your device does not have a camera", Toast.LENGTH_SHORT).show();
    	}
    }
    
    /** Check if this device has a camera. */
    private boolean checkCameraHardware(Context context) {
    	
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA))
            return true;	// this device has a camera
        else
            return false;	// no camera on this device
    }
    
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// inflate the menu, this adds items to the action bar if it is present.
		this.getSupportMenuInflater().inflate(R.menu.homescreen, menu);
		return true;
	}
    
    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
    	Intent intent;
    	
		switch (item.getItemId()) {
			
			case R.id.action_about:
				intent = new Intent(this, AboutUsActivity.class);
				startActivity(intent);
				return true;
				
			case R.id.action_privacy:
				intent = new Intent(this, PrivacyPolicyActivity.class);
				startActivity(intent);
				return true;
				
			case R.id.action_faq:
				intent = new Intent(this, FAQActivity.class);
				startActivity(intent);
				return true;
		}
		
		return super.onOptionsItemSelected(item);
	}
}
