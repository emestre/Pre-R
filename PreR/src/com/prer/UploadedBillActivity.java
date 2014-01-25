package com.prer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class UploadedBillActivity extends Activity {

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_uploaded_bill);
    }
	
	public void goHomeClick(View view) {
		Intent intent = new Intent(this, HomeScreenActivity.class);
		// clear the back stack so user effectively "restarts" app
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
	}
	
	public void uploadAnotherClick(View view) {
		Intent intent = new Intent(this, CameraActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
	}
	
	/** Intercept the back button being pressed. */
	@Override
	public void onBackPressed() {
		// do nothing, user cannot go back to ReviewBillActivity
	}
	
}
