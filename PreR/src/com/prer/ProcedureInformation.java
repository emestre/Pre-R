package com.prer;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
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
		
		procedureNameView.setText(getIntent().getExtras().getString("PROCEDURE"));
		procedurePriceView.setText(getIntent().getExtras().getString("PRICE"));
		procedureHospitalView.setText(getIntent().getExtras().getString("HOSPITAL") + getResources().getString(R.string.hospital_info));
		

		hospitalPhone.setText("Call Number");
		hospitalDirections.setText("Get Directions");
		hospitalWebsite.setText("Go To Website");

	}
	
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.procedure_information, menu);
		return true;
	}

	
}
