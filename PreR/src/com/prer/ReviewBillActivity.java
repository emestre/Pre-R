package com.prer;

import java.io.File;

import com.actionbarsherlock.app.SherlockActivity;

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
    	
        // set ImageView in XML to display the bill image
        ImageView capturedBill = (ImageView) findViewById(R.id.review_bill);
        capturedBill.setImageBitmap(rotatedImage);
        capturedBill.setVisibility(View.VISIBLE);
    }
    
    public void retakePictureClick(View view) {
    	// kill the ReviewBillActivity, acts just like a back button press
    	this.finish();
    }
    
    public void uploadBillClick(View view) {
    	RestClient client = new RestClient();
    	Log.d("Image path", mPathToImage);
    	client.postImage(mPathToImage, new PostCallback() {
			@Override
			public void onPostSuccess(String result) {
				// erase the image after it's uploaded
				File image = new File(mPathToImage);
				boolean success = image.delete();
				if (success) {
					Log.d(TAG, "image was successfully erased");
				}
				else {
					Log.d(TAG, "image could not be erased");
				}			
			}
    	});
    	
    	// start the bill upload status activity
    	Intent intent = new Intent(this, UploadedBillActivity.class);
    	startActivity(intent);
    }
}
