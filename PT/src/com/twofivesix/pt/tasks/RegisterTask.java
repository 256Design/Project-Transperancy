package com.twofivesix.pt.tasks;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.content.Context;

// FIXME Test me!
public class RegisterTask extends GeneralHttpTask {

	public RegisterTask(ProgressDialog progressDialog,
			OnResponseListener responder) {
		super(progressDialog, responder);
	}
	
	public RegisterTask(Context context, String progressMessage,
			OnResponseListener responder) {
		super(context, progressMessage, responder);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected Boolean doInBackground(String... params)
	{
		// Register User Here
		try
		{
			HttpClient client = new DefaultHttpClient();
		    HttpPost httppost = new HttpPost(
		    		"http://www.256design.com/projectTransparency/project/regesterShort.php");
		    
		    // Set values
		    String[] names = params[2].split(" ", 2);
		    String fName = names[0];
			String lName = names[1];
			
			// add values to list
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
	        nameValuePairs.add(new BasicNameValuePair(
	        		"emailAddress", params[0]));
	        nameValuePairs.add(new BasicNameValuePair(
	        		"password", params[1]));
	        nameValuePairs.add(new BasicNameValuePair("firstName", fName));
	        nameValuePairs.add(new BasicNameValuePair("lastName", lName));
	        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
	        
	        // Execute HTTP Post Request
			HttpResponse response = client.execute(httppost);
			responseCode = response.getStatusLine().getStatusCode();
		}
		catch (HttpHostConnectException e) {
			responseCode = 408;
		}
		catch (Exception e) {
			responseCode = 400;
			e.printStackTrace();
		}
		return responseCode == 201;
	}
}