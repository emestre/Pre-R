package com.prer;

import java.util.ArrayList;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast; 

//TODO Add CPT Code functionality
//TODO Write a bunch of test cases
//TODO Link results to the hospital view
public class SearchActivity extends Activity {

	protected ArrayAdapter<String> resultsAdapter; // List adapter to display
													// after search
	protected ArrayList<Procedure> mainProcedureList; // List containing set of
														// all procedures
	protected ArrayList<Procedure> filteredProcedures; // Procedures filtered by
														// zip code
	protected ArrayList<String> filteredResults; // Procedures filtered by zip
													// code and by name
	protected ListView results_listView; // ListView containing search results
	protected Button searchButton; // Search by zip code Button
	protected EditText searchEditText; // Search by name EditText
	protected EditText zipcodeEditText; // Search by zip code EditText
	private Context context;
	private String currentZip;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initLayout();
		context = getApplicationContext();

		mainProcedureList = new ArrayList<Procedure>();
		filteredProcedures = new ArrayList<Procedure>();
		filteredResults = new ArrayList<String>();
		String[] defaultProcedures = getResources().getStringArray(
				R.array.procedure_list); // Gets array of procedure names from
											// resources
		String[] defaultZipCodes = getResources().getStringArray(
				R.array.zipcode_list); // Gets array of zip codes from resources
		for (int i = 0; i < defaultProcedures.length; i++) {
			mainProcedureList.add(new Procedure(defaultProcedures[i],
					defaultZipCodes[i])); // Add all procedures into main list
		}
		resultsAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, filteredResults); // Setup
																		// ListView
																		// adapter
		results_listView.setAdapter(resultsAdapter);
		results_listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				Intent intent = new Intent(context, ProcedureResultsList.class);
				intent.putExtra("PROCEDURE", ((TextView) arg1
						.findViewById(android.R.id.text1)).getText().toString().toLowerCase());
				intent.putExtra("ZIPCODE", currentZip);
				startActivity(intent);
			}

		});
	}

	protected void initLayout() {
		setContentView(R.layout.main_search_layout); // Sets layout
		results_listView = (ListView) findViewById(R.id.results_list); // Initializes
																		// View
																		// fields
		searchButton = (Button) findViewById(R.id.search_button);
		searchEditText = (EditText) findViewById(R.id.search_bar);
		zipcodeEditText = (EditText) findViewById(R.id.zip_code_edittext);

		searchButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String zipcodeText = zipcodeEditText.getText().toString(); // Get
																			// entered
																			// zipcode
				if (!zipcodeText.isEmpty() && zipcodeText.length() == 5) {
					filteredProcedures.clear(); // Clear previous filter
					if (currentZip == null || !currentZip.equals(zipcodeText)) {
						currentZip = zipcodeText; // Save new zip
						searchEditText.setText(""); // Clear search field
					}

					for (Procedure procedure : mainProcedureList) {
						if (procedure.zipcode.equals(zipcodeText)) {
							filteredProcedures.add(procedure); // Add all
																// procedures
																// that match
						}
					}

					searchEditText.setVisibility(View.VISIBLE); // Show search
																// field
					searchEditText.requestFocus(); // Set focus
					getWindow() // Show keyboard
							.setSoftInputMode(
									WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
				} else {
					searchEditText.setText("");
					searchEditText.setVisibility(View.GONE);
					Toast toast = Toast.makeText(getApplicationContext(),
							getResources().getString(R.string.incorrect_zip),
							Toast.LENGTH_SHORT);
					toast.setGravity(Gravity.CENTER, 0, 0);
					toast.show();
				}
			}
		});

		searchEditText.addTextChangedListener(new TextWatcher() {

			@Override
			public void afterTextChanged(Editable arg0) {
				String searchText = searchEditText.getText().toString(); // Get
																			// search
																			// field
																			// text
				filteredResults.clear(); // Clear results
				if (!searchText.isEmpty()) {
					for (Procedure procedure : filteredProcedures) {
						if (procedure.name.toLowerCase().contains(
								searchText.toLowerCase())) {
							filteredResults.add(procedure.name); // Add
																	// procedures
																	// that
																	// match
						}
					}
					if (filteredResults.size() == 0)
						filteredResults.add(getResources().getString(
								R.string.no_results)); // Set default if no
														// results
				}
				resultsAdapter.notifyDataSetChanged(); // Notify adapter that
														// something has changed
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1,
					int arg2, int arg3) {
			}

			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2,
					int arg3) {
			}

		});

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.search, menu);
		return true;
	}

}
