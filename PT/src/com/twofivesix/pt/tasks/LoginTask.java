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
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import com.twofivesix.pt.R;
import com.twofivesix.pt.activities.LoginActivity;

public class LoginTask extends AsyncTask<String, Integer, Integer> {

	private ProgressDialog progressDialog;
	private LoginActivity activity;
	private int id = -1;

	public LoginTask(LoginActivity activity, ProgressDialog progressDialog)
	{
		this.activity = activity;
		this.progressDialog = progressDialog;
	}
	
	@Override
	protected void onPreExecute()
	{
		progressDialog.show();
	}
	
	@Override
	protected Integer doInBackground(String... args) 
	{
		String result = "";	
		int responseCode = 0;
		try 
		{
			HttpClient client = new DefaultHttpClient();
		    HttpPost httppost = new HttpPost("http://www.256design.com/projectTransparency/project/headerLogin.php");
		    
		    List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
	        nameValuePairs.add(new BasicNameValuePair("emailAddress", args[0]));
	        nameValuePairs.add(new BasicNameValuePair("password", args[1]));
				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
	       
			int executeCount = 0;
			HttpResponse response;
			do
			{
				publishProgress(executeCount+1,5);
				// Execute HTTP Post Request
				executeCount++;
				response = client.execute(httppost);
				responseCode = response.getStatusLine().getStatusCode();						
			} while (executeCount < 5 && responseCode == 408);
			
	        BufferedReader rd = new BufferedReader(new InputStreamReader(
					response.getEntity().getContent()));
			
			String line;
			while ((line = rd.readLine()) != null)
			{
				result = line.trim();
			}
			id = Integer.parseInt(result);
		}
		catch (HttpHostConnectException e) {
			responseCode = 408;
		}
		catch (Exception e) {
			responseCode = 400;
			e.printStackTrace();
		}
		return responseCode;
	}
	
	@Override
	protected void onProgressUpdate(Integer... values) {
		if(values.length == 2)
		{
			progressDialog.setMessage(activity.getText(R.string.logging_in)+"("+values[0]+"/"+values[1]+")");
		}
	}
	
	@Override
	protected void onPostExecute(Integer result)
	{
		progressDialog.dismiss();
		if(result == 202)
			activity.login(id);
		else if(result == 408)
			activity.showConnectionError(this);
		else
			activity.showLoginError("");
	}
}