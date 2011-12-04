package com.twofivesix.pt.activities;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;

import com.twofivesix.pt.R;
import com.twofivesix.pt.data.Question;
import com.twofivesix.pt.data.alarms.QuestionPromptAlarm;
import com.twofivesix.pt.data.alarms.ReportPromtAlarmHelper;
import com.twofivesix.pt.listAdapters.ReportQuestionListAdapter;
import com.twofivesix.pt.tasks.ReportTask;
import com.twofixesix.pt.helpers.DatabaseHelper;
import com.twofixesix.pt.helpers.NetworkConnectivityHelper;
import com.twofixesix.pt.helpers.SharedPreferencesHelper;

public class ReportingActivity extends Activity {
	
	private ArrayList<Question> questionArrayList;
	private SQLiteDatabase db;
	private ListView questionList;
	private Button saveButton;
	private SharedPreferencesHelper preferencesHelper;

	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.reporting_activity);
		
		preferencesHelper = new SharedPreferencesHelper(this);
		
		if(ReportPromtAlarmHelper.repeaterIsRunning(this))
			ReportPromtAlarmHelper.stopRepeatingReminder(this);
		QuestionPromptAlarm.closeNotification(this);
		
		db = (new DatabaseHelper(this)).getWritableDatabase();
        questionList = (ListView) findViewById(R.id.report_questions_list);
		questionArrayList = DatabaseHelper.buildQuestionsList(db);
		db.close();
        questionList.setAdapter(
        		new ReportQuestionListAdapter(this, questionArrayList));
        
        saveButton = (Button) findViewById(R.id.report_button);
        saveButton.setOnClickListener(new OnClickListener() {
        	
        	public void onClick(View v) {
        		sendReport();
        	}
        });
	}

	protected void sendReport()
	{
		String responses = "";
		for (int i = 0; i < questionArrayList.size(); i++) {
			ReportQuestionListAdapter adapter = 
					(ReportQuestionListAdapter) questionList.getAdapter();
			String response = adapter.getResponse(i);
			if(response == null)
				response = "null";
			//Log.d("SPENCER", adapter.getQuestionId(i) + "|" + response);
			responses += adapter.getQuestionId(i) + "|" + response + "\n";
		}
		
		ProgressDialog progressDialog = new ProgressDialog(ReportingActivity.this);
		progressDialog.setMessage(getText(R.string.sending_report));
		progressDialog.setCancelable(false);
		
		int userID = preferencesHelper.getUserID();
		ReportTask reportTask = new ReportTask(ReportingActivity.this, progressDialog);
		reportTask.execute(""+userID, responses);
	}
	
	public void successfulSubmit()
	{
		// success
		preferencesHelper.setLastReportDateToNow();
		setResult(RESULT_OK);
		finish();
	}
	
	public void failedSubmit(int responseCode)
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(R.string.error_failed_report);
		builder.setTitle(R.string.error);
		builder.setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});
		
		AlertDialog alert = builder.create();
		alert.show();
		Log.d("SPENCER", "Error reporting. Code: " + responseCode);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		if(NetworkConnectivityHelper.isConnected(this))
			enableReport();
		else
			disableReport();
		
		
		IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);        
		registerReceiver(networkStateReceiver, filter);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(networkStateReceiver);
	}
	
	protected BroadcastReceiver networkStateReceiver = new BroadcastReceiver() {

	    @Override
	    public void onReceive(Context context, Intent intent) {
	    	if(NetworkConnectivityHelper.isConnected(ReportingActivity.this))
				enableReport();
			else
				disableReport();
	    }
	};
	
	protected void disableReport() {
		saveButton.setEnabled(false);
		saveButton.setText(R.string.no_network_connection);
	}
	
	protected void enableReport() {
		saveButton.setEnabled(true);
		saveButton.setText(R.string.save_and_send);
	}
}
