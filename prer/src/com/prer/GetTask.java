package com.prer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.os.AsyncTask;

public class GetTask extends AsyncTask<String, String, String> {
	private String url;
    private RestTaskCallback callback;
    
    public GetTask(String url, RestTaskCallback callback){
		this.url = url;
        this.callback = callback;
    }
    
	@Override
	protected String doInBackground(String... arg0) {
		String respBody = null;
		try {
			HttpClient client = new DefaultHttpClient();
			HttpGet get = new HttpGet(url);
			HttpResponse resp = client.execute(get);
			respBody = getResponseBody(resp);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return respBody;
	}
	
	@Override
    protected void onPostExecute(String result) {
        callback.onTaskComplete(result);
        super.onPostExecute(result);
    }
	
	private String getResponseBody(HttpResponse resp) 
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

}
