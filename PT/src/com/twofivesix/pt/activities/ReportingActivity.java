package com.twofivesix.pt.activities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ExecutionException;

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
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.twofivesix.pt.R;
import com.twofivesix.pt.alarms.ReportPromptAlarm;
import com.twofivesix.pt.alarms.ReportPromtAlarmHelper;
import com.twofivesix.pt.data.Question;
import com.twofivesix.pt.helpers.DatabaseHelper;
import com.twofivesix.pt.helpers.NetworkConnectivityHelper;
import com.twofivesix.pt.helpers.SharedPreferencesHelper;
import com.twofivesix.pt.helpers.VersionAlertHelper;
import com.twofivesix.pt.interfaces.SyncCaller;
import com.twofivesix.pt.listAdapters.ReportQuestionListAdapter;
import com.twofivesix.pt.listAdapters.SingleRenderListView;
import com.twofivesix.pt.tasks.QuestionSyncTask;
import com.twofivesix.pt.tasks.ReportTask;

public class ReportingActivity extends Activity implements SyncCaller {
	
	private ArrayList<Question> questionArrayList;
	private SQLiteDatabase db;
	private SingleRenderListView questionList;
	private CheckBox footerCB;
	private Button saveButton;
	private SharedPreferencesHelper preferencesHelper;
	private ReportQuestionListAdapter questionAdapter;
	private ReportTask reportTask;

	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.reporting_activity);
		
		preferencesHelper = new SharedPreferencesHelper(this);
		
		questionList = (SingleRenderListView) findViewById(R.id.report_questions_list);
		saveButton = (Button) findViewById(R.id.report_button);
		
		saveButton.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				sendReport();
			}
		});
		
		
		// runs a check for first version run then show change log if something 
		// is different 
		new VersionAlertHelper(this, preferencesHelper);
		
		
		if(ReportPromtAlarmHelper.repeaterIsRunning(this))
			ReportPromtAlarmHelper.stopRepeatingReminder(this);
		ReportPromptAlarm.closeNotification(this);

		if(preferencesHelper.getAddFollowUp())
		{
			LinearLayout followUpRow;
			followUpRow = new LinearLayout(this);
			followUpRow.setOrientation(LinearLayout.HORIZONTAL);
			followUpRow.setGravity(Gravity.TOP);
			
			footerCB = new CheckBox(this);
			
			TextView footerTV = new TextView(this);
			footerTV.setText(R.string.reporting_follow_up);
			LayoutParams footerTVParams = new LinearLayout.LayoutParams(
					0, 
					LayoutParams.WRAP_CONTENT,
					1.0f);
			footerTV.setLayoutParams(footerTVParams);
			footerTV.setOnClickListener(new OnClickListener()
			{
				public void onClick(View v) {
					footerCB.setChecked(!footerCB.isChecked());
				}
			});
			
			followUpRow.addView(footerCB);
			followUpRow.addView(footerTV);
			questionList.addFooterView(followUpRow);
		}
		
		// Instantiate Db
		db = (new DatabaseHelper(this)).getWritableDatabase();
		
		// get old questions
		Question[] reuseQuestionArray = 
				(Question[]) getLastNonConfigurationInstance();
		// if there were reused questions
		if(reuseQuestionArray != null)
		{
			Log.d("SPENCER", "got something");
			setQuestionListByArray(reuseQuestionArray);
		}
		else
		{
			Log.d("SPENCER", "got nothing");
			int syncCount = preferencesHelper.getSyncCount();
			if(syncCount <= 0)
			{
				if(NetworkConnectivityHelper.isConnected(ReportingActivity.this))
				{
					ProgressDialog progressDialog = QuestionSyncTask.progressDialog(
							ReportingActivity.this);
					
					QuestionSyncTask questionsSyncTask = new QuestionSyncTask(
							ReportingActivity.this, 
							progressDialog, 
							db);
					Integer userID = preferencesHelper.getUserID();
					questionsSyncTask.execute(userID);
				}
			}
			else
			{
				preferencesHelper.setSyncCount(syncCount-1);
				setQuestionListFromDB();
			}

		}
	}
	
	@Override
	public Object onRetainNonConfigurationInstance()
	{
		return questionAdapter.getItemsWithResponses();
	}
	
	@Override
	protected void onDestroy()
	{
		db.close();
		super.onDestroy();
	}
	
	private void setQuestionListFromDB()
	{
		questionArrayList = DatabaseHelper.buildQuestionsList(db);
//		db.close();
		setListAdapter();
	}
	
	private void setListAdapter()
	{
		questionAdapter = new ReportQuestionListAdapter(this, questionArrayList);
		questionList.setAdapter(questionAdapter);
	}
	
	private void setQuestionListByArray(Question[] array)
	{
		questionArrayList = new ArrayList<Question>();
		Collections.addAll(questionArrayList, array);
		setListAdapter();
	}

	protected void sendReport()
	{
		String responses = "";
		int l = questionArrayList.size();
		if(questionArrayList.get(l-1) == null)
			l--;
		for (int i = 0; i < l; i++) {
			ReportQuestionListAdapter adapter = questionAdapter;
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
		reportTask = new ReportTask(ReportingActivity.this, progressDialog);
		Log.d("SPENCER", "isChecked" + 
				((null != footerCB && footerCB.isChecked()) ? "1" : "0"));
		reportTask.execute(
				""+userID, 
				responses, 
				(preferencesHelper.getSendToSelf()) ? "1" : "0",
				(null != footerCB && footerCB.isChecked()) ? "1" : "0"
		);
	}
	
	public void successfulSubmit()
	{
		preferencesHelper.setLastReportDateToNow();
		setResult(RESULT_OK);
		Toast.makeText(ReportingActivity.this, 
				getText(R.string.success_report), 
				Toast.LENGTH_LONG).show();
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
		
		// network connection monitor
		if(NetworkConnectivityHelper.isConnected(this))
			enableReportBtn();
		else
			disableReportBtn();
		
		IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);        
		registerReceiver(networkStateReceiver, filter);
		
		if(reportTask != null)
		{
			if(reportTask.getStatus() == Status.FINISHED)
			{
				try {
					int response = reportTask.get();
					if(reportTask.getResult())
						successfulSubmit();
					else
						failedSubmit(response);
				} catch (InterruptedException e) {
//					e.printStackTrace();
				} catch (ExecutionException e) {
//					e.printStackTrace();
				}
			}
		}
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
				enableReportBtn();
			else
				disableReportBtn();
	    }
	};
	
	protected void disableReportBtn() {
		saveButton.setEnabled(false);
		saveButton.setText(R.string.no_network_connection);
	}
	
	protected void enableReportBtn() {
		saveButton.setEnabled(true);
		saveButton.setText(R.string.save_and_send);
	}

	public void syncResults(Boolean result) {
		if(result)
		{
			setQuestionListFromDB();
			preferencesHelper.resetSyncCount();
		}
	}
}
