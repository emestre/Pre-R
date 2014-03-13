package com.prer;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang3.text.WordUtils;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class SearchActivity extends SherlockActivity {

	private static final long MIN_TIME_BW_UPDATES = 30000;
	private static final float MIN_DISTANCE_CHANGE_FOR_UPDATES = 1000;
	protected ArrayAdapter<String> resultsAdapter;
	protected ArrayList<String> filteredResults;
	protected ListView results_listView;
	protected Button searchButton;
	protected EditText searchEditText;
	protected AutoCompleteTextView addressEditText;
	protected TextView noResultsView;
	protected Button clearAddressButton;

	private Context context;
	private String currentAddr;
	private LocationManager locationManager;

	private ArrayList<String> cities;
	private String lastSearch;
	private int startFrom;
	
	private Toast toast;

	private final LocationListener mLocationListener = new LocationListener() {
		@Override
		public void onLocationChanged(final Location location) {
		}

		@Override
		public void onProviderDisabled(String provider) {
		}

		@Override
		public void onProviderEnabled(String provider) {
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
		}
	};
	private int endAt;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		context = getApplicationContext();

		initLayout();

		filteredResults = new ArrayList<String>();
		resultsAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, filteredResults);

		initListeners();
		getCities();

		AutoCompleteTextView autoCompView = (AutoCompleteTextView) findViewById(R.id.zip_code_edittext);
		autoCompView.setAdapter(new PlacesAutoCompleteAdapter(this,
				R.layout.list_item));

		results_listView.setAdapter(resultsAdapter);
		
		toast = new Toast(context);
	}

	private void initListeners() {

		clearAddressButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				addressEditText.setText("");
			}

		});

		results_listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				Intent intent = new Intent(context, ProcedureResultsList.class);
				intent.putExtra("PROCEDURE",
						((TextView) arg1.findViewById(android.R.id.text1))
								.getText().toString().toLowerCase());
				intent.putExtra("ADDRESS", currentAddr);
				startActivity(intent);
			}

		});

		addressEditText.addTextChangedListener(new TextWatcher() {

			@Override
			public void afterTextChanged(Editable s) {
				String zipcodeText = addressEditText.getText().toString();
				if(!isOnline()) {
					showToast("Please enable a network connection on your device");
				//	addressEditText.setText("");
					return;
				}
				if(!zipcodeText.isEmpty())
					setLocation(zipcodeText);
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}

		});

		searchEditText.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable arg0) {
				String zipcodeText = addressEditText.getText().toString();
				String searchText = searchEditText.getText().toString();
				if (searchText.isEmpty()) {
					filteredResults.clear();
					resultsAdapter.notifyDataSetChanged();
					noResultsView.setVisibility(View.GONE);
					return;
				}
				if(!isOnline()) {
					showToast("Please enable a network connection on your device");
					return;
				}
				if(zipcodeText.isEmpty() && getLocation() == null) {
					showToast("Please enter a location to search, or enable your GPS to use your current location");
					searchEditText.setText("");
				} else if (isNumeric(searchText) && searchText.length() == 5) {
					getResultsByCode(zipcodeText, searchText);
				} else if (!isNumeric(searchText)) {
					getResultsByName(zipcodeText, searchText);
				} else {
					filteredResults.clear();
				}

			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}

		});

		searchButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String zipcodeText = addressEditText.getText().toString();
				String searchText = searchEditText.getText().toString();
				if (searchText.isEmpty()) {
					Toast.makeText(
							SearchActivity.this,
							"Please enter at least 1 character in the search field",
							Toast.LENGTH_LONG).show();
					return;
				}
				getResultsByName(zipcodeText, searchText);
			}
		});
	}
	
	private boolean isOnline() {
	    ConnectivityManager cm =
	        (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo netInfo = cm.getActiveNetworkInfo();
	    if (netInfo != null && netInfo.isConnectedOrConnecting()) {
	        return true;
	    }
	    return false;
	}
	
	private void showToast(String msg) {
        try{ 
        	if(toast.getView().isShown())    
        		return;
        } catch (Exception e) {         
            
        }
        toast = Toast.makeText(SearchActivity.this, msg, Toast.LENGTH_LONG);
        toast.show();  //finally display it
    }

	private void getCities() {
		BufferedReader reader;
		cities = new ArrayList<String>();
		try {
			reader = new BufferedReader(new InputStreamReader(getAssets().open(
					"cities.txt")));
			String line = null;
			while ((line = reader.readLine()) != null) {
				cities.add(line);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static boolean isNumeric(String str) {
		return str.matches("-?\\d+(\\.\\d+)?");
	}

	private Address getLocationFromAddress(String addr) {

		final Geocoder geocoder = new Geocoder(this);
		try {
			List<Address> addresses = geocoder.getFromLocationName(addr, 1);
			if (addresses != null && !addresses.isEmpty()) {
				Address address = addresses.get(0);
				return address;
			} else {
				Toast.makeText(this, "Unable to geocode zipcode",
						Toast.LENGTH_LONG).show();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private void parseProceduresFromResponse(String response) {
		filteredResults.clear();
		noResultsView.setVisibility(View.GONE);
		JsonElement elem = new JsonParser().parse(response);

		JsonArray array = elem.getAsJsonArray();
		for (int index = 0; index < array.size(); ++index) {
			elem = array.get(index);
			JsonObject obj = elem.getAsJsonObject();
			Log.i("SERVICE", obj.toString());
			JsonElement innerElem = obj.get("name");
			String name = innerElem.getAsString();

			Procedure procedure = new Procedure(WordUtils.capitalize(name), "");
			if (!filteredResults.contains(procedure.name))
				filteredResults.add(procedure.name);
		}
		if (filteredResults.size() == 0)
			noResultsView.setVisibility(View.VISIBLE);
	}

	private void parseSingleServiceFromReponse(String response) {
		filteredResults.clear();
		noResultsView.setVisibility(View.GONE);
		JsonElement elem = new JsonParser().parse(response);
		JsonObject obj = elem.getAsJsonObject();
		JsonElement innerElem = obj.get("name");
		if (innerElem == null) {
			noResultsView.setVisibility(View.VISIBLE);
			return;
		}
		String name = innerElem.getAsString();

		Procedure procedure = new Procedure(WordUtils.capitalize(name), "");
		if (!filteredResults.contains(procedure.name))
			filteredResults.add(procedure.name);
	}

	protected void initLayout() {
		setContentView(R.layout.main_search_layout);

		results_listView = (ListView) findViewById(R.id.results_list);
		searchButton = (Button) findViewById(R.id.search_button);
		searchEditText = (EditText) findViewById(R.id.search_edittext);
		addressEditText = (AutoCompleteTextView) findViewById(R.id.zip_code_edittext);
		noResultsView = (TextView) findViewById(R.id.no_results_view);
		clearAddressButton = (Button) findViewById(R.id.clear_address_button);
		addressEditText.setThreshold(1);

		Location loc = getLocation();
		if (loc != null) {
			addressEditText.setHint("Using default location");
			searchEditText.requestFocus();
		}
	}

	private void getResultsByName(String zipcodeText, String searchText) {
		setLocation(zipcodeText);
		RestClient query = new RestClient();
		query.getServicesByName(searchText, new GetResponseCallback() {
			@Override
			void onDataReceived(String response) {
				parseProceduresFromResponse(response);
				resultsAdapter.notifyDataSetChanged();
			}
		});
	}

	private void getResultsByCode(String zipcodeText, String searchText) {
		setLocation(zipcodeText);
		RestClient query = new RestClient();
		query.getServicesByCptCode(searchText, new GetResponseCallback() {
			@Override
			void onDataReceived(String response) {
				parseSingleServiceFromReponse(response);
				resultsAdapter.notifyDataSetChanged();
			}
		});
	}

	private void setLocation(String zipcodeText) {
		Address addr = null;
		if (!zipcodeText.isEmpty()) {
			addr = getLocationFromAddress(zipcodeText);
			currentAddr = zipcodeText;
		} else {
			Location location = getLocation();
			Geocoder geocoder = new Geocoder(SearchActivity.this,
					Locale.getDefault());
			List<Address> addresses;
			try {
				addresses = geocoder.getFromLocation(location.getLatitude(),
						location.getLongitude(), 1);
				addr = addresses.get(0);
				currentAddr = addr.getPostalCode();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}

	private Location getLocation() {
		Location location = null;
		try {
			locationManager = (LocationManager) getApplicationContext()
					.getSystemService(LOCATION_SERVICE);

			// getting GPS status
			boolean isGPSEnabled = locationManager
					.isProviderEnabled(LocationManager.GPS_PROVIDER);

			// getting network status
			boolean isNetworkEnabled = locationManager
					.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

			if (isNetworkEnabled) {
				locationManager.requestLocationUpdates(
						LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES,
						MIN_DISTANCE_CHANGE_FOR_UPDATES, mLocationListener);
				Log.d("Network", "Network Enabled");
				if (locationManager != null) {
					location = locationManager
							.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
				}
			}
		if (isGPSEnabled) {
				if (location == null) {
					locationManager.requestLocationUpdates(
							LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES,
							MIN_DISTANCE_CHANGE_FOR_UPDATES, mLocationListener);
					Log.d("GPS", "GPS Enabled");
					if (locationManager != null) {
						location = locationManager
								.getLastKnownLocation(LocationManager.GPS_PROVIDER);
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return location;
	}

	private ArrayList<String> autocomplete(String typedText) {
		ArrayList<String> results = new ArrayList<String>();
		String oldText = typedText.substring(0, typedText.length() - 1);
		int start = 0;
		int end = cities.size();
		if (oldText.equals(lastSearch)) {
			start = startFrom;
			end = endAt + 1;
		}

		for (int i = start; i < end; i++) {
			String city = cities.get(i);
			if (city.toLowerCase().startsWith(typedText.toLowerCase())) {
				String[] splitCity = city.split(",");
				city = WordUtils.capitalize(splitCity[0].toLowerCase()) + ","
						+ splitCity[1];
				results.add(city);
			}

		}

		lastSearch = typedText;
		startFrom = cities.indexOf(results.get(0).toUpperCase());
		endAt = cities.indexOf(results.get(results.size() - 1).toUpperCase());
		return results;
	}

	private class PlacesAutoCompleteAdapter extends ArrayAdapter<String>
			implements Filterable {
		private ArrayList<String> resultList;

		public PlacesAutoCompleteAdapter(Context context, int textViewResourceId) {
			super(context, textViewResourceId);
		}

		@Override
		public int getCount() {
			return resultList.size();
		}

		@Override
		public String getItem(int index) {
			return resultList.get(index);
		}

		@Override
		public Filter getFilter() {
			Filter filter = new Filter() {
				@Override
				protected FilterResults performFiltering(CharSequence constraint) {
					FilterResults filterResults = new FilterResults();
					if (constraint != null) {
						// Retrieve the autocomplete results.
						resultList = autocomplete(constraint.toString());

						// Assign the data to the FilterResults
						filterResults.values = resultList;
						filterResults.count = resultList.size();
					}
					return filterResults;
				}

				@Override
				protected void publishResults(CharSequence constraint,
						FilterResults results) {
					if (results != null && results.count > 0) {
						notifyDataSetChanged();
					} else {
						notifyDataSetInvalidated();
					}
				}
			};
			return filter;
		}
	}
}
