package com.prer;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

public class ReviewBillActivity extends Activity {
	
	protected ImageView mCapturedBill;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_review_bill);
        displayImage();
    }
    
    private void displayImage() {
    	
    	// retrieve the file path from the intent
    	String path = (String) getIntent().getExtras().get("path");
    	// create a bitmap image from the file path
    	Bitmap billImage = BitmapFactory.decodeFile(path);
    	
    	// rotate the image 90 degrees clockwise
    	Matrix rotateMatrix = new Matrix();
    	rotateMatrix.postRotate(90);
    	Bitmap rotatedImage = Bitmap.createBitmap(billImage, 0, 0, billImage.getWidth(), 
    						billImage.getHeight(), rotateMatrix, false);
    	
        // create ImageView
        this.mCapturedBill = (ImageView) findViewById(R.id.review_bill);
        this.mCapturedBill.setImageBitmap(rotatedImage);
        this.mCapturedBill.setVisibility(View.VISIBLE);
    }
    
    public void retakePictureClick(View view) {
    	// kill the ReviewBillActivity, simulating a back button press
    	this.finish();
    }
    
    public void uploadBillClick(View view) {
    	
    }
}
