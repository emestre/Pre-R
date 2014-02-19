package com.prer;

import java.io.File;
import java.io.IOException;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class ReviewBillActivity extends SherlockActivity {
	
	private static final String TAG = "ReviewBillActivity";
	private static final int UPLOAD_SUCCESS = 201;
	private static final int UPLOAD_ERROR = 400;
	
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
    	
    	// try to read the EXIF tags of the JPG image
    	ExifInterface exif = null;
    	try {
    		exif = new ExifInterface(mPathToImage);
    	}
    	catch (IOException e) {
    		Log.d(TAG, e.getMessage());
    	}
    	
    	if (exif != null) {
    		// get the orientation attribute of the image
    		int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 
    												ExifInterface.ORIENTATION_NORMAL);
    		int degrees = 0;
    		switch (orientation) {
    		case ExifInterface.ORIENTATION_ROTATE_90:
    			degrees = 90;
    			break;
    			
    		case ExifInterface.ORIENTATION_ROTATE_180:
    			degrees = 180;
    			break;
    			
    		case ExifInterface.ORIENTATION_ROTATE_270:
    			degrees = 270;
    			break;
    		}
    		
    		Log.d(TAG, "rotation = " + degrees);
    		// rotate the image by the amount indicated in the EXIF tags
    		if (degrees != 0) {
    	    	Matrix rotateMatrix = new Matrix();
    	    	rotateMatrix.postRotate(degrees);
    	    	billImage = Bitmap.createBitmap(billImage, 0, 0, billImage.getWidth(), 
    	    						billImage.getHeight(), rotateMatrix, false);
    		}
    	}
    	
        // set ImageView to display the bill image
        ImageView capturedBill = (ImageView) findViewById(R.id.review_bill);
        capturedBill.setImageBitmap(billImage);
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
				String toastText = "";
				
				// check if the upload succeeded or not
				switch (Integer.parseInt(result)) {
				case UPLOAD_SUCCESS:
					toastText = "Upload succeeded";
					break;
				
				case UPLOAD_ERROR:
					toastText = "Upload failed";
					break;
				}
				
				Toast toast = Toast.makeText(ReviewBillActivity.this, toastText, Toast.LENGTH_SHORT);
		    	toast.show();
				
				deleteImage();
			}
    	});
    	
    	// start the bill upload status activity
    	Intent intent = new Intent(this, UploadedBillActivity.class);
    	startActivity(intent);
    }
}
