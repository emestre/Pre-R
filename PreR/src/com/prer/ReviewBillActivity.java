package com.prer;

import java.io.File;
import java.io.IOException;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

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
        setContentView(R.layout.activity_review_bill);
        
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
			Log.d(TAG, "deleting image...SUCCESS");
		else
			Log.d(TAG, "deleting image...FAILED");
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
    	    	rotateMatrix.preRotate(degrees);
    	    	billImage = Bitmap.createBitmap(billImage, 0, 0, billImage.getWidth(), 
    	    						billImage.getHeight(), rotateMatrix, false);
    		}
    		billImage = billImage.copy(Bitmap.Config.ARGB_8888, true);
    	}
    	
        // set ImageView to display the bill image
        ImageView capturedBill = (ImageView) findViewById(R.id.review_bill);
        capturedBill.setImageBitmap(billImage);
        capturedBill.setVisibility(View.VISIBLE);
        
        // run OCR on bitmap
//        new RunTesseractOCR(this.getApplicationContext()).execute(billImage);
    }
    
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = this.getSupportMenuInflater();
		inflater.inflate(R.menu.review_bill, menu);
		
		return true;
    }
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		switch(item.getItemId()) {
		case R.id.menu_retake:
			// user decided to re-take the picture
			// simulate a back button press, kill this activity
			finish();
			break;
			
		case R.id.menu_upload:
			if (isNetworkOnline()) {
				uploadBill();
			}
			else {
				Toast.makeText(this, "You need a network connection to upload", Toast.LENGTH_SHORT).show();
			}
			
			break;
			
		default:
			return super.onOptionsItemSelected(item);
		}

		return true;
	}
	
	public boolean isNetworkOnline() {
		boolean status=false;
		try {
			ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo netInfo = cm.getNetworkInfo(0);
			
			if (netInfo != null && netInfo.getState()==NetworkInfo.State.CONNECTED) {
				status= true;
			}
			else {
				netInfo = cm.getNetworkInfo(1);
				
				if (netInfo!=null && netInfo.getState()==NetworkInfo.State.CONNECTED)
					status= true;
			}
		}
		catch(Exception e)  {
			e.printStackTrace();  
			return false;
		}
		
		return status;
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
    }
    
    private AlertDialog buildSuccessDialog() {
    	
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Upload Succeeded");
        builder.setMessage("Thank you for making health care healthier!");
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
        builder.setMessage("Sorry, there was an problem with your upload.");
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
    
//    private class RunTesseractOCR extends AsyncTask<Bitmap, Integer, String> {
//    	
//    	private Context mContext;
//    	
//    	public RunTesseractOCR(Context context) {
//    		mContext = context;
//    	}
//    	
//    	@Override
//		protected String doInBackground(Bitmap... params) {
//			Bitmap image = params[0];
//			
//	        TessBaseAPI baseApi = new TessBaseAPI();
//	        baseApi.init(Environment.getExternalStorageDirectory() + "/tesseract", "eng");
//	        baseApi.setImage(image);
//	        String recognizedText = baseApi.getUTF8Text();
//	        baseApi.end();
//	            		
//    		return recognizedText;
//		}
//    	
//        protected void onPostExecute(String result) {
//        	Toast.makeText(mContext, result, Toast.LENGTH_LONG).show();
//        }
//    }
}
