package com.prer;

import java.io.File;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;

public class ReviewBillActivity extends Activity {
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.review_bill_layout);
        displayImage();
    }
    
    private void displayImage() {
    	File pic = (File) getIntent().getExtras().get(MediaStore.EXTRA_OUTPUT);
    	
		// create ImageView
		ImageView image = (ImageView)findViewById(R.id.review_bill);
		image.setImageURI(Uri.fromFile(pic));
		image.setVisibility(View.VISIBLE);
    }
}
