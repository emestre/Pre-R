package com.prer;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

public class SplashScreenActivity extends Activity {
	
	public static final String FIRST_RUN_PREF_NAME = "first run preference";
	public static final String IS_FIRST_RUN = "first run";
    private final int SPLASH_DISPLAY_TIME = 1250;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.activity_splashscreen);

        // New Handler to start the HomeScreenActivity 
        // and close the splash screen after some seconds
        new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {
                // go to tutorial or home screen based on if this is first run
            	SharedPreferences firstRunPreference = 
	    				getSharedPreferences(SplashScreenActivity.FIRST_RUN_PREF_NAME, Activity.MODE_PRIVATE);
            	
            	Intent intent;
            	if (firstRunPreference.getBoolean(IS_FIRST_RUN, true)) {
            		intent = new Intent(SplashScreenActivity.this, FirstUseActivity.class);
            	}
            	else {
            		intent = new Intent(SplashScreenActivity.this, HomeScreenActivity.class);
            	}
                
                startActivity(intent);
                finish();
            }
        }, SPLASH_DISPLAY_TIME);
    }
    
    @Override
    public void onBackPressed() {
    	// do nothing
    }
}
