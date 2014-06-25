package com.prer;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang3.text.WordUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
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
	protected AutoCompleteTextView locationEditText;
	protected TextView noResultsView;

	private Location currentLocation;
	private Location searchLocation;
	private LocationClient locationClient;
	private LocationManager locationManager;
	private boolean isBadLocation = true;

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
		results_listView.setAdapter(resultsAdapter);

		initListeners();
		getCities();

		toast = new Toast(context);
	}
	
	protected void initLayout() {
		setContentView(R.layout.activity_search);

		results_listView = (ListView) findViewById(R.id.results_list);
		searchEditText = (EditText) findViewById(R.id.search_edittext);
		locationEditText = (AutoCompleteTextView) findViewById(R.id.location_edittext);
		locationEditText.setThreshold(1);
		locationEditText.setAdapter(new PlacesAutoCompleteAdapter(this, R.layout.list_item));
		noResultsView = (TextView) findViewById(R.id.no_results_view);
	}

	private void initListeners() {

		results_listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				if (isBadLocation) {
					// hide the virtual keyboard
					InputMethodManager inputMethodManager = (InputMethodManager)  getSystemService(Activity.INPUT_METHOD_SERVICE);
				    inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
					
					showToast("Please enter a valid location to search from.");
					locationEditText.requestFocus();
					filteredResults.clear();
					resultsAdapter.notifyDataSetChanged();
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

		locationEditText.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (!hasFocus) {
					String locationText = locationEditText.getText().toString();
										
					if (!isOnline()) {
						showToast("A network connection is needed to search.");
						return;
					}
					
					if (!locationText.isEmpty()) {
						Log.d(TAG, "address entered: " + locationText);
						setLocation(locationText);
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
				// query the database for services matching the entered text
				getServiceSearchText();
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {}
		});
		
		searchEditText.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					// when focus returns to the search field,
					// re-query the database for matching services
					getServiceSearchText();
				}
				else {
					// when focus leaves the search field,
					// clear the auto-complete list so user can use the address field
					filteredResults.clear();
					resultsAdapter.notifyDataSetChanged();
					noResultsView.setVisibility(View.GONE);
				}
			}
			
		});
	}
	
	private void setLocation(String locationText) {			
		if (!locationText.isEmpty()) {
			if (locationText.equals("Current Location")) {
				this.searchLocation = this.currentLocation;
				this.isBadLocation = false;
			}
			else {
				// get the latitude and longitude from the entered text
				new MyGeocoderTask().execute(locationText);
			}
		}
		else {
			// empty address field, user must enter location
			this.searchLocation = null;
			this.isBadLocation = true;
		}
	}
	
	private void getServiceSearchText() {
		String searchText = searchEditText.getText().toString();
		
		if (searchText.isEmpty()) {
			filteredResults.clear();
			resultsAdapter.notifyDataSetChanged();
			noResultsView.setVisibility(View.GONE);
			return;
		}
		
		if (!isOnline()) {
			showToast("A network connection is needed to search.");
			return;
		}
		
		if (isNumeric(searchText) && searchText.length() == 5) {
			getResultsByCode(searchText);
		} 
		else if (!isNumeric(searchText)) {
			getResultsByName(searchText);
		}
		else {
			filteredResults.clear();
			resultsAdapter.notifyDataSetChanged();
		}
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
	
	private boolean isSearchTextFieldActive() {
		if (!this.searchEditText.hasFocus() || this.searchEditText.getText().toString().isEmpty()) {
			return false;
		}
		
		return true;
	}

	private void getResultsByName(String searchText) {		
		RestClient query = new RestClient();
		query.getServicesByName(searchText, new GetResponseCallback() {
			@Override
			void onDataReceived(String response) {
				if (isSearchTextFieldActive()) {
					parseProceduresFromResponse(response);
					resultsAdapter.notifyDataSetChanged();
				}
			}
		});
	}

	private void getResultsByCode(String searchText) {		
		RestClient query = new RestClient();
		query.getServicesByCptCode(searchText, new GetResponseCallback() {
			@Override
			void onDataReceived(String response) {
				if (isSearchTextFieldActive()) {
					parseSingleServiceFromReponse(response);
					resultsAdapter.notifyDataSetChanged();
				}
			}
		});
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
        }
        catch (Exception e) {}
        
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
	
	// START OF INNER CLASSES
	
	private class MyGeocoderTask extends AsyncTask<String, String, String> {
		
		protected void onPostExecute(String result) {
			if (searchLocation != null) {
				Log.d(TAG, "search location set to: " + searchLocation.toString());
			}
			else {
				Log.d(TAG, "search location set to NULL");
			}
	     }

		@Override
		protected String doInBackground(String... params) {
			String locationText = params[0];
			
			if (Geocoder.isPresent()) {
				// use Android provided API to lookup location
				getLatLngFromGeocoder(locationText);
			}
			else {
				// Android API not available on this device, make HTTP request
				// this only runs if device doesn't have Play store, Maps...
				getLatLngFromHttp(locationText);
			}
			
			return null;
		}
		
		private void getLatLngFromGeocoder(String locationText) {

			final Geocoder geocoder = new Geocoder(SearchActivity.this);
			try {
				List<Address> places = geocoder.getFromLocationName(locationText, 1);
				Address address = null;
				
				if (places != null && !places.isEmpty()) {
					address =  places.get(0);
				}
				
				if (address != null) {
					if (searchLocation != null) {
						searchLocation.reset();
					}
					else {
						searchLocation = new Location("custom");
					}
					searchLocation.setLatitude(address.getLatitude());
					searchLocation.setLongitude(address.getLongitude());
					
					isBadLocation = false;
				}
				else {
					searchLocation = null;
					isBadLocation = true;
				}
			}
			catch (IOException e) {
				getLatLngFromHttp(locationText);
				e.printStackTrace();
			}
		}
		
		private void getLatLngFromHttp(String locationText) {
			locationText = locationText.replace(" ", "");
			String url = "http://maps.google.com/maps/api/geocode/json?address=" + locationText + "&sensor=false";
		    HttpGet httpGet = new HttpGet(url);
		    HttpClient client = new DefaultHttpClient();
		    HttpResponse response;
		    
		    try {
		        response = client.execute(httpGet);
		        String json = EntityUtils.toString(response.getEntity());
		        JSONObject jsonObject = new JSONObject(json);

		        if (searchLocation != null) {
					searchLocation.reset();
				}
				else {
					searchLocation = new Location("custom");
				}
		        // parsing the JSON string for the latitude and longitude
	            searchLocation.setLatitude(((JSONArray)jsonObject.get("results")).getJSONObject(0)
	            		.getJSONObject("geometry").getJSONObject("location").getDouble("lat"));
	            searchLocation.setLongitude(((JSONArray)jsonObject.get("results")).getJSONObject(0)
	            		.getJSONObject("geometry").getJSONObject("location").getDouble("lng"));
	            
	            isBadLocation = false;
		    }
		    catch (ClientProtocolException e) {
		    	searchLocation = null;
		    	isBadLocation = true;
		        e.printStackTrace();
		    }
		    catch (IOException e) {
		    	searchLocation = null;
		    	isBadLocation = true;
				e.printStackTrace();
			}
		    catch (JSONException e) {
		    	Log.d(TAG, "error parsing JSON, no geocode results");
		    	searchLocation = null;
		    	isBadLocation = true;
	            e.printStackTrace();
	        }
		}
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
	
	// START OF LOCATION CALL BACKS
	
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
        	if (locationEditText.getText().toString().isEmpty()) {
	        	locationEditText.setText("Current Location");
	        	this.setLocation("Current Location");
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
					if (locationEditText.getText().toString().isEmpty()) {
						locationEditText.setText("Current Location");
			        	this.setLocation("Current Location");
					}
				}
				
				locationManager.requestLocationUpdates(
						LocationManager.NETWORK_PROVIDER, MIN_UPDATE_MSEC, MIN_DISTANCE_CHANGE, this);
			}
			else if (isGPSEnabled) {
				this.currentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
				
				if (this.currentLocation != null) {
					if (locationEditText.getText().toString().isEmpty()) {
						locationEditText.setText("Current Location");
			        	this.setLocation("Current Location");
					}
				}
				
				locationManager.requestLocationUpdates(
						LocationManager.GPS_PROVIDER, MIN_UPDATE_MSEC, MIN_DISTANCE_CHANGE, this);
			}
			else {
				// location services are disabled
				// obtaining user location is not possible if we get here
				this.currentLocation = null;
				this.showToast("Location services are not enabled on your device.");
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
		
		if (locationEditText.getText().toString().equals("Current Location")) {
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
