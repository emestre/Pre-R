package com.prer;

import java.io.File;
import java.io.IOException;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class ReviewBillActivity extends SherlockActivity {
	
	private static final String TAG = "ReviewBillActivity";
	private static final int UPLOAD_SUCCESS = 201;
	
	private String mPathToImage;
	private ProgressDialog mUploadingProgress;
	private AlertDialog mUploadStatusAlert;
		
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_review_bill);
        
        // enable the home icon as an up button
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        
        displayImage();
    }
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		// delete the image from internal storage on exit
		deleteImage();
	}
	
	private void deleteImage() {
		
		File image = new File(mPathToImage);
		boolean success = image.delete();
		
		if (success)
			Log.d(TAG, "image was successfully erased");
		else
			Log.d(TAG, "image could not be erased");
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
			// simulate a back button press, kill this activity
			finish();
			break;
			
		case R.id.submenu_upload:
			uploadBill();
			break;
			
		default:
			return super.onOptionsItemSelected(item);
		}

		return true;
	}
    
    private void uploadBill() {
    	
    	RestClient client = new RestClient();
    	client.postImage(mPathToImage, new PostCallback() {
			@Override
			public void onPostSuccess(String result) {
				
				// dismiss the progress dialog
				mUploadingProgress.dismiss();
				
				// check if the upload succeeded or not
				switch (Integer.parseInt(result)) {
				case UPLOAD_SUCCESS:
					Log.d(TAG, "uploading the image SUCCEEDED");
					
					// building the upload succeed alert window
					mUploadStatusAlert = buildSuccessDialog();
			        
					break;
				
				default:
					Log.d(TAG, "uploading the image FAILED");
					
			        // building the upload failed alert window
					mUploadStatusAlert = buildFailedDialog();
			        
					break;
				}
				
				// show the upload status alert dialog
				mUploadStatusAlert.show();
			}
    	});
    	
    	// display a loading spinner while the bill is uploading
    	mUploadingProgress = new ProgressDialog(this);
    	mUploadingProgress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
    	mUploadingProgress.setTitle("Uploading...");
    	mUploadingProgress.setMessage("Your bill is being uploaded to PreR.");
    	mUploadingProgress.setIndeterminate(true);
    	mUploadingProgress.setCancelable(true);
    	mUploadingProgress.show();
    	
//    	new CountDownTimer(5000, 2000) {
//    		private boolean test = true;
//
//			@Override
//			public void onFinish() {
//				uploading.dismiss();
//				if (test)
//					mSuccess.show();
//				else
//					mFailed.show();
//			}
//
//			@Override
//			public void onTick(long millisUntilFinished) {
//				// no updates from timer
//				test = !test;
//			}
//    	}.start();
    }
    
    private AlertDialog buildSuccessDialog() {
    	
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Upload Succeeded");
        builder.setMessage("Your bill was successfully uploaded to PreR.\n" +
        				   "Thank you for making health care healthier!");
        builder.setCancelable(false);
        builder.setPositiveButton("Upload New Page", new DialogInterface.OnClickListener() {
        	
        	@Override
        	public void onClick(DialogInterface dialog, int which) {
        		dialog.dismiss();
        		
        		Intent intent = new Intent(getApplicationContext(), CameraActivity.class);
        		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        		startActivity(intent);
        	}
		});
        builder.setNegativeButton("Home", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				
				Intent intent = new Intent(getApplicationContext(), HomeScreenActivity.class);
				// clear the back stack so user effectively "restarts" application
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
			}
		});
        
        return builder.create();
    }
    
    private AlertDialog buildFailedDialog() {
    	
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Upload Failed");
        builder.setMessage("Sorry, there was an error with your upload. " +
        				   "Google's servers are overloaded right now.");
        builder.setCancelable(false);
        builder.setPositiveButton("Try Again", new DialogInterface.OnClickListener() {
        	
        	@Override
        	public void onClick(DialogInterface dialog, int which) {
        		dialog.dismiss();
        		uploadBill();
        	}
		});
        builder.setNegativeButton("Home", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				
				Intent intent = new Intent(getApplicationContext(), HomeScreenActivity.class);
				// clear the back stack so user effectively "restarts" application
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
			}
		});
        
        return builder.create();
    }
}
