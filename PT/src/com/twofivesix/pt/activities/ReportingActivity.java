package com.twofivesix.pt.activities;

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

import android.app.Activity;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;

import com.twofivesix.pt.R;
import com.twofivesix.pt.data.Question;
import com.twofivesix.pt.data.alarms.QuestionPromptAlarm;
import com.twofivesix.pt.data.alarms.ReportPromtAlarmHelper;
import com.twofivesix.pt.listAdapters.ReportQuestionListAdapter;
import com.twofixesix.pt.helpers.DatabaseHelper;
import com.twofixesix.pt.helpers.NetworkConnectivityHelper;
import com.twofixesix.pt.helpers.SharedPreferencesHelper;

public class ReportingActivity extends Activity {
	
	private ArrayList<Question> questionArrayList;
	private SQLiteDatabase db;
	private ListView questionList;
	private Button saveButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.reporting_activity);
		
		if(ReportPromtAlarmHelper.repeaterIsRunning(this))
			ReportPromtAlarmHelper.stopRepeatingReminder(this);
		QuestionPromptAlarm.closeNotification(this);
		
		db = (new DatabaseHelper(this)).getWritableDatabase();
        questionList = (ListView) findViewById(R.id.report_questions_list);
		questionArrayList = DatabaseHelper.buildQuestionsList(db);
		db.close();
        questionList.setAdapter(new ReportQuestionListAdapter(this, questionArrayList));
        
        saveButton = (Button) findViewById(R.id.report_button);
        saveButton.setOnClickListener(new OnClickListener() {
        	
        	public void onClick(View v) {
        		if(NetworkConnectivityHelper.isConnected(ReportingActivity.this))
        		{
        			if(sendReport())
        			{
        				(new SharedPreferencesHelper(ReportingActivity.this)).setLastReportDateToNow();
        				// success
        				setResult(RESULT_OK);
        				finish();
        			}
        		}
        		else
        		{
        			// alert not connected
        		}
        	}
        });
	}

	protected boolean sendReport()
	{
		String responses = "";
		for (int i = 0; i < questionArrayList.size(); i++) {
			ReportQuestionListAdapter adapter = (ReportQuestionListAdapter) questionList.getAdapter();
			String response = adapter.getResponse(i);
			if(response == null)
				response = "null";
			//Log.d("SPENCER", adapter.getQuestionId(i) + "|" + response);
			responses += adapter.getQuestionId(i) + "|" + response + "\n";
		}
		
		String result;
		int responseCode = 0;
		try
		{
			HttpClient client = new DefaultHttpClient();
		    HttpPost httppost = new HttpPost("http://www.256design.com/projectTransparency/project/report.php?update&userID="+
		    		new SharedPreferencesHelper(ReportingActivity.this).getUserID());
			
			// add values to list
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("responses", responses));
	        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
	        
	        // Execute HTTP Post Request
	        HttpResponse response;
	        int tries = 5;
			do
			{
				response = client.execute(httppost);
			} while (--tries > 0 && responseCode == 408);

			responseCode = response.getStatusLine().getStatusCode();
			//Log.d("SPENCER", response.getStatusLine().getStatusCode() + " Status Code");					
	        
	        BufferedReader rd = new BufferedReader(new InputStreamReader(
					response.getEntity().getContent()));
			
			String line = "";
			result = line;
			while ((line = rd.readLine()) != null) {
				result = line;
				//Log.d("SPENCER", result);
			}
		}
		catch(Exception e)
		{
			result = "Error";
			//Log.e("SPENCER", "|" + e.getMessage() + "|");
		}
		
		//Log.d("SPENCER", "|" + result + "|");
		if(responseCode == 202)
		{
			return true;
		}
		else
		{
			return false;
		}
	}
}
