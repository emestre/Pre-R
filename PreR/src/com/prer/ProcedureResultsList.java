package com.prer;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.json.JSONStringer;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

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
		getData();
		adapter = new ProcedureViewAdapter(this, filteredProcedures);
		setContentView(R.layout.activity_procedure_results_list);
		procedureListView = (ListView) findViewById(R.id.procedure_listView);
		procedureListView.setAdapter(adapter);
	}

	private void getData() {

		String json = null;
		try {
			InputStream is = getAssets().open(procedureName + ".json");
			int size = is.available();
			byte[] buffer = new byte[size];
			is.read(buffer);
			is.close();
			json = new String(buffer, "UTF-8");

			Gson gson = new Gson();
			ProcedureSet procedureSet = gson.fromJson(json, ProcedureSet.class);
			Procedure[] procs = procedureSet.getProcedures();
			
			for (Procedure proc : procs) {
				if (proc.zipcode.equals(procedureZip)) {
					filteredProcedures.add(proc);
				}
			}
			
			Collections.sort(filteredProcedures, new PriceComparator());
			
		} catch (IOException ex) {
			ex.printStackTrace();
		}
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
