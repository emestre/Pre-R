package com.prer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang3.text.WordUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import android.location.Geocoder;
import android.location.Address;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class ProcedureResultsList extends Activity {

	private ArrayList<Procedure> filteredProcedures;
	private ProcedureViewAdapter adapter;
	private String procedureName;
	private String procedureAddr;
	private ListView procedureListView;
	
	private int radius = 25;
	private int maxResults = 2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		procedureName = getIntent().getExtras().getString("PROCEDURE");
		procedureAddr = getIntent().getExtras().getString("ADDRESS");
		filteredProcedures = new ArrayList<Procedure>();
		createProcedureList(this);
		
	}

	private void createProcedureList(Context context) {
		try {
			// Get lat,long from location
			Geocoder geo = new Geocoder(context);
			List<Address> addresses = geo.getFromLocationName(procedureAddr, maxResults);
			Address location = addresses.get(0);
			
			// Query server for nearest instances
			RestClient query = new RestClient();
			query.getProceduresByName(procedureName, radius, 
					location.getLatitude(), location.getLongitude(), 
					new GetResponseCallback() {
				@Override
				void onDataReceived(String response) {
					parseProceduresFromResponse(response);
					populateProcedureList();
				}
			});
		} catch (IOException e) {
			// TODO: retry/exit gracefully...do something
			e.printStackTrace();
		}
	}
	
	private void parseProceduresFromResponse(String response) {
		Log.i("RESPONSE", response);
		JsonElement elem = new JsonParser().parse(response);
		JsonArray array = elem.getAsJsonArray();
		String hospital_addr = null;
		String hospital_name = null;
		String cpt_code = null;
		for(int index = 0; index < array.size(); ++index) {
			elem = array.get(index);
			JsonObject obj = elem.getAsJsonObject();
			
			JsonElement innerElem = obj.get("service");
			JsonObject service = innerElem.getAsJsonObject();
			
			innerElem = obj.get("hospital");
			JsonObject hospital = innerElem.getAsJsonObject();
			
			Procedure procedure = 
					new Procedure(WordUtils.capitalize(service.get("name").getAsString()),
							hospital.get("zip_code").getAsString());
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
		setContentView(R.layout.activity_procedure_results_list);
		procedureListView = (ListView) findViewById(R.id.procedure_listView);
		procedureListView.setAdapter(adapter);
		
		procedureListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				Intent intent = new Intent(context, ProcedureInformation.class);
				intent.putExtra("PROCEDURE", ((TextView) arg1
						.findViewById(R.id.name_textView)).getText().toString());
				intent.putExtra("PRICE",  ((TextView) arg1
						.findViewById(R.id.price_textView)).getText().toString());
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
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.procedure_results_list, menu);
		return true;
	}

}
