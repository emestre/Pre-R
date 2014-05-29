package com.prer;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ProcedureView extends LinearLayout {
	
	private TextView priceTextView;
	private TextView nameTextView;
	private TextView hospitalTextView;
	private TextView distanceTextView;
	private TextView cptCodeTextView;
	private Procedure procedure;
	
	public ProcedureView(Context context, Procedure procedure) {
		super(context);
		LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.procedure_view, this, true);
		priceTextView = (TextView) findViewById(R.id.price_textView);
		nameTextView = (TextView) findViewById(R.id.name_textView);
		hospitalTextView = (TextView) findViewById(R.id.hospital_textView);
		distanceTextView = (TextView) findViewById(R.id.distance_textView);
		cptCodeTextView = (TextView) findViewById(R.id.cptCode_textView);
		setProcedure(procedure);
		requestLayout();
	}
	
	public void setProcedure(Procedure proc) {
		procedure = proc;
		priceTextView.setText(procedure.price);
		nameTextView.setText(procedure.name);
		distanceTextView.setText(procedure.distance);
		hospitalTextView.setText(procedure.hospital_name);
		cptCodeTextView.setText("CPT Code: " + procedure.cpt_code);
	}
	
	public Procedure getProcedure() {
		return procedure;
	}
	
}
