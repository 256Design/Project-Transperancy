package com.twofivesix.pt.data.alarms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.twofixesix.pt.helpers.SharedPreferencesHelper;

public class OnStartupResponse extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		SharedPreferencesHelper prefs = new SharedPreferencesHelper(context);
		Log.d("SPENCER", "reminderTime: " + prefs.getReminderTime());
		String reminderTime = prefs.getReminderTime();
		//Log.d("SPENCER", "reminder time + " + reminderTime);
		if(prefs.getRemindersOn())
			ReportPromtAlarmHelper.resetPendingIntent(context, reminderTime);
	}
}
