package com.prer;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Window;

public class SplashScreenActivity extends SherlockActivity {
	
    private final int SPLASH_DISPLAY_TIME = 1500;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // go full screen, hide the status and action bars before setContentView()
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
        						WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
        setContentView(R.layout.splashscreen_layout);

        // New Handler to start the HomeScreenActivity 
        // and close the splash screen after some seconds
        new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {
                /* Create an Intent that will start the HomeScreenActivity */
                Intent intent = new Intent(SplashScreenActivity.this, HomeScreenActivity.class);
                startActivity(intent);
                finish();
            }
        }, SPLASH_DISPLAY_TIME);
    }
}
