package com.twofivesix.pt.tasks;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;

import com.twofivesix.pt.activities.ReportingActivity;

public class ReportTask extends AsyncTask<String, Void, Integer> {
	
	private ProgressDialog progressDialog;
	private ReportingActivity activity;

	public ReportTask(ReportingActivity activity, ProgressDialog progressDialog)
	{
		this.activity = activity;
		this.progressDialog = progressDialog;
	}
	
	@Override
	protected void onPreExecute() {
		progressDialog.show();
	}
	
	@Override
	protected Integer doInBackground(String... args) {
		int responseCode = 0;
		try 
		{
			HttpClient client = new DefaultHttpClient();
			String uri = "http://www.256design.com/projectTransparency/project/report.php?update&userID="+
		    		args[0];
			if(args[2].equals("1"))
				uri += "&includeSelf";
			Log.d("SPENCER", "args[3] = " + args[3]);
			if(args[3].equals("1"))
				uri += "&followUp";
		    HttpPost httppost = new HttpPost(uri);
			
			// add values to list
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("responses", args[1]));
	        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
	        
	        // Execute HTTP Post Request
	        HttpResponse response;
	        int tries = 5;
			do
			{
				response = client.execute(httppost);
			} while (--tries > 0 && responseCode == 408);

			responseCode = response.getStatusLine().getStatusCode();
		}
		catch (Exception e) {
			responseCode = 408;
			e.printStackTrace();
		}
		
		return responseCode;
	}
	
	@Override
	protected void onPostExecute(Integer result) {
		progressDialog.dismiss();
		//Log.d("SPENCER", "result = " + result);
		if(result == 202)
			((ReportingActivity)activity).successfulSubmit();
		else
			((ReportingActivity)activity).failedSubmit(result);
	}

}
