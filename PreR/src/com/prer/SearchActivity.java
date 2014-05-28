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
import android.view.View.OnFocusChangeListener;
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
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationClient;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class SearchActivity extends SherlockActivity
	implements ConnectionCallbacks, OnConnectionFailedListener, LocationListener {
	
	private static final String TAG = "SearchActivity";
	
	private static final Locale DEFAULT_LOCALE = Locale.getDefault();
	private static final long MIN_UPDATE_MSEC = 1000;
	private static final float MIN_DISTANCE_CHANGE = 0;
	
	protected ArrayAdapter<String> resultsAdapter;
	protected ArrayList<String> filteredResults;
	protected ListView results_listView;
	protected EditText searchEditText;
	protected AutoCompleteTextView addressEditText;
	protected TextView noResultsView;
	protected Button clearAddressButton;

	private Location currentLocation;
	private Location searchLocation;
	private LocationClient locationClient;
	private LocationManager locationManager;
	private boolean isBadLocation = false;

	private ArrayList<String> cities;
	private String lastSearch;
	private int startFrom;
	private int endAt;
	
	private Context context;
	private Toast toast;
	
	@Override
    protected void onStart() {
        super.onStart();
        // Connect the client.
        locationClient.connect();
    }

    /*
     * Called when the Activity is no longer visible.
     */
    @Override
    protected void onStop() {
        // Disconnecting the client invalidates it.
    	if (locationClient.isConnected()) {
    		locationClient.disconnect();
    	}
        locationManager.removeUpdates(this);
        super.onStop();
    }

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		context = getApplicationContext();
		locationClient = new LocationClient(this, this, this);
		locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);

		initLayout();

		filteredResults = new ArrayList<String>();
		resultsAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, filteredResults);

		initListeners();
		getCities();

		AutoCompleteTextView autoCompView = (AutoCompleteTextView) findViewById(R.id.zip_code_edittext);
		autoCompView.setAdapter(new PlacesAutoCompleteAdapter(this, R.layout.list_item));

		results_listView.setAdapter(resultsAdapter);
		toast = new Toast(context);
	}
	
	protected void initLayout() {
		setContentView(R.layout.activity_search);

		results_listView = (ListView) findViewById(R.id.results_list);
		searchEditText = (EditText) findViewById(R.id.search_edittext);
		addressEditText = (AutoCompleteTextView) findViewById(R.id.zip_code_edittext);
		addressEditText.setThreshold(1);
		noResultsView = (TextView) findViewById(R.id.no_results_view);
		clearAddressButton = (Button) findViewById(R.id.clear_address_button);
	}

	private void initListeners() {

		clearAddressButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				addressEditText.selectAll();
			}
		});

		results_listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				if (isBadLocation) {
					showToast("Please enter a valid location to search from.");
					addressEditText.requestFocus();
					filteredResults.clear();
				}
				else {
					Intent intent = new Intent(context, ProcedureResultsListActivity.class);
					intent.putExtra("PROCEDURE",
							((TextView) arg1.findViewById(android.R.id.text1)).getText().toString().toLowerCase(DEFAULT_LOCALE));
					intent.putExtra("LATITUDE", searchLocation.getLatitude());
					intent.putExtra("LONGITUDE", searchLocation.getLongitude());
					startActivity(intent);
				}
			}
		});

		addressEditText.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (!hasFocus) {
					String zipcodeText = addressEditText.getText().toString();
										
					if (!isOnline()) {
						showToast("Please enable a network connection on your device");
						return;
					}
					
					if (!zipcodeText.isEmpty()) {
						setLocation(zipcodeText);
					}
					else {
						isBadLocation = true;
					}
				}
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
				
				if (!isOnline()) {
					showToast("Network connection needed to search.");
					return;
				}
				
				if (isNumeric(searchText) && searchText.length() == 5) {
					getResultsByCode(zipcodeText, searchText);
				} 
				else if (!isNumeric(searchText)) {
					getResultsByName(zipcodeText, searchText);
				}
				else {
					filteredResults.clear();
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {}
		});
	}
	
	private void setLocation(String zipcodeText) {	
		Address address = null;
		
		if (!zipcodeText.isEmpty()) {
			if (zipcodeText.equals("Current Location")) {
				this.searchLocation = this.currentLocation;
				this.isBadLocation = false;
			}
			else {
				address = getLocationFromAddress(zipcodeText);
				if (address != null) {
					Log.d(TAG, "address: " + address.toString());
					
					if (this.searchLocation != null) {
						this.searchLocation.reset();
					}
					else {
						this.searchLocation = new Location("custom");
					}
					this.searchLocation.setLatitude(address.getLatitude());
					this.searchLocation.setLongitude(address.getLongitude());
					
					this.isBadLocation = false;
				}
				else {
					this.isBadLocation = true;
				}
			}
		}
		else {
			// empty address field, user must enter location
			this.isBadLocation = true;
		}
		
		if (this.searchLocation != null) {
			Log.d(TAG, "search location set to: " + this.searchLocation.toString());
		}
		else {
			Log.d(TAG, "search location set to NULL");
		}
	}
	
	private Address getLocationFromAddress(String addr) {

		final Geocoder geocoder = new Geocoder(this);
		try {
			List<Address> addresses = geocoder.getFromLocationName(addr, 1);
			
			if (addresses != null && !addresses.isEmpty()) {
				return addresses.get(0);
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	private boolean isOnline() {
	    ConnectivityManager cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo netInfo = cm.getActiveNetworkInfo();
	    
	    if (netInfo != null && netInfo.isConnectedOrConnecting()) {
	        return true;
	    }
	    return false;
	}
	
	private void showToast(String msg) {
        try { 
        	if (toast.getView().isShown())    
        		return;
        } catch (Exception e) {}
        
        toast = Toast.makeText(SearchActivity.this, msg, Toast.LENGTH_LONG);
        toast.show();
    }

	private void getCities() {
		BufferedReader reader;
		cities = new ArrayList<String>();
		
		try {
			reader = new BufferedReader(new InputStreamReader(getAssets().open("cities.txt")));
			String line = null;
			
			while ((line = reader.readLine()) != null) {
				cities.add(line);
			}
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static boolean isNumeric(String str) {
		return str.matches("-?\\d+(\\.\\d+)?");
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

	private void getResultsByName(String zipcodeText, String searchText) {		
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
		RestClient query = new RestClient();
		query.getServicesByCptCode(searchText, new GetResponseCallback() {
			@Override
			void onDataReceived(String response) {
				parseSingleServiceFromReponse(response);
				resultsAdapter.notifyDataSetChanged();
			}
		});
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
		
		// add current location to list of selections if it's known
		if (this.currentLocation != null)
			results.add("Current Location");
		
		String city;
		for (int i = start; i < end; i++) {
			city = cities.get(i);
			if (city.toLowerCase(DEFAULT_LOCALE).startsWith(typedText.toLowerCase(DEFAULT_LOCALE))) {
				String[] splitCity = city.split(",");
				city = WordUtils.capitalize(splitCity[0].toLowerCase(DEFAULT_LOCALE)) + "," + splitCity[1];
				results.add(city);
			}
		}

		lastSearch = typedText;
		// setting the indices based on whether current location is included in list
		if (this.currentLocation != null) {
			if (results.size() == 1) {
				startFrom = 1;
				endAt = -1;
			}
			else {
				startFrom = cities.indexOf(results.get(1).toUpperCase(DEFAULT_LOCALE));
				endAt = cities.indexOf(results.get(results.size() - 1).toUpperCase(DEFAULT_LOCALE));
			}
		}
		else {
			if (results.size() > 0) {
				startFrom = cities.indexOf(results.get(0).toUpperCase(DEFAULT_LOCALE));
				endAt = cities.indexOf(results.get(results.size() - 1).toUpperCase(DEFAULT_LOCALE));
			}
			else {
				startFrom = 1;
				endAt = -1;
			}
		}
		
		return results;
	}

	private class PlacesAutoCompleteAdapter extends ArrayAdapter<String> implements Filterable {
		private ArrayList<String> resultList;

		public PlacesAutoCompleteAdapter(Context context, int textViewResourceId) {
			super(context, textViewResourceId);
		}

		@Override
		public int getCount() {
			if (resultList != null)
				return resultList.size();
			else
				return 0;
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
					ArrayList<String> list;
					
					if (constraint != null) {
						// Retrieve the auto complete results.
						list = autocomplete(constraint.toString());
					}
					else {
						list = new ArrayList<String>();
					}
					// Assign the filter results
					filterResults.values = list;
					filterResults.count = list.size();
					
					return filterResults;
				}

				@SuppressWarnings("unchecked")
				@Override
				protected void publishResults(CharSequence constraint, FilterResults results) {
					resultList = (ArrayList<String>) results.values;
					
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
	
	// START OF LOCATION CALL BACKS *******************************
	
	/*
     * Called by Location Services when the request to connect the
     * client finishes successfully. At this point, you can
     * request the current location or start periodic updates
     */
    @Override
    public void onConnected(Bundle dataBundle) {
        // connected to google play services, get location
    	Log.d(TAG, "CONNECTED to google play location services");
        
        if (this.locationClient.isConnected()) {
        	this.currentLocation = this.locationClient.getLastLocation();
        }
        
        if (this.currentLocation != null) {
        	Log.d(TAG, "last location: " + currentLocation.toString());
        	if (addressEditText.getText().toString().isEmpty()) {
        		addressEditText.requestFocus();
	        	addressEditText.setText("Current Location");
				searchEditText.requestFocus();
        	}
        }
        else {
        	Log.d(TAG, "last location returned NULL");
        	this.locationClient.disconnect();
        	this.getLocationUsingManager();
        }
    }
    
    /*
     * Called by Location Services if the connection to the
     * location client drops because of an error.
     */
    @Override
    public void onDisconnected() {
        // no longer connected to google play services, use location manager
    	Log.d(TAG, "DISCONNECTED from google play location services");
    }

    /*
     * Called by Location Services if the attempt to
     * Location Services fails.
     */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    	// could not connect to google play services, use location manager
    	Log.d(TAG, "google play location services connection FAILED");
    	Log.d(TAG, "error code: " + connectionResult.getErrorCode());
    	
    	this.getLocationUsingManager();
    }
    
    private void getLocationUsingManager() {				
		try {
			locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);

			// getting network status
			boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
			// getting GPS status
			boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

			Log.d(TAG, "GPS enabled: " + isGPSEnabled);
			Log.d(TAG, "Network location enabled: " + isNetworkEnabled);

			if (isNetworkEnabled) {
				this.currentLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
				
				if (this.currentLocation != null) {
					if (addressEditText.getText().toString().isEmpty()) {
						addressEditText.requestFocus();
						addressEditText.setText("Current Location");
						searchEditText.requestFocus();
					}
				}
				
				locationManager.requestLocationUpdates(
						LocationManager.NETWORK_PROVIDER, MIN_UPDATE_MSEC, MIN_DISTANCE_CHANGE, this);
			}
			else if (isGPSEnabled) {
				this.currentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
				
				if (this.currentLocation != null) {
					if (addressEditText.getText().toString().isEmpty()) {
						addressEditText.requestFocus();
						addressEditText.setText("Current Location");
						searchEditText.requestFocus();
					}
				}
				
				locationManager.requestLocationUpdates(
						LocationManager.GPS_PROVIDER, MIN_UPDATE_MSEC, MIN_DISTANCE_CHANGE, this);
			}
			else {
				// location services are disabled
				// obtaining user location is not possible if we get here
				this.showToast("Enabling your device's location services allows you to search from your current location.");
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
    
    // location manager listener call backs
	@Override
	public void onLocationChanged(final Location location) {
		if (this.currentLocation != null) {
			this.currentLocation.set(location);
		}
		else {
			this.currentLocation = new Location(location);
		}
		
		if (addressEditText.getText().toString().equals("Current Location")) {
			this.searchLocation = this.currentLocation;
		}
		
		locationManager.removeUpdates(this);
		Log.d(TAG, "updated current location: " + currentLocation.toString());
	}

	@Override
	public void onProviderDisabled(String provider) {}

	@Override
	public void onProviderEnabled(String provider) {}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {}
}
