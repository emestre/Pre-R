package com.prer;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import android.net.Uri;
import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class ProcedureInformation extends SherlockActivity {
	protected TextView procedureNameView;
	protected TextView procedureHospitalView;
	protected TextView procedurePriceView;
	protected Button hospitalPhone;
	protected Button hospitalDirections;
	protected Button hospitalWebsite;
	
	private String hospitalName;
	private String hospitalAddr;
	private String hospitalPhoneNumber;
	private String hospitalWebsiteUrl;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_procedure_information);

		procedureNameView = (TextView) findViewById(R.id.procedure_info_name);
		procedureHospitalView = (TextView) findViewById(R.id.procedure_info_hospital);
		procedurePriceView = (TextView) findViewById(R.id.procedure_info_price);

		hospitalPhone = (Button)findViewById(R.id.hospital_phone);
		hospitalDirections = (Button)findViewById(R.id.hospital_directions);
		hospitalWebsite = (Button)findViewById(R.id.hospital_website);
		
		procedureNameView.setText(getIntent().getExtras().getString("PROCEDURE"));
		procedurePriceView.setText(getIntent().getExtras().getString("PRICE"));
		
		hospitalName = getIntent().getExtras().getString("HOSPITAL_NAME");
		hospitalAddr = getIntent().getExtras().getString("HOSPITAL_ADDR");
		hospitalPhoneNumber = getIntent().getExtras().getString("HOSPITAL_PHONE");
		hospitalWebsiteUrl =  getIntent().getExtras().getString("HOSPITAL_WEBSITE");
		procedureHospitalView.setText(hospitalName + hospitalAddr);
		

		hospitalPhone.setText("Call Number");
		hospitalDirections.setText("Get Directions");
		hospitalWebsite.setText("Navigate to Website");

		initListeners();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.procedure_information, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch(item.getItemId())
		{
		case R.id.procedure_information_home_button:
			Intent intent = new Intent(this, HomeScreenActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); 
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent);
			finish();
			return true;
			
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	private void initListeners () {
		hospitalPhone.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Uri uri = Uri.parse("tel:" + hospitalPhoneNumber);
				Intent intent = new Intent(Intent.ACTION_VIEW, uri);
				startActivity(intent);
			}
		});
		
		hospitalDirections.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Uri uri = Uri.parse("geo:0,0?q=" + hospitalAddr);
				Intent intent = new Intent(Intent.ACTION_VIEW, uri);
				startActivity(intent);
			}
		});
		
		hospitalWebsite.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Uri uri = Uri.parse(hospitalWebsiteUrl);
				Intent intent = new Intent(Intent.ACTION_VIEW, uri);
				startActivity(intent);
			}
		});
	}

	
	
}
