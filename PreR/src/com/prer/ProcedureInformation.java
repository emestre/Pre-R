package com.prer;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.widget.Button;
import android.widget.TextView;

public class ProcedureInformation extends Activity {

	
	private String procedureName;
	private String procedureHospital;
	private String procedurePrice;
	private TextView procedureNameView;
	private TextView procedureHospitalView;
	private TextView procedurePriceView;
	private Button hospitalPhone;
	private Button hospitalDirections;
	private Button hospitalWebsite;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		String hospital_address = getResources().getString(R.string.hospital_info);
		
		setContentView(R.layout.activity_procedure_information);
		procedureName = getIntent().getExtras().getString("PROCEDURE");
		procedureHospital = getIntent().getExtras().getString("HOSPITAL");
		procedurePrice = getIntent().getExtras().getString("PRICE");

		procedureNameView = (TextView) findViewById(R.id.procedure_info_name);
		procedureHospitalView = (TextView) findViewById(R.id.procedure_info_hospital);
		procedurePriceView = (TextView) findViewById(R.id.procedure_info_price);

		hospitalPhone = (Button) findViewById(R.id.hospital_phone);
		hospitalDirections = (Button) findViewById(R.id.hospital_directions);
		hospitalWebsite = (Button) findViewById(R.id.hospital_website);
		
		procedureNameView.setText(procedureName);
		procedureHospitalView.setText(procedureHospital + hospital_address);
		procedurePriceView.setText(procedurePrice);

		hospitalPhone.setText("Call(805)555-5555");
		hospitalDirections.setText("Directions");
		hospitalWebsite.setText("www.SLOhospital.com");

	}
	
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.procedure_information, menu);
		return true;
	}

	
}
