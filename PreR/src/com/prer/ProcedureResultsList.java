package com.prer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

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
	private String procedureZip;
	private ListView procedureListView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		procedureName = getIntent().getExtras().getString("PROCEDURE");
		procedureZip = getIntent().getExtras().getString("ZIPCODE");
		filteredProcedures = new ArrayList<Procedure>();
		createProcedureList();
		
	}

	private void createProcedureList() {
		ClientQuery query = new ClientQuery();
		query.getProcedures(procedureName, procedureZip, 
				new GetResponseCallback() {
			@Override
			void onDataReceived(String response) {
				parseProceduresFromResponse(response);
				populateProcedureList();
			}
		});
	}
	
	private void parseProceduresFromResponse(String response) {
		JsonElement elem = new JsonParser().parse(response);
		JsonArray array = elem.getAsJsonArray();
		for(int index = 0; index < array.size(); ++index) {
			elem = array.get(index);
			JsonObject obj = elem.getAsJsonObject();
			Procedure procedure = 
					new Procedure(obj.get("proc_name").getAsString(),
							obj.get("zip_code").getAsString());
			procedure.setPrice("$" + obj.get("cost").getAsString());
			procedure.setDistance(obj.get("dist").getAsString() + "mi");
			procedure.setHospital(obj.get("hosp_name").getAsString());
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
				intent.putExtra("HOSPITAL",  ((TextView) arg1
						.findViewById(R.id.hospital_textView)).getText().toString());
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
