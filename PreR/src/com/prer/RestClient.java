package com.prer;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

// Design adapted from http://stackoverflow.com/questions/8267928/android-rest-client-sample

public class RestClient {
	private static final String BASE_URL = "http://prer-backend.appspot.com/v1";
	private static final String ENTRY_SEARCH = "/entries/search?";
	private static final String SERVICES_SEARCH = "/services/search?";;
	private static final String IMAGE_POST = "/images";
	
	public void getProceduresByName(String name, int radius, 
			double lat, double lng, final GetResponseCallback callback) {
		String url = null;
		try {
			url = BASE_URL + ENTRY_SEARCH + URLEncoder.encode("name=" + name +
					"&radius=" + radius + "&lat=" + lat + "&lng=" + lng, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		new GetTask(url, new RestTaskCallback() {
			@Override
			public void onTaskComplete(String result) {
				callback.onDataReceived(result);	
			}
		}).execute();
	}
	
	public void getProceduresByName(String name, int radius, 
			double lat, double lng, int limit, int offset,
			final GetResponseCallback callback) {
		String url = null;
		try {
			url = BASE_URL + ENTRY_SEARCH + URLEncoder.encode("name=" + name +
					"&radius=" + radius + "&lat=" + lat + "&lng=" + lng + 
					"&limit=" + limit + "&offset=" + offset, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		new GetTask(url, new RestTaskCallback() {
			@Override
			public void onTaskComplete(String result) {
				callback.onDataReceived(result);	
			}
		}).execute();
	}
	
	public void getProceduresByCptCode(String cptCode, int radius,
			double lat, double lng, final GetResponseCallback callback) {
		String url = null;
		try {
			url = BASE_URL + ENTRY_SEARCH + URLEncoder.encode("cpt_code=" + cptCode +
					"&radius=" + radius + "&lat=" + lat + "&lng=" + lng, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		new GetTask(url, new RestTaskCallback() {
			@Override
			public void onTaskComplete(String result) {
				callback.onDataReceived(result);	
			}
		}).execute();
	}
	
	public void getProceduresByCptCode(String cptCode, int radius,
			double lat, double lng, int limit, int offset,
			final GetResponseCallback callback) {
		String url = null;
		try {
			url = BASE_URL + ENTRY_SEARCH + URLEncoder.encode("cpt_code=" + cptCode +
					"&radius=" + radius + "&lat=" + lat + "&lng=" + lng + 
					"&limit=" + limit + "&offset=" + offset, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		new GetTask(url, new RestTaskCallback() {
			@Override
			public void onTaskComplete(String result) {
				callback.onDataReceived(result);	
			}
		}).execute();
	}
	
	public void getServicesByName(String name, final GetResponseCallback callback) {
		String url = null;
		try {
			url = BASE_URL + SERVICES_SEARCH + URLEncoder.encode("name=" + name, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		new GetTask(url, new RestTaskCallback() {
			@Override
			public void onTaskComplete(String result) {
				callback.onDataReceived(result);	
			}
		}).execute();
	}

	public void getServicesByCptCode(String code, final GetResponseCallback callback) {
		String url = null;
		try {
			url = BASE_URL + SERVICES_SEARCH + URLEncoder.encode("cpt_code=" + code, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		new GetTask(url, new RestTaskCallback() {
			@Override
			public void onTaskComplete(String result) {
				callback.onDataReceived(result);	
			}
		}).execute();
	}
	
	public void postImage(String fileName, final PostCallback callback) {
		String url = BASE_URL + IMAGE_POST;
		new PostTask(url, fileName, new RestTaskCallback() {
			@Override
			public void onTaskComplete(String result) {
				callback.onPostSuccess(result);
			}
		}).execute();
	}
}
