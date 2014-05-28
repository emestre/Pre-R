package com.prer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.apache.commons.lang3.text.WordUtils;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class ProcedureResultsListActivity extends SherlockActivity {

	private ArrayList<Procedure> filteredProcedures;
	private ProcedureViewAdapter adapter;
	private String procedureName;
	private double searchLat;
	private double searchLong;
	private ListView procedureListView;
	
	private int searchRadius = 25;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		procedureName = getIntent().getExtras().getString("PROCEDURE");
		searchLat = getIntent().getExtras().getDouble("LATITUDE");
		searchLong = getIntent().getExtras().getDouble("LONGITUDE");
		filteredProcedures = new ArrayList<Procedure>();
		createProcedureList();
	}

	private void createProcedureList() {
		// query server for nearest instances
		RestClient query = new RestClient();
		query.getProceduresByName(procedureName, searchRadius, searchLat, searchLong, new GetResponseCallback() {
			@Override
			void onDataReceived(String response) {
				parseProceduresFromResponse(response);
				populateProcedureList();
			}
		});
	}
	
	private void parseProceduresFromResponse(String response) {
		Log.i("RESPONSE", response);
		JsonElement elem = new JsonParser().parse(response);
		JsonArray array = elem.getAsJsonArray();
		String hospital_addr = null;
		String hospital_name = null;
		String cpt_code = null;
		
		for (int index = 0; index < array.size(); ++index) {
			elem = array.get(index);
			JsonObject obj = elem.getAsJsonObject();
			
			JsonElement innerElem = obj.get("service");
			JsonObject service = innerElem.getAsJsonObject();
			
			innerElem = obj.get("hospital");
			JsonObject hospital = innerElem.getAsJsonObject();
			
			Procedure procedure = 
					new Procedure(WordUtils.capitalize(service.get("name").getAsString()), hospital.get("zip_code").getAsString());
			procedure.setPrice("$" + obj.get("cost").getAsString());
			procedure.setDistance(obj.get("distance").getAsString() + " miles");
			hospital_name = WordUtils.capitalize(hospital.get("name").getAsString());
			hospital_addr = "\n" + WordUtils.capitalize(hospital.get("street").getAsString()); 
			hospital_addr += "\n" + WordUtils.capitalize(hospital.get("city").getAsString()); 
			hospital_addr +=  " " + hospital.get("zip_code").getAsString(); 
			cpt_code = service.get("cpt_code").getAsString();
			procedure.setHospitalPhone(hospital.get("phone_number").getAsString());
			procedure.setHospitalWebsite(hospital.get("url").getAsString());
			procedure.setHospitalName(hospital_name);
			procedure.setHospital(hospital_addr);
			procedure.setCptCode(cpt_code);
			filteredProcedures.add(procedure);
		}
		Collections.sort(filteredProcedures, new PriceComparator());
	}
	
	private void populateProcedureList() {
		final Context context = getApplicationContext();
		
		adapter = new ProcedureViewAdapter(context, filteredProcedures);
		this.setContentView(R.layout.activity_procedure_results_list);
		procedureListView = (ListView) findViewById(R.id.procedure_listView);
		procedureListView.setAdapter(adapter);
		
		procedureListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				Intent intent = new Intent(context, ProcedureInformationActivity.class);
				intent.putExtra("PROCEDURE", 
						((TextView) arg1.findViewById(R.id.name_textView)).getText().toString());
				intent.putExtra("PRICE",  
						((TextView) arg1.findViewById(R.id.price_textView)).getText().toString());
				
				Procedure procedure = ((ProcedureView)arg1).getProcedure();
				intent.putExtra("HOSPITAL_NAME",  procedure.getHospitalName());
				intent.putExtra("HOSPITAL_ADDR",  procedure.getHospitalAddr());
				intent.putExtra("HOSPITAL_PHONE", procedure.getHospitalPhoneNumber());
				intent.putExtra("HOSPITAL_WEBSITE", procedure.getHospitalWebsite());
				startActivity(intent);
			}
		});
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_sort_price:
				Collections.sort(filteredProcedures, new PriceComparator());
				adapter.notifyDataSetChanged();
				break;
				
			case R.id.action_sort_distance:
				Collections.sort(filteredProcedures, new DistanceComparator());
				adapter.notifyDataSetChanged();
				break;
				
			default:
				break;
		}
		
		return true;
	}

	public class PriceComparator implements Comparator<Procedure> {
		@Override
		public int compare(Procedure o1, Procedure o2) {
			return o1.price.compareTo(o2.price);
		}
	}

	public class DistanceComparator implements Comparator<Procedure> {
		@Override
		public int compare(Procedure o1, Procedure o2) {
			return o1.distance.compareTo(o2.distance);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// inflate the menu, this adds items to the action bar if it is present.
		this.getSupportMenuInflater().inflate(R.menu.procedure_results_list, menu);
		return true;
	}

}
