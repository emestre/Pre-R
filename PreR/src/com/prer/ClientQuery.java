package com.prer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.os.AsyncTask;

public class ClientQuery {
	private final String BASE_URL = "http://prer-backend.appspot.com/services";
	
	public ClientQuery() {
		
	}
	
	/* NOTE: Make own exception (ie. BadQueryParamtersException) 
	 * and throw that instead of returning null if given bad 
	 * params? Would make clear the fact that the params are bad;
	 * returning null could mean almost anything...
	 */ 
	public String getProcedures(String name, String zipCode) 
			throws ClientProtocolException, IOException, 
				ParseException, InterruptedException, ExecutionException {
		// Check params
		if(validQueryParams(name, zipCode)) 
			return null;
		
		// Create URI
		String URI = BASE_URL + "?name=" + name.replace(" ", "+") +
				"&zip=" + zipCode;


		 // Query server
		ClientQueryTask query = (ClientQueryTask)new ClientQueryTask().execute(URI);
		HttpResponse resp = query.get();
		
		// Return JSON String response
		return getJsonString(resp);
	}
	
	private boolean validQueryParams(String name, String zipCode) {
		// Check for null variables
		if(name == null || zipCode == null)
			return false;
		
		// Make sure zip code is a 5-digit number
		Pattern pattern = Pattern.compile("\\d{5}");
		Matcher matcher = pattern.matcher(zipCode);
		return matcher.find();
	}

	private String getJsonString(HttpResponse resp) 
			throws ParseException, IOException {
		HttpEntity entity = resp.getEntity();
		InputStream stream = entity.getContent();
		BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
		StringBuilder buf = new StringBuilder();
		
		String line = null;
		while((line = reader.readLine()) != null) {
			buf.append(line);
		}
		stream.close();
		return buf.toString();
	}

	// TODO: Move this to it's own class
	private class ClientQueryTask extends AsyncTask<String, Integer, HttpResponse> {
		@Override
		protected HttpResponse doInBackground(String... strings) {
			/* NOTE: Prototype - will probably break server communication
			 * multiple classes and functions later. 
			 * Query server
			 */
			HttpClient client = new DefaultHttpClient();
			HttpGet get = new HttpGet(strings[0]);
			HttpResponse resp = null;
			try {
				 resp = client.execute(get);
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return resp;
		}
	}
}
