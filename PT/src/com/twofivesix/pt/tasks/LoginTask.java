package com.twofivesix.pt.tasks;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import com.twofivesix.pt.activities.LoginActivity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;

public class LoginTask extends AsyncTask<String, Void, Integer> {

	private ProgressDialog progressDialog;
	private Activity activity;
	private int id = -1;

	public LoginTask(Activity activity, ProgressDialog progressDialog)
	{
		this.activity = activity;
		this.progressDialog = progressDialog;
	}
	
	@Override
	protected void onPreExecute() {
		progressDialog.show();
	}
	
	@Override
	protected Integer doInBackground(String... arg0) {
		// TODO Auto-generated method stub
		String result = "";
		int responseCode = 0;
		try 
		{
			HttpClient client = new DefaultHttpClient();
		    HttpPost httppost = new HttpPost("http://www.256design.com/projectTransparency/project/headerLogin.php");
		    
		    List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
	        nameValuePairs.add(new BasicNameValuePair("emailAddress", arg0[0]));
	        nameValuePairs.add(new BasicNameValuePair("password", arg0[1]));
				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
	       
			int executeCount = 0;
			HttpResponse response;
			do
			{
				// Execute HTTP Post Request
				//Log.d("SPENCER", "send login request");
				executeCount++;
				response = client.execute(httppost);
				responseCode = response.getStatusLine().getStatusCode();
				Log.d("SPENCER", response.getStatusLine().getStatusCode() + " Status Code");						
			} while (executeCount < 5 && responseCode == 408);
			
	        BufferedReader rd = new BufferedReader(new InputStreamReader(
					response.getEntity().getContent()));
			
			String line;
			while ((line = rd.readLine()) != null)
			{
				result = line.trim();
				Log.d("SPENCER", "Log on result: |" + result + "|");
			}
			id = Integer.parseInt(result);
		}
		catch (Exception e) {
			responseCode = 408;
			e.printStackTrace();
		}
		//id = 1;
		//return 200;
		return responseCode;
	}
	
	@Override
	protected void onPostExecute(Integer result) {
		progressDialog.dismiss();
		Log.d("SPENCER", "result = " + result);
		if(result == 202)
			((LoginActivity)activity).login(id);
		else
			((LoginActivity)activity).showLoginError("");
	}

}
