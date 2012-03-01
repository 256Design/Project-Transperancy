package com.twofivesix.pt.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;

import com.twofivesix.pt.R;
import com.twofivesix.pt.alarms.ReportPromptAlarm;
import com.twofivesix.pt.alarms.ReportPromtAlarmHelper;

public class NoteSelectedDialogActivity extends Activity 
{

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
        
        LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, 
        		LayoutParams.WRAP_CONTENT);
        LinearLayout layout = new LinearLayout(this);
        layout.setLayoutParams(params);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setGravity(Gravity.CENTER);
        
        Button reportButton = new Button(this);
        reportButton.setText(R.string.report_now);
        reportButton.setLayoutParams(params);
        reportButton.setOnClickListener(new OnClickListener()
        {
			public void onClick(View v)
			{
				startActivityForResult(new Intent(NoteSelectedDialogActivity.this, 
										 ReportingActivity.class), 0);
			}
		});
        layout.addView(reportButton);
        
        Button snoozeButton = new Button(this);
        snoozeButton.setText(R.string.snooze);
        snoozeButton.setLayoutParams(params);
        snoozeButton.setOnClickListener(new OnClickListener() 
        {
        	public void onClick(View v)
        	{
        		ReportPromptAlarm.closeNotification(NoteSelectedDialogActivity.this);
        		finish();
        	}
        });
        layout.addView(snoozeButton);
        
        Button dismissButton = new Button(this);
        dismissButton.setText(R.string.dismiss);
        dismissButton.setLayoutParams(params);
        dismissButton.setOnClickListener(new OnClickListener() 
        {
        	public void onClick(View v) 
        	{
        		if(ReportPromtAlarmHelper.repeaterIsRunning(
        									NoteSelectedDialogActivity.this))
        			ReportPromtAlarmHelper.stopRepeatingReminder(
        									NoteSelectedDialogActivity.this);
        		ReportPromptAlarm.closeNotification(
        									NoteSelectedDialogActivity.this);
        		finish();
        	}
        });
        layout.addView(dismissButton);
        
        setContentView(layout);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		finish();
	}	
}