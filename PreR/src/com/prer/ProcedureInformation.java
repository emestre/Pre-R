package com.prer;

import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class ProcedureInformation extends Activity {
	protected TextView procedureNameView;
	protected TextView procedureHospitalView;
	protected TextView procedurePriceView;
	protected Button hospitalPhone;
	protected Button hospitalDirections;
	protected Button hospitalWebsite;

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
		
		//procedureNameView.setText(getIntent().getExtras().getString("PROCEDURE"));
		//procedurePriceView.setText(getIntent().getExtras().getString("PRICE"));
		//procedureHospitalView.setText(getIntent().getExtras().getString("HOSPITAL"));
		

		hospitalPhone.setText("Call Number");
		hospitalDirections.setText("Get Directions");
		hospitalWebsite.setText("Search for Website");

		initListeners();
	}
	
	private void initListeners () {
		hospitalPhone.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Uri uri = Uri.parse("tel:0123456789");
				Intent intent = new Intent(Intent.ACTION_VIEW, uri);
				startActivity(intent);
			}
		});
		
		hospitalDirections.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Uri uri = Uri.parse("geo:0,0?q=1+Grand+Ave+San+Luis+Obispo");
				Intent intent = new Intent(Intent.ACTION_VIEW, uri);
				startActivity(intent);
			}
		});
		
		hospitalWebsite.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Uri uri = Uri.parse("http://google.com/#q=Hospital");
				Intent intent = new Intent(Intent.ACTION_VIEW, uri);
				startActivity(intent);
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.procedure_information, menu);
		return true;
	}

	
}
