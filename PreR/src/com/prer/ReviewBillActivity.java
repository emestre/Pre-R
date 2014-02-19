package com.prer;

import java.io.File;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

public class ReviewBillActivity extends SherlockActivity {
	
	private static final String TAG = "ReviewBillActivity";
	
	private String mPathToImage;
		
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_review_bill);
        
        // enable the home icon as an up button
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        
        displayImage();
    }
	
	private void displayImage() {
    	
    	// retrieve the file path from the intent
    	mPathToImage = (String) getIntent().getExtras().get(CameraActivity.KEY_IMAGE_PATH);
    	// create a bitmap image from the file path
    	Bitmap billImage = BitmapFactory.decodeFile(mPathToImage);
    	
    	// rotate the image 90 degrees clockwise
    	Matrix rotateMatrix = new Matrix();
    	rotateMatrix.postRotate(90);
    	Bitmap rotatedImage = Bitmap.createBitmap(billImage, 0, 0, billImage.getWidth(), 
    						billImage.getHeight(), rotateMatrix, false);
    	
        // set ImageView to display the bill image
        ImageView capturedBill = (ImageView) findViewById(R.id.review_bill);
        capturedBill.setImageBitmap(rotatedImage);
        capturedBill.setVisibility(View.VISIBLE);
    }
    
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = this.getSupportMenuInflater();
		inflater.inflate(R.menu.menu_review_bill, menu);
		
		return true;
    }
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		switch(item.getItemId()) {
		case android.R.id.home:
			// user decided to re-take the picture
			deleteImage();
			// simulate a back button press, kill this activity
			finish();
			break;
			
		case R.id.submenu_upload:
			Log.d(TAG, "got the submenu upload click");
			
			uploadBill();
			break;
			
		default:
			return super.onOptionsItemSelected(item);
		}

		return true;
	}
	
	private void deleteImage() {
		
		File image = new File(mPathToImage);
		boolean success = image.delete();
		
		if (success) {
			Log.d(TAG, "image was successfully erased");
		}
		else {
			Log.d(TAG, "image could not be erased");
		}
	}
    
    private void uploadBill() {
    	
    	RestClient client = new RestClient();
    	client.postImage(mPathToImage, new PostCallback() {
			@Override
			public void onPostSuccess(String result) {
				// check if the upload succeeded or not
				Log.d(TAG, "result of upload: " + result);
				
				deleteImage();
			}
    	});
    	
    	// start the bill upload status activity
    	Intent intent = new Intent(this, UploadedBillActivity.class);
    	startActivity(intent);
    }
}
