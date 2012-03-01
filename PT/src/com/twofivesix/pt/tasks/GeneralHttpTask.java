package com.twofivesix.pt.tasks;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.impl.client.DefaultHttpClient;
import android.content.Context;
import android.os.AsyncTask;

public class GeneralHttpTask extends AsyncTask<String, Integer, Boolean>
{
	protected android.app.ProgressDialog progressDialog;
	protected OnResponseListener responder;
	protected int responseCode;

	/** execute params:
	 * <ul>
	 * <li>Param1: Http Post request url</li>
	 * <li>Param2: Desired response code. Default: 200</li>
	 * <li>Param3: Attempts count. Default: 1 </li>
	 * </ul>
	 */
	public GeneralHttpTask(android.app.ProgressDialog progressDialog, OnResponseListener responder)
	{
		this.progressDialog = progressDialog;
		this.responder = responder;
	}
	
	public GeneralHttpTask(Context context, String progressMessage, OnResponseListener responder)
	{
		this.progressDialog = new ProgressDialog(context, progressMessage);
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
			responder.onFailure(Integer.toString(responseCode));
		}
	}
	
	public interface OnResponseListener {
		public void onSuccess();
		public void onFailure(String message);
	}
	
	public class ProgressDialog extends android.app.ProgressDialog
	{
		public ProgressDialog(Context context, String progressMessage) 
		{
			super(context);
			setCancelable(false);
			setMessage(progressMessage);
		}
		
	}
}