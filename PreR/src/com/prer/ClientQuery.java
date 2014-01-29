package com.prer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Design adapted from http://stackoverflow.com/questions/8267928/android-rest-client-sample

public class ClientQuery {
	private static final String BASE_URL = "http://prer-backend.appspot.com/services";
	
	public void getProcedures(String name, String zipCode, 
			final GetResponseCallback callback) {
		if(validQueryParams(name, zipCode)) {
			String url = BASE_URL + "?name=" + name.replace(" ", "+").toLowerCase() + 
					"&zip=" + zipCode;
			new GetTask(url, new RestTaskCallback() {
				@Override
				public void onTaskComplete(String result) {
					callback.onDataReceived(result);	
				}
			}).execute();
		}
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
}
