package com.twofivesix.pt.tasks;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;

public class GeneralHttpTask extends AsyncTask<String, Integer, Boolean>
{
	private ProgressDialog progressDialog;
	private OnResponseListener responder;
	private int responseCode;

	/** execute params:
	 * <ul>
	 * <li>Param1: Http Post request url</li>
	 * <li>Param2: Desired response code. Default: 200</li>
	 * <li>Param3: Attempts count. Default: 1 </li>
	 * </ul>
	 */
	public GeneralHttpTask(ProgressDialog progressDialog, OnResponseListener responder)
	{
		this.progressDialog = progressDialog;
		this.responder = responder;
	}

	@Override
	protected void onPreExecute() 
	{
		progressDialog.show();
	}
	
	@Override
	protected Boolean doInBackground(String... params) {
		int desiredCode = 200;
		int attemptsCount;
		responseCode = 0;
		try 
		{
			if(params.length >= 2)
				desiredCode = Integer.parseInt(params[1]);
			if(params.length >= 3)
				attemptsCount = Integer.parseInt(params[2]);
			else
				attemptsCount = 1;

			HttpClient client = new DefaultHttpClient();
		    HttpPost httppost = new HttpPost(params[0]);
	       
			int executeCount = 0;
			HttpResponse response;
			do
			{
				publishProgress(executeCount+1,attemptsCount);
				// Execute HTTP Post Request
				executeCount++;
				response = client.execute(httppost);
				responseCode = response.getStatusLine().getStatusCode();						
			} while (executeCount < attemptsCount && responseCode == 408);;
		}
		catch (HttpHostConnectException e) {
			responseCode = 408;
		}
		catch (Exception e) {
			responseCode = 400;
			e.printStackTrace();
		}
		return responseCode == desiredCode;
	}
	
	@Override
	protected void onPostExecute(Boolean result) {
		if (this.progressDialog.isShowing()) {
            this.progressDialog.dismiss();
        }
		if(result)
			responder.onSuccess();
		else
		{
			Log.d("SPENCER", "Error response. Code: " + responseCode);
			responder.onFailure("");
		}
	}
	
	public interface OnResponseListener {
		public void onSuccess();
		public void onFailure(String message);
	}
}
